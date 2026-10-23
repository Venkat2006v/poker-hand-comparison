package com.poker.model;

import java.util.Comparator;
import java.util.List;

/**
 * The scored form of a hand: its category, the ranks that break ties within that category (most significant
 * first), and the five cards that make the hand.
 */
public record HandEvaluation(HandCategory category, List<Integer> tieBreakRanks, List<Card> bestFive) {
    /**
     * Orders hands by strength: category first, then tiebreak ranks from left to right. Suits are ignored, so two
     * hands can compare as equal (a tie) while holding different cards. That is why this is a separate comparator
     * rather than {@code Comparable}, which would be inconsistent with the record's {@code equals}.
     */
    public static final Comparator<HandEvaluation> BY_STRENGTH = HandEvaluation::compareStrength;

    public HandEvaluation {
        tieBreakRanks = List.copyOf(tieBreakRanks);
        bestFive = List.copyOf(bestFive);
    }

    private static int compareStrength(HandEvaluation left, HandEvaluation right) {
        int byCategory = Integer.compare(left.category.strength(), right.category.strength());
        if (byCategory != 0) {
            return byCategory;
        }
        // Hands in the same category always carry the same number of tiebreak ranks.
        for (int i = 0; i < left.tieBreakRanks.size(); i++) {
            int byRank = Integer.compare(left.tieBreakRanks.get(i), right.tieBreakRanks.get(i));
            if (byRank != 0) {
                return byRank;
            }
        }
        return 0;
    }
}
