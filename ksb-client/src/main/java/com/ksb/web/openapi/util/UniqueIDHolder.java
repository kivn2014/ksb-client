package com.ksb.web.openapi.util;

import com.ksb.web.openapi.service.IdWorker;
import com.ksb.web.openapi.service.IdWorkerFromSnowflake;

public class UniqueIDHolder {
	private static IdWorker idWorker = new IdWorkerFromSnowflake();
	private static FileUploadUtil fileupload = new FileUploadUtil();

	public static IdWorker getIdWorker() {
		return idWorker;
	}
	
	protected static void setIdWorker(IdWorker idWorker) {
		if (idWorker != null
				&& IdWorker.class.isAssignableFrom(idWorker.getClass()))
			UniqueIDHolder.idWorker = idWorker;
	}
	
	public static FileUploadUtil getFileUpload(){
		return fileupload;
	}
	
	protected static void setFileUpload(){
		if(fileupload!=null && FileUploadUtil.class.isAssignableFrom(fileupload.getClass())){
			UniqueIDHolder.fileupload = fileupload;
		}
	}
	
	
	
	
}
