package com.poker.model;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PokerRequest(
        @ArraySchema(arraySchema = @Schema(description = "Between 2 and 4 hands")) List<HandInput> hands) {
}
