package com.todolist.events;

public class TaskCreatedEvent extends TaskEvent {
    private final Long ownerId;

    public TaskCreatedEvent(Long taskId, Long ownerId) {
        super(taskId);
        this.ownerId = ownerId;
    }

    public Long getOwnerId() {
        return ownerId;
    }
}
