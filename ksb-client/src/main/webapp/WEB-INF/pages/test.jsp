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
      <form name="uploadForm" method="POST" action="makeup_buyinfo"> 
 
        订单编号:<input type="text" name="wbid" value="3km-1" size="30"/><br/>
        配送员编号:<input type="text" name="cid" value="608224890197114880" size="30"/> <br/>
        买家 名称:<input type="text" name="name" size="30"/> <br/>
        买家 电话:<input type="text" name="phone" size="30"/> <br/>
        买家地址:<input type="text" name="address" size="30"/><br/>
        商家经度:<input type="text" name="sp_x" value="116.31096385344" size="30"/><br/>
        买家地址:<input type="text" name="sp_y" value="39.990285720615" size="30"/> <br/>
        
        
        <input type="submit" name="submit" value="submit"> 
        <input type="reset" name="reset" value="reset"> 
      </form> 
    </body> 
</html> 