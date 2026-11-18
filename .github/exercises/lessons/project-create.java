package dev.mikoto2000.workshop.projectcreate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import dev.mikoto2000.workshop.projectcreate.calcage.service.CalcAgeService;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectCreateTest {
  @Autowired MockMvc mvc;
  @Autowired CalcAgeService service;

  @Test void calculatesAgeOverHttp() throws Exception {
    mvc.perform(get("/api/calc-age").param("birthDate", LocalDate.now().minusYears(20).toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.age").value(20));
    mvc.perform(get("/api/calc-age").param("birthDate", "invalid"))
        .andExpect(status().isBadRequest());
    assertThrows(IllegalArgumentException.class, () -> service.calculateAge(null));
    assertThrows(IllegalArgumentException.class, () -> service.calculateAge(LocalDate.now().plusDays(1)));
  }
}
