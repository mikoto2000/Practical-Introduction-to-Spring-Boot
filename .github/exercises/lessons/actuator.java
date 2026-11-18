package dev.mikoto2000.springboot.database;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.DockerClientFactory;
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
      var docker = DockerClientFactory.instance().client();
      try {
        docker.stopContainerCmd(postgres.getContainerId()).withTimeout(0).exec();
        assertEquals(503, get(client, "/actuator/health").statusCode());
        assertEquals(500, get(client, "/orders/99999").statusCode());
      } finally {
        // Start the same container so the application's connection settings stay valid.
        docker.startContainerCmd(postgres.getContainerId()).exec();
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
