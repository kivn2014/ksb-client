package com.ksb.web.openapi.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.SystemPropertyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ksb.openapi.em.ProductType;
import com.ksb.openapi.entity.EnterpriseEntity;
import com.ksb.openapi.entity.ProductVersionEntity;
import com.ksb.openapi.entity.ResultEntity;
import com.ksb.openapi.entity.ShipperEntity;
import com.ksb.openapi.error.BaseSupportException;
import com.ksb.openapi.mobile.service.ShipperService;
import com.ksb.openapi.service.ProductVersionService;
import com.ksb.openapi.service.WaybillService;
import com.ksb.openapi.util.HTTPUtils;
import com.ksb.web.openapi.entity.AjaxEntity;
import com.ksb.web.openapi.entity.ResultPageEntity;

@Controller
@RequestMapping("/api")
public class OpenApiController {

	private Logger log = LogManager.getLogger(getClass());
	
	/*运单服务管理接口*/
	@Autowired
	WaybillService waybillService;
	
	@Autowired
	ProductVersionService productVersionService;
	
	@Autowired
	ShipperService shipperService;
	
	@Value("#{sys['apk_file_path']}")
	private String apkFilePath = null;// apk文件存放路径
	
	@Value("#{sys['shipper_app_name']}")
	private String spFileName = null;// 商家版apk文件名
	
	@Value("#{sys['courier_app_name']}")
	private String courierFileName = null;// 配送员版apk文件名	
	
	@Value("#{sys['shipper_default_ent']}")
	private String shipperDefaultEnt = null;
	
	
	@RequestMapping("/login")
	public String login() throws Exception {
		return "/login";
	}

	
	@RequestMapping("/main")
	public String actionLogin() throws Exception {

		return "main";
	}

	@RequestMapping("/address")
	public String validateAddress() throws Exception {

		return "address-v";
	}

	@RequestMapping("/add_delivery_ent")
	public @ResponseBody 
	       ResultEntity createEnterprise(EnterpriseEntity enterprise){
		
		ResultEntity rs = new ResultEntity();
		rs.setErrors("ER");
		if(enterprise==null){
			rs.setObj("参数为空");
			return rs;
		}
		if(StringUtils.isBlank(enterprise.getCity())){
			rs.setObj("城市为空");
			return rs;
		}
		if(StringUtils.isBlank(enterprise.getName())){
			rs.setObj("公司名称为空");
			return rs;
		}		
		if(StringUtils.isBlank(enterprise.getContact())){
			rs.setObj("联系人为空");
			return rs;
		}
		if(StringUtils.isBlank(enterprise.getTel())){
			rs.setObj("联系电话为空");
			return rs;
		}
		/*新创建的企业，默认状态是待审(不能接受新单)*/
		enterprise.setStatus("2");
		try{
			shipperService.createEnterprise(enterprise);
		}catch(Exception e){
			rs.setObj(e.getMessage());
			return rs;
		}
		
		rs.setErrors("OK");
		rs.setObj("");
		rs.setSuccess(true);
		return rs;
	}
	
	@RequestMapping("/add_shipper")
	public @ResponseBody 
	       ResultEntity createShipper(ShipperEntity enterprise){
		
		ResultEntity rs = new ResultEntity();
		rs.setErrors("ER");
		if(enterprise==null){
			rs.setObj("参数为空");
			return rs;
		}
		if(StringUtils.isBlank(enterprise.getCity())){
			rs.setObj("城市为空");
			return rs;
		}
		if(StringUtils.isBlank(enterprise.getName())){
			rs.setObj("名称为空");
			return rs;
		}		
		if(StringUtils.isBlank(enterprise.getContact())){
			rs.setObj("联系人为空");
			return rs;
		}
		if(StringUtils.isBlank(enterprise.getTel())){
			rs.setObj("联系电话为空");
			return rs;
		}
		/*新创建的商家，默认状态是待审(不能使用商家版APP)*/
		enterprise.setStatus("2");		
		try{
			shipperService.createShipper(enterprise, shipperDefaultEnt);
		}catch(Exception e){
			rs.setObj(e.getMessage());
			return rs;
		}
		
		rs.setErrors("OK");
		rs.setObj("");
		rs.setSuccess(true);
		return rs;
	}	
	
	
	/**
	 * 获取商家版APP最新版本信息
	 * @return
	 */
	@RequestMapping("/sp_version")
	public @ResponseBody 
	       ResultEntity getShipperAppCurVersion(){
		
		ResultEntity rs = new ResultEntity();
		ProductVersionEntity entity = new ProductVersionEntity();
		try{
			entity = productVersionService.queryLatestVersion(ProductType.SP.getName());
		}catch(Exception e){
			rs.setObj(e.getMessage());
			rs.setErrors("ER");
			return rs;
		}
		
		rs.setSuccess(true);
		rs.setObj(entity);
		rs.setErrors("OK");
		return rs;
	}
	
