package com.poker.model;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PokerResponse(
        @ArraySchema(arraySchema = @Schema(description = "Players with the best hand; more than one on a tie"),
                schema = @Schema(example = "A")) List<String> winners,
        @ArraySchema(arraySchema = @Schema(description = "Evaluation of every hand, in request order")) List<HandResult> results) {
}
