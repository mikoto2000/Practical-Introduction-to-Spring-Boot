package dev.mikoto2000.springboot.scheduling;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
class SchedulingTest {
  @Test void actuallySchedulesRepeatedWork(CapturedOutput output) {
    try (var app = new SpringApplicationBuilder(SchedulingApplication.class)
        .properties("spring.main.web-application-type=none").run()) {
      await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
        assertTrue(output.getAll().split("START", -1).length >= 3);
        assertTrue(output.getAll().split("END", -1).length >= 3);
      });
    }
  }
}
