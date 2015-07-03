package com.ksb.web.openapi.mobile.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ksb.openapi.entity.ResultEntity;
import com.ksb.openapi.entity.ShipperEntity;
import com.ksb.openapi.entity.ShipperUserEntity;
import com.ksb.openapi.entity.WayBillEntity;
import com.ksb.openapi.mobile.service.CourierService;
import com.ksb.openapi.mobile.service.EretailerService;
import com.ksb.openapi.mobile.service.MobileWaybillService;
import com.ksb.openapi.mobile.service.O2oWaybillService;
import com.ksb.openapi.mobile.service.ShipperService;
import com.ksb.openapi.service.WaybillService;
import com.ksb.openapi.util.DateUtil;
import com.ksb.web.openapi.entity.ResultPageEntity;

@Controller
@RequestMapping("/mobile")
public class AppApiController {

	private Logger log = LogManager.getLogger(getClass());
	private static final int defaultSize = 10;
	/*配送员管理*/
	@Autowired
	CourierService courierService;
	
	/*电商运单管理*/
	@Autowired
	EretailerService eretailerService;
	
	/*商家管理*/
	@Autowired
	ShipperService shipperService;
	
	/*O2O运单管理*/
	@Autowired
	O2oWaybillService o2oWaybillService;
	
	/**
	 * 手机用户登录
	 * @param request
	 * @return
	 */
	@RequestMapping("/login")
	public @ResponseBody 
	       ResultEntity appLogin(HttpServletRequest request){
		
		String userName = request.getParameter("name");
		String passwd = request.getParameter("password");
		//String eid = request.getParameter("eid");
//		if(eid==null){
//			eid = defaultEnterpriseId;
//		}
		log.entry(userName,passwd);
		ResultEntity rsEntity = new ResultEntity();
		if(StringUtils.isBlank(userName)){
			log.error("用户名为空");
			rsEntity.setErrors("用户名为空");
			return rsEntity;
		}
		
		if(StringUtils.isBlank(passwd)){
			log.error("用户名为空");
			rsEntity.setErrors("密码为空");
			return rsEntity;
		}
		
		try{
		    rsEntity = courierService.authen(userName, passwd,null);
		}catch(Exception e){
			rsEntity.setSuccess(false);
			rsEntity.setErrors(e.getMessage());
			return log.exit(rsEntity);
		}
		return log.exit(rsEntity);
	}
	
	/**
	 * 手机用户修改密码
	 * @param request
	 * @return
	 */
	@RequestMapping("/md_pwd")
	public @ResponseBody 
	       ResultEntity modifyPassword(HttpServletRequest request){
		
		ResultEntity rsEntity = new ResultEntity();
		String id = request.getParameter("id");
		String newpwd = request.getParameter("np");
		String userName = request.getParameter("un");
		String oldpwd = request.getParameter("op");
		log.entry(newpwd,oldpwd,userName);
		
		if(StringUtils.isBlank(oldpwd)){
			rsEntity.setErrors("原始密码为空");
			return rsEntity;
		}
		
		if(StringUtils.isBlank(newpwd)){
			rsEntity.setErrors("新密码为空");
			return rsEntity;
		}
		
		
		if(StringUtils.isBlank(userName)){
			rsEntity.setErrors("用户名为空");
			return rsEntity;
		}
		
		try{
		 rsEntity = courierService.updateCourierPasswd(null, userName, oldpwd, newpwd);
		}catch(Exception e){
			rsEntity.setSuccess(false);
			rsEntity.setErrors(e.getMessage());
			return log.exit(rsEntity);
		}
		return log.exit(rsEntity);
	} 
	
	/**
	 * 开工、收工（更改work_status、经纬度字段）
	 * @param cid
	 * @return
	 */
	@RequestMapping("/work_status")
	public @ResponseBody
	       ResultEntity courierWorkStatus(String cid,String t,String x,String y){
		log.entry(cid,t);
		ResultEntity rsEntity = new ResultEntity();
		if(StringUtils.isBlank(cid)){
			rsEntity.setErrors("ER");
			rsEntity.setObj("配送员编号为空");
			return rsEntity;
		}
		
		if(StringUtils.isBlank(t)){
			rsEntity.setErrors("ER");
			rsEntity.setObj("未指定操作类型");
			return rsEntity;
		}
		if(!t.equals("1")){
			if(!t.equals("2")){
				rsEntity.setErrors("ER");
				rsEntity.setObj("非法操作类型");
				return rsEntity;
			}
		}
		
		try{
		    courierService.updateCourierWorkStatus(cid, t,x,y);
		}catch(Exception e){
			rsEntity.setErrors("ER");
			rsEntity.setObj(e.getMessage());
			return log.exit(rsEntity);
		}
		rsEntity.setSuccess(true);
		rsEntity.setErrors("OK");
		rsEntity.setObj("");
		return log.exit(rsEntity);
	}
	
