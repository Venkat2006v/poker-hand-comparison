package com.poker.model;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record HandResult(
        @Schema(example = "A") String player,
        @Schema(allowableValues = {"Straight", "Three of a Kind", "Two of a Kind", "High Card"}, example = "Straight") String category,
        @ArraySchema(arraySchema = @Schema(description = "The five cards that make the hand, highest rank first"),
                schema = @Schema(example = "9H")) List<String> bestFive) {
}
