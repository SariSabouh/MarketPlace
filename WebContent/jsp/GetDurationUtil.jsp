<%@page import="cs499.controllers.BlackboardHandler"%>
<%@page import="com.google.gson.JsonArray"%>
<%@page import="java.util.List"%>
<%@ taglib uri="/bbNG" prefix="bbNG"%>
<%@ taglib uri="/bbData" prefix="bbData"%>
<%
	System.out.println("In GetDurationUtil");
	BlackboardHandler bbHandler = (BlackboardHandler) application.getAttribute("bbHandler");
	String duration = request.getParameter("duration");
	JsonArray json = new JsonArray();
	if(duration.equals("CONTINUOUS")){
		json.add("GRADE");
	}
	else{
		json.add("GRADE");
		json.add("DUEDATE");
		json.add("NUMATTEMPTS");
	}
	response.setContentType("application/json"); 
	response.setCharacterEncoding("utf-8"); 
	out.print(json.toString());
%>