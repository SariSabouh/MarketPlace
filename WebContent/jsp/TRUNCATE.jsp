<%@page import="cs499.controllers.MarketPlaceDAO"%>
<%@ taglib uri="/bbNG" prefix="bbNG"%>
<%@ taglib uri="/bbData" prefix="bbData"%>
<%
MarketPlaceDAO dbController = new MarketPlaceDAO(false, (String) application.getAttribute("courseId"), (String ) application.getAttribute("instructorId"));
	dbController.emptyDatabase();
	System.out.println("Truncated");
%>