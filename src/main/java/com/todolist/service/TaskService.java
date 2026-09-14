package com.todolist.service;

import com.todolist.entity.Task;
import com.todolist.enums.Priority;
import com.todolist.enums.TaskStatus;
import com.todolist.events.TaskCompletedEvent;
import com.todolist.events.TaskCreatedEvent;
import com.todolist.events.TaskEventPublisher;
import com.todolist.events.TaskOverdueEvent;
import com.todolist.exception.TaskNotFoundException;
import com.todolist.repo.TaskRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskEventPublisher publisher;
    private ReminderService reminderService; // wired after construction, see setReminderService

    public TaskService(TaskRepository taskRepository, TaskEventPublisher publisher) {
        this.taskRepository = taskRepository;
        this.publisher = publisher;
    }

  
    public void setReminderService(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    public Task createTask(Long ownerId, String title, LocalDateTime dueDate, Priority priority) {
        Task task = new Task(ownerId, title, dueDate, priority);
        Task saved = taskRepository.save(task);
        publisher.publish(new TaskCreatedEvent(saved.getId(), ownerId));
        return saved;
    }

    public Optional<Task> findById(Long id) {
        return taskRepository.findById(id);
    }

    public Task requireById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("No task with id " + id));
    }

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    public List<Task> findByOwner(Long ownerId) {
        return taskRepository.findByOwner(ownerId);
    }

    public Task complete(Long taskId) {
        Task task = requireById(taskId);
        task.complete();
        taskRepository.save(task);
        publisher.publish(new TaskCompletedEvent(taskId));
        return task;
    }

    public Task reschedule(Long taskId, LocalDateTime newDueDate) {
        Task task = requireById(taskId);
        LocalDateTime oldDueDate = task.getDueDate();
        Duration shift = Duration.between(oldDueDate, newDueDate);

        task.reschedule(newDueDate);
        taskRepository.save(task);

        if (reminderService != null) {
            reminderService.shiftRemindersForTask(taskId, shift);
        }
        return task;
    }

    // Scans for tasks past due and not yet DONE, marks them OVERDUE, and publishes events.

    public void sweepOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        for (Task task : taskRepository.findAll()) {
            boolean pastDue = task.getDueDate().isBefore(now);
            boolean stillOpen = task.getStatus() != TaskStatus.DONE && task.getStatus() != TaskStatus.OVERDUE;
            if (pastDue && stillOpen) {
                task.markOverdue();
                taskRepository.save(task);
                publisher.publish(new TaskOverdueEvent(task.getId()));
            }
        }
    }
}
