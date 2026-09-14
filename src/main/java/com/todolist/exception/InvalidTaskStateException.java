package com.todolist.exception;

public class InvalidTaskStateException extends TaskFlowException {
    public InvalidTaskStateException(String message) {
        super(message);
    }
}
