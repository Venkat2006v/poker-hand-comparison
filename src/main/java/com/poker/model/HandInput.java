package com.poker.model;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record HandInput(
        @Schema(description = "Player name, unique within the request", example = "A") String player,
        @ArraySchema(arraySchema = @Schema(description = "5 or 7 cards: rank (2-10, J, Q, K, A) followed by suit (C, D, H, S), case-insensitive"),
                schema = @Schema(example = "10H")) List<String> cards) {
}
