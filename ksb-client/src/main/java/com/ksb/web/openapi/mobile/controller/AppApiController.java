package com.ksb.web.openapi.mobile.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ksb.openapi.entity.BuyerEntity;
import com.ksb.openapi.entity.CourierEntity;
import com.ksb.openapi.entity.EnterpriseCityEntity;
import com.ksb.openapi.entity.ResultEntity;
import com.ksb.openapi.entity.ShipperAddressEntity;
import com.ksb.openapi.entity.ShipperEntity;
import com.ksb.openapi.entity.ShipperUserEntity;
import com.ksb.openapi.entity.ShipperWaybill;
import com.ksb.openapi.entity.WayBillEntity;
import com.ksb.openapi.error.BaseSupportException;
import com.ksb.openapi.mobile.service.CourierService;
import com.ksb.openapi.mobile.service.EretailerService;
import com.ksb.openapi.mobile.service.O2oWaybillService;
import com.ksb.openapi.mobile.service.RedisService;
import com.ksb.openapi.mobile.service.ShipperService;
import com.ksb.openapi.service.StatisticsService;
import com.ksb.openapi.util.DateUtil;
import com.ksb.openapi.util.ToolsUtil;
import com.ksb.web.openapi.entity.OrderSaveEntity;
import com.ksb.web.openapi.entity.ResultPageEntity;
import com.ksb.web.openapi.util.FileUploadUtil;
import com.ksb.web.openapi.util.UniqueIDHolder;

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
	
	@Autowired
	StatisticsService statisticsService;
	
	@Autowired
	RedisService redisService;
	
