package org.bashhead.helmes;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserDataTest {

  @Autowired
  MockMvc mockMvc;

  private final ObjectMapper json = new ObjectMapper();

  @Test
  void shouldReturnDefaultData() throws Exception {
    var uuid = UUID.randomUUID();

    var result = mockMvc.perform(get("/api/user-data/" + uuid))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    Map<String, Object> map = json.readValue(result, mapType());
    assertThat(map)
        .containsOnly(
            e("id", uuid.toString()),
            e("name", null),
            e("tos", false),
            e("sectors", List.of()));
  }

  @Test
  void canInsertData() throws Exception {
    var uuid = UUID.randomUUID();
    mockMvc.perform(put("/api/user-data")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(Map.ofEntries(
                e("id", uuid),
                e("name", "SomeName"),
                e("tos", true),
                e("sectors", List.of(1, 437, 269))))))
        .andExpect(status().isOk());

    var result = mockMvc.perform(get("/api/user-data/" + uuid))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    Map<String, Object> map = json.readValue(result, mapType());
    assertThat(map)
        .containsOnly(
            e("id", uuid.toString()),
            e("name", "SomeName"),
            e("tos", true),
            e("sectors", List.of(1, 269, 437)));
  }

  @Test
  void canModifyData() throws Exception {
    var uuid = UUID.randomUUID();
    mockMvc.perform(put("/api/user-data")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(Map.ofEntries(
                e("id", uuid),
                e("name", "SomeName"),
                e("tos", true),
                e("sectors", List.of(1, 437, 269))))))
        .andExpect(status().isOk());
    mockMvc.perform(put("/api/user-data")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(Map.ofEntries(
                e("id", uuid),
                e("name", "OtherName"),
                e("tos", true),
                e("sectors", List.of(2, 437, 230))))))
        .andExpect(status().isOk());

    var result = mockMvc.perform(get("/api/user-data/" + uuid))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    Map<String, Object> map = json.readValue(result, mapType());
    assertThat(map)
        .containsOnly(
            e("id", uuid.toString()),
            e("name", "OtherName"),
            e("tos", true),
            e("sectors", List.of(2, 230, 437)));
  }

  static Stream<Arguments> verifyValidationParams() {
    return Stream.of(
        Arguments.of(e("tos", false), "tos", "must be true"),
        Arguments.of(e("tos", null), "tos", "must not be null"),
        Arguments.of(e("id", null), "id", "must not be null"),
        Arguments.of(e("name", null), "name", "must not be blank"),
        Arguments.of(e("name", ""), "name", "must not be blank"),
        Arguments.of(e("sectors", null), "sectors", "must not be empty"),
        Arguments.of(e("sectors", List.of()), "sectors", "must not be empty"),
        Arguments.of(e("sectors", List.of(-2)), "sectors[0]", "must be greater than 0"),
        Arguments.of(e("sectors", Arrays.asList(1, null, 2)), "sectors[1]", "must not be null"));
  }

  @ParameterizedTest
  @MethodSource("verifyValidationParams")
  void verifyValidation(Map.Entry<String, Object> override, String field, String message) throws Exception {
    var result = mockMvc.perform(put("/api/user-data")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(ofEntries(Stream.of(
                    e("id", UUID.randomUUID()),
                    e("name", "SomeName"),
                    e("tos", true),
                    e("sectors", List.of(1)),
                    override)
                .toList()))))
        .andExpect(status().isBadRequest())
        .andReturn().getResponse().getContentAsString();

    Map<String, Object> map = json.readValue(result, mapType());
    assertThat(map)
        .contains(e("detail", "Validation failed"))
        .extracting("errors", LIST)
        .extracting("field", "message")
        .containsOnly(Tuple.tuple(field, message));
  }

  private static Map<String, Object> ofEntries(List<Map.Entry<String, Object>> entries) {
    Map<String, Object> result = new LinkedHashMap<>(entries.size());
    for (var entry : entries) {
      result.put(entry.getKey(), entry.getValue());
    }
    return unmodifiableMap(result);
  }

  private static Map.Entry<String, Object> e(String key, Object value) {
    return new AbstractMap.SimpleEntry<>(key, value);
  }

  private MapType mapType() {
    return json.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, Object.class);
  }
}