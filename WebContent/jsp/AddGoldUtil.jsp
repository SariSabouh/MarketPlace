<%@page import="cs499.controllers.BlackboardHandler"%>
<%@ taglib uri="/bbNG" prefix="bbNG"%>
<%@ taglib uri="/bbData" prefix="bbData"%>
<%
	System.out.println("In AddGoldUtil");
	BlackboardHandler bbHandler = (BlackboardHandler) application.getAttribute("bbHandler");
	try{
		bbHandler.addGoldToAll(request.getParameter("gold").replaceAll("[^0-9]", ""));
	}catch(Exception e){
		e.printStackTrace();
		response.setStatus(500);
	}
%>