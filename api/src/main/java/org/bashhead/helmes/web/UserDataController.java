package org.bashhead.helmes.web;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.UUID;
import org.bashhead.helmes.dao.UserDataDao;
import org.bashhead.helmes.model.UserData;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserDataController {

  private final UserDataDao dao;

  public UserDataController(UserDataDao dao) {
    this.dao = dao;
  }

  @GetMapping("/api/user-data/{id}")
  public UserData fetchUserData(@PathVariable UUID id) {
    return dao.tryFind(id).orElseGet(() -> new UserData(id, null, false, List.of()));
  }

  @Transactional
  @PutMapping("/api/user-data")
  public void saveUserData(@RequestBody @Validated UserDataRequest request) {
    dao.save(request.id(), request.name(), request.tos(), request.sectors());
  }

  public record UserDataRequest(
      @NotNull UUID id,
      @NotBlank String name,
      @NotNull @AssertTrue Boolean tos,
      @NotEmpty List<@NotNull @Positive Long> sectors
  ) {}
}
