package net.conriot.sona.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.Bukkit;

import lombok.Getter;

class BasicResult implements Result {
	private ArrayList<Object[]> rows;
	private int index;
	@Getter private boolean ok;
	
	public BasicResult(ResultSet data) {
		this.rows = new ArrayList<Object[]>();
		this.index = -1;
		this.ok = true;
		
		try {
			while(data.next()) {
				Object[] row = new Object[data.getMetaData().getColumnCount()];
				for(int i = 0; i < data.getMetaData().getColumnCount(); i++)
					row[i] = data.getObject(i);
				this.rows.add(row);
			}
		} catch (SQLException e) {
			Bukkit.getLogger().severe("Could not read SQL result set");
			Bukkit.getLogger().severe(e.getMessage());
			this.ok = false;
		}
	}

	@Override
	public boolean next() {
		if(this.index == (this.rows.size() - 1))
			return false;
		
		this.index++;
		
		return true;
	}

	@Override
	public Object get(int index) {
		return this.rows.get(this.index)[index];
	}
}
