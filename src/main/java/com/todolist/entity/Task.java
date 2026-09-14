package com.todolist.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import com.todolist.enums.Priority;
import com.todolist.enums.TaskStatus;
import com.todolist.exception.InvalidTaskStateException;

public class Task {
    private Long id;
    private final Long ownerId;
    private String title;
    private LocalDateTime dueDate;
    private Priority priority;
    private TaskStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime completedAt;


    public Task(Long id, Long ownerId, String title, LocalDateTime dueDate, Priority priority) {
        this.id = id;
        this.ownerId = Objects.requireNonNull(ownerId, "ownerId must not be null");
        this.title = requireNonBlank(title);
        this.dueDate = Objects.requireNonNull(dueDate, "dueDate must not be null");
        this.priority = priority == null ? Priority.MEDIUM : priority;
        this.status = TaskStatus.TODO;
        this.createdAt = LocalDateTime.now();
    }
    
    public Task(Long ownerId, String title, LocalDateTime dueDate, Priority priority) {
        this(null, ownerId, title, dueDate, priority);
    }
    
    private static String requireNonBlank(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        return title;
    }

    public void start() {
        if (status == TaskStatus.DONE) {
            throw new InvalidTaskStateException("Cannot start task " + id + " -- it is already DONE");
        }
        status = TaskStatus.IN_PROGRESS;
    }
    
    public void complete() {
        if (status == TaskStatus.DONE) {
            throw new InvalidTaskStateException("Task " + id + " is already DONE");
        }
        status = TaskStatus.DONE;
        completedAt = LocalDateTime.now();
    }
    
    public void markOverdue() {
        if (status == TaskStatus.DONE) {
            throw new InvalidTaskStateException("Cannot mark completed task " + id + " as OVERDUE");
        }
        status = TaskStatus.OVERDUE;
    }
    
     public void reschedule(LocalDateTime newDueDate) {
        if (status == TaskStatus.DONE) {
            throw new InvalidTaskStateException("Cannot reschedule completed task " + id);
        }
        Objects.requireNonNull(newDueDate, "newDueDate must not be null");
        this.dueDate = newDueDate;
        if (status == TaskStatus.OVERDUE && newDueDate.isAfter(LocalDateTime.now())) {
            status = TaskStatus.TODO;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = requireNonBlank(title);
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    /** Only used when rehydrating a Task from storage -- normal flow sets this via complete(). */
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    @Override
    public String toString() {
        return "Task{id=%d, title='%s', due=%s, priority=%s, status=%s}"
                .formatted(id, title, dueDate, priority, status);
    }
}
