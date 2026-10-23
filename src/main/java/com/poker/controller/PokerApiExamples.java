package com.poker.controller;

/**
 * Request and response examples shown in Swagger UI for {@code POST /api/poker/compare}. Kept here so the
 * controller stays readable. The valid examples follow the worked examples in the assignment; each response is
 * what the service actually returns for the matching request.
 */
final class PokerApiExamples {
    private PokerApiExamples() {
    }

    // --- Valid requests ---

    static final String STRAIGHT_BEATS_TRIPS = """
            {
              "hands": [
                {"player": "A", "cards": ["5H", "6D", "7S", "8C", "9H"]},
                {"player": "B", "cards": ["KC", "KD", "KH", "2S", "3C"]}
              ]
            }
            """;

    static final String HIGHER_PAIR_WINS = """
            {
              "hands": [
                {"player": "A", "cards": ["QH", "QD", "4C", "9S", "2H"]},
                {"player": "B", "cards": ["JC", "JD", "AS", "KH", "3C"]}
              ]
            }
            """;

    static final String KICKER_DECIDES = """
            {
              "hands": [
                {"player": "A", "cards": ["8C", "8D", "KC", "4H", "2S"]},
                {"player": "B", "cards": ["8H", "8S", "QC", "4C", "3S"]}
              ]
            }
            """;

    static final String TIE = """
            {
              "hands": [
                {"player": "A", "cards": ["2C", "3D", "4H", "9S", "KC"]},
                {"player": "B", "cards": ["2S", "3H", "4D", "9C", "KH"]}
              ]
            }
            """;

    static final String SEVEN_CARD_HAND = """
            {
              "hands": [
                {"player": "A", "cards": ["7C", "7D", "7H", "2S", "9C", "KH", "3D"]},
                {"player": "B", "cards": ["QC", "QD", "AS", "10H", "5C"]}
              ]
            }
            """;

    static final String ACE_LOW_STRAIGHT = """
            {
              "hands": [
                {"player": "A", "cards": ["AC", "2D", "3H", "4S", "5C"]},
                {"player": "B", "cards": ["2C", "3D", "4H", "5S", "6C"]}
              ]
            }
            """;

    static final String FOUR_HANDS_MIXED_SIZES = """
            {
              "hands": [
                {"player": "A", "cards": ["5C", "6D", "7H", "8S", "9C"]},
                {"player": "B", "cards": ["KC", "KD", "KH", "2S", "3C"]},
                {"player": "C", "cards": ["5D", "6H", "7S", "8C", "9D", "2C", "2D"]},
                {"player": "D", "cards": ["AC", "AD", "3H", "4S", "JC"]}
              ]
            }
            """;

    // --- Invalid requests ---

    static final String INVALID_CARD = """
            {
              "hands": [
                {"player": "A", "cards": ["1X", "6D", "7S", "8C", "9H"]},
                {"player": "B", "cards": ["KC", "KD", "KH", "2S", "3C"]}
              ]
            }
            """;

    static final String DUPLICATE_CARD = """
            {
              "hands": [
                {"player": "A", "cards": ["AS", "AS", "3H", "4D", "5C"]},
                {"player": "B", "cards": ["2S", "3S", "4S", "5S", "6S"]}
              ]
            }
            """;

    static final String WRONG_HAND_SIZE = """
            {
              "hands": [
                {"player": "A", "cards": ["2C", "3D", "4H", "5S", "6C", "7C"]},
                {"player": "B", "cards": ["2S", "3S", "4S", "5S", "6S"]}
              ]
            }
            """;

    static final String TOO_FEW_HANDS = """
            {
              "hands": [
                {"player": "A", "cards": ["2C", "3D", "4H", "9S", "KC"]}
              ]
            }
            """;

    // --- Successful responses ---

    static final String ONE_WINNER_RESPONSE = """
            {
              "winners": ["A"],
              "results": [
                {"player": "A", "category": "Straight", "bestFive": ["9H", "8C", "7S", "6D", "5H"]},
                {"player": "B", "category": "Three of a Kind", "bestFive": ["KC", "KD", "KH", "3C", "2S"]}
              ]
            }
            """;

    static final String KICKER_RESPONSE = """
            {
              "winners": ["A"],
              "results": [
                {"player": "A", "category": "Two of a Kind", "bestFive": ["KC", "8C", "8D", "4H", "2S"]},
                {"player": "B", "category": "Two of a Kind", "bestFive": ["QC", "8H", "8S", "4C", "3S"]}
              ]
            }
            """;

    static final String TIE_RESPONSE = """
            {
              "winners": ["A", "B"],
              "results": [
                {"player": "A", "category": "High Card", "bestFive": ["KC", "9S", "4H", "3D", "2C"]},
                {"player": "B", "category": "High Card", "bestFive": ["KH", "9C", "4D", "3H", "2S"]}
              ]
            }
            """;

    static final String SEVEN_CARD_RESPONSE = """
            {
              "winners": ["A"],
              "results": [
                {"player": "A", "category": "Three of a Kind", "bestFive": ["KH", "9C", "7C", "7D", "7H"]},
                {"player": "B", "category": "Two of a Kind", "bestFive": ["AS", "QC", "QD", "10H", "5C"]}
              ]
            }
            """;

    static final String FOUR_HANDS_RESPONSE = """
            {
              "winners": ["A", "C"],
              "results": [
                {"player": "A", "category": "Straight", "bestFive": ["9C", "8S", "7H", "6D", "5C"]},
                {"player": "B", "category": "Three of a Kind", "bestFive": ["KC", "KD", "KH", "3C", "2S"]},
                {"player": "C", "category": "Straight", "bestFive": ["9D", "8C", "7S", "6H", "5D"]},
                {"player": "D", "category": "Two of a Kind", "bestFive": ["AC", "AD", "JC", "4S", "3H"]}
              ]
            }
            """;

    // --- Error responses ---

    static final String INVALID_CARD_ERROR = """
            {"error": "Invalid suit 'X'; use C, D, H, or S"}
            """;

    static final String DUPLICATE_CARD_ERROR = """
            {"error": "Hand at index 0 contains duplicate cards"}
            """;

    static final String WRONG_HAND_SIZE_ERROR = """
            {"error": "Hand at index 0 must contain 5 or 7 cards"}
            """;

    static final String TOO_FEW_HANDS_ERROR = """
            {"error": "Provide between 2 and 4 hands"}
            """;

    static final String INVALID_JSON_ERROR = """
            {"error": "Request body must be valid JSON matching the poker request format"}
            """;
}