//	@Autowired 
//	private ServletContext servletContext;
	
	@Value("#{sys['shipper_default_ent']}")
	private String shipperDefaultEnt = null;
	
	@Value("#{sys['img_path']}")
	public String IMG_PATH = null;
	
	@Value("#{sys['img_suffix']}")
	public String IMAGE_SUFFIX = null;
	
	/**
	 * 配送员拍照上传 外卖小票
	 * @param req
	 * @param resp
	 * @return
	 */
	@RequestMapping("/upload_img")
	public @ResponseBody
	       ResultEntity uploadImg(HttpServletRequest req,HttpServletResponse resp){
		
		FileUploadUtil f = UniqueIDHolder.getFileUpload();
		//String path = servletContext.getRealPath(IMG_PATH);
		
		ResultEntity rsEntity = new ResultEntity();
		OrderSaveEntity osn = new OrderSaveEntity();
		try{
			/*上传的文件写入到磁盘中*/
			f.uploadImage(req, resp,IMG_PATH,IMAGE_SUFFIX,osn);
			
			/*校验文件的完整性*/
			if (StringUtils.isBlank(osn.getMd5_key())) {
				rsEntity.setErrors("图片MD5值为空");
				rsEntity.setObj("");
				return rsEntity;
			}
			if (!f.checkImageMD5(osn.getImg_id(), osn.getMd5_key())) {
				rsEntity.setErrors("图片MD5验证失败");
				rsEntity.setObj("");
				return rsEntity;
			}			
			if(StringUtils.isBlank(osn.getCid())){
				rsEntity.setErrors("配送员编号为空");
				rsEntity.setObj("");
				return rsEntity;
			}
			
			if(StringUtils.isBlank(osn.getWb_id())){
				rsEntity.setErrors("订单编号为空");
				rsEntity.setObj("");
				return rsEntity;
			}
			
			/*应付商家物品费用*/
			if(StringUtils.isBlank(osn.getPay_sp_fee())){
				return getErrorEntity("应付商家费用为空");
			} 
			
			/*判断应付商家费用*/
			if(!ToolsUtil.isFloat(osn.getPay_sp_fee())){
				return getErrorEntity("应付商家费用非数字");
			}
			
			/*应收用户物品费*/
			if(StringUtils.isBlank(osn.getFetch_buyer_fee())){
				return getErrorEntity("应收客户费用为空");
			}
			
			/*判断应付商家费用*/
			if(!ToolsUtil.isFloat(osn.getFetch_buyer_fee())){
				return getErrorEntity("应收客户费用非数字");
			}

			/*应收金额必须大于应付给商家的金额*/
			double fetch_fee = Double.parseDouble(osn.getFetch_buyer_fee());
			double pay_fee = Double.parseDouble(osn.getPay_sp_fee());
			if(fetch_fee<pay_fee){
				return getErrorEntity("应收客户费用必须大于应付金额");
			}
			
			/*上传的文件路径和文件名写入到数据库中*/
			o2oWaybillService.saveWaybillImgId(osn.getCid(), osn.getWb_id(), osn.getImg_id(),osn.getPay_sp_fee(),osn.getFetch_buyer_fee());
			
		}catch(Exception be){
			rsEntity.setSuccess(false);
			rsEntity.setErrors(be.getMessage());
			rsEntity.setObj("");
			return rsEntity;
		}
		
		return rsEntity;
	}
	
	/**
	 * 配送员补录 外卖的买家用户信息并且生成配送里程
	 * @param buyer
	 * @param sp_x
	 * @param sp_y
	 * @param sp_id
	 * @return
	 */
	@RequestMapping("/makeup_buyinfo")
	public @ResponseBody
	        ResultEntity saveWaybillBuyInfo(BuyerEntity buyer,String sp_x,String sp_y,String sp_id){
		
		ResultEntity rsEntity = new ResultEntity();
		
		/*客户地址*/
		String address = buyer.getAddress();
		if(StringUtils.isBlank(address)){
			return getErrorEntity("地址为空");
		}
		
		/*客户地址经纬度*/
//		String x = buyer.getAddress_x();
//		String y = buyer.getAddress_y();
//		if(StringUtils.isBlank(x) || StringUtils.isBlank(y)){
//			return getErrorEntity("无效的经纬度");
//		}
		
		
		/*客户名称*/
		String name = buyer.getName();
		if(StringUtils.isBlank(name)){
			return getErrorEntity("用户名为空");
		}
		
		/*客户手机号*/
		String phone = buyer.getPhone();
		if(StringUtils.isBlank(phone)){
			return getErrorEntity("手机号为空");
		}
		
		/*订单编号*/
		String wbid = buyer.getWbid();
		if(StringUtils.isBlank(wbid)){
			return getErrorEntity("订单编号为空");
		}
		
		/*配送员编号*/
		String cid = buyer.getCid();
		if(StringUtils.isBlank(cid)){
			return getErrorEntity("配送员为空");
		}
		
		/*商家经纬度*/
		if(StringUtils.isBlank(sp_x) || StringUtils.isBlank(sp_y)){
			return getErrorEntity("商家位置为空");
		}
		
		if(StringUtils.isBlank(sp_id)){
			return getErrorEntity("商家编号为空");
		}
		
		try{
			double x = Double.parseDouble(sp_x);
			double y = Double.parseDouble(sp_y);
			o2oWaybillService.saveBuyerInfo(buyer, x, y,sp_id);
		}catch(Exception e){
			return getErrorEntity(e.getMessage());
		}
		
		rsEntity.setSuccess(true);
		rsEntity.setErrors("OK");
		rsEntity.setObj("");
		
		return rsEntity;
	}
	
	@RequestMapping("/test_page")
	public String testPage(){
		
		return "test";
	}
	
	
	/**
	 * 配送员APP用户登录
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
	 * 配送员APP用户修改密码
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
	 * 配送员APP，开工、收工（开工的同时上报经纬度信息）
	 * @param cid
	 * @return
	 */
	@RequestMapping("/work_status")
	public @ResponseBody
	       ResultEntity courierWorkStatus(String cid,String eid,String t,String x,String y){
		log.entry(cid,t);
		ResultEntity rsEntity = new ResultEntity();
		if(StringUtils.isBlank(cid)){
			rsEntity.setErrors("ER");
			rsEntity.setObj("配送员编号为空");
			return rsEntity;
		}
		if(StringUtils.isBlank(eid)){
			rsEntity.setErrors("ER");
			rsEntity.setObj("配送员隶属公司为空");
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
			/*如果经纬度不为空，则经纬度和状态一块更新*/
		    courierService.updateCourierWorkStatus(cid,eid, t,x,y);
		}catch(Exception e){
			rsEntity.setErrors("ER");
			rsEntity.setObj(e.getMessage());
			return log.exit(rsEntity);
		}
		
		/*异步把配送员开工收工状态更新到redis*/
		//new AsynWorkStatus(cid,t).run();
		
		/*传递了经纬度信息*/
		if(StringUtils.isNotBlank(x)&&StringUtils.isNotBlank(y)){
			/*改为异步*/
//		    new AsynRecordGps(cid, eid, x, y).run();
		}
		
		rsEntity.setSuccess(true);
		rsEntity.setErrors("OK");
		rsEntity.setObj("");
		return log.exit(rsEntity);
	}
	
	/**
	 * 异步更新配送员开工收工状态
	 * @author houshipeng
	 *
	 */
	class AsynWorkStatus extends Thread{
		
		public String cid = null;
		public String workStatus = null;
		public AsynWorkStatus(String cid,String workStatus){
			this.cid = cid;
			this.workStatus = workStatus;
		}
		
		public void run(){
			try{
				redisService.updateWorkStatus(this.cid, this.workStatus);
			}catch(Exception e){}
		}
	}
	
	/**
	 * 异步记录配送员最新位置
	 * @author houshipeng
	 *
	 */
	class AsynRecordGps extends Thread{
		
		String cid = null;
		String eid = null;
		String x = null;
		String y = null;
		public AsynRecordGps(String cid,String eid,String x,String y){
			this.cid = cid;
			this.eid = eid;
			this.x = x;
			this.y = y;
		}
		
		public void run(){
			try{
				redisService.recordCourierGps(this.cid, this.eid, this.x, this.y);
			}catch(Exception e){}
		}
	}
	
	/**
	 * 配送员APP，配送员手工为订单填写备注信息
	 * @param cid
	 * @param id
	 * @param remarks
	 * @return
	 * @deprecated
	 */
	@RequestMapping("/add_custom_remarks")
	public @ResponseBody
	       ResultEntity addCustomRemarkByCourier(String cid,String id,String remarks){
		
		ResultEntity rs = new ResultEntity();
		if(StringUtils.isBlank(cid)){
			rs.setErrors("ER");
			rs.setObj("配送员编号为空");
			return rs;
		}
		
		if(StringUtils.isBlank(id)){
			rs.setErrors("ER");
			rs.setObj("订单编号为空");
			return rs;
		}
		
		try{
			o2oWaybillService.updateCustomRemark(cid, id, remarks);
		}catch(Exception e){
			rs.setErrors("ER");
			rs.setObj(e.getMessage());
			return rs;
		}

		rs.setSuccess(true);
		rs.setErrors("OK");
		rs.setObj("");
		return rs;
	}
	
	
	/**
	 * 配送员APP，配送员今日配送情况(配送总单数、物品费用总额、应付商家费用、应收客户费用、应上缴费用)
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
	 * 配送员APP，电商模块根据面单上的编号识别商家名称
	 * @param id
	 * @return
	 * @deprecated
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
		rsMap.put("name", "电商运单");
		rsEntity.setSuccess(true);
		rsEntity.setErrors("OK");
		rsEntity.setObj(rsMap);
		return log.exit(rsEntity);
	}
	
	/**
	 * 配送员app,电商模块扫描面单上的条形码，并返回面单对应的订单基本信息
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
	 * 配送员APP，电商模块电商运单入库
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
	 * 商家版APP，商家发布配送需求
	 * @param shipperWaybill
	 * @return
	 */
	@RequestMapping("/sp_save_order")
	public @ResponseBody
	        ResultEntity shipperCreatewaybill(ShipperWaybill shipperWaybill ){
		
		ResultEntity rsEntity = new ResultEntity();
		
		Map<String, String> saveMap = new HashMap<String, String>();
		
		String remarks = shipperWaybill.getRemarks();
		
		saveMap.put("remarks", remarks);
		
		String shipperId = shipperWaybill.getSp_id();
		if(StringUtils.isBlank(shipperId)){
			rsEntity.setErrors("ER");
			rsEntity.setObj("商家编号为空");
			return rsEntity;
		}
		
		String cityCode = shipperWaybill.getCity_code();
		if(StringUtils.isBlank(cityCode)){
			rsEntity.setErrors("ER");
			rsEntity.setObj("未指定城市");
			return log.exit(rsEntity);
		}
		String x = shipperWaybill.getX();
		String y = shipperWaybill.getY();
		if(StringUtils.isBlank(x)||StringUtils.isBlank(y)){
			rsEntity.setErrors("ER");
			rsEntity.setObj("无法获取商家位置");
			return log.exit(rsEntity);
			
		}
		
		saveMap.put("city_code", cityCode);
		saveMap.put("city_name", shipperWaybill.getCity_name());
		
		/*商家经纬度(用于订单分配)*/
		saveMap.put("x", x);
		saveMap.put("y", y);
		saveMap.put("sp_id", shipperId);
		saveMap.put("cargo_type", shipperWaybill.getCargo_type());
		
		String cargoNum = shipperWaybill.getCargo_num();
		if(StringUtils.isBlank(cargoNum)){
			cargoNum = "1";
		}
		saveMap.put("cargo_num", cargoNum);
		
		String waybillNum = shipperWaybill.getNum();
		
		/*订单总量为空或者为0，默认设置为1*/
		if(StringUtils.isBlank(waybillNum)||"0".equals(waybillNum)){
			waybillNum = "1";
		}
		
		/*判断订单数量是否是数字*/
		if(!ToolsUtil.isInt(waybillNum)){
			return getErrorEntity("订单量必须是数字");
		}
		
		saveMap.put("waybill_num", waybillNum);
		
		/*物品费用*/
		String cargoPrice = shipperWaybill.getCargo_price();
		saveMap.put("cargo_price", cargoPrice);
		
		/*配送员手机号(如果不为空则为驻店模式)*/
		String c_phone = shipperWaybill.getC_phone();
		
		/*商家下单指定配送员*/
		saveMap.put("c_phone", c_phone);
		saveMap.put("status", "2");
		
		/*驻店模式*/
		if(StringUtils.isNotBlank(c_phone)){
			/*查询配送员是否存在*/
			CourierEntity courierEntity = courierService.queryCourier(null, null, c_phone);
			/*未找到指定的配送员*/
			if(courierEntity == null){
				return getErrorEntity("配送员未开工或者手机号不存在");
			}
			
			String cid = courierEntity.getId();
			String enterprise_id = courierEntity.getEnterprise_id();
			saveMap.put("cid", cid);
			saveMap.put("delivery_eid_id", enterprise_id);
		}
		
		try{
		    shipperService.createWaybill(saveMap);
		}catch(Exception e){
			rsEntity.setErrors("ER");
			rsEntity.setObj("系统异常：提交失败");
			return log.exit(rsEntity);
		}
		
		rsEntity.setSuccess(true);
		rsEntity.setErrors("OK");
		rsEntity.setObj("订单提交成功!");
		return log.exit(rsEntity);
	}
	
	
	/**
	 * 异步把配送员配送状态更新到redis中
	 * @author houshipeng
	 *
	 */
	class AsynDeliveryStatus extends Thread{
		
		public String cid = null;
		public String status = null;
		public AsynDeliveryStatus(String cid,String status){
			this.cid = cid;
			this.status = status;
		}
		public void run(){
			/*更新配送员的配送状态*/
			/*数据库 配置状态物理字段，生成订单的时候，已经更新，在此只需要更新redis中得配送状态即可*/
			try{
				redisService.updateDeliveryStatus(this.cid, status);
			}catch(Exception e){
			}
		}
	}
	
	
	/**
	 * 查询周边3公里范围内的空闲配送员
	 * @param eids
	 * @param x
	 * @param y
	 * @return
	 */
	private String queryNearCourier(String eids,String x,String y){
		List<String> courierList = new ArrayList<String>();
        /*查询商家周边(默认3公里)范围内的配送员，如果是多个商家，则在比较多个商家中那个离的最近(后期考虑增加权重值，用于做订单的优先分配)*/
		
//		boolean redisIsWork = redisService.redisIsWork();
		
        log.entry("courierService.queryNearCourier",eids,x,y);
        courierList = courierService.queryNearCourier(eids, x, y);
        
        log.entry("数据库中检索配送员结果: ",courierList);
        if(courierList==null||courierList.size()==0){
			return null;
        }
        
        /*获取配送员(现在是获取一个配送员，之后需要改为：如果有多个配送公司，从每个公司里面找最近的配送员，每个最近的再一次排序，找更近的)*/
        return String.valueOf(courierList.get(0));
        
        /*如果商家周边3公里范围内没有配送员，则提示 商家，周边暂时没有配送员，请稍后再提交(下一版改为 用户可以提交，提交后的订单，定时扫描是否有 可分配的配送员)，
         * 如果超过等待时间，自动取消(或者提醒商家取消订单)
         * 或者在地图上显示周边的配送员，如果地图上没有可用的配送员，需要在地图上提醒
         * */
		
		/*返回的结果为快递员id，如果返回为null，表示周边没有配送员*/
	}
	
	/**
	 * 配送员版APP，订单列表title，每个状态的订单数量
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
	 * 商家版app， 订单列表title，每个状态的订单数量
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
	
	/**
	 * 商家列表
	 * @return
	 */
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
	
	/**
	 * 商家常用地址列表
	 * @param sp_id
	 * @return
	 */
	@RequestMapping("/sp_address_list")
	public @ResponseBody
	        ResultEntity shipperAddressList(String sp_id){
		
		ResultEntity rsEntity = new ResultEntity();
		
		if(StringUtils.isBlank(sp_id)){
			return getErrorEntity("商家编号为空");
		}
		
		List<ShipperAddressEntity> list = null;
		try{
			list = shipperService.queryShipperAddressList(sp_id);
		}catch(Exception e){
			return log.exit(getErrorEntity(e.getMessage()));
		}
		
		rsEntity.setSuccess(true);
		rsEntity.setErrors("OK");
		rsEntity.setObj(list);
		return log.exit(rsEntity);
		
	}
	
	/**
	 * 商家增加发单地址
	 * @param entity
	 * @return
	 */
	@RequestMapping("/sp_add_address")
	public @ResponseBody
	        ResultEntity addShipperAddress(ShipperAddressEntity entity){
		
		if(entity==null){
			return getErrorEntity("未上传任何参数");
		}
		
		/*商家编号*/
		if(StringUtils.isBlank(entity.getSp_id())){
			return getErrorEntity("商家编号为空");
		}
		
		/*地址信息*/
		if(StringUtils.isBlank(entity.getAddress())){
			return getErrorEntity("地址为空");
		}
		
		/*联系人信息*/
		if(StringUtils.isBlank(entity.getContact())){
			return getErrorEntity("联系人为空");
		}
		
		/*联系人手机号*/
		if(StringUtils.isBlank(entity.getPhone())){
			return getErrorEntity("联系人手机号为空");
		}
		
		/*地址对应经纬度信息*/
		if(StringUtils.isBlank(entity.getAddress_x()) || StringUtils.isBlank(entity.getAddress_y())){
			return getErrorEntity("地址经纬度信息为空");
		}
		
		/*citycode*/
		if(StringUtils.isBlank(entity.getCity_code())){
			return getErrorEntity("city_code为空");
		}
		
		try{
			shipperService.addShipperAddress(entity);
		}catch(Exception e){
			return getErrorEntity(e.getMessage());
		}
		
		return getSuccessEntity("");
	}
	
	/**
	 * 商家常用地址列表中，设置一个为默认地址
	 * @param sp_id
	 * @param address_id
	 * @return
	 */
	@RequestMapping("/sp_default_address")
	public @ResponseBody 
	        ResultEntity setShipperDefaultAddress(String sp_id,String address_id){
		
		if(StringUtils.isBlank(sp_id)){
			return getErrorEntity("商家编号为空");
		}
		if(StringUtils.isBlank(address_id)){
			return getErrorEntity("未指定商家地址");
		}
		
		try{
			shipperService.shipperDefaultAddress(sp_id, address_id);
		}catch(Exception e){
			return getErrorEntity(e.getMessage());
		}
		
		return getSuccessEntity("");
	}
	
	@RequestMapping("/edit_sp_address")
	public @ResponseBody
	        ResultEntity editShipperAddress(ShipperAddressEntity entity){
		
		if(entity==null){
			return getErrorEntity("未上传任何参数");
		}
		
		/*商家编号*/
		if(StringUtils.isBlank(entity.getSp_id())){
			return getErrorEntity("商家编号为空");
		}
		
		/*商家地址编号*/
		if(StringUtils.isBlank(entity.getId())){
			return getErrorEntity("地址编号为空");
		}
		
		/*地址信息*/
		if(StringUtils.isBlank(entity.getAddress())){
			return getErrorEntity("地址为空");
		}
		
		/*联系人信息*/
		if(StringUtils.isBlank(entity.getContact())){
			return getErrorEntity("联系人为空");
		}
		
		/*联系人手机号*/
		if(StringUtils.isBlank(entity.getPhone())){
			return getErrorEntity("联系人手机号为空");
		}
		
		/*地址对应经纬度信息*/
		if(StringUtils.isBlank(entity.getAddress_x()) || StringUtils.isBlank(entity.getAddress_y())){
			return getErrorEntity("地址经纬度信息为空");
		}
		
		/*citycode*/
		if(StringUtils.isBlank(entity.getCity_code())){
			return getErrorEntity("city_code为空");
		}
		
		try{
			shipperService.editShipperAddress(entity);
		}catch(Exception e){
			return getErrorEntity(e.getMessage());
		}
		
		return getSuccessEntity("");
	}
	
	@RequestMapping("/remove_sp_address")
	public @ResponseBody
	        ResultEntity removeShipperAddress(String sp_id,String address_id){
		
		if(StringUtils.isBlank(sp_id)){
			return getErrorEntity("商家编号为空");
		}
		if(StringUtils.isBlank(address_id)){
			return getErrorEntity("未指定商家地址");
		}
		
		try{
			shipperService.cancelShipperAddress(sp_id, address_id);
		}catch(Exception e){
			return getErrorEntity(e.getMessage());
		}
		
		return getSuccessEntity("");
	}
	
	
	/**
	 * 配送员APP，根据订单状态查询配送员当前天所有符合条件的订单
	 * @param request
	 * @return
	 */
	@RequestMapping("/query_courier_waybill")
	public @ResponseBody
	       ResultPageEntity searchWaybillByCourier(HttpServletRequest request){
		
		String cid = request.getParameter("cid");
		if(StringUtils.isBlank(cid)){
			return getDefaultPageErrorEntity("快递员ID为空");
		}
		String page = request.getParameter("page");
		if(StringUtils.isBlank(page)){
			page = "1";
		}
		String status = request.getParameter("status");
		if(StringUtils.isBlank(status)){
			return getDefaultPageErrorEntity("状态为空");
		}
		
		/*每页显示的记录数(不提供 默认10条)*/
		String size = request.getParameter("size");
		int sizeInt = defaultSize;
		if(StringUtils.isNotBlank(size)){
			try{
				sizeInt = Integer.parseInt(size);
			}catch(Exception e){
				return getDefaultPageErrorEntity("[size]非法参数值");
			}
		}
		String opType = request.getParameter("t");
		if(StringUtils.isBlank(opType)){
			opType = "2";
		}
		
		ResultPageEntity rsEntity = new ResultPageEntity();
		
		Map<String, Object> rsMap = null;
		
		/*判断page 和size参数是否为数字*/
		int pageInt = 0;
		
		try{
			pageInt = Integer.parseInt(page);
		}catch(Exception e){
			return getDefaultPageErrorEntity("非法页码");
		}
		
		if(pageInt==0){
			return getDefaultPageErrorEntity("非法页码");
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
		long totalPage = (Long.parseLong(count.toString()) + sizeInt -1) / sizeInt;
		rsEntity.setTotalPage(totalPage);
		rsEntity.setLimit(sizeInt);
		rsEntity.setStart(skip);
		rsEntity.setErrors("OK");
		return log.exit(rsEntity);
	}
	
	/**
	 * 商家版APP，根据订单状态查询配送员当前天所有符合条件的订单
	 * @param request
	 * @return
	 */
	@RequestMapping("/query_sp_waybill")
	public @ResponseBody
	       ResultPageEntity searchWaybillByShipper(HttpServletRequest request){
		
		String spId = request.getParameter("sp_id");
		if(StringUtils.isBlank(spId)){
			return getDefaultPageErrorEntity("商家ID为空");
		}
		String page = request.getParameter("page");
		if(StringUtils.isBlank(page)){
			//return getDefaultErrorEntity("页码为空");
			page = "1";
		}
		String status = request.getParameter("status");
		if(StringUtils.isBlank(status)){
			return getDefaultPageErrorEntity("状态为空");
		}
		
		/*每页显示的记录数(不提供 默认10条)*/
		String size = request.getParameter("size");
		int sizeInt = defaultSize;
		if(StringUtils.isNotBlank(size)){
			try{
				sizeInt = Integer.parseInt(size);
			}catch(Exception e){
				return getDefaultPageErrorEntity("[size]非法参数值");
			}
		}
		
		ResultPageEntity rsEntity = new ResultPageEntity();
		
		Map<String, Object> rsMap = null;
		
		int pageInt = 0;
		try{
			pageInt = Integer.parseInt(page);
		}catch(Exception e){
			return getDefaultPageErrorEntity("非法页码");
		}
		
		if(pageInt==0){
			return getDefaultPageErrorEntity("非法页码");
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
		long totalPage = (Long.parseLong(count.toString()) + sizeInt -1) / sizeInt;
		rsEntity.setTotalPage(totalPage);
		rsEntity.setLimit(sizeInt);
		rsEntity.setStart(skip);
		rsEntity.setErrors("OK");
		return log.exit(rsEntity);
	}
	
	/**
	 * 配送员版app，修改订单状态(取件、签收动作)
	 * @param request
	 * @return
	 */
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
		
        String status = request.getParameter("status");
		if(StringUtils.isBlank(status)){
			rs.setObj("运单状态为空");
			rs.setErrors("ER");
			return rs;
		}
		
        String cid = request.getParameter("cid");
		if(StringUtils.isBlank(cid)){
			rs.setObj("操作员ID为空");
			rs.setErrors("ER");
			return rs;
		}
		paraMap.put("cid", cid);
		
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
		
//		int statusInt = Integer.parseInt(status);
//		/*5表示配送完成，<0表示异常件或者订单取消*/
//		if(statusInt==5||statusInt<0){
//			//new AsynDeliveryStatus(cid, "0").run();
//		}
		
		rs.setSuccess(true);
		rs.setErrors("OK");
		rs.setObj("");
		return log.exit(rs);
	}
	
	
	/**
	 * 配送员app，批量操作订单(批量取件、批量签收订单)
	 * @param request
	 * @return
	 */
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
	
	/**
	 * 配送员APP，定期上报当前位置
	 * @param cid
	 * @param eid
	 * @param x
	 * @param y
	 * @return
	 */
	@RequestMapping("/report_gps")
	public @ResponseBody
	        ResultEntity reportGps(String cid,String eid,String x,String y){
		
		ResultEntity rs = new ResultEntity();
		
		/*配送员位置更新到数据库中*/
		try{
			courierService.recordCourierGps(cid, eid, x, y);
		}catch(Exception e){}
		
		
		/*配送员位置更新到redis中*/
		try{
			//new AsynRecordGps(cid, eid, x, y).run();
		}catch(Exception e){}
		
		rs.setSuccess(true);
		rs.setObj("");
		rs.setErrors("OK");
		return rs;
	}
	
	/**
	 * 商家版APP，用户登录
	 * @param un
	 * @param pwd
	 * @return
	 */
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
	
	/**
	 * 老版本商家版APP，设置商家位置(不支持商家地址列表)
	 * @param entity
	 * @return
	 */
	@RequestMapping("/sp_setaddress")
	public @ResponseBody
	       ResultEntity shipperSetAddress(ShipperUserEntity entity){
		
		ResultEntity rs = new ResultEntity();
		ShipperUserEntity spentity = null;
		try{
			spentity = shipperService.updateShipperDefualtAddress(entity);
		}catch(Exception e){
			rs.setErrors("ER");
			rs.setObj(e.getMessage());
			return rs;
		}
		rs.setSuccess(true);
		rs.setObj(spentity);
		rs.setErrors("OK");
		return rs;
	}
	
	/**
	 * 商家版APP，修改订单状态(取消订单)
	 * @param sp_id
	 * @param sp_uid
	 * @param status
	 * @param id
	 * @return
	 */
	@RequestMapping("/sp_handle_waybill")
	public @ResponseBody
	       ResultEntity handleWaybill(String sp_id,String sp_uid,String status,String id){
		
		ResultEntity rsEntity = new ResultEntity();
		
		if(StringUtils.isBlank(sp_id)){
			rsEntity.setErrors("ER");
			rsEntity.setObj("商家编号为空");
			return rsEntity;
		}
		if(StringUtils.isBlank(id)){
			rsEntity.setErrors("ER");
			rsEntity.setObj("订单编号为空");
			return rsEntity;
		}		
		if(StringUtils.isBlank(status)){
			rsEntity.setErrors("ER");
			rsEntity.setObj("状态值为空");
			return rsEntity;
		}		
		try{
			 shipperService.shipperHandleWaybill(sp_id, sp_uid, id, status);
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
	
	/**
	 * 商家版APP，根据日期取件查询发布的订单信息
	 * @param sp_id
	 * @param sp_uid
	 * @param st
	 * @param et
	 * @return
	 */
	@RequestMapping("/count_sp_waybill")
	public @ResponseBody
	       ResultEntity countShipperWaybillByDate(String sp_id,String sp_uid,String st,String et){
		
		ResultEntity rsEntity = new ResultEntity();
		
		if(StringUtils.isBlank(sp_id)){
			rsEntity.setErrors("ER");
			rsEntity.setObj("商家编号为空");
			return rsEntity;
		}
		if(StringUtils.isBlank(st)||StringUtils.isBlank(et)){
			rsEntity.setErrors("ER");
			rsEntity.setObj("请指定查询时间范围");
			return rsEntity;
		}		
		
		
		Map<String, Map<String, String>> rsMap = new HashMap<String, Map<String,String>>();
		try{
			long startTime = DateUtil.getStartTime(st);
			long endTime = DateUtil.getEndTime(et);
			
			rsMap = statisticsService.groupQueryShipperStatusByDate(sp_id, sp_uid, startTime, endTime);
		}catch(Exception e){
			rsEntity.setSuccess(false);
			rsEntity.setErrors(e.getMessage());
			return log.exit(rsEntity);
		}
			
		rsEntity.setSuccess(true);
		rsEntity.setObj(rsMap);
		rsEntity.setErrors("OK");
			
		return log.exit(rsEntity);
	}
	
	
	/**
	 * 商家版app，修改登录密码
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
	
	/**
	 * 当前支持的配送城市列表
	 * @return
	 */
	@RequestMapping("/city_list")
	public @ResponseBody
	       ResultEntity cityList(){
		
		ResultEntity rsEntity = new ResultEntity();
		
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		
		
		Map<String, Object> map1 = new HashMap<String, Object>();
		
		/*广东市*/
		List<Map<String, String>> gd = new ArrayList<Map<String, String>>();
		
		Map<String, String> gz = new HashMap<String, String>();
		gz.put("city_name", "广州市");
		gz.put("city_code", "020");
		
		Map<String, String> dg = new HashMap<String, String>();
		dg.put("city_name", "东莞市");
		dg.put("city_code", "0769");		
		
		gd.add(gz);
		gd.add(dg);
		map1.put("province","广东省");
		map1.put("citys", gd);
		
		list.add(map1);
		
		Map<String, Object> map2 = new HashMap<String, Object>();
		/*北京市*/
		List<Map<String, String>> bj = new ArrayList<Map<String, String>>();
		
		Map<String, String> b = new HashMap<String, String>();
		b.put("city_name", "北京市");
		b.put("city_code", "010");
		
		bj.add(b);
		
		map2.put("province", "北京市");
		map2.put("citys", bj);
		list.add(map2);
		
		Map<String, Object> map3 = new HashMap<String, Object>();
		/*山东省*/
		List<Map<String, String>> sd = new ArrayList<Map<String, String>>();
		
		Map<String, String> jn = new HashMap<String, String>();
		jn.put("city_name", "济南市");
		jn.put("city_code", "0531");
		sd.add(jn);
		map3.put("province", "山东省");
		map3.put("citys", sd);
		list.add(map3);
		
		rsEntity.setSuccess(true);
		rsEntity.setErrors("");
		rsEntity.setObj(list);
		return log.exit(rsEntity);
	}
	
	/**
	 * 返回分页对象默认数据
	 * @param errorInfo
	 * @return
	 */
	public ResultPageEntity getDefaultPageErrorEntity(String errorInfo){
		ResultPageEntity pageEntity = new ResultPageEntity();
		pageEntity.setSuccess(false);
		pageEntity.setLimit(0);
		pageEntity.setStart(0);
		pageEntity.setTotalCount(0);
		pageEntity.setPage(0);
		pageEntity.setErrors(errorInfo);
		pageEntity.setPage(0);
		
		return pageEntity;
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
