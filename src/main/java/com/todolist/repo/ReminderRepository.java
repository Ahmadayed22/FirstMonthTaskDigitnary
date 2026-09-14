package com.todolist.repo;




import com.todolist.config.Database;
import com.todolist.entity.Reminder;
import com.todolist.enums.ReminderChannel;
import com.todolist.exception.TaskFlowException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReminderRepository implements Repository<Reminder, Long> {

    @Override
    public Reminder save(Reminder reminder) {
        return reminder.getId() == null ? insert(reminder) : update(reminder);
    }

    private Reminder insert(Reminder reminder) {
        String sql = "INSERT INTO reminders (task_id, trigger_time, channel, sent) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = Database.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, reminder);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    reminder.setId(keys.getLong(1));
                }
            }
            return reminder;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to insert reminder", e);
        }
    }

    private Reminder update(Reminder reminder) {
        String sql = "UPDATE reminders SET task_id=?, trigger_time=?, channel=?, sent=? WHERE id=?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            bind(ps, reminder);
            ps.setLong(5, reminder.getId());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new TaskFlowException("No reminder with id " + reminder.getId());
            }
            return reminder;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update reminder", e);
        }
    }

    private void bind(PreparedStatement ps, Reminder reminder) throws SQLException {
        ps.setLong(1, reminder.getTaskId());
        ps.setTimestamp(2, Timestamp.valueOf(reminder.getTriggerTime()));
        ps.setString(3, reminder.getChannel().name());
        ps.setBoolean(4, reminder.isSent());
    }

    @Override
    public Optional<Reminder> findById(Long id) {
        try (PreparedStatement ps = Database.getConnection()
                .prepareStatement("SELECT * FROM reminders WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find reminder " + id, e);
        }
    }

    @Override
    public List<Reminder> findAll() {
        List<Reminder> result = new ArrayList<>();
        try (Statement st = Database.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM reminders ORDER BY id")) {
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list reminders", e);
        }
    }

    public List<Reminder> findByTaskId(Long taskId) {
        List<Reminder> result = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection()
                .prepareStatement("SELECT * FROM reminders WHERE task_id = ? ORDER BY trigger_time")) {
            ps.setLong(1, taskId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find reminders for task " + taskId, e);
        }
    }

    public List<Reminder> findDueForDispatch() {
        List<Reminder> result = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "SELECT * FROM reminders WHERE sent = FALSE AND trigger_time <= CURRENT_TIMESTAMP")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find due reminders", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try (PreparedStatement ps = Database.getConnection()
                .prepareStatement("DELETE FROM reminders WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete reminder " + id, e);
        }
    }

    @Override
    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    private Reminder map(ResultSet rs) throws SQLException {
        Reminder reminder = new Reminder(
                rs.getLong("id"),
                rs.getLong("task_id"),
                rs.getTimestamp("trigger_time").toLocalDateTime(),
                ReminderChannel.valueOf(rs.getString("channel"))
        );
        if (rs.getBoolean("sent")) {
            reminder.markSent();
        }
        return reminder;
    }
}
