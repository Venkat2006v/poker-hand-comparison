package com.poker.exception;

import io.swagger.v3.oas.annotations.media.Schema;

public record ApiError(@Schema(example = "Invalid suit 'X'; use C, D, H, or S") String error) {
}
