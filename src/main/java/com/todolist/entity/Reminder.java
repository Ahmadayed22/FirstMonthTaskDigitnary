package com.todolist.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import com.todolist.enums.ReminderChannel;

public class Reminder {
    private Long id;
    private final Long taskId;
    private LocalDateTime triggerTime;
    private final ReminderChannel channel;
    private boolean sent;

    
    public Reminder(Long id, Long taskId, LocalDateTime triggerTime, ReminderChannel channel) {
        this.id = id;
        this.taskId = Objects.requireNonNull(taskId, "taskId must not be null");
        this.triggerTime = Objects.requireNonNull(triggerTime, "triggerTime must not be null");
        this.channel = channel == null ? ReminderChannel.EMAIL : channel;
        this.sent = false;
    }

    public Reminder(Long taskId, LocalDateTime triggerTime, ReminderChannel channel) {
        this(null, taskId, triggerTime, channel);
    }

    public void reschedule(LocalDateTime newTriggerTime) {
        this.triggerTime = Objects.requireNonNull(newTriggerTime, "newTriggerTime must not be null");
        this.sent = false;
    }

     public void markSent() {
        this.sent = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public LocalDateTime getTriggerTime() {
        return triggerTime;
    }

    public ReminderChannel getChannel() {
        return channel;
    }

    public boolean isSent() {
        return sent;
    }

    @Override
    public String toString() {
        return "Reminder{id=%d, taskId=%d, trigger=%s, channel=%s, sent=%s}"
                .formatted(id, taskId, triggerTime, channel, sent);
    }
    

}
