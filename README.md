# todolist

todolist **task and reminder management system** built as a Java backend capstone project.

At first, it looks like a simple to-do list, but the main goal of the project was to practice backend development and software design concepts such as:

* Object-Oriented Programming (OOP)
* JDBC and SQL
* Generic programming
* Repository pattern
* Caching
* Merge Sort
* Java Streams
* Factory Method pattern
* Observer / Pub-Sub pattern
* Multithreading and concurrency

The application uses an embedded **H2 database** and provides a small CLI demo that shows the main features working together.

---

## Requirements

Before running the project, make sure you have:

* **JDK 21**
* **Maven **
* Internet access for the first Maven build so the H2 dependency can be downloaded.

---

## Running the Project

First, build the project using Maven:

```bash
mvn clean package
```

After building, the application can be started with:

```bash
java -cp "target\classes;$env:USERPROFILE\.m2\repository\com\h2database\h2\2.3.232\h2-2.3.232.jar" com.todolist.App
```

The application creates an embedded H2 database under:

```text
./data/
```

The database schema is automatically created from:

```text
src/main/resources/schema.sql
```

On the first run, the application creates the database and runs a short demo.

The demo includes:

1. Registering two users
2. Creating four tasks
3. Completing a task
4. Rescheduling a task
5. Checking for overdue tasks
6. Displaying tasks that are due soon
7. Calculating task statistics using Java Streams
8. Dispatching due reminders concurrently

---

# Project Structure

```text
com.todolist
│
├── entity
│   ├── User
│   ├── Task
│   ├── Reminder
│  
├── Enums
│   ├── Priority
│   ├── ReminderChannel
│   ├── TaskStatus
├── exception
│   └── todolistException hierarchy
│
├── repository
│   ├── Repository<T, ID>
│   ├── JDBC repositories
│   ├── Cache<K, V>
│   └── DueTodayCache
│
├── config
│   └── Database
│
├── algorithm
│   └── DueSoonSorter
│
├── events
│   ├── TaskEvent
│   ├── TaskCreated
│   ├── TaskCompleted
│   ├── TaskOverdue
│   ├── TaskEventPublisher
│   └── TaskEventListener
│
├── reminder
│   ├── ReminderSender
│   ├── EmailReminderSender
│   ├── SmsReminderSender
│   ├── PushReminderSender
│   └── ReminderFactory
│
├── service
│   ├── TaskService
│   ├── ReminderService
│   └── StatsService
│
└── APP
```

The project is separated into different layers so that each part has a clear responsibility.

### enity

Contains the main application objects such as `User`, `Task`, and `Reminder`.

The entity objects also contain behavior instead of being simple data containers. For example, a task can change its state through methods such as:

```java
task.start();
task.complete();
task.reschedule();
```

This allows the `Task` object to validate its own state transitions.

### Repository

Responsible for communication with the database.

The project uses a generic repository interface:

```java
Repository<T, ID>
```

This allows the same repository abstraction to be reused for different entities.

For example:

```java
Repository<Task, Long>
Repository<User, Long>
Repository<Reminder, Long>
```

### Service

Contains the main application and business logic.

Services coordinate repositories, events, reminders, and other parts of the application.

### Events

Contains the Observer / Pub-Sub implementation used to notify different parts of the system when something happens to a task.

### Reminder

Contains the reminder sending logic and the Factory implementation.

### Algorithm

Contains the custom sorting implementation used for the "due soon" task list.

---

# Why JDBC Instead of JPA?

I decided to use **JDBC instead of JPA/Hibernate** because this project was mainly about understanding what is happening at the database level.

The project only has a few tables, and one of the goals was to implement the repository and database interaction myself.

With JDBC, the SQL is visible and I can clearly see which query is executed.

For example:

```sql
SELECT * FROM tasks WHERE user_id = ?
```

Using JPA would hide a lot of this behind the ORM.

JPA provides useful features such as:

* Entity lifecycle management
* Dirty checking
* Lazy loading
* First-level caching

However, those features were not necessary for this project and would have hidden some of the database concepts I wanted to practice.

The generic:

```java
Repository<T, ID>
```

provides a reusable abstraction while still allowing the SQL and JDBC operations to remain visible.

---

# Design Patterns

The project uses two main design patterns:

1. Factory Method
2. Observer / Pub-Sub

I intentionally didn't add additional patterns just for the sake of having more patterns. The existing patterns solve real problems in the application.

---

## Factory Method

The Factory pattern is implemented through:

```java
ReminderFactory
```

The application supports different reminder channels:

```text
EMAIL
SMS
PUSH
```

Each channel has its own implementation:

