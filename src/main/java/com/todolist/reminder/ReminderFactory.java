package com.todolist.reminder;

import com.todolist.enums.ReminderChannel;

public class ReminderFactory {

    public ReminderSender createSender(ReminderChannel channel) {
        return switch (channel) {
            case EMAIL -> new EmailReminderSender();
            case SMS -> new SmsReminderSender();
            case PUSH -> new PushReminderSender();
        };
    }
}
