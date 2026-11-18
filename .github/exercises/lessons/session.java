package dev.mikoto2000.springboot.session;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;

class SessionTest {
  @Test void sharesAndInvalidatesSessionAcrossTwoServers() throws Exception {
    String namespace = "session-ci-" + java.util.UUID.randomUUID();
    String[] settings = {"--server.port=0", "--spring.session.data.redis.namespace=" + namespace};
    try (var a = new SpringApplicationBuilder(LessonApplication.class).run(settings);
         var b = new SpringApplicationBuilder(LessonApplication.class).run(
             settings[0], settings[1], "--lab.instance=B");
         var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()) {
      String first = "http://127.0.0.1:" + ((WebServerApplicationContext) a).getWebServer().getPort();
      String second = "http://127.0.0.1:" + ((WebServerApplicationContext) b).getWebServer().getPort();
      var saved = client.send(HttpRequest.newBuilder(URI.create(first + "/session?name=Alice"))
          .timeout(Duration.ofSeconds(10)).POST(HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
      assertEquals(204, saved.statusCode());
      String cookie = saved.headers().firstValue("Set-Cookie").orElseThrow().split(";", 2)[0];
      assertTrue(cookie.startsWith("SESSION="));
      assertEquals("{\"instance\":\"A\",\"name\":\"Alice\"}", send(client, first, cookie, "GET").body());
      assertEquals("{\"instance\":\"B\",\"name\":\"Alice\"}", send(client, second, cookie, "GET").body());
      assertEquals("{\"instance\":\"B\",\"name\":null}", send(client, second, "", "GET").body());
      assertEquals(204, send(client, second, cookie, "DELETE").statusCode());
      assertEquals("{\"instance\":\"A\",\"name\":null}", send(client, first, cookie, "GET").body());
    }
  }

  private HttpResponse<String> send(HttpClient client, String base, String cookie, String method) throws Exception {
    return client.send(HttpRequest.newBuilder(URI.create(base + "/session"))
        .timeout(Duration.ofSeconds(10)).header("Cookie", cookie)
        .method(method, HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
  }
}
