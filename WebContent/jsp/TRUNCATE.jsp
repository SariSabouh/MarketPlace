<%@page import="cs499.controllers.BlackboardHandler"%>
<%@page import="cs499.controllers.MarketPlaceDAO"%>
<%@page import="cs499.itemHandler.Item"%>
<%@page import="cs499.exceptions.ItemUseException"%>
<%@ taglib uri="/bbNG" prefix="bbNG"%>
<%@ taglib uri="/bbData" prefix="bbData"%>
<%
	if(request.getParameter("itemName").equals("DELETE")){
		MarketPlaceDAO dbController = new MarketPlaceDAO(false);
		dbController.emptyDatabase();
	}
	System.out.println("Truncated");
%>