	/**
	 * 快递员配送统计(默认是当天配送)
	 * @param cid
	 * @return
	 */
	@RequestMapping("/delivery_count")
	public @ResponseBody
	       ResultEntity distributionStatistics(String cid){
	   	log.entry(cid);
		ResultEntity rsEntity = new ResultEntity();
		if(StringUtils.isBlank(cid)){
			rsEntity.setErrors("ER");
			rsEntity.setObj("配送员编号为空");
			return log.exit(rsEntity);
		}
		
		Map<String, String> rsMap = new HashMap<String, String>();
		try{
			rsMap = o2oWaybillService.courierPaymentStat(cid);
		}catch(Exception e){
			rsEntity.setErrors("ER");
			rsEntity.setObj(e.getMessage());
			return log.exit(rsEntity);
		}
		
		rsEntity.setSuccess(true);
		rsEntity.setErrors("OK");
		rsEntity.setObj(rsMap);
		return log.exit(rsEntity);
	}
	
	/**
	 * 运单编号识别
	 * @param id
	 * @return
	 */
	@RequestMapping("/scan_identify")
	public @ResponseBody
	       ResultEntity scanIdentify(String id){
		log.entry(id);
		ResultEntity rsEntity = new ResultEntity();
		
		if(StringUtils.isBlank(id)){
			rsEntity.setErrors("ER");
			rsEntity.setObj("运单编号为空");
			return log.exit(rsEntity);
		}
		
		if(id.startsWith("ksb_")){
			rsEntity.setErrors("ER");
			rsEntity.setObj("unidentify");
			return log.exit(rsEntity);
		}
		
		Map<String, String> rsMap = new HashMap<String, String>();
		rsMap.put("id", id);
		rsMap.put("sp_id", "1");
		rsMap.put("name", "天猫超市");
		rsEntity.setSuccess(true);
		rsEntity.setErrors("OK");
		rsEntity.setObj(rsMap);
		return log.exit(rsEntity);
	}
	
	/**
	 * 扫描运单
	 * @param id
	 * @param cid
	 * @return
	 */
	@RequestMapping("/scan_order")
	public @ResponseBody
	        ResultEntity scanWaybill(String id,String cid){
		log.entry(id,cid);
		ResultEntity rsEntity = new ResultEntity();
		
		if(StringUtils.isBlank(id)){
			rsEntity.setObj("运单编号为空");
			rsEntity.setErrors("ER");
			return log.exit(rsEntity);
		}
		if(StringUtils.isBlank(cid)){
			rsEntity.setObj("配送员编号为空");
			rsEntity.setErrors("ER");
			return log.exit(rsEntity);
		}
		
		WayBillEntity wbentity = null;
		try{
			wbentity = eretailerService.scanOlWaybill(cid, id);
		}catch(Exception e){
			rsEntity.setErrors("ER");
			rsEntity.setObj(e.getMessage());
			return log.exit(rsEntity);
		}
		
		/*没找到相关记录，可以insert*/
		if(wbentity==null){
			rsEntity.setSuccess(true);
			rsEntity.setErrors("OK");
			Map<String, String> rsMap = new HashMap<String, String>();
		    rsMap.put("op","INSERT");
			
			rsEntity.setObj(rsMap);
			return log.exit(rsEntity);
		}

		/*封装需要返回的结果*/
		Map<String, String> rsMap = new HashMap<String, String>();
		rsMap.put("is_topay", wbentity.getIs_topay());
		rsMap.put("cargo_price", wbentity.getCargo_price());
		rsMap.put("remarks", wbentity.getRemarks());
		rsMap.put("status", wbentity.getWaybill_status());
		rsMap.put("op", "VIEW");
		rsMap.put("shipper_origin_id", wbentity.getShipper_origin_id());
		rsMap.put("id", wbentity.getId());
		rsEntity.setErrors("OK");
		rsEntity.setSuccess(true);
		rsEntity.setObj(rsMap);
		
		return log.exit(rsEntity);
	}
	
