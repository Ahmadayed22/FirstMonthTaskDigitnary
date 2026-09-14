package com.todolist.repo;

import com.todolist.config.Database;
import com.todolist.entity.User;
import com.todolist.enums.ReminderChannel;
import com.todolist.exception.DuplicateUserException;
import com.todolist.exception.UserNotFoundException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository implements Repository<User, Long> {

    @Override
    public User save(User user) {
        return user.getId() == null ? insert(user) : update(user);
    }

    private User insert(User user) {
        if (findByEmail(user.getEmail()).isPresent()) {
            throw new DuplicateUserException("A user with email " + user.getEmail() + " already exists");
        }
        String sql = "INSERT INTO users (name, email, preferred_channel) VALUES (?, ?, ?)";
        try (PreparedStatement ps = Database.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPreferredChannel().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getLong(1));
                }
            }
            return user;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to insert user", e);
        }
    }

    private User update(User user) {
        String sql = "UPDATE users SET name = ?, email = ?, preferred_channel = ? WHERE id = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPreferredChannel().name());
            ps.setLong(4, user.getId());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new UserNotFoundException("No user with id " + user.getId());
            }
            return user;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update user", e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find user " + id, e);
        }
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to look up user by email", e);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> result = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id";
        try (Statement st = Database.getConnection().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list users", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete user " + id, e);
        }
    }

    @Override
    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("email"),
                ReminderChannel.valueOf(rs.getString("preferred_channel")));
    }
}