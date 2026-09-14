package com.todolist.reminder;

import com.todolist.entity.Reminder;
import com.todolist.entity.User;

public class PushReminderSender implements ReminderSender {
    @Override
    public void send(Reminder reminder, User recipient) {
        System.out.printf("[push] To: %s -- reminder for task %d (trigger was %s)%n",
                recipient.getName(), reminder.getTaskId(), reminder.getTriggerTime());
        reminder.markSent();
    }
}
