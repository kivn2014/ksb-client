package com.ksb.web.openapi.service;

public interface IdWorker {
	public long nextId(long workerId, long datacenterId);
	public long nextId(long dataId);
}
