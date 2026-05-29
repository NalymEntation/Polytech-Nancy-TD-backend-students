package com.example.todoapp.dao;

import com.example.todoapp.donnee.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) gérant la persistance des entités {@link Task} dans une base de données SQLite.
 */
public class TaskDao {

    /** Chaîne de connexion JDBC pointant vers le fichier local SQLite */
    private static final String DB_URL = "jdbc:sqlite:todoapp.db";

    /**
     * Constructeur du DAO. Initialise la base de données à l'instanciation.
     */
    public TaskDao() {
        initDatabase();
    }

    /**
     * Crée la table 'tasks' si elle n'existe pas encore.
     * Gère la conversion des types SQLite (ex: INTEGER pour les booléens).
     */
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
            System.err.println("Erreur d'initialisation BDD : " + e.getMessage());
        }
    }

    /**
     * Méthode utilitaire pour mapper une ligne de résultat SQL en objet Java {@link Task}.
     * * @param rs Le ResultSet positionné sur la ligne courante.
     * @return Une instance de Task.
     * @throws SQLException En cas d'erreur de lecture de la colonne.
     */
    private Task mapRowToTask(ResultSet rs) throws SQLException {
        return new Task(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getInt("done") == 1 // Conversion de l'entier SQLite (1/0) en booléen
        );
    }

    /**
     * Insère une nouvelle tâche en base et récupère son ID auto-généré.
     * * @param task L'objet Task contenant les données à insérer (l'ID est ignoré).
     * @return La tâche avec son identifiant définitif généré par SQLite.
     */
    public Task insert(Task task) {
        String sql = "INSERT INTO tasks(title, description, done) VALUES(?, ?, ?)";

        // RETURN_GENERATED_KEYS permet de récupérer l'ID créé par l'AUTOINCREMENT
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, task.title());
            pstmt.setString(2, task.description());
            pstmt.setInt(3, task.done() ? 1 : 0);
            pstmt.executeUpdate();

            // Extraction de l'ID généré
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return new Task(rs.getInt(1), task.title(), task.description(), task.done());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur BDD lors de l'insertion", e);
        }
        return task;
    }

    /**
     * Recherche une tâche par son identifiant unique.
     * * @param id L'identifiant à rechercher.
     * @return Un Optional contenant la tâche si trouvée, sinon Optional.empty().
     */
    public Optional<Task> findById(int id) {
        String sql = "SELECT id, title, description, done FROM tasks WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRowToTask(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur BDD lecture", e);
        }
        return Optional.empty();
    }

    /**
     * Récupère la liste complète des tâches stockées.
     * * @return Une liste contenant toutes les tâches (vide si la table est vide).
     */
    public List<Task> findall() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT id, title, description, done FROM tasks";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) tasks.add(mapRowToTask(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erreur BDD findall", e);
        }
        return tasks;
    }

    /**
     * Supprime une tâche en fonction de son ID.
     * * @param id L'identifiant de la tâche à supprimer.
     * @return Le nombre de lignes supprimées (1 en cas de succès, 0 si l'ID n'existait pas).
     */
    public int remove(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur BDD suppression", e);
        }
    }

    /**
     * Met à jour les informations d'une tâche existante.
     * * @param id   L'identifiant de la tâche à modifier.
     * @param task Les nouvelles données à appliquer.
     * @return Un Optional contenant la tâche mise à jour en cas de succès.
     */
    public Optional<Task> modif(int id, Task task) {
        String sql = "UPDATE tasks SET title = ?, description = ?, done = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task.title());
            pstmt.setString(2, task.description());
            pstmt.setInt(3, task.done() ? 1 : 0);
            pstmt.setInt(4, id);

            if (pstmt.executeUpdate() > 0) {
                return Optional.of(new Task(id, task.title(), task.description(), task.done()));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur BDD modification", e);
        }
        return Optional.empty();
    }

    /**
     * Vide intégralement la table des tâches.
     * * @return Une liste vide.
     */
    public List<Task> remove_all() {
        String sql = "DELETE FROM tasks";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur BDD suppression totale", e);
        }
        return new ArrayList<>();
    }

    /**
     * Compte le nombre total de tâches enregistrées.
     * * @return Le nombre de tâches.
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM tasks";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur BDD count", e);
        }
        return 0;
    }
}