```text
EmailReminderSender
SmsReminderSender
PushReminderSender
```

Instead of creating these classes directly throughout the application, the caller asks the factory for the correct sender.

Conceptually:

```text
ReminderFactory
       |
       +---- EMAIL → EmailReminderSender
       |
       +---- SMS   → SmsReminderSender
       |
       +---- PUSH  → PushReminderSender
```

This keeps the creation logic in one place.

The rest of the application doesn't need to know which concrete class it should instantiate.

The reminder senders are simulated in this project, so they print a message instead of actually sending an email, SMS, or push notification.

---

# Observer / Pub-Sub

The Observer pattern is implemented using:

```text
TaskEventPublisher
TaskEventListener
```

The application creates events when important things happen to tasks.

Examples:

```text
TaskCreated
TaskCompleted
TaskOverdue
```

The event publisher sends these events to all registered listeners.

For example:

```text
                    TaskService
                         |
                         v
                TaskEventPublisher
                  /       |       \
                 /        |        \
                v         v         v
           Logging    Reminder    Stats
           Listener   Listener   Listener
```

The listeners have different responsibilities.

### LoggingTaskEventListener

Logs task events.

### ReminderSchedulingListener

Creates/schedules the default reminder when a task is created.

### StatsTrackingListener

Keeps in-memory statistics about task events.

The listeners don't know about each other.

Also, `TaskService` doesn't need to know which listeners are registered.

It simply publishes an event.

This keeps the different parts of the application loosely coupled.

---

# Generic Repository

One of the goals of the project was to implement a generic repository instead of creating a completely different interface for every entity.

The repository is defined using:

```java
Repository<T, ID>
```

`T` represents the entity type.

`ID` represents the ID type.

For example:

```java
Repository<Task, Long>
```

means:

> This repository works with `Task` objects and uses `Long` for their IDs.

The same idea can be used for other entities:

```java
Repository<User, Long>
Repository<Reminder, Long>
```

This is an example of how Java Generics can be used to create reusable code.

---

# Caching

The project also contains a generic cache:

```java
Cache<K, V>
```

Where:

* `K` is the key
* `V` is the value

For example:

```java
Cache<Long, Task>
```

means that tasks are stored using their IDs as keys.

The project also has:

```java
DueTodayCache
```

which is used for tasks that are due today.

The main idea behind caching is to avoid unnecessary database queries when the required data is already available in memory.

---

# Hand-Rolled Merge Sort

The class:

```java
DueSoonSorter
```

contains a custom implementation of **Merge Sort**.

The method:

```java
sortByDueDateThenPriority()
```

sorts tasks using two conditions:

### First: Due Date

Tasks with an earlier due date come first.

### Second: Priority

If two tasks have the same due date, priority is used:

```text
HIGH
MEDIUM
LOW
```

For example:

```text
Task A → Tomorrow, LOW
Task B → Today, HIGH
Task C → Today, LOW
Task D → Tomorrow, HIGH
```

The result would be:

```text
Task B
Task C
Task D
Task A
```

The sorter was implemented manually without using:

```java
Collections.sort()
```

or:

```java
Arrays.sort()
```

or a built-in `Comparator` inside the sorting algorithm.

### Why Merge Sort?

I chose Merge Sort because it is **stable** and has a predictable:

```text
O(n log n)
```

time complexity.

Stability is useful because the tasks are sorted using multiple keys. If two tasks have equal sorting keys, a stable sort keeps their relative order predictable.

Merge Sort also guarantees `O(n log n)` in the worst case, while a basic Quicksort implementation can degrade to `O(n²)`.

For the size of this project, the extra memory required by Merge Sort is small and predictable.

---

# Stream Aggregations

`StatsService` uses Java Streams to calculate different task statistics.

The project currently calculates:

### 1. Tasks completed per user

Counts how many tasks each user completed during the last 7 days.

Example:

```text
User 1 → 5 tasks
User 2 → 3 tasks
```

### 2. Overdue tasks grouped by priority

Example:

```text
HIGH   → 3
MEDIUM → 5
LOW    → 2
```

### 3. Average time to completion

Calculates the average time between task creation and task completion.

The result is displayed in hours.

### 4. Total tasks by priority

An additional aggregation that counts all tasks grouped by priority.

Example:

```text
HIGH   → 10
MEDIUM → 15
LOW    → 8
```

---

# Rescheduling Tasks and Reminders

One of the design decisions in the project is what should happen when a task's due date changes after a reminder has already been scheduled.

The decision was to **move the reminder by the same amount of time as the due date moved**.

For example:

```text
Original task due date:
Friday 10:00

Original reminder:
Thursday 10:00
```

