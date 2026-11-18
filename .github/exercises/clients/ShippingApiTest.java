package dev.mikoto2000.springboot.shipping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ShippingApiTest {
  static final MockShippingServer upstream;
  static {
    try {
      upstream = new MockShippingServer();
    } catch (IOException ex) {
      throw new ExceptionInInitializerError(ex);
    }
  }

  @DynamicPropertySource
  static void settings(DynamicPropertyRegistry registry) {
    registry.add("shipping.base-url", upstream::baseUrl);
    registry.add("shipping.read-timeout", () -> "200ms");
  }

  @Autowired MockMvc mvc;

  @BeforeEach void reset() { upstream.reset(); }
  @AfterAll static void stop() { upstream.close(); }

  @Test
  void returnsTheQuoteFromTheUpstream() throws Exception {
    mvc.perform(get("/shipping-quotes/tokyo"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.area").value("tokyo"))
        .andExpect(jsonPath("$.feeYen").value(600));
    assertEquals(1, upstream.calls("tokyo"));
  }

  @Test
  void missingAreaReturns404WithoutRetrying() throws Exception {
    mvc.perform(get("/shipping-quotes/unknown"))
        .andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
        .andExpect(jsonPath("$.status").value(404));
    assertEquals(1, upstream.calls("unknown"));
  }

  @Test
  void serverErrorReturns502WithoutRetrying() throws Exception {
    mvc.perform(get("/shipping-quotes/maintenance"))
        .andExpect(status().isBadGateway())
        .andExpect(jsonPath("$.status").value(502));
    assertEquals(1, upstream.calls("maintenance"));
  }

  @Test
  void readTimeoutReturns504WithoutRetrying() throws Exception {
    mvc.perform(get("/shipping-quotes/slow"))
        .andExpect(status().isGatewayTimeout())
        .andExpect(jsonPath("$.status").value(504));
    assertEquals(1, upstream.calls("slow"));
  }

  @Test
  void emptyBodyReturns502() throws Exception {
    mvc.perform(get("/shipping-quotes/empty"))
        .andExpect(status().isBadGateway())
        .andExpect(jsonPath("$.status").value(502));
    assertEquals(1, upstream.calls("empty"));
  }
}
