package com.todolist.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TaskEventPublisher {

    //subscribers
    private final List<TaskEventListener> listeners = new CopyOnWriteArrayList<>();

    public void subscribe(TaskEventListener listener) {
        listeners.add(listener);
    }

    public void unsubscribe(TaskEventListener listener) {
        listeners.remove(listener);
    }

    public void publish(TaskEvent event) {
        for (TaskEventListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}
