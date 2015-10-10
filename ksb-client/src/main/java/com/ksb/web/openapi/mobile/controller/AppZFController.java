package com.ksb.web.openapi.mobile.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ksb.openapi.entity.ResultEntity;
import com.ksb.openapi.error.BaseSupportException;
import com.ksb.openapi.mobile.service.ChargeService;
import com.pingplusplus.model.Charge;

@Controller
@RequestMapping("/mobile/pay")
public class AppZFController {

	@Autowired
	ChargeService chargeService;
	
	/**
	 * 商家充值
	 * @param channel_name 在线支付渠道(alipay:支付宝APP支付 ; alipay_qr:支付宝扫码支付 ; wx:微信支付 ; wx_pub:微信公众号支付 ; wx_pub_qr:微信公众号扫码 ; bfb:百度钱包支付)
	 * @param amount
	 * @return
	 */
	@RequestMapping(value="/sp_recharge",method=RequestMethod.POST)
	public @ResponseBody
	       Object shipperRecharge(HttpServletRequest req){
		
		String postData = null;
		try{
			postData = getPostData(req);
		}catch(Exception e){
			return getErrorEntity(e.getMessage());
		}
		if(postData==null){
			return errorPayInfo("未提供任何参数");
		}
		
		JSONObject json = null;
		try{
			json = JSON.parseObject(postData);
			if(json==null){
				return errorPayInfo("客户端数据解析异常");
			}
		}catch(Exception e){
			return errorPayInfo("客户端数据解析异常");
		}
		
		
		String channel = json.getString("channel");
		if(StringUtils.isBlank(channel)){
			return errorPayInfo("支付渠道为空");
		}
		
		String amount = json.getString("amount");
		if(StringUtils.isBlank(amount)){
			return errorPayInfo("支付金额为空");
		}
		
		String spId = json.getString("sp_id");
		if(StringUtils.isBlank(spId)){
			return errorPayInfo("商家编号为空");
		}
		
		Charge charge = chargeService.shipperRecharge(channel, amount, spId);
		
		return charge;
	}
	
	@RequestMapping("/pay_success")
	public @ResponseBody Object paySuccessHooks(HttpServletRequest req){
		
		try{
			getPostData(req);
		}catch(Exception e){
			
		}
		
		return null;
	}
	
	private String getPostData(HttpServletRequest req) {
		StringBuffer sb = new StringBuffer("");
		try {
			InputStreamReader in = new InputStreamReader(req.getInputStream());
			BufferedReader bufReader = new BufferedReader(in);

			String buf = bufReader.readLine();
			while (buf != null) {
				sb.append(buf);
				buf = bufReader.readLine();
			}
		} catch (Exception e) {
			throw new BaseSupportException(e);
		}
		
		System.out.println(sb);
		return sb.toString();
	}
	
	/**
	 * 返回操作对象默认值(操作出现异常)
	 * @param errorInfo
	 * @return
	 */
	public ResultEntity getErrorEntity(String errorInfo){
		if(StringUtils.isNotBlank(errorInfo)){
			errorInfo = errorInfo.replace("java.lang.reflect.UndeclaredThrowableException", "system error");
			errorInfo = errorInfo.replace("com.ksb.openapi.error.BaseSupportException", "");
		}
		ResultEntity rs = new ResultEntity();
		rs.setSuccess(false);
		rs.setErrors(errorInfo);
		rs.setObj("");
		return rs;
	}
	
	
	public Map<String, String> errorPayInfo(String msg){
		Map<String, String> map = new HashMap<String, String>();
		map.put("ksb_result", "false");
		map.put("ksb_msg", msg);
		return map;
	}
	
	/**
	 * 返回操作对象默认值(操作正常)
	 * @param obj
	 * @return
	 */
	public ResultEntity getSuccessEntity(Object obj){
		ResultEntity rs = new ResultEntity();
		rs.setSuccess(true);
		rs.setErrors("OK");
		rs.setObj(obj);
		return rs;
	} 
}