	/**
	 * 电商运单入库
	 * @param request
	 * @return
	 */
	@RequestMapping("/save_order")
	public @ResponseBody
	        ResultEntity olwaybill2DB(HttpServletRequest request){
		
		ResultEntity rsEntity = new ResultEntity();
		
		Map<String, String> saveMap = new HashMap<String, String>();
		String cid = request.getParameter("cid");
		if(StringUtils.isBlank(cid)){
			rsEntity.setErrors("ER");
			rsEntity.setObj("配送员ID为空");
			return rsEntity;
		}
		saveMap.put("cid", cid);
		String oid = request.getParameter("oid");
		if(StringUtils.isBlank(oid)){
			rsEntity.setErrors("ER");
			rsEntity.setObj("电商运单号");
			return rsEntity;
		}
		saveMap.put("oid", oid);
		String remarks = request.getParameter("remarks");
		
		saveMap.put("remarks", remarks);
		String isTopay = request.getParameter("is_topay");
		if(StringUtils.isBlank(isTopay)){
			rsEntity.setErrors("ER");
			rsEntity.setObj("是否到付为空");
			return rsEntity;
		}
		saveMap.put("is_topay", isTopay);
		String fee = request.getParameter("fee");
		if(isTopay.equals("1")){
			if(StringUtils.isBlank(fee)){
				rsEntity.setErrors("ER");
				rsEntity.setObj("到付的金额为空");
				return rsEntity;
			}
		}
		String shipperId = request.getParameter("sp_id");
		if(StringUtils.isBlank(shipperId)){
			rsEntity.setErrors("ER");
			rsEntity.setObj("未指定电商信息");
			return rsEntity;
		}
		
		saveMap.put("sp_id", shipperId);
		saveMap.put("fee", fee==null?"0.0":fee);
		log.entry(saveMap);
		try{
		    eretailerService.createOlWaybill(saveMap);
		}catch(Exception e){
			rsEntity.setErrors("ER");
			rsEntity.setObj(e.getMessage());
			return log.exit(rsEntity);
		}
		
		rsEntity.setSuccess(true);
		rsEntity.setObj("");
		rsEntity.setErrors("OK");
		return log.exit(rsEntity);
	}
	
	/**
	 * 电商运单入库
	 * @param request
	 * @return
	 */
	@RequestMapping("/sp_save_order")
	public @ResponseBody
	        ResultEntity shipperCreatewaybill(HttpServletRequest request){
		
		ResultEntity rsEntity = new ResultEntity();
		
		Map<String, String> saveMap = new HashMap<String, String>();
		
		String remarks = request.getParameter("remarks");
		
		saveMap.put("remarks", remarks);
		
		String shipperId = request.getParameter("sp_id");
		if(StringUtils.isBlank(shipperId)){
			rsEntity.setErrors("ER");
			rsEntity.setObj("商家编号为空");
			return rsEntity;
		}
		
		saveMap.put("sp_id", shipperId);
		log.entry(saveMap);
		try{
		    shipperService.createWaybill(saveMap);
		}catch(Exception e){
			rsEntity.setErrors("ER");
			rsEntity.setObj(e.getMessage());
			return log.exit(rsEntity);
		}
		
		rsEntity.setSuccess(true);
		rsEntity.setErrors("OK");
		rsEntity.setObj("");
		return log.exit(rsEntity);
	}	
	
	
	/**
	 * 快递员
	 * @param courierId
	 * @return
	 */
	@RequestMapping("/today_count")
	public @ResponseBody
	       ResultEntity courierCurrentDayWaybillCount(String cid,String t){
		log.entry(cid,t);
		ResultEntity rsEntity = new ResultEntity();
		if(StringUtils.isBlank(cid)){
			rsEntity.setErrors("ER");
			rsEntity.setObj("快递员ID为空");
			return log.exit(rsEntity);
		}
		
		if(StringUtils.isBlank(t)){
			t = "1";
		}
		
		
		List<Map<String, String>> rsList = new ArrayList<Map<String,String>>();
		try{
			if(t.equals("1")){
				rsList = eretailerService.currentDayWayBillStatistic(cid);
			}else if(t.equals("2")){
				rsList = o2oWaybillService.currentDayWayBillStatistic(cid);
			}else{
				rsEntity.setErrors("ER");
				rsEntity.setObj("操作参数t["+t+"]，无法识别");
				return log.exit(rsEntity);
			}
			
		}catch(Exception e){
			rsEntity.setErrors("ER");
			rsEntity.setObj(e.getMessage());
			return log.exit(rsEntity);
		}
		
		rsEntity.setSuccess(true);
		rsEntity.setObj(rsList);
		rsEntity.setErrors("OK");
		return log.exit(rsEntity);
	}

