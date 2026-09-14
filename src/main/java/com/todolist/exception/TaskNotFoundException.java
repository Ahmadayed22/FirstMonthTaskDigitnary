package com.todolist.exception;

public class TaskNotFoundException extends TaskFlowException {
      public TaskNotFoundException(String message) {
        super(message);
    }
}
