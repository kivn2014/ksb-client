
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<!--[if IE 8]> <html class="ie8" lang="zh"> <![endif]-->
<!--[if IE 9]> <html class="ie9" lang="zh"> <![endif]-->
<!--[if !IE]><!-->
<html lang="zh">
<!--<![endif]-->
<head>
<meta charset="utf-8" />
<title>快送宝 地址验证</title>

<link
	href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.min.css"
	rel="stylesheet">
<script
	src="${pageContext.request.contextPath}/js/jquery/jquery-2.1.4.min.js"></script>

<script type="text/javascript">
	$(document).ready(function() {

		$('#search_from_suggest').bind('input', function() {

			var ad = $("#from_address_suggest").get(0).value;

			var a = '/openapi/bd_address?address=' + encodeURIComponent(ad)

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

	});
</script>

</head>
<body>


	<div class="panel-body clearfix" id="search_from_suggest">
		<div class="form-body">
			<div class="form-group">
				<h5 class='color-gray'>
					<strong>建议：输入相关的地址，从系统下拉地址里选择选择</strong>
				</h5>
				<div id="from_address_selector">
					<div class="input-group col-xs-12 col-sm-4 col-md-4 col-lg-4">
						<input class="form-control" type="text" autocomplete="off"
							placeholder="输入您所在的大厦名称或周边标志性建筑" name="from_address_suggest"
							id="from_address_suggest" /> <span class="input-group-btn"></span>
					</div>

				</div>
			</div>

		</div>
	</div>

	</div>

	<form role="form">
		<div class="input-group col-xs-12 col-sm-4 col-md-4 col-lg-4">
			<select multiple class="form-control" id="address-select"
				style="height: 400px;">
				<tbody id="address-detail">
			</select>
		</div>
</body>
</html>
