"""Build runnable exercise projects from the code blocks published in the lessons."""

import argparse
from hashlib import sha256
from pathlib import Path
import re
from shutil import copyfile
from zipfile import ZipFile


ROOT = Path(__file__).resolve().parents[2]
FIXTURES = ROOT / ".github" / "exercises"
FENCE = re.compile(r'^```\w+(?: title="([^"]+)")?\n(.*?)^```[ \t]*$', re.M | re.S)
PACKAGE = re.compile(r"^package ([\w.]+);", re.M)
SECURITY_ZIP_SHA256 = "88c5c01339fde0d0bbbc35bacd614cc934277704e540530b225f9d0b9baf178a"


def blocks(chapter):
    path = ROOT / "docs" / chapter
    snippets = {}
    for title, body in FENCE.findall(path.read_text(encoding="utf-8")):
        if title:
            if title in snippets:
                raise ValueError(f"{chapter}: duplicate code block title {title!r}")
            snippets[title] = body
    return snippets


def write(path, body):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(body, encoding="utf-8")


def write_java(project, scope, snippets, *names):
    for name in names:
        body = snippets[name]
        package = PACKAGE.search(body)
        if package is None:
            raise ValueError(f"{name}: missing package declaration")
        directory = project / "src" / scope / "java" / package[1].replace(".", "/")
        write(directory / name, body)


def transactional_service(order):
    """Apply the lesson's import and two method replacements in order."""
    service = order["OrderService.java"].replace(
        "import org.springframework.stereotype.Service;",
        "import org.springframework.stereotype.Service;\n"
        + order["トランザクションの import"].strip(),
    )
    replacements = order["トランザクション付きメソッド"].strip().split("\n\n")
    if len(replacements) != 2:
        raise ValueError("注文 API のトランザクション例には 2 メソッドが必要です")
    for method in replacements:
        indented = "\n".join("  " + line for line in method.splitlines())
        original = indented.split("\n", 1)[1]
        if service.count(original) != 1 or not indented.startswith("  @Transactional\n"):
            raise ValueError("注文 API の変更前後でメソッドの本文が一致しません")
        service = service.replace(original, indented, 1)
    return service


def prepare_orders(project):
    order = blocks("SpringBoot入門/DBアクセス・トランザクション入門.md")
    integration = blocks("SpringBootテスト入門/DBを含む統合テスト入門.md")
    order["OrderService.java"] = transactional_service(order)

    pom = (FIXTURES / "orders-pom.xml").read_text(encoding="utf-8")
    dependencies = (
        order["MyBatis の依存関係"],
        integration["Web MVC テストの依存関係"],
        integration["PostgreSQL テストの依存関係"],
    )
    dependency_xml = "\n".join(
        "    " + line for part in dependencies for line in part.strip().splitlines()
    )
    write(project / "pom.xml", pom.replace("    <!-- lesson dependencies -->", dependency_xml))
    write_java(project, "main", order, "Order.java", "OrderRequest.java",
               "OrderWithItemRequest.java", "OrderMapper.java", "OrderService.java",
               "OrderController.java")
    write_java(project, "main", integration, "OrderStorageExceptionHandler.java")
    write_java(project, "test", integration, "OrderApiContract.java", "OrderApiH2Test.java",
               "PostgresTestConfiguration.java", "OrderApiPostgresTest.java")
    write(project / "src/main/resources/application.yaml", order["application.yaml"])
    write(project / "src/main/resources/schema.sql", order["schema.sql"])
    write(project / "src/test/resources/application-integration.yaml",
          integration["application-integration.yaml"])
    app = project / "src/main/java/dev/mikoto2000/springboot/database/DatabaseApplication.java"
    copyfile(FIXTURES / "DatabaseApplication.java", app)


def prepare_rfc9457(project):
    lesson = blocks("SpringBoot入門/RFC9457エラー応答入門.md")
    copy_pom = (FIXTURES / "rfc9457-pom.xml").read_text(encoding="utf-8")
    write(project / "pom.xml", copy_pom)
    write_java(project, "main", lesson, "Task.java", "TaskRequest.java", "TaskService.java",
               "TaskNotFoundException.java", "TaskAlreadyCompletedException.java")
    package = "dev/mikoto2000/springboot/rfc9457"
    test_file = project / "src/test/java" / package / "TaskServiceTest.java"
    write(test_file, (FIXTURES / "TaskServiceTest.java").read_text(encoding="utf-8"))


def prepare_web_project(project, package, application, dependencies):
    pom = (FIXTURES / "web-pom.xml").read_text(encoding="utf-8")
    if "spring-boot-starter-webmvc-test" not in dependencies:
        dependencies += """
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webmvc-test</artifactId>
  <scope>test</scope>
</dependency>
"""
    write(project / "pom.xml", pom.replace("<!-- lesson dependencies -->", dependencies))
    body = (f"package {package};\n"
            "import org.springframework.boot.SpringApplication;\n"
            "import org.springframework.boot.autoconfigure.SpringBootApplication;\n"
            f"@SpringBootApplication public class {application} {{\n"
            f"  public static void main(String[] args) {{ SpringApplication.run({application}.class, args); }}\n"
            "}\n")
    write(project / "src/main/java" / package.replace(".", "/") / f"{application}.java", body)


def copy_tests(project, package, *names):
    for name in names:
        write(project / "src/test/java" / package.replace(".", "/") / name,
              (FIXTURES / "clients" / name).read_text(encoding="utf-8"))


