<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="devonenterprise.service.cache.refresher.*" %>
<%@ page import="devonenterprise.ext.service.cache.refresher.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>초기화</title>
</head>
<body>
케쉬 작업 시작..
<%
try{
	devon.core.util.cache.manager.LCacheManager.reset();
	%>
    처리 완료 되었습니다.
	<%}catch(Throwable e){
    e.printStackTrace();
%>
	<%= e.toString() %>
<% } %>


</body>
</html>