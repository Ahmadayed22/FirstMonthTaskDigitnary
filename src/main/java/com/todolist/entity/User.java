package com.todolist.entity;

import java.util.Objects;
import java.util.regex.Pattern;

import com.todolist.enums.ReminderChannel;
import com.todolist.exception.InvalidEmailException;
public class User {
    
    private Long id;
    private final String name;
    private final String email;
    private ReminderChannel preferredChannel;
     private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
       public User(Long id, String name, String email, ReminderChannel preferredChannel) {
           this.id = id;
           this.name = Objects.requireNonNull(name, "name must not be null");
           this.email = validateEmail(email);
           this.preferredChannel = preferredChannel == null ? ReminderChannel.EMAIL : preferredChannel;
       }
    
       public User(String name, String email, ReminderChannel preferredChannel) {
           this(null, name, email, preferredChannel);
       }

       
       private static String validateEmail(String candidate) {
           if (candidate == null || !EMAIL_PATTERN.matcher(candidate).matches()) {
               throw new InvalidEmailException("Not a valid email address: " + candidate);
           }
           return candidate;
       }
    
    
        public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public ReminderChannel getPreferredChannel() {
        return preferredChannel;
    }

    public void setPreferredChannel(ReminderChannel preferredChannel) {
        this.preferredChannel = preferredChannel;
    }

    @Override
    public String toString() {
        return "User{id=%d, name='%s', email='%s', channel=%s}"
                .formatted(id, name, email, preferredChannel);
    }

}
