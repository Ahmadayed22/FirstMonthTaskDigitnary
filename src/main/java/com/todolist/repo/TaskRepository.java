package com.todolist.repo;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.todolist.config.Database;
import com.todolist.entity.Task;
import com.todolist.enums.Priority;
import com.todolist.enums.TaskStatus;
import com.todolist.exception.TaskNotFoundException;


public class TaskRepository  implements Repository<Task, Long> {

    @Override
    public Task save(Task task) {
        return task.getId() == null ? insert(task) : update(task);
    }

    private Task insert(Task task) {
        String sql = "INSERT INTO tasks (owner_id, title, due_date, priority, status, created_at, completed_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = Database.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, task);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    task.setId(keys.getLong(1));
                }
            }
            return task;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to insert task", e);
        }
    }

    private Task update(Task task) {
        String sql = "UPDATE tasks SET owner_id=?, title=?, due_date=?, priority=?, status=?, "
                + "created_at=?, completed_at=? WHERE id=?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            bind(ps, task);
            ps.setLong(8, task.getId());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new TaskNotFoundException("No task with id " + task.getId());
            }
            return task;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update task", e);
        }
    }

    private void bind(PreparedStatement ps, Task task) throws SQLException {
        ps.setLong(1, task.getOwnerId());
        ps.setString(2, task.getTitle());
        ps.setTimestamp(3, Timestamp.valueOf(task.getDueDate()));
        ps.setString(4, task.getPriority().name());
        ps.setString(5, task.getStatus().name());
        ps.setTimestamp(6, Timestamp.valueOf(task.getCreatedAt()));
        ps.setTimestamp(7, task.getCompletedAt() == null ? null : Timestamp.valueOf(task.getCompletedAt()));
    }

    @Override
    public Optional<Task> findById(Long id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find task " + id, e);
        }
    }

    @Override
    public List<Task> findAll() {
        return queryList("SELECT * FROM tasks ORDER BY id", ps -> {
        });
    }

    public List<Task> findByOwner(Long ownerId) {
        return queryList("SELECT * FROM tasks WHERE owner_id = ? ORDER BY id",
                ps -> ps.setLong(1, ownerId));
    }

    public List<Task> findByStatus(TaskStatus status) {
        return queryList("SELECT * FROM tasks WHERE status = ? ORDER BY due_date",
                ps -> ps.setString(1, status.name()));
    }

    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private List<Task> queryList(String sql, Binder binder) {
        List<Task> result = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Query failed: " + sql, e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("DELETE FROM tasks WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete task " + id, e);
        }
    }

    @Override
    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    private Task map(ResultSet rs) throws SQLException {
        Task task = new Task(
                rs.getLong("id"),
                rs.getLong("owner_id"),
                rs.getString("title"),
                rs.getTimestamp("due_date").toLocalDateTime(),
                Priority.valueOf(rs.getString("priority"))
        );
        task.setStatus(TaskStatus.valueOf(rs.getString("status")));
        Timestamp completed = rs.getTimestamp("completed_at");
        if (completed != null) {
            task.setCompletedAt(completed.toLocalDateTime());
        }
        return task;
    }
}
