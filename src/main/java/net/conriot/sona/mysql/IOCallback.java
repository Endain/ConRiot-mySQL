package net.conriot.sona.mysql;

public interface IOCallback {
	public void complete(boolean success, Object tag, Result result);
}