	/**
	 * 商家版app 列表title，每个状态的运单数量
	 * @param sp_id
	 * @return
	 */
	@RequestMapping("/sp_today_count")
	public @ResponseBody
	       ResultEntity shipperCurrentDayWaybillCount(String sp_id){
		log.entry(sp_id);
		ResultEntity rsEntity = new ResultEntity();
		if(StringUtils.isBlank(sp_id)){
			rsEntity.setErrors("ER");
			rsEntity.setObj("电商编号为空");
			return log.exit(rsEntity);
		}
		
		List<Map<String, String>> rsList = new ArrayList<Map<String,String>>();
		try{
			
			rsList = shipperService.currentDayShipperWayBillStatistic(sp_id);
			
		}catch(Exception e){
			rsEntity.setErrors("ER");
			rsEntity.setObj(e.getMessage());
			return log.exit(rsEntity);
		}
		
		rsEntity.setSuccess(true);
		rsEntity.setObj(rsList);
		rsEntity.setErrors("OK");
		return log.exit(rsEntity);
	}	
	
	@RequestMapping("/olsp_list")
	public @ResponseBody
	        ResultEntity olShipperList(){
		
		ResultEntity rsEntity = new ResultEntity();
		List<ShipperEntity> list = null;
		try{
			Map<String, String> map = new HashMap<String, String>();
			map.put("t", "1");
			list = shipperService.queryShipperList(map);
		}catch(Exception e){
			rsEntity.setErrors("ER");
			rsEntity.setObj(e.getMessage());
			return log.exit(rsEntity);
		}
		
		rsEntity.setSuccess(true);
		rsEntity.setErrors("OK");
		rsEntity.setObj(list);
		return log.exit(rsEntity);
	}
	
	@RequestMapping("/query_courier_waybill")
	public @ResponseBody
	       ResultPageEntity searchWaybillByCourier(HttpServletRequest request){
		
		String cid = request.getParameter("cid");
		if(StringUtils.isBlank(cid)){
			return getDefaultErrorEntity("快递员ID为空");
		}
		String page = request.getParameter("page");
		if(StringUtils.isBlank(page)){
//			return getDefaultErrorEntity("页码为空");
			page = "1";
		}
		String status = request.getParameter("status");
		if(StringUtils.isBlank(status)){
			return getDefaultErrorEntity("状态为空");
		}
		
		/*每页显示的记录数(不提供 默认10条)*/
		String size = request.getParameter("size");
		int sizeInt = defaultSize;
		if(StringUtils.isNotBlank(size)){
			sizeInt = Integer.parseInt(size);
		}
		String opType = request.getParameter("t");
		if(StringUtils.isBlank(opType)){
			opType = "1";
		}
		
		ResultPageEntity rsEntity = new ResultPageEntity();
		
		Map<String, Object> rsMap = null;
		
		int pageInt = Integer.parseInt(page);
		if(pageInt==0){
			
		}
		int skip = (pageInt-1)*sizeInt;
		
		log.entry(cid,pageInt,status,sizeInt,skip,opType);
		
		try{
			if(opType.equals("1")){
				/*电商*/
				rsMap = eretailerService.queryWaybillByCourier(cid, null, status, skip, sizeInt);
				log.entry(rsMap);
			}else if(opType.equals("2")){
				/*O2O*/
				rsMap = o2oWaybillService.queryWaybillByCourier(cid, null, status, skip, sizeInt);
				log.entry(rsMap);
			}else{
				log.error("参数t["+opType+"]未知的操作类型");
				return log.exit(rsEntity);
			}
		    
		}catch(Exception e){
			log.error("快递员["+cid+"] 查询状态为["+status+"] 时出现异常:",e);
			return log.exit(rsEntity);
		}
		
		List<Map<String, String>> rsList = (List<Map<String, String>>)rsMap.get("1");
		Object count = rsMap.get("2");
		rsEntity.setObj(rsList);
		
		rsEntity.setPage(pageInt);
		rsEntity.setSuccess(true);
		rsEntity.setTotalCount(Long.parseLong(count.toString()));
		rsEntity.setLimit(sizeInt);
		rsEntity.setStart(skip);
		rsEntity.setErrors("OK");
		return log.exit(rsEntity);
	}
	
