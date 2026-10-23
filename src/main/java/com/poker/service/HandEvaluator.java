package com.poker.service;

import com.poker.model.Card;
import com.poker.model.HandCategory;
import com.poker.model.HandEvaluation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Finds the best five-card hand among 5 or more distinct cards in O(n) time.
 *
 * <p>Instead of trying every five-card combination, the cards are grouped by rank once and the best hand is
 * chosen greedily. Greedy is safe because hands compare by category first and then by tiebreak ranks from
 * left to right: taking the best category, then the highest leading rank, then the highest remaining kickers
 * always produces the largest possible evaluation.
 *
 * <p>When several cards of the same rank could fill a slot, the lowest suit (C, D, H, S) is taken, so the
 * returned best five is deterministic. Suits never affect the ranking itself.
 */
@Component
public class HandEvaluator {
    private static final int HAND_SIZE = 5;
    private static final int ACE = 14;
    private static final int LOW_ACE = 1;
    private static final int LOWEST_STRAIGHT_HIGH = 5;

    /** Highest rank first; suit only breaks ties between same-rank cards, for consistent display. */
    private static final Comparator<Card> DISPLAY_ORDER =
            Comparator.comparingInt(Card::rank).reversed().thenComparing(Card::suit);

    /** Expects at least 5 distinct cards; callers validate the input. */
    public HandEvaluation evaluate(List<Card> cards) {
        if (cards.size() < HAND_SIZE) {
            throw new IllegalArgumentException("A hand needs at least " + HAND_SIZE + " cards");
        }
        List<List<Card>> byRank = groupByRank(cards);

        int straightHigh = highestStraightTopRank(byRank);
        if (straightHigh > 0) {
            return buildStraight(straightHigh, byRank);
        }
        int trips = highestRankWithAtLeast(3, byRank);
        if (trips > 0) {
            return buildHandWithKickers(HandCategory.THREE_OF_A_KIND, trips, 3, byRank);
        }
        int pair = highestRankWithAtLeast(2, byRank);
        if (pair > 0) {
            return buildHandWithKickers(HandCategory.PAIR, pair, 2, byRank);
        }
        return buildHandWithKickers(HandCategory.HIGH_CARD, 0, 0, byRank);
    }

    /** Index = rank (2..14); each bucket is ordered by suit. At most 4 cards per bucket, so sorting is O(1). */
    private List<List<Card>> groupByRank(List<Card> cards) {
        List<List<Card>> byRank = new ArrayList<>(ACE + 1);
        for (int rank = 0; rank <= ACE; rank++) {
            byRank.add(new ArrayList<>(4));
        }
        for (Card card : cards) {
            byRank.get(card.rank()).add(card);
        }
        // Suits never affect ranking. Sorting each bucket by suit only makes the choice between equal cards
        // consistent, so the same cards always produce the same bestFive, whatever order the client sent them in.
        byRank.forEach(bucket -> bucket.sort(Comparator.comparing(Card::suit)));
        return byRank;
    }

    /** Returns the top rank of the highest straight, or 0 if there is none. An Ace also counts as rank 1. */
    private int highestStraightTopRank(List<List<Card>> byRank) {
        // Highest first, so the first straight found is the best one.
        for (int high = ACE; high >= LOWEST_STRAIGHT_HIGH; high--) {
            boolean isStraight = true;
            for (int rank = high; rank > high - HAND_SIZE; rank--) {
                if (byRank.get(bucketRank(rank)).isEmpty()) {
                    isStraight = false;
                    break;
                }
            }
            if (isStraight) {
                return high;
            }
        }
        return 0;
    }

    private HandEvaluation buildStraight(int high, List<List<Card>> byRank) {
        List<Card> bestFive = new ArrayList<>(HAND_SIZE);
        for (int rank = high; rank > high - HAND_SIZE; rank--) {
            bestFive.add(byRank.get(bucketRank(rank)).getFirst());
        }
        return new HandEvaluation(HandCategory.STRAIGHT, List.of(high), sortedForDisplay(bestFive));
    }

    private int highestRankWithAtLeast(int count, List<List<Card>> byRank) {
        for (int rank = ACE; rank >= 2; rank--) {
            if (byRank.get(rank).size() >= count) {
                return rank;
            }
        }
        return 0;
    }

    /**
     * Takes {@code leadCount} cards of {@code leadRank} (none for High Card), then fills the hand with the highest
     * remaining cards as kickers. Tiebreak ranks are the lead rank followed by the kicker ranks, highest first.
     * For four of a kind the fourth card is an ordinary kicker, which is how it falls back to Three of a Kind.
     */
    private HandEvaluation buildHandWithKickers(
            HandCategory category, int leadRank, int leadCount, List<List<Card>> byRank) {
        List<Card> bestFive = new ArrayList<>(HAND_SIZE);
        List<Integer> tieBreakRanks = new ArrayList<>();

        // Step 1: add the leading pair or three of a kind.
        if (leadCount > 0) {
            bestFive.addAll(byRank.get(leadRank).subList(0, leadCount));
            tieBreakRanks.add(leadRank);
        }

        // Step 2: fill the remaining slots with the highest cards.
        addKickers(bestFive, tieBreakRanks, leadRank, leadCount, byRank);

        return new HandEvaluation(category, tieBreakRanks, sortedForDisplay(bestFive));
    }

    /** Adds the highest remaining cards to {@code bestFive} (and their ranks to {@code tieBreakRanks}) until it holds five. */
    private void addKickers(
            List<Card> bestFive, List<Integer> tieBreakRanks, int leadRank, int leadCount, List<List<Card>> byRank) {
        for (int rank = ACE; rank >= 2 && bestFive.size() < HAND_SIZE; rank--) {
            List<Card> bucket = byRank.get(rank);
            // Skip cards already taken as the leading group.
            int startIndex = (rank == leadRank) ? leadCount : 0;
            for (int i = startIndex; i < bucket.size() && bestFive.size() < HAND_SIZE; i++) {
                bestFive.add(bucket.get(i));
                tieBreakRanks.add(rank);
            }
        }
    }

    private List<Card> sortedForDisplay(List<Card> cards) {
        return cards.stream().sorted(DISPLAY_ORDER).toList();
    }

    /** Rank 1 stands for the low Ace in A-2-3-4-5; its cards live in the Ace bucket. */
    private static int bucketRank(int rank) {
        return rank == LOW_ACE ? ACE : rank;
    }
}
