package com.todolist.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import com.todolist.entity.Task;
import com.todolist.enums.Priority;
import com.todolist.enums.TaskStatus;
import com.todolist.repo.TaskRepository;

public class StatsService {

    private final TaskRepository taskRepository;

    public StatsService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /** 1) Tasks completed per user, restricted to the last 7 days. */
    public Map<Long, Long> completedPerUserThisWeek() {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        return taskRepository.findAll().stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE)
                .filter(t -> t.getCompletedAt() != null && t.getCompletedAt().isAfter(weekAgo))
                .collect(Collectors.groupingBy(Task::getOwnerId, Collectors.counting()));
    }

    /** 2) Count of currently-overdue tasks, grouped by priority. */
    public Map<Priority, Long> overdueCountByPriority() {
        return taskRepository.findAll().stream()
                .filter(t -> t.getStatus() == TaskStatus.OVERDUE)
                .collect(Collectors.groupingBy(Task::getPriority, Collectors.counting()));
    }

    /** 3) Average time-to-completion, in hours, across all completed tasks. */
    public double averageTimeToCompletionHours() {
        return taskRepository.findAll().stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE && t.getCompletedAt() != null)
                .mapToLong(t -> Duration.between(t.getCreatedAt(), t.getCompletedAt()).toMinutes())
                .average()
                .orElse(0.0) / 60.0;
    }

    /** 4)  total task count per priority, across all statuses. */
    public Map<Priority, Long> countByPriority() {
        return taskRepository.findAll().stream()
                .collect(Collectors.groupingBy(Task::getPriority, Collectors.counting()));
    }
}
