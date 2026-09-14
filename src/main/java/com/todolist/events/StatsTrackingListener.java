package com.todolist.events;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class StatsTrackingListener implements TaskEventListener {

    private final AtomicLong createdCount = new AtomicLong();
    private final AtomicLong completedCount = new AtomicLong();
    private final Map<Long, AtomicLong> overdueCountByTask = new ConcurrentHashMap<>();

    @Override
    public void onEvent(TaskEvent event) {
        if (event instanceof TaskCreatedEvent) {
            createdCount.incrementAndGet();
        } else if (event instanceof TaskCompletedEvent) {
            completedCount.incrementAndGet();
        } else if (event instanceof TaskOverdueEvent overdue) {
            overdueCountByTask.computeIfAbsent(overdue.getTaskId(), id -> new AtomicLong()).incrementAndGet();
        }
    }

    public long getCreatedCount() {
        return createdCount.get();
    }

    public long getCompletedCount() {
        return completedCount.get();
    }
}
