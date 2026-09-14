package com.todolist.reminder;

import com.todolist.entity.Reminder;
import com.todolist.entity.User;

public class EmailReminderSender implements ReminderSender {
    @Override
    public void send(Reminder reminder, User recipient) {
        System.out.printf("[email] To: %s -- reminder for task %d (trigger was %s)%n",
                recipient.getEmail(), reminder.getTaskId(), reminder.getTriggerTime());
        reminder.markSent();
    }
}
