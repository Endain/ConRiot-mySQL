package net.conriot.sona.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;

import lombok.Getter;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

class DatabaseManager {
	private MySQL plugin;
	public BoneCP pool;
	@Getter private boolean ok;
	
	public DatabaseManager(MySQL plugin) {
		this.plugin = plugin;
		
		// Attempt to set up the connection pool
		try {
			// Load the driver
			Class.forName("com.mysql.jdbc.Driver"); 
			// Set up the config
			BoneCPConfig config = new BoneCPConfig();
			config.setJdbcUrl("jdbc:mysql://127.1.0.0:3306/example");
			config.setUsername("user");
			config.setPassword("password");
			config.setMinConnectionsPerPartition(5);
			config.setMaxConnectionsPerPartition(10);
			config.setPartitionCount(3);
			// Create the connection pool
			this.pool = new BoneCP(config);
			// Flag the manager as ready to roll
			this.ok = true;
		} catch (SQLException e) {
			// Error creating the connection pool
			Bukkit.getLogger().severe("Could not create a connection pool with the given SQL credentials");
			Bukkit.getLogger().severe(e.getMessage());
		} catch (ClassNotFoundException e) {
			// Error loading driver
			Bukkit.getLogger().severe("Could not load the JDBC driver");
			Bukkit.getLogger().severe(e.getMessage());
		}
	}
	
	public void execute(IOCallback caller, Object tag, Query query) {
		// Ensure caller is valid
		if(caller == null)
			return;
		
		// Check for any invalid conitions that would prevent a proper query
		if(plugin == null || !ok || query == null || !(query instanceof BasicQuery) || ((BasicQuery)query).getQuery() == null) {
			Bukkit.getLogger().warning("Passed a query that could not be executed");
			Bukkit.getScheduler().runTask(this.plugin, new Rejoin(caller, tag, null, false));
			return;
		}
		
		// Perform an asynchronous query
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Async(caller, tag, (BasicQuery) query));
	}
	
	private class Rejoin implements Runnable {
		private IOCallback caller;
		private Object tag;
		private Result result;
		private boolean success;
		
		public Rejoin(IOCallback caller, Object tag, Result result, boolean success) {
			this.caller = caller;
			this.tag = tag;
			this.result = result;
			this.success = success;
		}
		
		@Override
		public void run() {
			// Make a call to the given callback with a result object to notify of completion
			this.caller.complete(this.success, this.tag, this.result);
		}
	}
	
	private class Async implements Runnable {
		private IOCallback caller;
		private Object tag;
		private BasicQuery query;
		
		public Async(IOCallback caller, Object tag, BasicQuery query) {
			this.caller = caller;
			this.tag = tag;
			this.query = query;
		}
		
		@Override
		public void run() {
			// Assume failure until we know otherwise
			boolean failed = true;
			
			// Try to create a connection to the database
			Connection c;
			try {
				c = pool.getConnection();
			} catch (SQLException e) {
				Bukkit.getLogger().severe("Could not get SQL connection from the connection pool");
				Bukkit.getLogger().severe(e.getMessage());
				c = null;
			}
			if(c != null) {
				// Try to prepare a statement for execution
				PreparedStatement ps = query.prepare(c);
				if(ps != null) {
					// Try to execute the statement
					try {
						if(ps.execute()) {
							// The query returned some results, copy them into a result object
							ResultSet rs = ps.getResultSet();
							BasicResult r = new BasicResult(rs);
							// try to close the result set
							try {
								rs.close();
							} catch (SQLException e) {
								Bukkit.getLogger().severe("Could not properly close SQL result set");
								Bukkit.getLogger().severe(e.getMessage());
								r = null;
							}
							// Verify result integrity
							if(r != null && r.isOk()) {
								// Note explicitly that we have NOT failed
								failed = false;
								// Rejoin and send our result back
								Bukkit.getScheduler().runTaskLater(plugin, new Rejoin(caller, tag, r, true), 0);
							}
						} else {
							// The query succeeded, but no data was returned
							// Note explicitly that we have NOT failed
							failed = false;
							// Rejoin and do not send a result
							Bukkit.getScheduler().runTaskLater(plugin, new Rejoin(caller, tag, null, true), 0);
						}
					} catch (SQLException e) {
						Bukkit.getLogger().severe("Could not execute SQL query");
						Bukkit.getLogger().severe(e.getMessage());
					}
					// Try to close our prepared statement
					try {
						ps.close();
					} catch (SQLException e) {
						Bukkit.getLogger().severe("Could not properly close prepared SQL statement");
						Bukkit.getLogger().severe(e.getMessage());
					}
				}
				// Try to close our connection
				try {
					c.close();
				} catch (SQLException e) {
					Bukkit.getLogger().severe("Could not properly close SQL connection");
					Bukkit.getLogger().severe(e.getMessage());
				}
			}
			
			// If no explicit success, notify our caller that the query failed
			if(failed)
				Bukkit.getScheduler().runTaskLater(plugin, new Rejoin(caller, tag, null, false), 0);
		}
	}
}
