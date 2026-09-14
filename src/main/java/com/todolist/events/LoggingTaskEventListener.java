package com.todolist.events;

public class LoggingTaskEventListener implements TaskEventListener {
    @Override
    public void onEvent(TaskEvent event) {
        System.out.printf("[log] %s on task %d at %s%n",
                event.getClass().getSimpleName(), event.getTaskId(), event.getOccurredAt());
    }
}
