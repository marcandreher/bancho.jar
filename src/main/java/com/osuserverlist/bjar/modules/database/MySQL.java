package com.osuserverlist.bjar.modules.database;

import java.sql.*;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;

@Getter
public final class MySQL implements AutoCloseable {

	private static final Logger log = LoggerFactory.getLogger(MySQL.class);
	private static final int COLUMN_WIDTH = 20;

	private final long connectionCreated;
	private final String caller;
	private Connection connection;

	public MySQL(Connection connection) {
		this.connectionCreated = System.currentTimeMillis();
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		this.caller = stack[Math.min(4, stack.length - 1)].getClassName();
		this.connection = connection;
		Database.runningConnections.add(this);
	}

	@Override
	public synchronized void close() {
		if (connection == null)
			return;
		try {
			if (!connection.isClosed()) {
				Database.currentConnections--;
				Database.runningConnections.remove(this);
				connection.close();
			}
		} catch (SQLException ex) {
			log.warn("Failed to close connection: {}", ex.getMessage());
		} finally {
			connection = null;
		}
	}

	public boolean tableExists(String tableName) {
		try (ResultSet rs = connection.getMetaData()
				.getTables(connection.getCatalog(), null, tableName, new String[] { "TABLE" })) {
			return rs.next();
		} catch (SQLException e) {
			log.error("Error checking if table exists: {}", tableName, e);
			return false;
		}
	}

	/**
	 * Prepare a statement with bound parameters. Caller is responsible for closing
	 * it.
	 */
	public PreparedStatement query(String sql, Object... args) {
		return prepareStatement(sql, false, args);
	}

	public PreparedStatement query(String sql, List<String> args) {
		return query(sql, args.toArray());
	}

	/** Execute a DML statement (INSERT / UPDATE / DELETE). */
	public void exec(String sql, Object... args) {
		try (PreparedStatement stmt = prepareStatement(sql, false, args)) {
			if (stmt != null)
				stmt.execute();
		} catch (SQLException ex) {
			log.error("exec() failed [{}]: {}", caller, ex.getMessage(), ex);
		}
	}

	/**
	 * Execute an INSERT and return the generated key, or rows affected if no key
	 * was generated.
	 */
	public int execKeys(String sql, Object... args) {
		PreparedStatement stmt = prepareStatement(sql, true, args);
		if (stmt == null)
			return -1;
		try (stmt) {
			stmt.executeUpdate();
			try (ResultSet rs = stmt.getGeneratedKeys()) {
				return rs.next() ? rs.getInt(1) : 0;
			}
		} catch (SQLException ex) {
			log.error("execKeys() failed: {}", ex.getMessage(), ex);
			return -1;
		}
	}

	/** Print a query result to stdout — useful for debugging. */
	public void printQuery(String sql, Object... args) {
		try (PreparedStatement stmt = query(sql, args);
				ResultSet rs = stmt != null ? stmt.executeQuery() : null) {
			if (rs == null)
				return;
			ResultSetMetaData meta = rs.getMetaData();
			int cols = meta.getColumnCount();
			for (int i = 1; i <= cols; i++)
				System.out.printf("%-" + COLUMN_WIDTH + "s", meta.getColumnName(i));
			System.out.println();
			while (rs.next()) {
				for (int i = 1; i <= cols; i++)
					System.out.printf("%-" + COLUMN_WIDTH + "s", rs.getString(i));
				System.out.println();
			}
		} catch (SQLException e) {
			log.error("printQuery() failed: {}", e.getMessage(), e);
		}
	}

	public Integer lastInsertId() {
		try (ResultSet rs = query("SELECT LAST_INSERT_ID()").executeQuery()) {
			if (rs.next())
				return rs.getInt(1);
		} catch (SQLException e) {
			log.error("Error getting last insert ID: {}", e.getMessage(), e);
		}
		return null;
	}

	// -------------------------------------------------------------------------
	// Internal helpers
	// -------------------------------------------------------------------------

	private PreparedStatement prepareStatement(String sql, boolean returnGeneratedKeys, Object... args) {
		if (!isOpen()) {
			log.error("Cannot prepare statement — connection is null or closed");
			return null;
		}
		try {
			int flags = returnGeneratedKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS;
			PreparedStatement stmt = connection.prepareStatement(sql, flags);
			bindArgs(stmt, args);
			logSQL(stmt.toString());
			return stmt;
		} catch (SQLException ex) {
			log.error("prepareStatement() failed: {}", ex.getMessage(), ex);
			return null;
		}
	}

	private static void bindArgs(PreparedStatement stmt, Object... args) throws SQLException {
		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			int pos = i + 1;
			if (arg == null)
				stmt.setNull(pos, Types.NULL);
			else if (arg instanceof String s)
				stmt.setString(pos, s);
			else if (arg instanceof Integer n)
				stmt.setInt(pos, n);
			else if (arg instanceof Long n)
				stmt.setLong(pos, n);
			else if (arg instanceof Boolean b)
				stmt.setBoolean(pos, b);
			else if (arg instanceof Double d)
				stmt.setDouble(pos, d);
			else if (arg instanceof Float f)
				stmt.setFloat(pos, f);
			else if (arg instanceof Timestamp t) {
				stmt.setTimestamp(pos, t);
			}
			else
				throw new IllegalArgumentException(
						"Unsupported parameter type: " + arg.getClass().getSimpleName());
		}
	}

	private boolean isOpen() {
		try {
			return connection != null && !connection.isClosed();
		} catch (SQLException e) {
			return false;
		}
	}

	private void logSQL(String sql) {
		if (Database.config.isLogSql())
			Database.logger.debug(sql.stripTrailing());
	}
}