package com.ksb.web.openapi.entity;

public class AjaxEntity {

	private boolean success = false;
	private String errors = null;

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getErrors() {
		return errors;
	}

	public void setErrors(String errors) {
		this.errors = errors;
	}

	@Override
	public String toString() {
		return "result [success=" + success + ", errors=" + errors + "]";
	}
}
