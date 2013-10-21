package net.conriot.sona.mysql;

public interface IOCallback {
	// Define a callback method that notes success and passes back a tag and a result object
	public void complete(boolean success, Object tag, Result result);
}
