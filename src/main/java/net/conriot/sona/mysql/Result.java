package net.conriot.sona.mysql;

public interface Result {
	public boolean next();
	public Object get(int index);
}
