package com.todolist.events;

import com.todolist.service.ReminderService;
import com.todolist.service.TaskService;

public class ReminderSchedulingListener implements TaskEventListener {

    private final TaskService taskService;
    private final ReminderService reminderService;

    public ReminderSchedulingListener(TaskService taskService, ReminderService reminderService) {
        this.taskService = taskService;
        this.reminderService = reminderService;
    }

    @Override
    public void onEvent(TaskEvent event) {
        if (event instanceof TaskCreatedEvent created) {
            taskService.findById(created.getTaskId())
                    .ifPresent(reminderService::scheduleDefaultReminder);
        }
    }
}
