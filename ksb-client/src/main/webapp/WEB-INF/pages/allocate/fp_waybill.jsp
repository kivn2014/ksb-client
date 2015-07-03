<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html lang="zh-CN">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>快送宝 分配运单</title>
    
<script src="${pageContext.request.contextPath}/js/jquery/jquery-2.1.4.min.js"></script>
<script src="${pageContext.request.contextPath}/js/waybill_status.js"></script>
<script src="${pageContext.request.contextPath}/js/bootstrap/bootstrap.min.js"></script>
<link href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.min.css" rel="stylesheet">
<link href="${pageContext.request.contextPath}/css/dashboard.css" rel="stylesheet">
   
<script type="text/javascript">

   var courierId= '600577960457535488';

	function searchMe() {
		
 		$.post("<%=request.getContextPath()%>/openapi/query_waybill",
 				{
 			    'waybillStatus':'0',
 			    'waybillId':''
 			    },
 				function(data){
 					var containerBody = $("#bodyid").empty();
 					var htm = "";
 					data = $.parseJSON(data);
 					if(data.list != null && data.list.length > 0) {
 					
 						s = data.success;
 						if(!s) {//查询失败
 							alert(data.desc);
 							return;
 						}
 						
 						info = data.list;
 						$.each(info,function(i,n){
 			    			$("<tr/>").append($("<td/>").append(n.id))
 							.append($("<td/>").append(n.shipper_name))
 							.append($("<td/>").append(n.shipper_address))
 							.append($("<td/>").append(n.shipper_phone))
 							.append($("<td/>").append(n.buyer_name))
 							.append($("<td/>").append(n.buyer_address))
 							.append($("<td/>").append(n.buyer_phone))
 							.append($("<td/>").append(getStatus(n.status)))
 							.append($("<td/>").append("<button/>").find("button").addClass("btn btn-primary").append("分配给我").click(function(){
 							  	$.post("/openapi/do_fp_waybill",{
 							  		'waybillId':n.id,
 							  		'courierId':courierId
 							  		},
 										function(data){
 											data = $.parseJSON(data);
 											
 											s = data.success;
 											if(!s) {//执行失败
 												alert(data.errors);
 												return;
 											}else{
 												alert("运单分配分配成功!");
 												searchMe();
 											}
 									});
 							}))
 			    			.appendTo($("#bodyid"))
 							
 						});
 						
 					}else {
 						containerBody.html("<tr><td colspan='8'>未查询到数据<td></tr>");
 					}
 					
 			});
		
	}
	

</script>

  </head>

  <body>

    <nav class="navbar navbar-inverse navbar-fixed-top">
      <div class="container-fluid">
        <div class="navbar-header">
          <a class="navbar-brand" href="#">3 KM</a>
        </div>
        <div id="navbar" class="navbar-collapse collapse">
          <ul class="nav navbar-nav navbar-right">
            <li><a href="#">用户</a></li>
            <li><a href="<%=request.getContextPath()%>/openapi/add_waybill" target="_blank"><font color="red">在线下单</font></a></li>
            <li><a href="<%=request.getContextPath()%>/openapi/fp_waybill" target="_blank"><font color="red">运单分配</font></a></li>
            <li><a href="http://www.3gongli.com/" target="_blank">联系我们</a></li>
            <li><a href="#">帮助</a></li>
          </ul>
        </div>
      </div>
    </nav>

    <div class="container-fluid">
      <div class="row">
        <div class="col-sm-3 col-md-2 sidebar">
          <ul class="nav nav-sidebar">
            <li class="active"><a href="#">所有<span class="sr-only">(current)</span></a></li>
          </ul>
          
        </div>
        <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
          <h1 class="page-header">快送宝 运单分配管理(临时页面，模拟运单分配)</h1>

				<form class="form-inline">
					<button type="button" onclick="javascript:searchMe()" class="btn btn-default" id="dataSearchId">点击我代替自动刷新</button>
				</form>
				<div class="table-responsive">
            <table class="table table-striped">
              <thead>
                <tr>
                  <th>运单编号</th>
                  <th>商家名称</th>
                  <th>商家地址</th>
                  <th>商家电话</th>
                  <th>收货人姓名</th>
                  <th>收货人地址</th>
                  <th>收货人电话</th>
                  <th>状态</th>
                  <th>管理</th>
                </tr>
              </thead>
              <tbody id="bodyid">
               
                
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

  </body>
  
</html>

