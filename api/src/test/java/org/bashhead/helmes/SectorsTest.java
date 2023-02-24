package org.bashhead.helmes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SectorsTest {

  @Autowired
  MockMvc mockMvc;

  private final ObjectMapper json = new ObjectMapper();

  @Test
  void shouldReturnListOfSectors() throws Exception {
    var result = mockMvc.perform(get("/api/sector"))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    assertThat(json.readTree(result))
        .filteredOn(node -> Set.of(1, 269, 437).contains(node.path("id").asInt()))
        .extracting(node -> tuple(node.path("name").textValue(), node.path("level").intValue()))
        .containsOnly(
            tuple("Manufacturing", 0),
            tuple("Other", 2),
            tuple("Boat/Yacht building", 3));
  }
}