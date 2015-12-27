<%@page import="cs499.controllers.BlackboardHandler"%>
<%@page import="cs499.controllers.MarketPlaceDAO"%>
<%@page import="cs499.itemHandler.Item"%>
<%@ taglib uri="/bbNG" prefix="bbNG"%>
<%@ taglib uri="/bbData" prefix="bbData"%>
<%
	System.out.println("In UseItemUtil");
	BlackboardHandler bbHandler = (BlackboardHandler) application.getAttribute("bbHandler");
	MarketPlaceDAO dbController = new MarketPlaceDAO(false);
	String itemName = request.getParameter("itemName");
	Item item = dbController.loadItem(itemName);
	String columnName = request.getParameter("columnName");
	try{
		System.out.println("Item loaded: " + item.getName());
		bbHandler.useItem(item, columnName);
	}catch(Exception e){
		e.printStackTrace();
		response.setStatus(500);
	}
	System.out.println("Item Use ENDED");
%>