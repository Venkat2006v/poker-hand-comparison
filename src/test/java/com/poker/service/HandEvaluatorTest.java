package com.poker.service;

import com.poker.model.Card;
import com.poker.model.HandCategory;
import com.poker.model.HandEvaluation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HandEvaluatorTest {
    private final HandEvaluator evaluator = new HandEvaluator();

    // --- Straights ---

    @Test
    void recognizesAceLowStraightAsFiveHigh() {
        HandEvaluation result = evaluate("AC", "2D", "3H", "4S", "5C");

        assertEquals(HandCategory.STRAIGHT, result.category());
        assertEquals(List.of(5), result.tieBreakRanks());
    }

    @Test
    void recognizesAceHighStraight() {
        HandEvaluation result = evaluate("10C", "JD", "QH", "KS", "AC");

        assertEquals(HandCategory.STRAIGHT, result.category());
        assertEquals(List.of(14), result.tieBreakRanks());
    }

    @Test
    void wrapAroundIsNotAStraight() {
        assertEquals(HandCategory.HIGH_CARD, evaluate("QC", "KD", "AH", "2S", "3C").category());
    }

    @Test
    void findsAStraightInsideSevenCards() {
        HandEvaluation result = evaluate("2C", "9D", "5H", "KS", "6C", "8D", "7H");

        assertEquals(HandCategory.STRAIGHT, result.category());
        assertEquals(List.of(9), result.tieBreakRanks());
        assertEquals(List.of("9D", "8D", "7H", "6C", "5H"), notation(result));
    }

    @Test
    void sevenCardsPreferTheHigherStraight() {
        assertEquals(List.of(9), evaluate("4C", "5D", "6H", "7S", "8C", "9D", "2H").tieBreakRanks());
    }

    @Test
    void sevenCardsPreferSixHighOverTheWheel() {
        assertEquals(List.of(6), evaluate("AC", "2D", "3H", "4S", "5C", "6D", "KH").tieBreakRanks());
    }

    @Test
    void straightBeatsTripsInTheSameSevenCards() {
        HandEvaluation result = evaluate("5C", "5D", "5H", "6S", "7C", "8D", "9H");

        assertEquals(HandCategory.STRAIGHT, result.category());
        assertEquals(List.of(9), result.tieBreakRanks());
    }

    // --- Categories this exercise does not score ---

    @Test
    void fourOfAKindIsScoredAsThreeOfAKindWithTheFourthCardAsAKicker() {
        HandEvaluation result = evaluate("KC", "KD", "KH", "KS", "2C");

        assertEquals(HandCategory.THREE_OF_A_KIND, result.category());
        assertEquals(List.of(13, 13, 2), result.tieBreakRanks());
    }

    @Test
    void sevenCardQuadsPreferAHigherKickerOverTheFourthCard() {
        HandEvaluation result = evaluate("7C", "7D", "7H", "7S", "KC", "9D", "3H");

        assertEquals(List.of(7, 13, 9), result.tieBreakRanks());
    }

    @Test
    void fullHouseIsScoredAsThreeOfAKind() {
        HandEvaluation result = evaluate("7C", "7D", "7H", "KS", "KC");

        assertEquals(HandCategory.THREE_OF_A_KIND, result.category());
        assertEquals(List.of(7, 13, 13), result.tieBreakRanks());
    }

    @Test
    void twoTripsInSevenCardsUseTheHigherTrips() {
        assertEquals(List.of(13, 7, 7), evaluate("7C", "7D", "7H", "KS", "KC", "KD", "3H").tieBreakRanks());
    }

    @Test
    void twoPairIsScoredAsAPairOfTheHigherRank() {
        HandEvaluation result = evaluate("KC", "KD", "7H", "7S", "3C");

        assertEquals(HandCategory.PAIR, result.category());
        assertEquals(List.of(13, 7, 7, 3), result.tieBreakRanks());
    }

    @Test
    void threePairsInSevenCardsKeepTheBestKickers() {
        assertEquals(List.of(13, 14, 7, 7), evaluate("KC", "KD", "7H", "7S", "3C", "3D", "AH").tieBreakRanks());
    }

    // --- Best five from seven ---

    @Test
    void picksTripsAndTheTwoHighestKickersFromSeven() {
        HandEvaluation result = evaluate("7C", "7D", "7H", "2S", "9C", "KH", "3D");

        assertEquals(HandCategory.THREE_OF_A_KIND, result.category());
        assertEquals(List.of(7, 13, 9), result.tieBreakRanks());
        assertEquals(List.of("KH", "9C", "7C", "7D", "7H"), notation(result));
    }

    @Test
    void highCardKeepsTheFiveHighestOfSeven() {
        HandEvaluation result = evaluate("2C", "4D", "6H", "8S", "10C", "QD", "AH");

        assertEquals(HandCategory.HIGH_CARD, result.category());
        assertEquals(List.of(14, 12, 10, 8, 6), result.tieBreakRanks());
    }

    // --- Agreement with the reference evaluator ---

    @Test
    void matchesReferenceEvaluatorOnRandomHands() {
        List<Card> deck = new ArrayList<>();
        for (int rank = 2; rank <= 14; rank++) {
            for (Card.Suit suit : Card.Suit.values()) {
                deck.add(new Card(rank, suit));
            }
        }
        Random random = new Random(42);
        for (int trial = 0; trial < 20_000; trial++) {
            Collections.shuffle(deck, random);
            List<Card> hand = List.copyOf(deck.subList(0, trial % 2 == 0 ? 5 : 7));

            HandEvaluation fast = evaluator.evaluate(hand);
            HandEvaluation reference = ReferenceEvaluator.evaluate(hand);

            assertEquals(reference.category(), fast.category(), () -> "category for " + hand);
            assertEquals(reference.tieBreakRanks(), fast.tieBreakRanks(), () -> "tiebreak ranks for " + hand);
            assertEquals(5, fast.bestFive().size());
            assertTrue(hand.containsAll(fast.bestFive()), () -> "best five must come from " + hand);
            assertEquals(0, HandEvaluation.BY_STRENGTH.compare(fast, evaluator.evaluate(fast.bestFive())),
                    () -> "best five must score the same as the hand for " + hand);
        }
    }

    private HandEvaluation evaluate(String... cards) {
        return evaluator.evaluate(Arrays.stream(cards).map(Card::parse).toList());
    }

    private List<String> notation(HandEvaluation evaluation) {
        return evaluation.bestFive().stream().map(Card::notation).toList();
    }
}
