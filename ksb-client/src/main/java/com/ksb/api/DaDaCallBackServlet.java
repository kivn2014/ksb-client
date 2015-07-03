package com.ksb.api;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class DemoServlet
 */
public class DaDaCallBackServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public DaDaCallBackServlet() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		System.out.println("========================");
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		request.setCharacterEncoding("UTF-8"); 
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		
		Map<String, String[]> pm = request.getParameterMap();
		
		if(pm!=null&&pm.size()>0){
			
			Iterator<Entry<String, String[]>> it = pm.entrySet().iterator();
			while(it.hasNext()){
				
				Entry<String, String[]> entry = it.next();
				
				String k = entry.getKey();
				System.out.println("k===>"+k);
				String[] vs = entry.getValue();
				if(vs!=null&&vs.length>0){
					System.out.println("v===>"+vs[0]);
				}
				
			}
			
		}
		
		
	}

}
