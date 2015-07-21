package com.ksb.web.openapi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class IndexController {

//	private Logger log = LogManager.getLogger(getClass());
	
	
	@RequestMapping("/index")
	public String login() throws Exception {

		return "/index";
	}

	

}
