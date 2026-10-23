package com.poker.controller;

import com.poker.exception.ApiError;
import com.poker.model.PokerRequest;
import com.poker.model.PokerResponse;
import com.poker.service.PokerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.poker.controller.PokerApiExamples.*;

@RestController
@RequestMapping("/api/poker")
@Tag(name = "Poker", description = "Compare poker hands")
public class PokerController {
    private static final String JSON = "application/json";

    private final PokerService pokerService;

    public PokerController(PokerService pokerService) {
        this.pokerService = pokerService;
    }

    @PostMapping("/compare")
    @Operation(summary = "Compare 2–4 poker hands",
            description = "Each hand has 5 or 7 cards. 7-card hands are scored by their best five cards. "
                    + "Categories, strongest first: Straight, Three of a Kind, Two of a Kind (Pair), High Card. "
                    + "All hands that tie for best are returned as winners. "
                    + "Pick an example from the request body dropdown to try a scenario.")
    @ApiResponse(responseCode = "200", description = "Winners and each hand's evaluation",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = PokerResponse.class), examples = {
                    @ExampleObject(name = "One winner", value = ONE_WINNER_RESPONSE),
                    @ExampleObject(name = "Kicker decides", value = KICKER_RESPONSE),
                    @ExampleObject(name = "Tie", value = TIE_RESPONSE),
                    @ExampleObject(name = "Best five from seven", value = SEVEN_CARD_RESPONSE),
                    @ExampleObject(name = "Four hands, two winners", value = FOUR_HANDS_RESPONSE)}))
    @ApiResponse(responseCode = "400", description = "Invalid request, such as a malformed card or wrong number of hands",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiError.class), examples = {
                    @ExampleObject(name = "Invalid card", value = INVALID_CARD_ERROR),
                    @ExampleObject(name = "Duplicate card", value = DUPLICATE_CARD_ERROR),
                    @ExampleObject(name = "Wrong hand size", value = WRONG_HAND_SIZE_ERROR),
                    @ExampleObject(name = "Too few hands", value = TOO_FEW_HANDS_ERROR),
                    @ExampleObject(name = "Invalid JSON", value = INVALID_JSON_ERROR)}))
    public PokerResponse compare(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = JSON, examples = {
                    @ExampleObject(name = "Straight beats Three of a Kind", value = STRAIGHT_BEATS_TRIPS),
                    @ExampleObject(name = "Higher pair wins", value = HIGHER_PAIR_WINS),
                    @ExampleObject(name = "Kicker decides", value = KICKER_DECIDES),
                    @ExampleObject(name = "Tie", value = TIE),
                    @ExampleObject(name = "Best five from seven cards", value = SEVEN_CARD_HAND),
                    @ExampleObject(name = "Ace-low straight loses to six-high", value = ACE_LOW_STRAIGHT),
                    @ExampleObject(name = "Four hands, mixed sizes", value = FOUR_HANDS_MIXED_SIZES),
                    @ExampleObject(name = "Invalid: bad card", value = INVALID_CARD),
                    @ExampleObject(name = "Invalid: duplicate card", value = DUPLICATE_CARD),
                    @ExampleObject(name = "Invalid: 6-card hand", value = WRONG_HAND_SIZE),
                    @ExampleObject(name = "Invalid: only one hand", value = TOO_FEW_HANDS)}))
            @RequestBody PokerRequest request) {
        return pokerService.compare(request);
    }
}
