package com.osuserverlist.bjar.modules.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;

@Data
public final class MySQL implements AutoCloseable {

	private static Logger log = LoggerFactory.getLogger(MySQL.class);

	public long connectionCreated;
	public String caller;
	private Connection currentCon;

	private final int COLUMN_WIDTH = 20;

	public MySQL(Connection currentCon) {
		open(currentCon);
	}

	public synchronized void open(Connection currentCon) {
		this.connectionCreated = System.currentTimeMillis();
		caller = Thread.currentThread().getStackTrace()[4].getClassName();
		Database.runningConnections.add(this);
		this.currentCon = currentCon;
	}

	public synchronized void close() {
		try {
			if (!currentCon.isClosed()) {
				Database.currentConnections--;
				Database.runningConnections.remove(this);
				currentCon.close();
				currentCon = null;
			}
		} catch (Exception ex) {
			log.warn("Failed to close connection: Number " + Database.currentConnections);
		}

	}

	public boolean tableExists(String tableName) {
		try (ResultSet rs = currentCon.getMetaData().getTables(currentCon.getCatalog(), null, tableName,
				new String[] { "TABLE" })) {
			return rs.next();
		} catch (SQLException e) {
			log.error("Error checking if table exists: " + tableName, e);
			return false;
		}
	}

	private void printResultSet(PreparedStatement stmt) {
		try (ResultSet resultSet = stmt.executeQuery()) {
			ResultSetMetaData metaData = resultSet.getMetaData();
			int columnCount = metaData.getColumnCount();

			for (int i = 1; i <= columnCount; i++) {
				String columnName = metaData.getColumnName(i);
				System.out.printf("%-" + COLUMN_WIDTH + "s", columnName);
			}
			System.out.println();

			while (resultSet.next()) {
				for (int i = 1; i <= columnCount; i++) {
					String columnValue = resultSet.getString(i);
					System.out.printf("%-" + COLUMN_WIDTH + "s", columnValue);
				}
				System.out.println();
			}
		} catch (SQLException e) {
			log.error("MySQL Exec Error: " + e.getMessage(), e);
		}
	}

	public PreparedStatement query(String sql, Object... args) {
		try {
			if (currentCon == null || currentCon.isClosed()) {
				log.error("Cannot create query - connection is null or closed");
				return null;
			}
			
			PreparedStatement stmt = currentCon.prepareStatement(sql);
			for (int i = 0; i < args.length; i++) {
				if (isNumeric(args[i].toString())) {
					stmt.setInt(i + 1, Integer.parseInt(args[i].toString()));
				} else {
					stmt.setString(i + 1, (String) args[i]);
				}
			}
				
			logSQL(stmt.toString());
			
			
			return stmt;
		} catch (Exception ex) {
			log.error("MySQL Exec Error: " + ex.getMessage(), ex);
			return null;
		}
	}


	public void printQuery(String sql, String... args) {
		PreparedStatement stmt = query(sql, (Object[]) args);
		printResultSet(stmt);
	}


	public PreparedStatement query(String sql, List<String> args) {
		try {
			if (currentCon == null || currentCon.isClosed()) {
				log.error("Cannot create query - connection is null or closed");
				return null;
			}
			
			PreparedStatement stmt = currentCon.prepareStatement(sql);
			for (int i = 0; i < args.size(); i++)
				stmt.setString(i + 1, args.get(i));

			return stmt;

		} catch (Exception ex) {
			log.error("MySQL Exec Error: " + ex.getMessage(), ex);
			return null;
		}
	}

	public void exec(String sql, Object... args) {
		try {
			if (currentCon == null || currentCon.isClosed()) {
				log.error("Cannot execute query - connection is null or closed");
				return;
			}
			
			try (PreparedStatement stmt = currentCon.prepareStatement(sql)) {
				for (int i = 0; i < args.length; i++) {
					if (args[i] instanceof String) {
						stmt.setString(i + 1, (String) args[i]);
					} else if (args[i] instanceof Integer) {
						stmt.setInt(i + 1, (Integer) args[i]);
					} else if (args[i] instanceof Long) {
						stmt.setLong(i + 1, (Long) args[i]);
					} else if (args[i] instanceof Boolean) {
						stmt.setBoolean(i + 1, (Boolean) args[i]);
					} else if (args[i] instanceof Double) {
						stmt.setDouble(i + 1, (Double) args[i]);
					} else {
						throw new IllegalArgumentException("Unsupported parameter type: " + args[i].getClass());
					}
				}
				logSQL(stmt.toString());
				stmt.execute();
			}
		} catch (Exception ex) {
			// Print last calling class
			log.error("MySQL Exec Error: " + ex.getMessage() + " | called from " + getCaller(), ex);
		}
	}

	public int execKeys(String sql, Object... args) {
		try {
			if (currentCon == null || currentCon.isClosed()) {
				log.error("Cannot execute query - connection is null or closed");
				return -1;
			}
			
			PreparedStatement stmt = currentCon.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			for (int i = 0; i < args.length; i++) {
				if (args[i] instanceof String) {
					stmt.setString(i + 1, (String) args[i]);
				} else if (args[i] instanceof Integer) {
					stmt.setInt(i + 1, (Integer) args[i]);
				} else if (args[i] instanceof Long) {
					stmt.setLong(i + 1, (Long) args[i]);
				} else if (args[i] instanceof Boolean) {
					stmt.setBoolean(i + 1, (Boolean) args[i]);
				} else if (args[i] instanceof Double) {
					stmt.setDouble(i + 1, (Double) args[i]);
				} else {
					throw new IllegalArgumentException("Unsupported parameter type: " + args[i].getClass());
				}
			}

			int rowsAffected = stmt.executeUpdate();

			logSQL(stmt.toString());

			ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				int generatedKey = rs.getInt(1);
				return generatedKey;
			} else {
				return rowsAffected;
			}
		} catch (Exception ex) {
			log.error("MySQL Exec Error: " + ex.getMessage(), ex);
			return -1;
		}
	}

	private void logSQL(String message) {
		if (Database.config.isLogSql()) {
			Database.logger.debug(message.replaceAll("[\r\n]+$", ""));
		}
	}

	public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }

        // Only allow optional leading minus and digits, no decimal
        if (!strNum.matches("-?\\d+")) {
            return false;
        }

        try {
            Integer.parseInt(strNum);
            // No need to check range, parseInt throws if out of bounds
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}