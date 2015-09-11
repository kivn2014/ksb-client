package com.ksb.web.openapi.entity;

import java.io.Serializable;

public class OrderSaveEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4404316325488501763L;
	
	/*配送员ID*/
	private String cid;
	/*订单ID*/
	private String wb_id;
	/*上传的小票照片文件MD5值*/
	private String md5_key;
	/*上传的小票照片名称*/
	private String img_id;
	/*订单中商家编号*/
	private String sp_id;
	/*每次需求的订单总数*/
	private String waybill_num;
	/*订单城市号*/
	private String city_code;
	/*订单备注*/
	private String remarks;
	/*商家当前地址经度*/
	private String sp_x;
	/*商家当前地址纬度*/
	private String sp_y;
    /*商家地址编号(商家多个常用发货地址)*/
	private String sp_address_id;
	/*配送货物类型*/
	private String cargo_type;
	/*外卖平台ID(百度外卖#1；饿了么#3信息；自营手写订单由商家自己定义)*/
	private String source_id;
	/*应付商家费用*/
	private String pay_sp_fee;
	/*应收客户金额*/
	private String fetch_buyer_fee;
	
	public String getPay_sp_fee() {
		return pay_sp_fee;
	}
	public void setPay_sp_fee(String pay_sp_fee) {
		this.pay_sp_fee = pay_sp_fee;
	}
	public String getFetch_buyer_fee() {
		return fetch_buyer_fee;
	}
	public void setFetch_buyer_fee(String fetch_buyer_fee) {
		this.fetch_buyer_fee = fetch_buyer_fee;
	}
	public String getMd5_key() {
		return md5_key;
	}
	public void setMd5_key(String md5_key) {
		this.md5_key = md5_key;
	}
	public String getImg_id() {
		return img_id;
	}
	public void setImg_id(String img_id) {
		this.img_id = img_id;
	}
	public String getSp_id() {
		return sp_id;
	}
	public void setSp_id(String sp_id) {
		this.sp_id = sp_id;
	}
	public String getWaybill_num() {
		return waybill_num;
	}
	public void setWaybill_num(String waybill_num) {
		this.waybill_num = waybill_num;
	}
	public String getCity_code() {
		return city_code;
	}
	public void setCity_code(String city_code) {
		this.city_code = city_code;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getSp_x() {
		return sp_x;
	}
	public void setSp_x(String sp_x) {
		this.sp_x = sp_x;
	}
	public String getSp_y() {
		return sp_y;
	}
	public void setSp_y(String sp_y) {
		this.sp_y = sp_y;
	}
	public String getSp_address_id() {
		return sp_address_id;
	}
	public void setSp_address_id(String sp_address_id) {
		this.sp_address_id = sp_address_id;
	}
	public String getCargo_type() {
		return cargo_type;
	}
	public void setCargo_type(String cargo_type) {
		this.cargo_type = cargo_type;
	}
	public String getSource_id() {
		return source_id;
	}
	public void setSource_id(String source_id) {
		this.source_id = source_id;
	}
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	public String getWb_id() {
		return wb_id;
	}
	public void setWb_id(String wb_id) {
		this.wb_id = wb_id;
	}
	
	
}
