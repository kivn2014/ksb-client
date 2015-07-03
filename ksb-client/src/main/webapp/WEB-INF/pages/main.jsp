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

    <title>快送宝</title>
    
<script src="${pageContext.request.contextPath}/js/jquery/jquery-2.1.4.min.js"></script>
<script src="${pageContext.request.contextPath}/js/bootstrap/bootstrap.min.js"></script>
<link href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.min.css" rel="stylesheet">
<link href="${pageContext.request.contextPath}/css/dashboard.css" rel="stylesheet">
   
<script type="text/javascript">
    
   var shipper_id = '601350459122253824';

	function openDetailDialog(orderId) {
	  	
	  	$.post("<%=request.getContextPath()%>/openapi/search_sp",
			{
			'id':shipper_id,
			'orderId':orderId
			},
			function(data){
				var containerBody = $("#waybillDetailId").empty();
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
					    htm +=  "<tr><td>" + n.pay_type + "</td>"
 			    			+ '<td>' + n.payment_status + '</td>'
 			    			+ '<td>' + n.cargo_type + '</td>'
 			    			+ '<td>' + n.cargo_weight + '</td>'
 			    			+ '<td>' + n.cargo_price + '</td>'
 			    			+ '<td>' + n.cargo_num + '</td>'
 			    			+ '<td>' + n.create_time + '</td>'
 			    			+ '<td>' + n.finish_time + '</td>'
		
					});
					
					containerBody.html(htm);
				}else {
					containerBody.html("<tr><td colspan='5'>未查询到数据<td></tr>");
				}
				
				
				$('#addModal').modal({});
		});
  	
  	}
    
	function searchMe() {
		
		//alert("神速开发中，敬请期待...")

		//获取表单中得查询字段值
		var status=$("#waybillStatusId").val();
		var orderId=$("#orderId").val();
		var name=$("#buyerNameId").val();
		var phone=$("#buyerPhoneId").val();
		
 		$.post("<%=request.getContextPath()%>/openapi/search_sp",
 				{'id':shipper_id,
 			     'waybillStatus':status,
 			     'buyerName':name,
 			     'buyerPhone':phone,
 			     'orderId':orderId
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
 						    htm +=  '<tr>'
 			    			+ '<td>' + n.shipper_origin_id + '</td>'
 			    			+ '<td>' + n.third_platform_name + '</td>'
 			    			+ '<td>' + n.courier_name + '</td>'
 			    			+ '<td>' + n.courier_phone + '</td>'
 			    			+ '<td>' + n.buyer_address + '</td>'
 			    			+ '<td>' + n.buyer_name +' / '+n.buyer_phone+'</td>'
 			    			+ '<td>' + n.status + '</td>'
 			    			+ '<td>'
 			    				 + '<button type="button" class="btn btn-primary" name="addDss" onclick="javascript:openDetailDialog(' + n.shipper_origin_id + ')">详情</button>'
 			    			+ '</td>'
 			    			+ '</tr>';
 			
 						});
 						
 						containerBody.html(htm);
 					}else {
 						containerBody.html("<tr><td colspan='7'>未查询到数据<td></tr>");
 					}
 					
 			});
		
	}
	
	function openDetail(waybillId){
		$('#addModal').modal({});
		
	}
</script>

  </head>

  <body>

    <nav class="navbar navbar-inverse navbar-fixed-top">
      <div class="container-fluid">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand" href="#">3 KM</a>
        </div>
        <div id="navbar" class="navbar-collapse collapse">
          <ul class="nav navbar-nav navbar-right">
            <li><a href="#">用户</a></li>
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
            <li class="active"><a href="#">配送信息 <span class="sr-only">(current)</span></a></li>
           <!--  <li><a href="#">财务结算</a></li>
            <li><a href="#">绩效考核</a></li> -->
          </ul>
          
        </div>
        <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
          <h1 class="page-header">快送宝 商户自助服务平台</h1>

				<form class="form-inline">
					<div class="form-group">
						<label class="sr-only" for="orderId">orderId</label> 
						<input type="text" class="form-control"
							id="orderId" placeholder="运单ID">
					</div>
					<div class="form-group">
						<label class="sr-only" for="buyerName">buyerName</label>
						<input type="text" class="form-control"
							id="buyerNameId" placeholder="收货人姓名">
					</div>	
					<div class="form-group">
						<label class="sr-only" for="buyerPhone">buyerPhone</label>
						<input type="text" class="form-control"
							id="buyerPhoneId" placeholder="收货人电话">
					</div>		
					<div class="form-group">
						<!-- <label class="sr-only" for="waybillStatus">waybillStatus</label> <input
							type="text" class="form-control" id="waybillStatus"
							placeholder="状态">  -->
						<!-- <span class="input-group-addon" id="sizing-addon1">zhuang'tai</span>	 -->
						<select class="form-control" id="waybillStatusId" style="width: 150px;">
						    <option value="">所有</option>
							<option value="0">未配送</option>
							<option value="1">配送中</option>
							<option value="5">完成</option>
						</select>
					</div>										
					<button type="button" onclick="javascript:searchMe()" class="btn btn-default" id="dataSearchId">search</button>
				</form>
				<div class="table-responsive">
            <table class="table table-striped">
              <thead>
                <tr>
                  <th>运单编号(商家)</th>
                  <th>配送公司</th>
                  <th>配送员姓名</th>
                  <th>配送员电话</th>
                  <th>买家地址</th>
                  <th>买家姓名/电话</th>
                  <th>运单状态</th>
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

