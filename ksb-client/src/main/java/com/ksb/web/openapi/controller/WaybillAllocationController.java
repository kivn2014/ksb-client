package com.ksb.web.openapi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ksb.web.openapi.entity.ResultPageEntity;

@Controller
@RequestMapping("/allocate")
public class WaybillAllocationController {

	
	
	
	@RequestMapping("/query")
	public @ResponseBody
	       ResultPageEntity queryUnAllocateWaybill(){
		
		
		
		
		return null;
	}
	
	
	
	
}
