package com.todolist;

import java.time.LocalDateTime;

import com.todolist.config.Database;
import com.todolist.entity.Task;
import com.todolist.entity.User;
import com.todolist.enums.Priority;
import com.todolist.enums.ReminderChannel;
import com.todolist.events.LoggingTaskEventListener;
import com.todolist.events.ReminderSchedulingListener;
import com.todolist.events.StatsTrackingListener;
import com.todolist.events.TaskEventPublisher;
import com.todolist.reminder.ReminderFactory;
import com.todolist.repo.ReminderRepository;
import com.todolist.repo.TaskRepository;
import com.todolist.repo.UserRepository;
import com.todolist.service.ReminderService;
import com.todolist.service.ReportService;
import com.todolist.service.StatsService;
import com.todolist.service.TaskService;
import com.todolist.service.UserService;

public class App 
{
    public static void main( String[] args )
    {
        Database.initSchema();

        UserRepository userRepository = new UserRepository();
        TaskRepository taskRepository = new TaskRepository();
        ReminderRepository reminderRepository = new ReminderRepository();

        TaskEventPublisher publisher = new TaskEventPublisher();
        UserService userService = new UserService(userRepository);
        TaskService taskService = new TaskService(taskRepository, publisher);
        ReminderService reminderService = new ReminderService(
                reminderRepository, taskRepository, userRepository, new ReminderFactory());
        taskService.setReminderService(reminderService);

        StatsTrackingListener statsListener = new StatsTrackingListener();
        publisher.subscribe(new LoggingTaskEventListener());
        publisher.subscribe(new ReminderSchedulingListener(taskService, reminderService));
        publisher.subscribe(statsListener);

        ReportService reportService = new ReportService(taskRepository);
        StatsService statsService = new StatsService(taskRepository);

        // --- demo data ---
        User ahmad = userService.register("ahmad ayed", "ahmad@gmail.com");
        User ammar = userService.register("ammar Rahal", "ammar@gamil.com");
        ammar.setPreferredChannel(ReminderChannel.SMS);
        userRepository.save(ammar);

        Task t1 = taskService.createTask(ahmad.getId(), "Finish JDBC layer",
                LocalDateTime.now().plusDays(1), Priority.HIGH);
        Task t2 = taskService.createTask(ahmad.getId(), "Write README",
                LocalDateTime.now().plusDays(3), Priority.MEDIUM);
        Task t3 = taskService.createTask(ammar.getId(), "Review pull request",
                LocalDateTime.now().minusHours(2), Priority.HIGH);
        taskService.createTask(ammar.getId(), "Update dependencies",
                LocalDateTime.now().plusDays(1), Priority.LOW);

        taskService.complete(t2.getId());

        // Judgment call in action: push t1's due date back by two days.
        // Its reminder (originally 24h before the old due date) should move
        // with it, preserving the 24h lead time.
        taskService.reschedule(t1.getId(), t1.getDueDate().plusDays(2));

        // t3 was created already 2h in the past, so this sweep marks it OVERDUE.
        taskService.sweepOverdueTasks();

        System.out.println();
        System.out.println("=== Due-soon report (hand-rolled merge sort, next 7 days) ===");
        reportService.dueSoonReport(LocalDateTime.now().plusDays(7)).forEach(System.out::println);

        System.out.println();
        System.out.println("=== Stream aggregations ===");
        System.out.println("Completed per user (last 7 days): " + statsService.completedPerUserThisWeek());
        System.out.println("Overdue count by priority: " + statsService.overdueCountByPriority());
        System.out.printf("Average time-to-completion: %.2f hours%n", statsService.averageTimeToCompletionHours());
        System.out.println("Total tasks by priority: " + statsService.countByPriority());

        System.out.println();
   

        System.out.println();
        System.out.println("Events observed -- created: " + statsListener.getCreatedCount()
                + ", completed: " + statsListener.getCompletedCount());
    }
    
    }
    
    

