/**
 * Author: The Alliance
 */

package foodfinder.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mysql.jdbc.DatabaseMetaData;

public class DbContext {

	private Connection connection;
	private Statement statement;
	private String connectionString;
	private String server;
	private String database;
	private String username;
	private String password;
	
	
	public DbContext(String server, String database, String username, String password) {
		this.server = server;
		this.database = database;
		this.username = username;
		this.password = password;
		
		initialize();
	}
	
	public void initialize() {
		connectionString = "jdbc:mysql://" + server + ":3306/" + database + "?user=" + username + "&password=" + password;
		
		try {
			connection = DriverManager.getConnection(connectionString);
			statement = connection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void dispose() {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean createDatabase(String database) {
		
		ResultSet result = null;
		boolean valid = false;
		
		try {
			result = statement.executeQuery("CREATE DATABASE `" + database + "`");
			valid = true;
		} catch (SQLException e) {
			e.printStackTrace();
			valid = false;
		} finally {
			closeResult(result);
		}
		
		return valid;
	}
	
	public List<Map<String, Object>> selectQuery(String table, List<String> cols, String condition) {
		
		ResultSet result = null;
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		
		List<String> columns = new ArrayList<String>();
		if (cols == null || cols.isEmpty()) {
			columns = getTableSchema(table, true);
		} else
			columns = cols;
		
		StringBuilder query = new StringBuilder("SELECT ");
		
		for (int i = 0; i < columns.size(); i++) {
			String col = columns.get(i);
			
			query.append("`" + col + "`");
			query.append(i < columns.size() - 1 ? ", " : " ");
		}
		
		query.append("FROM `" + table + "`");
		
		if (condition != null) {
			query.append(" WHERE " + condition);
		}
		
		query.append(";");
		
		try {
			
			result = statement.executeQuery(query.toString());
						
			while (result.next()) {
				
				Map<String, Object> tuple = new HashMap<String, Object>();
				for (String col : columns) {
					tuple.put(col, result.getObject(col));
				}
				data.add(tuple);
			}
			
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			closeResult(result);
		}
		
		return data;
	}
	
	
	public List<Map<String, Object>> CustomQuery(String query){
		ResultSet result = null;
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			
			result = statement.executeQuery(query);
			ResultSetMetaData rsmd = result.getMetaData(); 
			
			while (result.next()) {
				
				Map<String, Object> tuple = new HashMap<String, Object>();
				
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					tuple.put(rsmd.getColumnName(i), result.getObject(rsmd.getColumnName(i)));
				}
				data.add(tuple);
			}
			
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			closeResult(result);
		}
		return data;
	}
	
	public boolean insert(String table, List<Object> values) {
		
		try {
			
			List<String> columns = getTableSchema(table, false);
			
			StringBuilder builder = new StringBuilder();
			builder.append("INSERT INTO `" + table + "` (");
			
			for (int i = 0; i < columns.size(); i++) {
				String col = columns.get(i);
				builder.append("`" + col + "`");
				builder.append(i < columns.size() - 1 ? "," : "");
			}
			
			builder.append(") VALUES(");
			
			for (int i = 0; i < values.size(); i++) {
				Object val = values.get(i);
				builder.append("'" + val.toString() + "'");
				builder.append(i < values.size() - 1 ? "," : "");
			}
			
			builder.append(");");
			
			String query = builder.toString();
			
			return !statement.execute(query);
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
		return false;
	}
	
	public boolean update(String table, List<Object> values) {
		try {
			
			List<String> columns = getTableSchema(table, true);
			
			// UPDATE [table] SET [col]=... WHERE all
			
			StringBuilder builder = new StringBuilder();
			builder.append("UPDATE `" + table + "` SET ");
			
			for (int i = 1; i < columns.size(); i++) {
				String col = columns.get(i);
				Object val = values.get(i);
				builder.append("`" + col + "` = '" + val + "'");
				builder.append(i < columns.size() - 1 ? "," : "");
			}
			
			builder.append(" WHERE `" + columns.get(0) + "` = '" + values.get(0) + "';");
			
			String query = builder.toString();
			
			return !statement.execute(query);
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
		return false;
	}
	
	public List<String> getTableSchema(String table, boolean withAutoIncrement) {
		
		try {
			
			DatabaseMetaData metaData = (DatabaseMetaData) connection.getMetaData();
			
			ResultSet result = metaData.getColumns(null, null, table, null);
			
			List<String> columns = new ArrayList<String>();
			
			while (result.next())  {
				if (withAutoIncrement || result.getString("IS_AUTOINCREMENT").equals("NO"))
					columns.add(result.getString("COLUMN_NAME"));
			}
			
			return columns;
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
		return null;
		
	}
	
	private void closeResult(ResultSet result) {
		if (result != null) {
			try {
				result.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private List<String> getResultMetaData(ResultSet result) {
		try {
			
			ResultSetMetaData metaData = (ResultSetMetaData) result.getMetaData();
			int count = metaData.getColumnCount();
			List<String> columnNames = new ArrayList<String>();

			for (int i = 1; i <= count; i++) {
				columnNames.add(metaData.getColumnLabel(i));
			}
			
			return columnNames;
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		return null;
	}
	
}
