package com.example.todoapp.dao;

import com.example.todoapp.donnee.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for {@link Task} model using SQLite.
 */
public class TaskDao {

    private static final String DB_URL = "jdbc:sqlite:todoapp.db";

    public TaskDao() {
        initDatabase();
    }

    private void initDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS tasks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL," +
                "description TEXT," +
                "done INTEGER NOT NULL" +
                ");";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Erreur d'initialisation de la BDD : " + e.getMessage());
        }
    }

    private Task mapRowToTask(ResultSet rs) throws SQLException {
        return new Task(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getInt("done") == 1
        );
    }

    public Task insert(Task task) {
        String sql = "INSERT INTO tasks(title, description, done) VALUES(?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, task.title());
            pstmt.setString(2, task.description());
            pstmt.setInt(3, task.done() ? 1 : 0);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    return new Task(generatedId, task.title(), task.description(), task.done());
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion : " + e.getMessage());
        }
        return task; // Retourne la tâche sans ID en cas d'échec SQL (cas rare)
    }

    public Optional<Task> findById(int id) {
        String sql = "SELECT id, title, description, done FROM tasks WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToTask(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche : " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Task> findall() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT id, title, description, done FROM tasks";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tasks.add(mapRowToTask(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du findall : " + e.getMessage());
        }
        return tasks;
    }

    public int remove(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
            return 0;
        }
    }

    public Optional<Task> modif(int id, Task task) {
        String sql = "UPDATE tasks SET title = ?, description = ?, done = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, task.title());
            pstmt.setString(2, task.description());
            pstmt.setInt(3, task.done() ? 1 : 0);
            pstmt.setInt(4, id);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                return Optional.of(new Task(id, task.title(), task.description(), task.done()));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification : " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Task> remove_all() {
        String sql = "DELETE FROM tasks";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression totale : " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM tasks";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage : " + e.getMessage());
        }
        return 0;
    }
}