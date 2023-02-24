package org.bashhead.helmes.model;

import java.util.List;
import java.util.UUID;

public record UserData(
    UUID id,
    String name,
    Boolean tos,
    List<Long> sectors) {
}
