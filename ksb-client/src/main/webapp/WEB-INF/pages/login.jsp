<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="zh-CN">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- 上述3个meta标签*必须*放在最前面，任何其他内容都*必须*跟随其后！ -->
    <meta name="description" content="">
    <meta name="author" content="">

    <title>Signin Template for Bootstrap</title>

    <!-- Bootstrap core CSS -->
    <!-- <link href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.min.css" rel="stylesheet"> -->
    <link href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.min.css" rel="stylesheet">
    
    <link href="${pageContext.request.contextPath}/css/signin.css" rel="stylesheet">
    <script src="${pageContext.request.contextPath}/js/jquery/jquery-2.1.4.min.js"></script>



    <!-- Just for debugging purposes. Don't actually copy these 2 lines! -->
    <!--[if lt IE 9]><script src="../../assets/js/ie8-responsive-file-warning.js"></script><![endif]-->
   <!--  <script src="../../assets/js/ie-emulation-modes-warning.js"></script> -->

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
      <script src="http://cdn.bootcss.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="http://cdn.bootcss.com/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->
<script type="text/javascript">
	function login() {
		var un = $("#inputName").get(0).value
		var passwd = $("#inputPassword").get(0).value
		if (un == '3gongli' && passwd == '3gongli') {
			//$.get('/security/login',function(){
				//document.Url="/security/doLogin";
				location.href="/openapi/main"
			//});
			//$("loginForm").submit();
		} else {
			alert("用户名或者密码错误!")
		}
	}
</script>
</head>

  <body>

    <div class="container">

      <form class="form-signin" id="loginForm">
        <h2 class="form-signin-heading">Please sign in</h2>
        <label for="inputName" class="sr-only">User name</label>
        <input type="email" id="inputName" class="form-control" placeholder="User name" required autofocus>
        <label for="inputPassword" class="sr-only">Password</label>
        <input type="password" id="inputPassword" class="form-control" placeholder="Password" required>
        <div class="checkbox">
          <label>
            <input type="checkbox" value="remember-me"> Remember me
          </label>
        </div>
        <button class="btn btn-lg btn-primary btn-block" type="button" onclick="javascript:login()">Sign in</button>
      </form>

    </div> <!-- /container -->

  </body>
</html>
