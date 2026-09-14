package com.todolist.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.todolist.entity.Task;
import com.todolist.enums.TaskStatus;
import com.todolist.repo.TaskRepository;
import com.todolist.sortalgorithm.DueSoonSorter;

public class ReportService {

    private final TaskRepository taskRepository;
    private final DueSoonSorter sorter = new DueSoonSorter();

    public ReportService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    //Get all unfinished tasks that are due before a specific deadline, then sort them by due date and priority.
    public List<Task> dueSoonReport(LocalDateTime windowEnd) {
        List<Task> candidates = taskRepository.findAll().stream()
                .filter(t -> t.getStatus() != TaskStatus.DONE)
                .filter(t -> !t.getDueDate().isAfter(windowEnd))
                .collect(Collectors.toList());
        return sorter.sortByDueDateThenPriority(candidates);
    }
}
