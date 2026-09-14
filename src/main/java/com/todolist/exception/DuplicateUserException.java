package com.todolist.exception;

public class DuplicateUserException extends  TaskFlowException{
      public DuplicateUserException(String message) {
        super(message);
    }
}
