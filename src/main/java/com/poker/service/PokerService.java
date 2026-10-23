package com.poker.service;

import com.poker.model.Card;
import com.poker.model.HandEvaluation;
import com.poker.model.HandInput;
import com.poker.model.HandResult;
import com.poker.model.PokerRequest;
import com.poker.model.PokerResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Validates a comparison request, scores each hand and picks the winners. Scoring lives in {@link HandEvaluator}. */
@Service
public class PokerService {
    private static final int MIN_HANDS = 2;
    private static final int MAX_HANDS = 4;
    private static final int FIVE_CARD_HAND = 5;
    private static final int SEVEN_CARD_HAND = 7;

    private final HandEvaluator handEvaluator;

    public PokerService(HandEvaluator handEvaluator) {
        this.handEvaluator = handEvaluator;
    }

    public PokerResponse compare(PokerRequest request) {
        if (request == null || request.hands() == null) {
            throw new IllegalArgumentException("Request must contain a hands array");
        }
        List<HandInput> hands = request.hands();
        if (hands.size() < MIN_HANDS || hands.size() > MAX_HANDS) {
            throw new IllegalArgumentException("Provide between " + MIN_HANDS + " and " + MAX_HANDS + " hands");
        }

        Set<String> players = new HashSet<>();
        List<HandResult> results = new ArrayList<>();
        List<HandEvaluation> evaluations = new ArrayList<>();
        for (int i = 0; i < hands.size(); i++) {
            HandInput hand = hands.get(i);
            if (hand == null) {
                throw new IllegalArgumentException("Hand at index " + i + " cannot be null");
            }
            String player = requireUniquePlayerName(hand.player(), players);
            HandEvaluation evaluation = handEvaluator.evaluate(parseCards(hand.cards(), i));
            evaluations.add(evaluation);
            results.add(toResult(player, evaluation));
        }
        return new PokerResponse(findWinners(evaluations, results), List.copyOf(results));
    }

    /** Returns the players whose hands tie for best, in request order. */
    private List<String> findWinners(List<HandEvaluation> evaluations, List<HandResult> results) {
        HandEvaluation best = evaluations.stream().max(HandEvaluation.BY_STRENGTH).orElseThrow();
        List<String> winners = new ArrayList<>();
        for (int i = 0; i < evaluations.size(); i++) {
            if (HandEvaluation.BY_STRENGTH.compare(evaluations.get(i), best) == 0) {
                winners.add(results.get(i).player());
            }
        }
        return List.copyOf(winners);
    }

    private HandResult toResult(String player, HandEvaluation evaluation) {
        List<String> bestFive = evaluation.bestFive().stream().map(Card::notation).toList();
        return new HandResult(player, evaluation.category().label(), bestFive);
    }

    /** Checks the name is non-blank and not already used, records it in {@code seen}, and returns it trimmed. */
    private String requireUniquePlayerName(String player, Set<String> seen) {
        if (player == null || player.isBlank()) {
            throw new IllegalArgumentException("Each hand must have a non-empty player name");
        }
        String trimmed = player.trim();
        if (!seen.add(trimmed)) {
            throw new IllegalArgumentException("Player names must be unique: '" + trimmed + "'");
        }
        return trimmed;
    }

    private List<Card> parseCards(List<String> rawCards, int handIndex) {
        if (rawCards == null) {
            throw new IllegalArgumentException("Cards are required for hand at index " + handIndex);
        }
        if (rawCards.size() != FIVE_CARD_HAND && rawCards.size() != SEVEN_CARD_HAND) {
            throw new IllegalArgumentException("Hand at index " + handIndex + " must contain "
                    + FIVE_CARD_HAND + " or " + SEVEN_CARD_HAND + " cards");
        }
        List<Card> cards = rawCards.stream().map(Card::parse).toList();
        // Clients can send the same card twice; that hand can't be scored correctly.
        if (new HashSet<>(cards).size() != cards.size()) {
            throw new IllegalArgumentException("Hand at index " + handIndex + " contains duplicate cards");
        }
        return cards;
    }
}