	@RequestMapping("/query_sp_waybill")
	public @ResponseBody
	       ResultPageEntity searchWaybillByShipper(HttpServletRequest request){
		
		String spId = request.getParameter("sp_id");
		if(StringUtils.isBlank(spId)){
			return getDefaultErrorEntity("商家ID为空");
		}
		String page = request.getParameter("page");
		if(StringUtils.isBlank(page)){
			//return getDefaultErrorEntity("页码为空");
			page = "1";
		}
		String status = request.getParameter("status");
		if(StringUtils.isBlank(status)){
			return getDefaultErrorEntity("状态为空");
		}
		
		/*每页显示的记录数(不提供 默认10条)*/
		String size = request.getParameter("size");
		int sizeInt = defaultSize;
		if(StringUtils.isNotBlank(size)){
			sizeInt = Integer.parseInt(size);
		}
		
		ResultPageEntity rsEntity = new ResultPageEntity();
		
		Map<String, Object> rsMap = null;
		
		int pageInt = Integer.parseInt(page);
		if(pageInt==0){
			return getDefaultErrorEntity("页码从1开始");
		}
		int skip = (pageInt-1)*sizeInt;
		
		log.entry(spId,pageInt,status,sizeInt,skip);
		try{

			rsMap = shipperService.queryWaybillByShipper(spId, null, status, skip, sizeInt);
		}catch(Exception e){
			log.error("商家["+spId+"] 查询状态为["+status+"] 时出现异常:",e);
			return log.exit(rsEntity);
		}
		
		List<Map<String, String>> rsList = (List<Map<String, String>>)rsMap.get("1");
		Object count = rsMap.get("2");
		rsEntity.setObj(rsList);
		
		rsEntity.setPage(pageInt);
		rsEntity.setSuccess(true);
		rsEntity.setTotalCount(Long.parseLong(count.toString()));
		rsEntity.setLimit(sizeInt);
		rsEntity.setStart(skip);
		rsEntity.setErrors("OK");
		return log.exit(rsEntity);
	}
	
	@RequestMapping("/handle_waybill")
	public @ResponseBody
	       ResultEntity updateWaybillStatusByCourier(HttpServletRequest request){
		
		ResultEntity rs = new ResultEntity();
		
		Map<String, String> paraMap = new HashMap<String, String>();
		
		String id = request.getParameter("id");
		if(StringUtils.isBlank(id)){
			rs.setObj("运单ID为空");
			rs.setErrors("ER");
			return rs;
		}
		paraMap.put("id", id);
		
        String cid = request.getParameter("cid");
		if(StringUtils.isBlank(cid)){
			rs.setObj("配送员ID为空");
			rs.setErrors("ER");
			return rs;
		}
		paraMap.put("cid", cid);
        String status = request.getParameter("status");
		if(StringUtils.isBlank(status)){
			rs.setObj("运单状态为空");
			rs.setErrors("ER");
			return rs;
		}
		paraMap.put("status", status);
		String remarks = request.getParameter("remarks");
		paraMap.put("remarks", remarks);
		
		log.entry(paraMap);
		
		try{
			eretailerService.updateWaybillStatus(paraMap);
		}catch(Exception e){
			rs.setErrors("ER");
			rs.setObj(e.getMessage());
			return log.exit(rs);
		}
		
		rs.setSuccess(true);
		rs.setErrors("OK");
		rs.setObj("");
		return log.exit(rs);
	}
	
	@RequestMapping("/batch_handle_waybill")
	public @ResponseBody
	       ResultEntity batchUpdateWaybillStatusByCourier(HttpServletRequest request){
		
		ResultEntity rs = new ResultEntity();
		
		Map<String, String> paraMap = new HashMap<String, String>();
		
        String cid = request.getParameter("cid");
		if(StringUtils.isBlank(cid)){
			rs.setObj("配送员ID为空");
			rs.setErrors("ER");
			return rs;
		}
		paraMap.put("cid", cid);
		
        String handleList = request.getParameter("handle_list");
		if(StringUtils.isBlank(handleList)){
			rs.setObj("未提供任何运单信息");
			rs.setErrors("ER");
			return rs;
		}
		paraMap.put("handle_list", handleList);
		
		try{
			o2oWaybillService.batchFetchO2OWaybill(paraMap);
		}catch(Exception e){
			rs.setErrors("ER");
			rs.setObj(e.getMessage());
			return rs;
		}
		log.entry(paraMap);
		rs.setSuccess(true);
		rs.setErrors("OK");
		rs.setObj("");
		return rs;
	}
	
