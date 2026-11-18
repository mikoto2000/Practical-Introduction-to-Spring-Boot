package dev.mikoto2000.springboot.validation.workshop.firststep;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ValidationTest {
  @Autowired MockMvc mvc;

  @Test void missingAndEmptyParametersUseDefaultValue() throws Exception {
    mvc.perform(get("/number-echo"))
        .andExpect(status().isOk()).andExpect(content().string("0\n"));
    mvc.perform(get("/number-echo").param("message", ""))
        .andExpect(status().isOk()).andExpect(content().string("0\n"));
    mvc.perform(get("/number-echo").param("message", "123"))
        .andExpect(status().isOk()).andExpect(content().string("123\n"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"123a", "12345678901"})
  void rejectsInvalidParameters(String message) throws Exception {
    mvc.perform(get("/number-echo").param("message", message))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].field").value("message"));
  }

  @Test void validatesBodiesAndRejectsMalformedJson() throws Exception {
    mvc.perform(post("/bean-echo").contentType(MediaType.APPLICATION_JSON)
        .content("{\"stringValue\":\"hello\",\"integerValue\":1,\"doubleValue\":1.5}"))
        .andExpect(status().isOk());
    mvc.perform(post("/bean-echo").contentType(MediaType.APPLICATION_JSON)
        .content("{\"stringValue\":null,\"integerValue\":1,\"doubleValue\":1.5}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].field").value("stringValue"));
    mvc.perform(post("/bean-echo").contentType(MediaType.APPLICATION_JSON).content("{"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].message").value("JSON の形式または値の型が不正です"));
  }
}