The reminder is 24 hours before the task.

If the task is moved to:

```text
Saturday 10:00
```

the reminder moves to:

```text
Friday 10:00
```

This keeps the original 24-hour lead time.

The logic is handled by:

```java
TaskService.reschedule()
```

and:

```java
ReminderService.shiftRemindersForTask()
```

### Why not leave the reminder unchanged?

Because the reminder would no longer represent "24 hours before the task."

### Why not delete the reminder?

Because changing the due date doesn't mean the user wants to cancel the reminder.

Moving the reminder preserves the user's original intention.

---

# Task State Rules

There are also some rules around task state changes.

A task that is already:

```text
DONE
```

cannot be rescheduled.

The reason is that rescheduling a completed task shouldn't automatically reopen or otherwise change completed work.

Another rule applies to overdue tasks.

If an overdue task is moved into the future:

```text
OVERDUE
    ↓
new due date is in the future
    ↓
TODO
```

For example:

```text
Before:

Due date: Yesterday
Status: OVERDUE
```

After rescheduling:

```text
Due date: Tomorrow
Status: TODO
```

This makes sense because the task is no longer overdue.

---

# Concurrent Reminder Dispatch

The project also demonstrates basic Java concurrency.

The method:

```java
ReminderService.dispatchDueConcurrently(int threadPoolSize)
```

uses:

```java
ExecutorService
```

with a fixed thread pool.

Instead of processing every reminder sequentially:

```text
Reminder 1 → send
Reminder 2 → send
Reminder 3 → send
Reminder 4 → send
```

multiple reminders can be processed by different worker threads:

```text
              Thread Pool
            /      |      \
           /       |       \
          v        v        v
       Thread 1 Thread 2 Thread 3
          |        |        |
       Reminder  Reminder  Reminder
          1         2         3
```

Each due reminder is submitted once, so the same reminder is not intentionally processed by multiple workers.

---

# Database Connection and Concurrency

The application uses a shared JDBC connection through:

```java
Database.getConnection()
```

Since multiple worker threads can access the database, the connection access is synchronized.

The basic idea is:

```text
Thread 1 ----\
Thread 2 -----+----> Shared DB Connection
Thread 3 ----/
```

Synchronization prevents multiple threads from accessing the protected connection operations at the same time.

For a production application, I would use a proper **database connection pool** instead of sharing one synchronized connection.

For example, a connection pool such as HikariCP would allow multiple database connections to be managed efficiently.

For this project, the synchronized connection is enough to demonstrate concurrent reminder dispatch without introducing a data race.

---

# Overall Flow

The main application flow can be summarized like this:

```text
User Action
     |
     v
TaskService
     |
     +------------------+
     |                  |
     v                  v
Repository         Event Publisher
     |                  |
     v                  +---------> Logging Listener
   H2 DB                |
                        +---------> Reminder Listener
                        |
                        +---------> Stats Listener
```

For reminders:

```text
Task
 |
 v
ReminderService
 |
 v
ReminderFactory
 |
 +------> EmailReminderSender
 |
 +------> SmsReminderSender
 |
 +------> PushReminderSender
```

For the due-soon task list:

```text
Tasks
  |
  v
DueSoonSorter
  |
  v
Merge Sort
  |
  +---- Due Date
  |
  +---- Priority
  |
  v
Sorted Tasks
```

---

# Main Concepts Demonstrated

The main purpose of todolist is not to build a complicated task manager.

The goal is to demonstrate how different backend concepts can work together in one application.

| Concept            | Where it is used                                      |
| ------------------ | ----------------------------------------------------- |
| OOP                | Domain classes such as `Task`, `User`, and `Reminder` |
| Encapsulation      | Task state transitions                                |
| Generics           | `Repository<T, ID>` and `Cache<K, V>`                 |
| JDBC               | Database persistence                                  |
| H2                 | Embedded database                                     |
| Repository Pattern | Database access layer                                 |
| Caching            | `Cache` and `DueTodayCache`                           |
| Factory Pattern    | `ReminderFactory`                                     |
| Observer / Pub-Sub | `TaskEventPublisher`                                  |
| Merge Sort         | `DueSoonSorter`                                       |
| Java Streams       | `StatsService`                                        |
| Concurrency        | `ExecutorService`                                     |
| Exception Handling | `todolistException` hierarchy                         |

---

# Final Notes

todolist was designed as a learning-focused backend project.

Instead of relying heavily on frameworks that automatically handle everything, the project intentionally implements several features manually. This makes the code more explicit and helps demonstrate how the different components actually work.

The main focus was on understanding **why** each component exists and how the components communicate with each other, rather than simply building the largest possible application.
