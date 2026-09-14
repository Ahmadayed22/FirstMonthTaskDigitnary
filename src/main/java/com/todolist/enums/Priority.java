package com.todolist.enums;

public enum Priority {
    LOW(3),
    MEDIUM(2),
    HIGH(1);

    private final int urgencyRank;

    Priority(int urgencyRank) {
        this.urgencyRank = urgencyRank;
    }

    /** Lower rank = more urgent. Used as a tiebreaker by the hand-rolled sort. */
    public int urgencyRank() {
        return urgencyRank;
    }
}