<%@page import="cs499.controllers.BlackboardHandler"%>
<%@page import="com.google.gson.JsonArray"%>
<%@page import="java.util.List"%>
<%@ taglib uri="/bbNG" prefix="bbNG"%>
<%@ taglib uri="/bbData" prefix="bbData"%>
<%
	BlackboardHandler bbHandler = (BlackboardHandler) application.getAttribute("bbHandler");
	List<String> columns = (bbHandler.getAllColumnsByType(request.getParameter("category")));
	JsonArray json = new JsonArray();
	for(String name : columns){
		json.add(name);
	}
	response.setContentType("application/json"); 
	response.setCharacterEncoding("utf-8"); 
	out.print(json.toString());
%>