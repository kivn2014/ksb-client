
<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<!--[if IE 8]> <html class="ie8" lang="zh"> <![endif]-->
<!--[if IE 9]> <html class="ie9" lang="zh"> <![endif]-->
<!--[if !IE]><!-->
<html lang="zh">
<!--<![endif]-->
<head>
<meta charset="utf-8" />
<title>快送宝 在线提交运单</title>

<link
	href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.min.css"
	rel="stylesheet">
<script
	src="${pageContext.request.contextPath}/js/jquery/jquery-2.1.4.min.js"></script>

<script type="text/javascript">

/* 	$(document).ready(function() {

		$('#search_from_suggest').bind('input', function() {

			var ad = $("#from_address_suggest").get(0).value;

			var a = '/security/bd_address?address=' + encodeURIComponent(ad)

			$.get(a, {}, function(data) {
				var containerBody = $("#address-select").empty();
				var htm = "";
				data = $.parseJSON(data);

				result = data.result;

				$.each(result, function(i, n) {

					htm += "<option>" + n.name + "</option>"

				});

				containerBody.html(htm);

			});

		});

	}); */
	
	function savewaybill(){
		
		var bn = $("#buyer_name").get(0).value;
		var bp = $("#buyer_phone").get(0).value;
		var ba = $("#buyer_address").get(0).value;
		
		var sn = $("#shipper_name").get(0).value;
		var sp = $("#shipper_phone").get(0).value;
		var sa = $("#shipper_address").get(0).value;
		
		var pt = $("#pay_type").get(0).value;
		var ip1 = $("#is_prepay").get(0).value;
		
		$.post('<%=request.getContextPath()%>/openapi/do_add_waybill', 
		{
			"buyer_name":bn,
			"buyer_phone":bp,
			"buyer_address":ba,
			"shipper_name":sn,
			"shipper_phone":sp,
			"shipper_address":sa,
			"pay_type":pt,
			"is_prepay":ip1
		}, 
	    function(data) {
			data = $.parseJSON(data);

			s = data.success;
			if (!s) {//执行失败
				alert(data.errors);
				return;
			}else{
				alert("您的运单已经入库,请后续关注");
				return;
			}


		});

	}
</script>

</head>
<body>


	<div class="panel-body clearfix" id="search_from_suggest">
		<div class="form-body">
			<div class="form-group">
				<h3 class='color-gray'>
					<strong>在线提交运单(发货人和收货人信息会自动保存)</strong>
				</h3>
				<h5 class='color-gray'>
					<strong>如果地址正确但是系统无法识别，请输入附近大厦、学校、商场等标志性建筑 (例如：中关村图书大厦)</strong>
				</h5>			
				</br>
					<div class="input-group col-xs-12 col-sm-4 col-md-4 col-lg-4">
						<!-- <input class="form-control" type="select" autocomplete="off"
							placeholder="支付类型" id="pay_type" />  -->
						<select class="form-control" id="pay_type" style="width: 450px;">
						    <option value="0">支付:现金</option>
							<option value="1">支付:支付宝</option>
							<option value="2">支付:微信</option>
							<option value="3">支付:银联卡</option>
						</select>							
					</div>	
					<div class="input-group col-xs-12 col-sm-4 col-md-4 col-lg-4"></div>	
					<div class="input-group col-xs-12 col-sm-4 col-md-4 col-lg-4">
						<!-- <input class="form-control" type="select" autocomplete="off"
							placeholder="支付类型" id="pay_type" />  -->
						<select class="form-control" id="is_prepay" style="width: 450px;">
						    <option value="0">垫付:否</option>
							<option value="1">垫付:是</option>
						</select>							
					</div>		
					<div class="input-group col-xs-12 col-sm-4 col-md-4 col-lg-4"></div>				
				</br>
				<div id="from_address_selector">
					<div class="input-group col-xs-12 col-sm-4 col-md-4 col-lg-4">
						<input class="form-control" type="text" autocomplete="off"
							placeholder="发货人姓名" id="shipper_name" /> 
					</div>
					<div class="input-group col-xs-12 col-sm-4 col-md-4 col-lg-4"></div>
					<div class="input-group col-xs-12 col-sm-4 col-md-4 col-lg-4">
						<input class="form-control" type="text" autocomplete="off"
							placeholder="发货人电话(手机)" id="shipper_phone" /> 
					</div>
					<div class="input-group col-xs-12 col-sm-4 col-md-4 col-lg-4"></div>
					<div class="input-group col-xs-12 col-sm-4 col-md-4 col-lg-4">
						<input class="form-control" type="text" autocomplete="off"
							placeholder="发货人地址" id="shipper_address" /> 
					</div>		
					</br>			
					<div class="input-group col-xs-12 col-sm-4 col-md-4 col-lg-4"></div>
					<div class="input-group col-xs-12 col-sm-4 col-md-4 col-lg-4">
						<input class="form-control" type="text" autocomplete="off"
							placeholder="收货人姓名" id="buyer_name" /> 
					</div>		
					<div class="input-group col-xs-12 col-sm-4 col-md-4 col-lg-4"></div>
					<div class="input-group col-xs-12 col-sm-4 col-md-4 col-lg-4">
						<input class="form-control" type="text" autocomplete="off"
							placeholder="收货人电话(手机)" id="buyer_phone" /> 
					</div>					
					<div class="input-group col-xs-12 col-sm-4 col-md-4 col-lg-4"></div>
					<div class="input-group col-xs-12 col-sm-4 col-md-4 col-lg-4">
						<input class="form-control" type="text" autocomplete="off"
							placeholder="收货人地址" id="buyer_address" /> 
					</div>	
					</br>
					<div>
					<button type="button" onclick="javascript:savewaybill()" class="btn btn-default" id="dataSearchId">submit</button>
					</div>									
				</div>
			</div>

		</div>
	</div>


</body>
</html>
