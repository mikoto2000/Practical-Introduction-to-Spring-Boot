package dev.mikoto2000.springboot.logging;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class LoggingTest {
  @Autowired MockMvc mvc;

  @Test void logsAccessBusinessEventsAndMethodExecution(CapturedOutput output) throws Exception {
    mvc.perform(post("/addUser").param("name", "Alice\nInjected"))
        .andExpect(status().isOk());
    mvc.perform(get("/getUsers"))
        .andExpect(status().isOk()).andExpect(jsonPath("$[0]").value("Alice\nInjected"));
    mvc.perform(delete("/removeUser").param("name", "Alice\nInjected"))
        .andExpect(status().isOk());
    assertTrue(output.getAll().contains("Add user: name=Alice?Injected"));
    assertTrue(output.getAll().contains("Remove user: name=Alice?Injected"));
    assertTrue(output.getAll().contains("START UserController#addUser"));
    assertTrue(output.getAll().contains("END   UserController#addUser"));
    assertTrue(output.getAll().contains("success=SUCCESS"));
    assertNull(MDC.get("traceId"));
    assertNull(MDC.get("user"));
  }

  @Test void logsFailuresAndCleansMdc(CapturedOutput output) {
    assertThrows(Exception.class, () -> mvc.perform(get("/fireException")));
    assertTrue(output.getAll().contains("ERROR UserController#fireException"));
    assertTrue(output.getAll().contains("success=FAIL"));
    assertNull(MDC.get("traceId"));
    assertNull(MDC.get("user"));
  }
}
