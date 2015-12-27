<%@page import="cs499.controllers.MarketPlaceDAO"%>
<%@ taglib uri="/bbNG" prefix="bbNG"%>
<%@ taglib uri="/bbData" prefix="bbData"%>
<%
	MarketPlaceDAO dbController = new MarketPlaceDAO(false);
	dbController.emptyDatabase();
	System.out.println("Truncated");
%>