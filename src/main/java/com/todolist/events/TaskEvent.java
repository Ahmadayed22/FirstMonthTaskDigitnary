package com.todolist.events;

import java.time.LocalDateTime;
// event object 
// base type for every event that happens to a task
public abstract class TaskEvent {
    private final Long taskId;
    private final LocalDateTime occurredAt;

    protected TaskEvent(Long taskId) {
        this.taskId = taskId;
        this.occurredAt = LocalDateTime.now();
    }

    public Long getTaskId() {
        return taskId;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
