package com.todolist.exception;

public class UserNotFoundException extends TaskFlowException {
       public UserNotFoundException(String message) {
        super(message);
    }
}
