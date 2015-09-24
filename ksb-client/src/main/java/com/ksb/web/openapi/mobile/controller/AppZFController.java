package com.ksb.web.openapi.mobile.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ksb.openapi.entity.ResultEntity;
import com.ksb.openapi.mobile.service.ChargeService;
import com.pingplusplus.Pingpp;

@Controller
@RequestMapping("/mobile/pay")
public class AppZFController {

	@Value("#{sys['pingxx_app_id']}")
	public String PINGXX_API_ID = null;
	
	public String PINGXX_API_KEY = null;
	
	public ChargeService chargeService;
	
	/*初始化设置ping++ app key*/
	public AppZFController(){
		Pingpp.apiKey = PINGXX_API_KEY;
	}
	
	@Value("#{sys['pingxx_app_key']}")
	public void setPingxxApiKey(String appKey){
		this.PINGXX_API_KEY = appKey;
	}
	
	/**
	 * 商家充值
	 * @param channel_name 在线支付渠道(alipay:支付宝APP支付 ; alipay_qr:支付宝扫码支付 ; wx:微信支付 ; wx_pub:微信公众号支付 ; wx_pub_qr:微信公众号扫码 ; bfb:百度钱包支付)
	 * @param amount
	 * @return
	 */
	@RequestMapping(value="/sp_recharge",method=RequestMethod.POST)
	public @ResponseBody
	       ResultEntity shipperRecharge(String channel_name,String amount,String sp_id){
		
		chargeService.shipperRecharge(channel_name, amount, sp_id, PINGXX_API_ID);
		
		
		return null;
	}
	
	
	
}
