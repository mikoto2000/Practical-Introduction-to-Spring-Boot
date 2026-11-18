package dev.mikoto2000.springboot.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CacheApiTest {
  @Autowired MockMvc mvc;
  @Autowired CacheManager cacheManager;
  @MockitoSpyBean ProductStore store;

  @BeforeEach
  void reset() {
    cacheManager.getCache("products").clear();
    store.updatePrice(1, 1200);
    store.updatePrice(2, 800);
    clearInvocations(store);
  }

  @Test
  void repeatedReadsUseTheCache() throws Exception {
    for (int i = 0; i < 2; i++) {
      mvc.perform(get("/products/1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.priceYen").value(1200));
    }
    verify(store, times(1)).findById(1);
  }

  @Test
  void updateEvictsOnlyTheUpdatedProduct() throws Exception {
    mvc.perform(get("/products/1")).andExpect(status().isOk());
    mvc.perform(get("/products/2")).andExpect(status().isOk());
    mvc.perform(put("/products/1/price").param("priceYen", "1500"))
        .andExpect(status().isNoContent());
    for (int i = 0; i < 2; i++) {
      mvc.perform(get("/products/1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.priceYen").value(1500));
    }
    mvc.perform(get("/products/2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.priceYen").value(800));
    verify(store, times(2)).findById(1);
    verify(store, times(1)).findById(2);
  }

  @Test
  void invalidUpdateDoesNotChangeTheStoredOrCachedPrice() throws Exception {
    mvc.perform(get("/products/1")).andExpect(status().isOk());
    mvc.perform(put("/products/1/price").param("priceYen", "-1"))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/products/1"))
        .andExpect(jsonPath("$.priceYen").value(1200));
    verify(store, times(1)).findById(1);
    assertEquals(1200, store.findById(1).priceYen());
  }
}
