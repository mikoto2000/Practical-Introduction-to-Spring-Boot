package dev.mikoto2000.springboot.database;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.DriverManager;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("actuator-lab")
@Import(PostgresTestConfiguration.class)
class ActuatorTest {
  @LocalServerPort int port;
  @Autowired javax.sql.DataSource dataSource;
  @Autowired PostgreSQLContainer postgres;
  private final JsonMapper json = new JsonMapper();

  @Test void reportsDatabaseOutageRecoveryAndStatusSpecificMetrics() throws Exception {
    new ResourceDatabasePopulator(new ClassPathResource("schema.sql")).execute(dataSource);
    try (var client = HttpClient.newHttpClient()) {
      assertEquals(200, get(client, "/actuator/health").statusCode());
      assertEquals(404, get(client, "/orders/99999").statusCode());
      await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertEquals(1, count(client)));
      double before = count(client);
      assertEquals(404, get(client, "/orders/99999").statusCode());
      await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertEquals(before + 1, count(client)));
      assertEquals(404, get(client, "/actuator/env").statusCode());
      // Keep the server and its random mapped port alive while denying application
      // connections. Restarting a container can change that port under Testcontainers.
      String adminUrl = "jdbc:postgresql://" + postgres.getHost() + ":" + postgres.getMappedPort(5432) + "/postgres";
      try (var admin = DriverManager.getConnection(adminUrl, postgres.getUsername(), postgres.getPassword());
           var statement = admin.createStatement();
           var terminate = admin.prepareStatement("SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = ?")) {
        String database = "\"" + postgres.getDatabaseName().replace("\"", "\"\"") + "\"";
        try {
          statement.execute("ALTER DATABASE " + database + " ALLOW_CONNECTIONS false");
          terminate.setString(1, postgres.getDatabaseName());
          terminate.execute();
          assertEquals(503, get(client, "/actuator/health").statusCode());
          assertEquals(500, get(client, "/orders/99999").statusCode());
        } finally {
          statement.execute("ALTER DATABASE " + database + " ALLOW_CONNECTIONS true");
        }
      }
      await().atMost(Duration.ofSeconds(60)).untilAsserted(() ->
          assertEquals(200, get(client, "/actuator/health").statusCode()));
    }
  }

  private double count(HttpClient client) throws Exception {
    var response = get(client, "/actuator/metrics/http.server.requests?tag=method:GET&tag=status:404&tag=uri:/orders/%7Bid%7D");
    assertEquals(200, response.statusCode());
    for (var measurement : json.readTree(response.body()).get("measurements")) {
      if (measurement.get("statistic").asString().equals("COUNT")) return measurement.get("value").asDouble();
    }
    throw new AssertionError("COUNT measurement missing");
  }

  private HttpResponse<String> get(HttpClient client, String path) throws Exception {
    return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
        .timeout(Duration.ofSeconds(15)).GET().build(), HttpResponse.BodyHandlers.ofString());
  }
}
