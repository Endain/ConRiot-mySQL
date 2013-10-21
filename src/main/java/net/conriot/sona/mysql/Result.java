package net.conriot.sona.mysql;

public interface Result {
	// Define a result interface that a called may read query results from
	public boolean next();
	public Object get(int index);
}
