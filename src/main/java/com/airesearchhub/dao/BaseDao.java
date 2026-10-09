package com.airesearchhub.dao;

import com.airesearchhub.config.DatabaseManager;
import com.airesearchhub.exception.DatabaseException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * BaseDao abstract class demonstrating Object-Oriented Inheritance,
 * Exception Handling, and JDBC parameter binding.
 */
public abstract class BaseDao<T, ID> implements GenericDao<T, ID> {

    protected Connection getConnection() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    protected void setParameters(PreparedStatement ps, Object... params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
        }
    }

    @FunctionalInterface
    public interface ResultSetExtractor<R> {
        R extract(ResultSet rs) throws SQLException;
    }

    protected Long executeInsertWithGeneratedKey(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParameters(ps, params);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DatabaseException("Insert failed, no rows affected.");
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Database insert error: " + e.getMessage(), e);
        }
    }

    protected int executeUpdate(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParameters(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Database update error: " + e.getMessage(), e);
        }
    }
}
