package com.poker.service;

import com.poker.model.HandInput;
import com.poker.model.PokerRequest;
import com.poker.model.PokerResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PokerServiceTest {
    private final PokerService service = new PokerService(new HandEvaluator());

    // --- Worked examples from the specification ---

    @Test
    void straightBeatsThreeOfAKind() {
        var result = compare(
                hand("A", "5H", "6D", "7S", "8C", "9H"),
                hand("B", "KC", "KD", "KH", "2S", "3C"));

        assertEquals(List.of("A"), result.winners());
        assertEquals("Straight", result.results().getFirst().category());
    }

    @Test
    void pairRankOutranksAnOpponentsKickers() {
        var result = compare(
                hand("Queens", "QH", "QD", "4C", "9S", "2H"),
                hand("Jacks", "JC", "JD", "AS", "KH", "3C"));

        assertEquals(List.of("Queens"), result.winners());
    }

    @Test
    void comparesPairKickersFromHighestToLowest() {
        var result = compare(
                hand("A", "8C", "8D", "KC", "4H", "2S"),
                hand("B", "8H", "8S", "QC", "4C", "3S"));

        assertEquals(List.of("A"), result.winners());
    }

    @Test
    void allowsTiesAndDoesNotUseSuitsToBreakThem() {
        var result = compare(
                hand("A", "2C", "3D", "4H", "9S", "KC"),
                hand("B", "2S", "3H", "4D", "9C", "KH"));

        assertEquals(List.of("A", "B"), result.winners());
    }

    @Test
    void picksBestFiveCardsFromSeven() {
        var result = compare(
                hand("A", "7C", "7D", "7H", "2S", "9C", "KH", "3D"),
                hand("B", "5C", "6D", "7S", "8H", "9D"));

        assertEquals(List.of("B"), result.winners());
        assertEquals("Three of a Kind", result.results().getFirst().category());
        assertEquals(List.of("KH", "9C", "7C", "7D", "7H"), result.results().getFirst().bestFive());
    }

    // --- Tiebreakers ---

    @Test
    void fourOfAKindLosesToAStraight() {
        var result = compare(
                hand("Quads", "KC", "KD", "KH", "KS", "2C"),
                hand("Straight", "2D", "3H", "4S", "5C", "6D"));

        assertEquals(List.of("Straight"), result.winners());
    }

    @Test
    void fourOfAKindBeatsThreeOfTheSameRank() {
        var result = compare(
                hand("Quads", "KC", "KD", "KH", "KS", "2C"),
                hand("Trips", "KC", "KD", "KH", "3S", "2D"));

        assertEquals(List.of("Quads"), result.winners());
    }

    @Test
    void fourOfAKindBeatsAPair() {
        var result = compare(
                hand("Quads", "KC", "KD", "KH", "KS", "2C"),
                hand("Pair", "3C", "3D", "5H", "6S", "9C"));

        assertEquals(List.of("Quads"), result.winners());
    }

    @Test
    void comparesThreeOfAKindByTripRank() {
        var result = compare(
                hand("Kings", "KC", "KD", "KH", "2S", "3C"),
                hand("Fives", "5C", "5D", "5H", "AS", "KS"));

        assertEquals(List.of("Kings"), result.winners());
    }

    @Test
    void comparesThreeOfAKindKickersWhenTripsMatch() {
        var result = compare(
                hand("A", "7C", "7D", "7H", "2S", "9C", "KH", "3D"),
                hand("B", "7S", "7C", "7D", "QS", "9H", "4C", "3S"));

        assertEquals(List.of("A"), result.winners());
    }

    @Test
    void comparesStraightsByTheirTopCard() {
        var result = compare(
                hand("Nine high", "5C", "6D", "7H", "8S", "9C"),
                hand("Ten high", "6C", "7D", "8H", "9S", "10C"));

        assertEquals(List.of("Ten high"), result.winners());
    }

    @Test
    void aceLowStraightLosesToSixHighStraight() {
        var result = compare(
                hand("Wheel", "AC", "2D", "3H", "4S", "5C"),
                hand("Six high", "2C", "3D", "4H", "5S", "6C"));

        assertEquals(List.of("Six high"), result.winners());
    }

    @Test
    void comparesHighCardsBeyondTheFirstCard() {
        var result = compare(
                hand("A", "KC", "9D", "7H", "4S", "2C"),
                hand("B", "KD", "9H", "6S", "4C", "3D"));

        assertEquals(List.of("A"), result.winners());
    }

    @Test
    void comparesHighCardsDownToTheLastCard() {
        var result = compare(
                hand("A", "KC", "9D", "7H", "4S", "3C"),
                hand("B", "KD", "9H", "7S", "4C", "2D"));

        assertEquals(List.of("A"), result.winners());
    }

    // --- Multiple hands and mixed sizes ---

    @Test
    void supportsFourHandsWithAThreeWayTie() {
        var result = compare(
                hand("A", "5C", "6D", "7H", "8S", "9C"),
                hand("B", "5D", "6H", "7S", "8C", "9D"),
                hand("C", "KC", "KD", "KH", "2S", "3C"),
                hand("D", "5H", "6S", "7C", "8D", "9H", "2C", "2D"));

        assertEquals(List.of("A", "B", "D"), result.winners());
    }

    @Test
    void supportsMixedHandSizes() {
        var result = compare(
                hand("Five", "QH", "QD", "4C", "9S", "2H"),
                hand("Seven", "AC", "AD", "3H", "5S", "8C", "10D", "JH"),
                hand("Five again", "2C", "3D", "4H", "6S", "8D"));

        assertEquals(List.of("Seven"), result.winners());
    }

    // --- Invalid input ---

    @Test
    void rejectsDuplicateCardsWithinAHand() {
        assertInvalid(
                hand("A", "AS", "AS", "3H", "4D", "5C"),
                hand("B", "2S", "3S", "4S", "5S", "6S"));
    }

    @Test
    void requiresAtLeastTwoHands() {
        assertInvalid(hand("A", "2C", "3D", "4H", "5S", "6C"));
    }

    @Test
    void rejectsMoreThanFourHands() {
        assertInvalid(
                hand("A", "2C", "3D", "4H", "5S", "6C"),
                hand("B", "2C", "3D", "4H", "5S", "6C"),
                hand("C", "2C", "3D", "4H", "5S", "6C"),
                hand("D", "2C", "3D", "4H", "5S", "6C"),
                hand("E", "2C", "3D", "4H", "5S", "6C"));
    }

    @Test
    void rejectsUnsupportedHandSizes() {
        assertInvalid(
                hand("A", "2C", "3D", "4H", "5S", "6C", "7C"),
                hand("B", "2S", "3S", "4S", "5S", "6S", "7S"));
    }

    @Test
    void rejectsNineCardHands() {
        assertInvalid(
                hand("A", "2C", "3D", "4H", "5S", "6C", "7C", "8C", "9C", "10C"),
                hand("B", "2S", "3S", "4S", "5S", "6S"));
    }

    @Test
    void rejectsBlankPlayerNames() {
        assertInvalid(
                hand(" ", "2C", "3D", "4H", "5S", "6C"),
                hand("B", "2S", "3S", "4S", "5S", "6S"));
    }

    @Test
    void rejectsDuplicatePlayerNames() {
        assertInvalid(
                hand("A", "2C", "3D", "4H", "5S", "6C"),
                hand("A", "2S", "3S", "4S", "5S", "6S"));
    }

    @Test
    void rejectsMissingHands() {
        assertThrows(IllegalArgumentException.class, () -> service.compare(new PokerRequest(null)));
    }

    @Test
    void rejectsMalformedCards() {
        for (String bad : List.of("1C", "11H", "ZS", "AX", "A", "", "+5H", "05H", "010H")) {
            assertInvalid(
                    hand("A", bad, "3D", "4H", "5S", "6C"),
                    hand("B", "2S", "3S", "4S", "5S", "6S"));
        }
    }

    private PokerResponse compare(HandInput... hands) {
        return service.compare(new PokerRequest(List.of(hands)));
    }

    private void assertInvalid(HandInput... hands) {
        assertThrows(IllegalArgumentException.class, () -> compare(hands));
    }

    private HandInput hand(String player, String... cards) {
        return new HandInput(player, List.of(cards));
    }
}
