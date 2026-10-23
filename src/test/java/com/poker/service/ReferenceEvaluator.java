package com.poker.service;

import com.poker.model.Card;
import com.poker.model.HandCategory;
import com.poker.model.HandEvaluation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Reference evaluator used only by tests to check {@link HandEvaluator}; it is not used by the application.
 *
 * <p>It scores every five-card combination of a hand (C(n, 5) of them, 21 for seven cards) and keeps the best.
 * That is slow but easy to see is correct, which makes it a trustworthy answer key. {@link HandEvaluator} instead
 * picks the best five greedily in O(n), and {@code HandEvaluatorTest.matchesReferenceEvaluatorOnRandomHands}
 * compares the two on thousands of random hands to catch cases the hand-written tests do not cover.
 */
final class ReferenceEvaluator {
    private ReferenceEvaluator() {
    }

    static HandEvaluation evaluate(List<Card> cards) {
        List<List<Card>> combinations = new ArrayList<>();
        choose(cards, 0, new ArrayList<>(), combinations);
        HandEvaluation best = null;
        for (List<Card> combination : combinations) {
            HandEvaluation candidate = evaluateFive(combination);
            if (best == null || HandEvaluation.BY_STRENGTH.compare(candidate, best) > 0) {
                best = candidate;
            }
        }
        return best;
    }

    private static void choose(List<Card> cards, int index, List<Card> selected, List<List<Card>> combinations) {
        if (selected.size() == 5) {
            combinations.add(List.copyOf(selected));
            return;
        }
        for (int i = index; i <= cards.size() - (5 - selected.size()); i++) {
            selected.add(cards.get(i));
            choose(cards, i + 1, selected, combinations);
            selected.removeLast();
        }
    }

    private static HandEvaluation evaluateFive(List<Card> cards) {
        Map<Integer, Integer> counts = new TreeMap<>(Comparator.reverseOrder());
        cards.forEach(card -> counts.merge(card.rank(), 1, Integer::sum));
        List<Integer> ranksDescending = cards.stream().map(Card::rank).sorted(Comparator.reverseOrder()).toList();

        int straightHigh = straightHigh(ranksDescending);
        if (straightHigh > 0) {
            return new HandEvaluation(HandCategory.STRAIGHT, List.of(straightHigh), cards);
        }
        for (int need : List.of(3, 2)) {
            for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
                if (entry.getValue() >= need) {
                    List<Integer> ranks = new ArrayList<>(ranksDescending);
                    for (int i = 0; i < need; i++) {
                        ranks.remove(entry.getKey());
                    }
                    ranks.addFirst(entry.getKey());
                    HandCategory category = need == 3 ? HandCategory.THREE_OF_A_KIND : HandCategory.PAIR;
                    return new HandEvaluation(category, ranks, cards);
                }
            }
        }
        return new HandEvaluation(HandCategory.HIGH_CARD, ranksDescending, cards);
    }

    private static int straightHigh(List<Integer> ranksDescending) {
        if (ranksDescending.stream().distinct().count() != 5) {
            return 0;
        }
        if (ranksDescending.equals(List.of(14, 5, 4, 3, 2))) {
            return 5;
        }
        return ranksDescending.getFirst() - ranksDescending.getLast() == 4 ? ranksDescending.getFirst() : 0;
    }
}
