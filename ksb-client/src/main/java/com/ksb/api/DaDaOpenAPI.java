package com.ksb.api;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
//import com.ß
import com.ksb.openapi.util.HTTPUtils;
import com.ksb.openapi.util.MD5Util;

public class DaDaOpenAPI {

	/*达达API域名*/
	public static final String DADA_RESTAPI_HOST = "http://public.ga.dev.imdada.cn"; 
//	public static final String DADA_RESTAPI_HOST="http://public.imdada.cn/"; 
	
	/*达达获取授权码的scope*/
	public static final String DADA_SCOPE = "dada_base";
	
	/*达达APPKEY*/
	public static final String DADA_APP_KEY = "dada999334390160b605";
	
	/*达达rest api nonce*/
	public static final String SIGNATURE_NONCE = "dada";
	
	
	/***
	 * 根据达达APP key或者授权码
	 * 
	 */
	private String getGrantCode(){
		
		StringBuffer uri = new StringBuffer(DADA_RESTAPI_HOST+"/oauth/authorize/?");
		
		uri.append("app_key="+DADA_APP_KEY);
		uri.append("&scope="+DADA_SCOPE);
		
		String resultStr = null;
		try {
			resultStr = HTTPUtils.executeGet(uri.toString()).getObj().toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JSONObject jsonObj = JSON.parseObject(resultStr);
		String resultStatus = jsonObj.get("status").toString();
		
		if(resultStatus.equals("fail")){
			String errorCode = jsonObj.getString("errorCode").toString();
			System.out.println("获取达达授权码失败,错误码为："+errorCode);
			return null;
		}
		String grantCode = jsonObj.getJSONObject("result").get("grant_code").toString();
		
		return grantCode;
	}
	
	
	/**
	 * 获取请求达达openapi的token值
	 */
	public String getDADAToken(){
		
		/*达达的接口类型参数，表示根据appkey获取rest api请求token*/
		String grantType = "authorization_code";
//		/oauth/access_token/?
		
		StringBuffer uri = new StringBuffer(DADA_RESTAPI_HOST+"/oauth/access_token/?");
		uri.append("grant_type="+grantType);
		uri.append("&app_key="+DADA_APP_KEY);
		uri.append("&grant_code="+getGrantCode());
		
		String resultRs = null;
		try {
			resultRs = HTTPUtils.executeGet(uri.toString()).getObj().toString();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(resultRs==null){
			System.out.println("无法从达达open api中获取token信息");
			return null;
		}
		
		
		JSONObject jsonObj = null;
		try{
			jsonObj = JSON.parseObject(resultRs);
		}catch(Exception e){
			System.out.println("达达open api获取token接口 返回的数据格式异常 "+e.getMessage());
			return null;
		}
		
		String accessToken = jsonObj.getJSONObject("result").get("access_token").toString();
		
		return accessToken;
	}
	
	
	public String getSignatureByToken(String token,String timestamp){
		
        String[] arrTmp = { token, timestamp, SIGNATURE_NONCE };
        Arrays.sort(arrTmp);  
        StringBuffer sb = new StringBuffer();  
        
        /*将三个参数字符串拼接成一个字符串进行md5加密*/
        for (int i = 0; i < arrTmp.length; i++) {  
            sb.append(arrTmp[i]);  
        }  
        
        /*进行MD5签名*/
        String str = MD5Util.MD5(sb.toString());
        
        return str;
	}
	
	
	
	/**
	 * 创建达达快递订单
	 * @param paramMap
	 */
	public String createDaDaOrder(Map<String, String> paramMap){
		
		/*首先把运单数据 存入到快送宝数据库*/
		String ksbId = waybill2ksbDB(paramMap);
		
		String token = getDADAToken();
		
		long timestamps = new Date().getTime();
		
		String sn = getSignatureByToken(token, String.valueOf(timestamps));
		
		/*把快送宝运单ID、token、timestamp、sn数据放入到map中(需要把这些参数提供给达达快递rest api用于安全验证)*/
		paramMap.put("token", token);
		paramMap.put("timestamp", String.valueOf(timestamps));
		paramMap.put("signature", sn);
//		paramMap.put("origin_id", ksbId);
		
		/*把运单请求转发给达达rest api*/
		String dadaOrderId = requestCreateDADAOrder(paramMap);
//		System.out.println("达达快递ID========"+dadaOrderId);
		
		/*把达达快递的订单号更新到快递宝数据库中*/
		
		return dadaOrderId;
	}
	
	/**
	 * 获取达达配送员信息
	 * @param ksbOrderId
	 * @param dmId
	 */
	public void getDADADMInfo(String ksbOrderId,String dmId){
		
		
		
		
	}
	
	
	/**
	 * 获取达达订单状态
	 * @param ksbWayBillId
	 * @return
	 * @throws Exception 
	 */
	public String getDADAOrderStatus(String ksbWayBillId) throws Exception{
		
		String token = getDADAToken();
		
		long timestamps = new Date().getTime();
		
		String sn = getSignatureByToken(token, String.valueOf(timestamps));
		
		StringBuffer dadaApiRequestUrl = new StringBuffer(DADA_RESTAPI_HOST+"/v1_0/getOrderInfo/?");
		
		dadaApiRequestUrl.append("token="+token);
		dadaApiRequestUrl.append("&timestamp="+timestamps);
		dadaApiRequestUrl.append("&signature="+sn);
		dadaApiRequestUrl.append("&order_id="+ksbWayBillId);
		
		String dadaApiRs = HTTPUtils.executeGet(dadaApiRequestUrl.toString()).getObj().toString();
		
		if(dadaApiRs==null){
			System.out.println("达达open api请求失败");
			return null;
		}
		
		
		JSONObject jsonObj = null;
		try{
			jsonObj = JSON.parseObject(dadaApiRs);
		}catch(Exception e){
			System.out.println("达达open api 获取订单状态接口返回的数据格式异常 "+e.getMessage());
			return null;
		}
		
		if(jsonObj.get("status")==null){
			System.out.println("达达open api获取订单状态接口返回的数据格式异常 "+jsonObj.toJSONString());
			return null;
		}
		
		String status = jsonObj.getString("status");
		if(status.equals("fail")){
			System.out.println("请求达达open api获取订单状态接口失败，达达返回的错误码["+jsonObj.getString("errorCode")+"]");
			return null;
		}
		
		
		
//		String returnRs = jsonObj.getJSONObject("result").get("status_code").toString()+";"+jsonObj.getJSONObject("result").get("status").toString();
		String returnRs = jsonObj.getJSONObject("result").get("status_code").toString();
		
		return returnRs;
	}
	
	
	
	
	/**
	 * 运单转给达达处理
	 * @param paramMap
	 * @return
	 */
	public String requestCreateDADAOrder(Map<String, String> paramMap){
		
		
		StringBuffer requestUrl = new StringBuffer(DADA_RESTAPI_HOST+"/v1_0/addOrder/");
		
		String dadaApiRs = null;
		try {
			dadaApiRs = HTTPUtils.executePost(requestUrl.toString(), paramMap).getObj().toString();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		JSONObject jsonObj = null;
		
		try{
			jsonObj = JSON.parseObject(dadaApiRs);
		}catch(Exception e){
			System.out.println("达达rest api请求异常: "+e.getMessage());
			return null;
		}
		
		if(jsonObj.get("status")==null){
			System.out.println("请求达达rest api创建订单接口返回的结果格式异常");
			return null;
		}
		
		String status = jsonObj.get("status").toString();
		if(status.equals("fail")){
			System.out.println("达达rest api请求失败,错误码["+jsonObj.get("errorCode")+"]");
			return null;
		}
		
		return jsonObj.get("orderid").toString();
		
	}
	
	/**
	 * 运单数据入快送宝数据库
	 * @param pm
	 * @return
	 */
	private String waybill2ksbDB(Map<String, String> pm){
		
		long randomNum = Math.round(Math.random()*8999+1000);
		return "ksb-"+randomNum;
	}
	
	
	public void getTokenInfo(){
		String token = getDADAToken();
		
		long timestamps = new Date().getTime();
		
		String sn = getSignatureByToken(token, String.valueOf(timestamps));
		
		System.out.println("token===>"+token);
		System.out.println("timestamps===>"+timestamps);
		System.out.println("sn===>"+sn);
		
	}
	
	
	public void demo(){
		
		/*创建达达api订单*/
		
		
		/*查看达达订单状态*/
		
	}
	
	
	
	public static void main(String[] args) {
		
		DaDaOpenAPI ddApi = new DaDaOpenAPI();
		
//		System.out.println(ddApi.getßGrantCode());
		
//		System.out.println(ddApi.getDADAToken());
		
		ddApi.getTokenInfo();
		
	}
	
	
}
