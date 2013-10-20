package net.conriot.sona.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.Bukkit;

import lombok.Getter;

class BasicQuery implements Query {
	@Getter private String query;
	@Getter private ArrayList<Object> values;
	
	public BasicQuery() {
		this.query = null;
		this.values = new ArrayList<Object>();
	}
	
	public PreparedStatement prepare(Connection c) {
		PreparedStatement pr = null;
		
		try {
			pr = c.prepareStatement(this.query);
			for(int i = 0; i < this.values.size(); i++)
				pr.setObject(i, this.values.get(i));
		} catch (SQLException e) {
			Bukkit.getLogger().severe("Could not prepare SQL statement: '" + this.query + "'");
			Bukkit.getLogger().severe(e.getMessage());
			pr = null;
		}
		
		return pr;
	}
	
	@Override
	public void setQuery(String query) {
		this.query = query;
	}

	@Override
	public void add(Object value) {
		this.values.add(value);
	}
}
