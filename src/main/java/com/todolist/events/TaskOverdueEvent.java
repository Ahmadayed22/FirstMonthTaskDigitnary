package com.todolist.events;

public class TaskOverdueEvent extends TaskEvent {
    public TaskOverdueEvent(Long taskId) {
        super(taskId);
    }
}
