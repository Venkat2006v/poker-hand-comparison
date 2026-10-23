package com.poker.model;

import java.util.Locale;
import java.util.Objects;

public record Card(int rank, Suit suit) {
    public Card {
        if (rank < 2 || rank > 14) {
            throw new IllegalArgumentException("Card rank must be between 2 and 14");
        }
        Objects.requireNonNull(suit, "suit");
    }

    public static Card parse(String notation) {
        if (notation == null) {
            throw new IllegalArgumentException("Card cannot be null");
        }
        String value = notation.trim().toUpperCase(Locale.ROOT);
        if (value.length() < 2) {
            throw new IllegalArgumentException("Invalid card '" + notation + "'; use rank followed by suit, for example AS or 10H");
        }

        Suit suit = Suit.fromCode(value.charAt(value.length() - 1));
        String rankText = value.substring(0, value.length() - 1);
        int rank = switch (rankText) {
            case "A" -> 14;
            case "K" -> 13;
            case "Q" -> 12;
            case "J" -> 11;
            default -> parseNumericRank(rankText, notation);
        };
        return new Card(rank, suit);
    }

    private static int parseNumericRank(String rankText, String notation) {
        // Match the exact spellings 2-9 and 10, so forms like "+5" or "05" that Integer.parseInt accepts are rejected.
        if (rankText.matches("[2-9]|10")) {
            return Integer.parseInt(rankText);
        }
        throw new IllegalArgumentException("Invalid card rank in '" + notation + "'");
    }

    public String notation() {
        String rankText = switch (rank) {
            case 14 -> "A";
            case 13 -> "K";
            case 12 -> "Q";
            case 11 -> "J";
            default -> Integer.toString(rank);
        };
        return rankText + suit.code;
    }

    public enum Suit {
        CLUBS('C'), DIAMONDS('D'), HEARTS('H'), SPADES('S');

        private final char code;

        Suit(char code) {
            this.code = code;
        }

        public char code() {
            return code;
        }

        private static Suit fromCode(char code) {
            for (Suit suit : values()) {
                if (suit.code == code) {
                    return suit;
                }
            }
            throw new IllegalArgumentException("Invalid suit '" + code + "'; use C, D, H, or S");
        }
    }
}
