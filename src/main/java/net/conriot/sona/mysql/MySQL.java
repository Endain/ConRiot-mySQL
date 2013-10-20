package net.conriot.sona.mysql;

import org.bukkit.plugin.java.JavaPlugin;

public class MySQL extends JavaPlugin 
{
	private static DatabaseManager db;
	
	@Override
	public void onEnable()
	{
		db = new DatabaseManager(this);
	}
	
	@Override
	public void onDisable()
	{
		// Nothing to do here
	}
	
	public static Query makeQuery() {
		return new BasicQuery();
	}
	
	public static boolean execute(IOCallback caller, Object tag, Query query) {
		if(db == null || !db.isOk())
			return false;
		
		db.execute(caller, tag, query);
		
		return true;
	}
}
