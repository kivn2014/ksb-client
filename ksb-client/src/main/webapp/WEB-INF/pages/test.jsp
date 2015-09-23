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

    <title>test</title>
    <body > 
      <form name="uploadForm" method="POST" action="sp_add_address"> 
 
        商家编号:<input type="text" name="sp_id" value="643339214603026432" size="30"/><br/>
        <!-- 地址编号:<input type="text" name="id" value="1" size="10"/><br/> -->
        联系人:<input type="text" name="contact" value="王先生" size="30"/><br/>
        联系人电话:<input type="text" name="phone" value="110" size="30"/> <br/>
        地址:<input type="text" name="address" value="北京市海淀区苏州街银科大厦" size="30"/> <br/>
        铭牌号:<input type="text" name="address_detail" value="8层" size="30"/> <br/>
        经度:<input type="text" name="address_x" value="116.590872" size="30"/><br/>
        维度:<input type="text" name="address_y" value="39.4348762" size="30"/><br/>
        citycode:<input type="text" name="city_code" value="010" size="30"/> <br/>
        
        <input type="submit" name="submit" value="submit"> 
        <input type="reset" name="reset" value="reset"> 
      </form> 
    </body> 
</html> 