	/**
	 * 获取配送员版APP最新版本信息
	 * @return
	 */
	@RequestMapping("/courier_version")
	public @ResponseBody 
	       ResultEntity getCourierAppCurVersion(){
		
		ResultEntity rs = new ResultEntity();
		ProductVersionEntity entity = new ProductVersionEntity();
		try{
			entity = productVersionService.queryLatestVersion(ProductType.COURIER.getName());
		}catch(Exception e){
			rs.setObj(e.getMessage());
			rs.setErrors("ER");
			return rs;
		}
		
		rs.setSuccess(true);
		rs.setObj(entity);
		rs.setErrors("OK");
		return rs;
	}
	
    @RequestMapping("/download/shippers_app")
    public ModelAndView downloadSpApp(HttpServletRequest request, HttpServletResponse response,String v){
       

		java.io.BufferedInputStream bis = null;
		java.io.BufferedOutputStream bos = null;
		String apkFile = apkFilePath + "shippers_app" + File.separator + v+ File.separator + spFileName;

		try {
			response.setContentType("text/html;charset=utf-8");
			request.setCharacterEncoding("UTF-8");
			
			long fileLength = new File(apkFile).length();
			response.setContentType("application/x-download;");
			response.setHeader("Content-disposition", "attachment; filename="+ spFileName);
			response.setHeader("Content-Length", String.valueOf(fileLength));
			bis = new BufferedInputStream(new FileInputStream(apkFile));
			bos = new BufferedOutputStream(response.getOutputStream());
			byte[] buff = new byte[2048];
			int bytesRead;
			while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
				bos.write(buff, 0, bytesRead);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try{
				if (bis != null)
					bis.close();
				if (bos != null)
					bos.close();
			}catch(Exception e){}
		}

        return null;
    }
    
    @RequestMapping("/download/courier_app")
    public ResponseEntity<byte[]> downloadCourierApp(HttpServletRequest request, HttpServletResponse response,String v) throws IOException {
    	
    	 String apkFile = apkFilePath+"courier_app"+File.separator+v+File.separator+courierFileName;
    	 
 		java.io.BufferedInputStream bis = null;
 		java.io.BufferedOutputStream bos = null;

 		try {
 			response.setContentType("text/html;charset=utf-8");
 			request.setCharacterEncoding("UTF-8");
 			
 			long fileLength = new File(apkFile).length();
 			response.setContentType("application/x-download;");
 			response.setHeader("Content-disposition", "attachment; filename="+ courierFileName);
 			response.setHeader("Content-Length", String.valueOf(fileLength));
 			bis = new BufferedInputStream(new FileInputStream(apkFile));
 			bos = new BufferedOutputStream(response.getOutputStream());
 			byte[] buff = new byte[2048];
 			int bytesRead;
 			while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
 				bos.write(buff, 0, bytesRead);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try{
 				if (bis != null)
 					bis.close();
 				if (bos != null)
 					bos.close();
 			}catch(Exception e){}
 		}

         return null;
    } 
    
	
	/**
	 * 百度地理编码服务地址验证
	 * @param address
	 * @return
	 */
	@RequestMapping("/bd_address")
	public @ResponseBody String validateAddressByDBMap(String address) {

		String addressEncode = java.net.URLEncoder.encode(address);
		String bdUrl = "http://api.map.baidu.com/place/v2/suggestion?region=131&output=json&ak=zNC2uIzYGKnY3V8D7iCBbLsi&query="
				+ addressEncode;

		String returnRs = null;
		try{
		returnRs = HTTPUtils.executeGet(bdUrl).getObj().toString();
		}catch(Exception e){
			
		}
		return returnRs;
	}

	/**
	 * 基于快送宝提交运单 web页面快速提交运单
	 * @return
	 */
	@RequestMapping("/add_waybill")
	public String addWaybillPage(){
		
		return "add_waybill";
	}
	
	/**
	 * 基于快送宝 web页面快速提交运单(快速运单入库)
	 * @param request
	 * @return
	 */
	@RequestMapping("/do_add_waybill")
	public @ResponseBody
	       AjaxEntity addWaybillByWeb(HttpServletRequest request){
	
		AjaxEntity rsEntity = new AjaxEntity();
		rsEntity.setSuccess(true);
		
		Map<String, String[]> requestMap = request.getParameterMap();
		
		try {
			waybillService.addWaybillByWeb(converRequestMap(requestMap));
		} catch (Exception e) {
			rsEntity.setErrors(e.getMessage());
			rsEntity.setSuccess(false);
			return rsEntity;
		}
		
		rsEntity.setErrors("OK");
		return rsEntity;
	}