	@RequestMapping("/report_gps")
	public @ResponseBody
	        ResultEntity reportGps(String cid,String x,String y){
		
		System.out.println(DateUtil.getTimeNow(new Date())+"----"+cid+"----"+x+"----"+y);
		ResultEntity rs = new ResultEntity();
		
		rs.setSuccess(true);
		rs.setObj("");
		rs.setErrors("OK");
		return rs;
	}

	@RequestMapping("/sp_login")
	public @ResponseBody
	       ResultEntity shipperUserLogin(String un,String pwd){
		ResultEntity rs = new ResultEntity();
		
		
		if(StringUtils.isBlank(un)){
			rs.setErrors("ER");
			rs.setObj("用户名为空");
			return rs;
		}
		if(StringUtils.isBlank(pwd)){
			rs.setErrors("ER");
			rs.setObj("密码为空");
			return rs;
		}		
		
		try{
			rs = shipperService.authenSpUser(un, pwd);
		}catch(Exception e){
			rs.setErrors("ER");
			rs.setObj(e.getMessage());
			return rs;
		}
	
		return rs;
	}
	
	@RequestMapping("/sp_setaddress")
	public @ResponseBody
	       ResultEntity shipperSetAddress(ShipperUserEntity entity){
		
		ResultEntity rs = new ResultEntity();
		try{
			shipperService.updateShipperDefualtAddress(entity);;
		}catch(Exception e){
			rs.setErrors("ER");
			rs.setObj(e.getMessage());
			return rs;
		}
		rs.setSuccess(true);
		rs.setObj("");
		rs.setErrors("OK");
		return rs;
	}
	
	
	/**
	 * 商家版app修改登录密码
	 * @param request
	 * @return
	 */
	@RequestMapping("/sp_mdpwd")
	public @ResponseBody 
	       ResultEntity modifyShipperPassword(HttpServletRequest request){
		
		ResultEntity rsEntity = new ResultEntity();
//		String id = request.getParameter("id");
		String newpwd = request.getParameter("np");
		String userName = request.getParameter("un");
		String oldpwd = request.getParameter("op");
		log.entry(newpwd,oldpwd,userName);
		
		if(StringUtils.isBlank(oldpwd)){
			rsEntity.setErrors("原始密码为空");
			return rsEntity;
		}
		
		if(StringUtils.isBlank(newpwd)){
			rsEntity.setErrors("新密码为空");
			return rsEntity;
		}
		
		if(StringUtils.isBlank(userName)){
			rsEntity.setErrors("用户名为空");
			return rsEntity;
		}
		
		try{
		 rsEntity = shipperService.updateShipperUserPasswd(userName, oldpwd, newpwd);
		}catch(Exception e){
			rsEntity.setSuccess(false);
			rsEntity.setErrors(e.getMessage());
			return log.exit(rsEntity);
		}
		
		rsEntity.setSuccess(true);
		rsEntity.setObj("");
		rsEntity.setErrors("OK");
		
		return log.exit(rsEntity);
	} 
	
	
	
	
	
	@RequestMapping("/city_list")
	public @ResponseBody
	       ResultEntity cityList(){
		
		ResultEntity rsEntity = new ResultEntity();
		
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		
		
		Map<String, Object> map1 = new HashMap<String, Object>();
		
		/*广东市*/
		List<String> gd = new ArrayList<String>();
		gd.add("广州市");
		gd.add("东莞市");
		map1.put("provicen","广东省");
		map1.put("city", gd);
		
		list.add(map1);
		
		Map<String, Object> map2 = new HashMap<String, Object>();
		/*北京市*/
		List<String> bj = new ArrayList<String>();
		bj.add("北京市");
		map2.put("province", "北京市");
		map2.put("city", bj);
		list.add(map2);
		
		Map<String, Object> map3 = new HashMap<String, Object>();
		/*山东省*/
		List<String> sd = new ArrayList<String>();
		sd.add("济南市");
		map3.put("province", "山东省");
		map3.put("city", sd);
		list.add(map3);
		
		rsEntity.setSuccess(true);
		rsEntity.setErrors("");
		rsEntity.setObj(list);
		return log.exit(rsEntity);
	}
	
	
	public ResultPageEntity getDefaultErrorEntity(String errorInfo){
		ResultPageEntity pageEntity = new ResultPageEntity();
		pageEntity.setSuccess(false);
		pageEntity.setLimit(0);
		pageEntity.setStart(0);
		pageEntity.setTotalCount(0);
		pageEntity.setErrors(errorInfo);
		pageEntity.setPage(0);
		
		return pageEntity;
	}
	
	
}
