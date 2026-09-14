package com.todolist.enums;

public enum Priority {
    LOW(3),
    MEDIUM(2),
    HIGH(1);

    private final int urgencyRank;

    Priority(int urgencyRank) {
        this.urgencyRank = urgencyRank;
    }

    public int urgencyRank() {
        return urgencyRank;
    }
}