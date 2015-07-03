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

    <title>快送宝 配送员版APP</title>
    
<script src="${pageContext.request.contextPath}/js/jquery/jquery-2.1.4.min.js"></script>
<script src="${pageContext.request.contextPath}/js/waybill_status.js"></script>
<script src="${pageContext.request.contextPath}/js/bootstrap/bootstrap.min.js"></script>
<link href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.min.css" rel="stylesheet">
<link href="${pageContext.request.contextPath}/css/dashboard.css" rel="stylesheet">
   
<script type="text/javascript">
    
   var courierId= '600577960457535488';
    

	$(document).ready(function(){
		
	$('ul > li').click(function(){//鼠标点击也可以切换 
		//curLi=$(this);
		$(this).siblings('li').removeClass('active').end().addClass('active');
	}); 
	});
	
	function searchMe(waybillStatus) {
		
 		$.post("<%=request.getContextPath()%>/mobile/query_courier_waybill",
 				{
 			    'cid':courierId,
 			    'waybillStatus':waybillStatus
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
 			    			
 			    			var trObj = $("<tr/>");
 			    			if(waybillStatus==''||waybillStatus=='5'){
 			    				trObj
 	 							.append($("<td/>").append(n.shipper_name))
 	 							.append($("<td/>").append(n.shipper_address))
 	 							.append($("<td/>").append(n.shipper_phone))
 	 							.append($("<td/>").append(n.buyer_name))
 	 							.append($("<td/>").append(n.buyer_address))
 	 							.append($("<td/>").append(n.buyer_phone))
 	 							.append($("<td/>").append(getStatus(n.status)))
 	 							.append($("<td/>").append(""))
 	 							.appendTo($("#bodyid"))
 			    			}else{
 			    				 trObj
 	 							.append($("<td/>").append(n.shipper_name))
 	 							.append($("<td/>").append(n.shipper_address))
 	 							.append($("<td/>").append(n.shipper_phone))
 	 							.append($("<td/>").append(n.buyer_name))
 	 							.append($("<td/>").append(n.buyer_address))
 	 							.append($("<td/>").append(n.buyer_phone))
 	 							.append($("<td/>").append(getStatus(n.status)))

 	 							/*取件*/
 	 							.append($("<td/>").append("<button/>").find("button").addClass("btn btn-primary").append("取件").click(function(){
 	 								if(waybillStatus==''||waybillStatus!='1'){
 	 									alert("非法动作");
 	 									return;
 	 								}
 	 							  	$.post("/mobile/handle_waybill",{
 	 							  		'waybillId':n.id,
 	 							  		'courierId':courierId,
 	 							  		'waybillStatus':'3',
 	 							  		},
 	 										function(data){
 	 											data = $.parseJSON(data);
 	 											
 	 											s = data.success;
 	 											if(!s) {//执行失败
 	 												alert(data.errors);
 	 												return;
 	 											}else{
 	 												alert("取件成功!");
 	 												gotoIndex();
 	 											}
 	 									});
 	 							}))
 	 							
 	 							/*签收*/
 	 							.append($("<td/>").append("<button/>").find("button").addClass("btn btn-primary").append("签收").click(function(){
 									if(waybillStatus==''||waybillStatus!='3'){
 	 									alert("非法动作");
 	 									return;
 	 								}
 	 							  	$.post("/mobile/handle_waybill",{
 	 							  		'waybillId':n.id,
 	 							  		'courierId':courierId,
 	 							  		'waybillStatus':'5'
 	 							  		},
 	 										function(data){
 	 											data = $.parseJSON(data);
 	 											
 	 											s = data.success;
 	 											if(!s) {//执行失败
 	 												alert(data.errors);
 	 												return;
 	 											}else{
 	 												alert("完成配送");
 	 												gotoIndex();
 	 											}
 	 									});
 	 							})) 
 	 							
 	 							/*拒收*/
 	  							.append($("<td/>").append("<button/>").find("button").addClass("btn btn-primary").append("拒收").click(function(){
 	 							  /* 	$.post("/openapi/do_fp_waybill",{
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
 	 									}); */
 	 									alert("暂不支持拒收")
 	 							}))		
 	 							
 	  							/*取消运单*/
 	  							 /* .append($("<td/>").append("<button/>").find("button").addClass("btn btn-primary").append("取消").click(function(){
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
 	 									alert("暂不支持取消")
 	 							}))		*/							
 	 							
 	 			    			.appendTo($("#bodyid"))
 			    			}
 			
 						});
 						
 					}else {
 						containerBody.html("<tr><td colspan='7'>未查询到数据<td></tr>");
 					}
 					
 			});
		
	}
	function gotoIndex() {
		location.href="/mobile/mobile_main"
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
            <li><a href="#">侯世鹏</a></li>
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
            <li class="active"><a href="javascript:searchMe('')">所有<span class="sr-only">(current)</span></a></li>
            <li><a href="javascript:searchMe(1)">待处理</a></li>
            <li><a href="javascript:searchMe(3)">配送中</a></li> 
            <li><a href="javascript:searchMe(5)">完成</a></li>
            <li><a href="javascript:searchMe(-2)">异常件</a></li> 
          </ul>
          
        </div>
        <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
          <h1 class="page-header">快送宝 配送员版APP</h1>

				<form class="form-inline">
					<button type="button" onclick="javascript:searchMe()" class="btn btn-default" id="dataSearchId">点击我代替自动刷新</button>
					<span><font color="red" size="5">[所有] 标签下 暂时不能操作运单状态</font></span>
				</form>
				<div class="table-responsive">
            <table class="table table-striped">
              <thead>
                <tr>
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
  
    <!-- Modal -->  
  <div class="modal fade" id="addModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
        <h4 class="modal-title" id="myModalLabel">运单详细信息</h4>
      </div>
      <div class="modal-body">
       <table class="table table-striped dssBasic">
       		<thead>
	       		<tr>
					<td>支付类型</td><td>支付状态</td><td>货物类型</td><td>货物重量</td><td>货物价格</td><td>货物数量</td><td>运单创建时间</td><td>运单完成时间</td>
				</tr>
       		</thead>
			<tbody id="waybillDetailId">
				
			</tbody>
       </table>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-primary" id="stopDss" data-dismiss="modal">关闭</button>
      </div>
    </div>
  </div>
</div>
</html>