def prepare_shipping(project, retry=False):
    shipping = blocks("SpringBoot入門/外部API連携入門.md")
    package = "dev.mikoto2000.springboot.shipping"
    prepare_web_project(project, package, "ShippingApplication", shipping["RestClient の依存関係"])
    settings = shipping["application.yaml"]
    if retry:
        lesson = blocks("SpringBoot入門/リトライ入門.md")
        shipping["ShippingClient.java"] = lesson["ShippingClient.java"]
        settings = settings.split("\nshipping:\n")[0] + "\n" + lesson["shipping の設定"]
    write_java(project, "main", shipping, "ShippingClient.java", "ShippingController.java", "ShippingQuote.java")
    write(project / "src/main/resources/application.yaml", settings)
    copy_tests(project, package, "MockShippingServer.java", "ShippingApiTest.java")
    if retry:
        copy_tests(project, package, "ShippingRetryTest.java")


def prepare_cache(project, redis=False):
    cache = blocks("SpringBoot入門/キャッシュ入門.md")
    package = "dev.mikoto2000.springboot.cache"
    dependencies = cache["キャッシュの依存関係"]
    settings = cache["キャッシュの設定"]
    if redis:
        lesson = blocks("SpringBoot入門/Redisキャッシュ入門.md")
        dependencies += lesson["Redis の依存関係"]
        settings = lesson["application.yaml"]
        cache["Product.java"] = lesson["Product.java"]
    prepare_web_project(project, package, "CacheApplication", dependencies)
    # Apply exactly the two annotation additions instructed by the lesson.
    service = cache["ProductService.java"].replace(
        "import org.springframework.stereotype.Service;",
        "import org.springframework.stereotype.Service;\n"
        "import org.springframework.cache.annotation.Cacheable;\n"
        "import org.springframework.cache.annotation.CacheEvict;")
    for method, title in (("public Product getProduct", "取得時のキャッシュ設定"),
                          ("public void updatePrice", "更新時のキャッシュ設定")):
        if service.count(method) != 1:
            raise ValueError(f"ProductService: expected one {method}")
        service = service.replace(method, cache[title].strip() + "\n  " + method)
    cache["ProductService.java"] = service
    write_java(project, "main", cache, "CacheApplication.java", "Product.java",
               "ProductStore.java", "ProductService.java", "ProductController.java")
    write(project / "src/main/resources/application.yaml", settings)
    copy_tests(project, package, "CacheApiTest.java")
    if redis:
        copy_tests(project, package, "RedisCacheTest.java")


def prepare_security_tests(project):
    lesson = blocks("SpringBootテスト入門/SpringSecurityテスト入門.md")
    package = "dev.mikoto2000.springboot.securitytest"
    dependencies = lesson["Security テストの依存関係"] + """
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
"""
    prepare_web_project(project, package, "SecurityTestApplication", dependencies)
    write_java(project, "main", lesson, "PageController.java", "SecurityConfig.java")
    write_java(project, "test", lesson, "AccessControlTest.java", "LoginLogoutTest.java")


def prepare_security(destination, archive):
    if sha256(archive.read_bytes()).hexdigest() != SECURITY_ZIP_SHA256:
        raise ValueError("Security 配布 ZIP の SHA-256 が一致しません")
    destination.mkdir(parents=True, exist_ok=True)
    with ZipFile(archive) as release:
        release.extractall(destination)
    project = destination / "spring-boot-security-workshop-base"
    first = blocks("SpringSecurity入門/Vol1.md")
    second = blocks("SpringSecurity入門/Vol2.md")
    e2e = blocks("SpringBootテスト入門/E2Eテスト入門.md")

    pom = project / "pom.xml"
    write(pom, pom.read_text(encoding="utf-8").replace(
        "<java.version>25</java.version>", first["pom.xml の properties 内（抜粋）"].strip()))
    write_java(project, "main", first, "IndexController.java", "PrivateController.java")
    write_java(project, "main", second, "LoginController.java", "SignupController.java",
               "User.java", "UsersMapper.java", "UserDetailsServiceImpl.java", "SecurityConfig.java")
    resources = project / "src/main/resources"
    application = resources / "application.yaml"
    write(application, application.read_text(encoding="utf-8") + "\n" + first["ループバックの設定"])
    for name in ("schema.sql", "data.sql"):
        write(resources / name, second[name])
    for name in ("index.html", "private.html", "login.html", "signup.html"):
        write(resources / "templates" / name, second[name])
    write(resources / "application-e2e.yaml", e2e["application-e2e.yaml"])

    browser_tests = project / "e2e"
    write(browser_tests / "playwright.config.ts", e2e["playwright.config.ts"])
    test_body = "\n".join(e2e[title] for title in (
        "tests/auth.spec.ts", "tests/auth.spec.ts に追加（ユーザー登録）",
        "tests/auth.spec.ts に追加（ログアウト）",
    ))
    write(browser_tests / "tests/auth.spec.ts", test_body)
    for name in ("package.json", "package-lock.json", "playwright.ci.config.ts"):
        copyfile(FIXTURES / "playwright" / name, browser_tests / name)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("destination", type=Path)
    selection = parser.add_mutually_exclusive_group()
    selection.add_argument("--lesson", choices=["orders", "rfc9457", "shipping", "retry", "cache", "redis-cache", "security-tests"], default="orders")
    selection.add_argument("--security-zip", type=Path,
                        help="Security の配布 ZIP。指定時は Security と E2E を生成します。")
    options = parser.parse_args()
    if options.security_zip:
        prepare_security(options.destination, options.security_zip)
    elif options.lesson == "rfc9457":
        prepare_rfc9457(options.destination)
    elif options.lesson in ("shipping", "retry"):
        prepare_shipping(options.destination, retry=options.lesson == "retry")
    elif options.lesson in ("cache", "redis-cache"):
        prepare_cache(options.destination, redis=options.lesson == "redis-cache")
    elif options.lesson == "security-tests":
        prepare_security_tests(options.destination)
    else:
        prepare_orders(options.destination)


if __name__ == "__main__":
    main()
