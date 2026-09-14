package com.todolist.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.todolist.entity.Reminder;
import com.todolist.entity.Task;
import com.todolist.entity.User;
import com.todolist.enums.ReminderChannel;
import com.todolist.reminder.ReminderFactory;
import com.todolist.reminder.ReminderSender;
import com.todolist.repo.ReminderRepository;
import com.todolist.repo.TaskRepository;
import com.todolist.repo.UserRepository;

public class ReminderService {

    private static final Duration DEFAULT_LEAD_TIME = Duration.ofHours(24);

    private final ReminderRepository reminderRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ReminderFactory reminderFactory;

    public ReminderService(ReminderRepository reminderRepository,
                            TaskRepository taskRepository,
                            UserRepository userRepository,
                            ReminderFactory reminderFactory) {
        this.reminderRepository = reminderRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.reminderFactory = reminderFactory;
    }

    /** Schedules a reminder 24h before due, on the owner's preferred channel. */
    public Reminder scheduleDefaultReminder(Task task) {
        LocalDateTime triggerTime = task.getDueDate().minus(DEFAULT_LEAD_TIME);
        ReminderChannel channel = userRepository.findById(task.getOwnerId())
                .map(User::getPreferredChannel)
                .orElse(ReminderChannel.EMAIL);
        Reminder reminder = new Reminder(task.getId(), triggerTime, channel);
        return reminderRepository.save(reminder);
    }

    /** Judgment-call support: shift every reminder tied to a task by the same duration its due date moved. */
    public void shiftRemindersForTask(Long taskId, Duration shift) {
        for (Reminder reminder : reminderRepository.findByTaskId(taskId)) {
            reminder.reschedule(reminder.getTriggerTime().plus(shift));
            reminderRepository.save(reminder);
        }
    }

    public List<Reminder> findDueForDispatch() {
        return reminderRepository.findDueForDispatch();
    }

    /** Sequential baseline dispatch. */
    public void dispatchDue() {
        for (Reminder reminder : findDueForDispatch()) {
            dispatchOne(reminder);
        }
    }


    public void dispatchDueConcurrently(int threadPoolSize) {
        List<Reminder> due = findDueForDispatch();
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        try {
            for (Reminder reminder : due) {
                executor.submit(() -> dispatchOne(reminder));
            }
        } finally {
            executor.shutdown();
            try {
                executor.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void dispatchOne(Reminder reminder) {
        Task task = taskRepository.findById(reminder.getTaskId())
                .orElseThrow(() -> new IllegalStateException("Reminder points at a missing task"));
        User recipient = userRepository.findById(task.getOwnerId())
                .orElseThrow(() -> new IllegalStateException("Task has no valid owner"));
        ReminderSender sender = reminderFactory.createSender(reminder.getChannel());
        sender.send(reminder, recipient);
        reminderRepository.save(reminder);
    }
}
