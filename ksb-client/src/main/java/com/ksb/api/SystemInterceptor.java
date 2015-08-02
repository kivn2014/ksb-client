package com.ksb.api;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Servlet Filter implementation class SystemInterceptor
 */
public class SystemInterceptor extends HandlerInterceptorAdapter implements Filter {
       
    /**
     * @see HandlerInterceptorAdapter#HandlerInterceptorAdapter()
     */
    public SystemInterceptor() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		// TODO Auto-generated method stub
		// place your code here

		 request.setCharacterEncoding("UTF-8"); 
		 response.setCharacterEncoding("UTF-8");
		 response.setContentType("text/html;charset=UTF-8");  
		 
         PrintWriter out = response.getWriter();  
         StringBuilder builder = new StringBuilder();  
         builder.append("<script type=\"text/javascript\" charset=\"UTF-8\">");  
         builder.append("alert(\"页面过期，请重新登录\");");  
         builder.append("window.top.location.href=\"");  
         builder.append("/index.html\";</script>");  
         out.print(builder.toString());  
         out.close();
		
		
		// pass the request along the filter chain
		chain.doFilter(request, response);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

}
