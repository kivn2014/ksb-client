package com.ksb.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import com.ksb.openapi.service.WaybillService;

/**
 * Servlet implementation class InitTestServlet
 */
public class InitTestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	
	
	WaybillService waybillService;
	
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public InitTestServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		DaDaOpenAPI api = new DaDaOpenAPI();
		
		String id = request.getParameter("id");
		
		PrintWriter out = response.getWriter();
		
		
	}
	
	private Map<String, String> getMap(String id){
		
		Map<String, String> pm = new HashMap<String, String>();
		pm.put("token", "ce03975d2398e356ce03975d2398e356");
		pm.put("timestamp", "1431500302130");
		pm.put("signature", "244ced78032c928777418151dbc9c7ac");
		pm.put("origin_id", id);
		pm.put("city_name", "北京");
		pm.put("city_code", "010");
		pm.put("pay_for_supplier_fee", "0");
		pm.put("fetch_from_receiver_fee", "0");
		pm.put("deliver_fee", "0");
		pm.put("create_time", "1431334370856");
		pm.put("info", "wu");

		pm.put("cargo_type", "1");
		pm.put("cargo_weight", "1");
		pm.put("cargo_price", "0");
		pm.put("cargo_num", "0");
		pm.put("is_prepay", "1");
		pm.put("expected_fetch_time", "0");
		pm.put("expected_finish_time", "0");

		pm.put("supplier_id", "1");
		pm.put("supplier_name", "月月美食");
		pm.put("supplier_address", "哈喽哈哈哈哈哈哈哈哈");
		pm.put("supplier_phone", "12222222222");
		pm.put("supplier_lat", "0");
		pm.put("supplier_lng", "0");
		pm.put("invoice_title", "");

		pm.put("receiver_name", "测试数据");
		pm.put("receiver_address", "月月美食月月美食");
		pm.put("receiver_phone", "122222222222");
		pm.put("receiver_tel", "998888");
		pm.put("receiver_lat", "0");
		pm.put("receiver_lng", "0");
		pm.put("callback", "http://123.57.217.101:8080/demo/callback");
		
		return pm;
	}
	

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
