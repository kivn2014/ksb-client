package com.ksb.web.openapi.mobile.controller;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ksb.openapi.error.BaseSupportException;
import com.ksb.openapi.mobile.service.MobileWaybillService;
import com.ksb.web.openapi.entity.AjaxEntity;
import com.ksb.web.openapi.entity.ResultPageEntity;

@Controller
@RequestMapping("/mobile_demo")
public class MobileApiController {

	private Logger log = LogManager.getLogger(getClass());
	
	@Autowired
	MobileWaybillService waybillService;
	
	
	/**
	 * 登录页面跳转
	 * @return
	 */
	@RequestMapping("/mobile_login")
	public String loginPage(){
		return "mobileapi/mobile_login";
	}

	/**
	 * 登录成功后的首页
	 * @return
	 */
	@RequestMapping("/mobile_main")
	public String mobileMainPage(){
		return "mobileapi/mobile_main";
	}
	
	
	/**
	 * 执行登录验证
	 * @param name
	 * @param password
	 * @return
	 */
	@RequestMapping("/do_mobile_login")
	public @ResponseBody
	       AjaxEntity doLogin(String name,String password){
		
		AjaxEntity rsEntity = new AjaxEntity();
		

		rsEntity.setSuccess(false);
		if(StringUtils.isBlank(name) && StringUtils.isBlank(password)){
			rsEntity.setErrors("用户名密码为空");
			return rsEntity;
		}
		
		if(StringUtils.isBlank(name) || StringUtils.isBlank(password)){
			name = "";
			password = "";
		}
		
		if(name.equals("3gongli") && password.equals("3gongli")){
			rsEntity.setSuccess(true);
			rsEntity.setErrors("ok");
			return rsEntity;
		}
		rsEntity.setErrors("用户名或者密码不正确");
		return rsEntity; 
	}
	
	@RequestMapping("/query_courier_waybill")
	public @ResponseBody
	       ResultPageEntity searchWaybillByCourier(String cid,String waybillStatus,String waybillId){
		
		log.trace(cid,waybillStatus,waybillId);
		
		ResultPageEntity rsEntity = new ResultPageEntity();
		
		Map<String, Object> rsMap = null;
		try{
		    rsMap = waybillService.searchWaybillByCourier(cid, waybillId, waybillStatus, 0, 50);
		}catch(Exception e){
			rsEntity.setSuccess(false);
			log.error("快递员["+cid+"] 查询状态为["+waybillStatus+"] 时出现异常:",e);
			return log.exit(rsEntity);
		}
		
		List<Map<String, String>> rsList = (List<Map<String, String>>)rsMap.get("1");
		Object count = rsMap.get("2");
		rsEntity.setObj(rsList);
		rsEntity.setSuccess(true);
		rsEntity.setTotalCount(Long.parseLong(count.toString()));
		rsEntity.setLimit(0);
		rsEntity.setStart(0);
		return rsEntity;
	}
	
	
	@RequestMapping("/handle_waybill")
	public @ResponseBody
	       AjaxEntity handleWaybillByCourier(String cid,String waybillId,String waybillStatus){ 
		
		log.trace(cid,waybillId,waybillStatus);
		
		AjaxEntity rsEntity = new AjaxEntity();
		rsEntity.setSuccess(true);
		
		try{
		   waybillService.updateWaybillStatusByCourier(waybillId, cid, waybillStatus);
		}catch(Exception e){
			//throw new BaseSupportException(e);
			rsEntity.setSuccess(false);
			rsEntity.setErrors(e.getMessage());
			return rsEntity;
		}
		
		rsEntity.setErrors("ok");
		return rsEntity;
	}
	
}
