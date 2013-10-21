package net.conriot.sona.mysql;

public interface Query {
	// Define a query interface that a caller may pass a query and parameters to
	public void setQuery(String query);
	public void add(Object value);
}
