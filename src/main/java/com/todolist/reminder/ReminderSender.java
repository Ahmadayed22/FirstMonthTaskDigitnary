package com.todolist.reminder;

import com.todolist.entity.Reminder;
import com.todolist.entity.User;

public interface ReminderSender {
    void send(Reminder reminder, User recipient);
}
