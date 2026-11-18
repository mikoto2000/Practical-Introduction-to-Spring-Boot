package dev.mikoto2000.springboot.shipping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

class ShippingRetryTest {
  @ParameterizedTest
  @ValueSource(ints = {0, 1, 2})
  void respectsRetryLimitAndWaitsBetweenAttempts(int retries) throws Exception {
    try (var upstream = new MockShippingServer()) {
      var builder = RestClient.builder().requestFactory(new SimpleClientHttpRequestFactory());
      var client = new ShippingClient(builder, upstream.baseUrl(), Duration.ofSeconds(1),
          Duration.ofSeconds(1), retries, Duration.ofMillis(50));
      long start = System.nanoTime();
      if (retries < 2) {
        var failure = assertThrows(ResponseStatusException.class, () -> client.getQuote("flaky"));
        assertEquals(502, failure.getStatusCode().value());
      } else {
        assertEquals("flaky", client.getQuote("flaky").area());
      }
      long elapsedMillis = Duration.ofNanos(System.nanoTime() - start).toMillis();
      assertEquals(retries + 1, upstream.calls("flaky"));
      assertTrue(elapsedMillis >= retries * 50L,
          "Each retry must include the configured delay");
    }
  }
}
