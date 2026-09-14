package com.todolist.events;

public class TaskCompletedEvent extends TaskEvent {
    public TaskCompletedEvent(Long taskId) {
        super(taskId);
    }
}
