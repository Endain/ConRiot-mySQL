package net.conriot.sona.mysql;

import org.bukkit.plugin.java.JavaPlugin;

public class MySQL extends JavaPlugin {
	private static DatabaseManager db;
	
	@Override
	public void onEnable() {
		// Instantiate a single DatabaseManager
		db = new DatabaseManager(this);
	}
	
	@Override
	public void onDisable() {
		// Nothing to do here
	}
	
	// Public API function to get a new query object from the plugin
	public static Query makeQuery() {
		return new BasicQuery();
	}
	
	// Public API function to execute a query object
	public static boolean execute(IOCallback caller, Object tag, Query query) {
		if(db == null || !db.isOk())
			return false;
		
		db.execute(caller, tag, query);
		
		return true;
	}
}