	/**
	 * 对外提供web api，支持批量运单入库
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/batch_add_waybill",method = RequestMethod.POST)
	public @ResponseBody
	       AjaxEntity addWaybillByApi(HttpServletRequest request){
	
		AjaxEntity rsEntity = new AjaxEntity();
		rsEntity.setSuccess(true);
		
		Map<String, String[]> requestMap = request.getParameterMap();
		
		
		try {
			waybillService.addWaybillByOpenApi(converRequestMap(requestMap));
		} catch (Exception e) {
			rsEntity.setErrors(e.getMessage());
			rsEntity.setSuccess(false);
			return rsEntity;
		}
		
		rsEntity.setErrors("ok");
		return rsEntity;
	}
	
	/**
	 * request的getParameterMap转换为普通map
	 * @param requestMap
	 * @return
	 */
	private Map<String, String> converRequestMap(Map<String, String[]> requestMap){
		
		Map<String, String> rsMap = new HashMap<String, String>();
		
		if(requestMap==null||requestMap.size()==0){
			return rsMap;
		}
		
		Iterator<Entry<String, String[]>> it = requestMap.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, String[]> entry = it.next();
			rsMap.put(entry.getKey(), entry.getValue()[0]);
		}
		
		return rsMap;
	}
	
	/**
	 * 运单分配页面
	 * @return
	 */
	@RequestMapping("/fp_waybill")
	public String allocationWaybillPage(){
		
		return "fp_waybill";
	}
	
	/**
	 * 为配送员分配运单
	 * @param waybillId
	 * @param courierId
	 * @return
	 */
	@RequestMapping("/do_fp_waybill")
	public @ResponseBody 
	       AjaxEntity allocationWaybill2courier(String waybillId,String courierId ){
		
		AjaxEntity rsEntity = new AjaxEntity();
		rsEntity.setSuccess(false);
		
		if(StringUtils.isBlank(waybillId)){
			rsEntity.setErrors("参数: 运单编号为空");
			return rsEntity;
		}
		if(StringUtils.isBlank(courierId)){
			rsEntity.setErrors("参数: 配送员编号为空");
			return rsEntity;
		}		
		
		try{
			waybillService.allocationWaybill2Courier(waybillId, courierId);
		}catch(Exception e){
			rsEntity.setErrors(e.getMessage());
			return rsEntity;
		}
		
		rsEntity.setSuccess(true);
		rsEntity.setErrors("ok");
		return rsEntity;
	}
	
	
	/**
	 * 运单数据查询(暂时 仅支持根据运单ID和运单状态查询；后期需要扩展)
	 * @param waybillStatus
	 * @param waybillId
	 * @return
	 */
	@RequestMapping("/query_waybill")
	public @ResponseBody
	       ResultPageEntity searchWaybillByCourier(String waybillStatus,String waybillId){
		
		log.trace(waybillStatus,waybillId);
		
		ResultPageEntity rsEntity = new ResultPageEntity();
		
		/*封装查询入参*/
		Map<String, String> pm = new HashMap<String, String>();
		pm.put("waybill_status", waybillStatus);
		pm.put("waybill_id", waybillId);
		
		
		Map<String, Object> rsMap = null;
		try{
		    rsMap = waybillService.searchWaybillInfo(pm, 0, 50);
		}catch(Exception e){
			rsEntity.setSuccess(false);
			log.error("运单查询时出现异常:",e);
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
	
	/**
	 * 接入快送宝openapi的商户查询
	 * @param id
	 * @param orderId
	 * @param waybillStatus
	 * @param buyerName
	 * @param buyerPhone
	 * @return
	 */
	@RequestMapping("/search_sp")
	public @ResponseBody ResultPageEntity searchShipperOrderInfo(Long id,
			String orderId,String waybillStatus, String buyerName, String buyerPhone) {

		/*组装返回的数据*/
		ResultPageEntity rsEntity = new ResultPageEntity();
		rsEntity.setLimit(0);
		rsEntity.setStart(0);
		rsEntity.setSuccess(true);
		rsEntity.setTotalCount(0);
		
		Map<String, Object> rsMap = null;
		try{
		    rsMap = waybillService.searchShipperOrderInfo(id, orderId, waybillStatus, buyerName, buyerPhone);
		    if(rsMap==null){
				log.error("web调用查询商家订单信息接口异常");
				rsEntity.setSuccess(false);
				return log.exit(rsEntity);
		    }
		}catch(Exception e){
			log.error("web调用查询商家订单信息接口异常",e);
			rsEntity.setSuccess(false);
		}
		
		List<Map<String, Object>>rsList = (List<Map<String, Object>>)rsMap.get("1");
		rsEntity.setObj(rsList);
		
		return log.exit(rsEntity);
	}

}
