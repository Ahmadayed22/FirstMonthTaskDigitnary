package com.todolist.repo;

import com.todolist.entity.Task;
import java.time.LocalDate;
//specifically remembers Which tasks are due today?
public class DueTodayCache {

    private final Cache<Long, Task> cache = new Cache<>(200);

    public void refresh(Iterable<Task> allTasks) {
        cache.clear();
        LocalDate today = LocalDate.now();
        for (Task task : allTasks) {
            if (task.getDueDate().toLocalDate().isEqual(today)) {
                cache.put(task.getId(), task);
            }
        }
    }

    public boolean isDueToday(Long taskId) {
        return cache.get(taskId).isPresent();
    }

    public int count() {
        return cache.size();
    }
}
