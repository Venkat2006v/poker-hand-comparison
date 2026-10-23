package com.poker.model;

public enum HandCategory {
    HIGH_CARD("High Card", 0),
    PAIR("Two of a Kind", 1),
    THREE_OF_A_KIND("Three of a Kind", 2),
    STRAIGHT("Straight", 3);

    private final String label;
    private final int strength;

    HandCategory(String label, int strength) {
        this.label = label;
        this.strength = strength;
    }

    public String label() {
        return label;
    }

    public int strength() {
        return strength;
    }
}
