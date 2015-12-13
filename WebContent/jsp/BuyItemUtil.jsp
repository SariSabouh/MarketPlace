<%@page import="cs499.controllers.BlackboardHandler"%>
<%@page import="cs499.controllers.MarketPlaceDAO"%>
<%@page import="cs499.exceptions.ItemProcessException"%>
<%@ taglib uri="/bbNG" prefix="bbNG"%>
<%@ taglib uri="/bbData" prefix="bbData"%>
<%
	System.out.println("In BuyItemUtil");
	BlackboardHandler bbHandler = (BlackboardHandler) application.getAttribute("bbHandler");
	MarketPlaceDAO dbController = new MarketPlaceDAO(false);
	String itemName = request.getParameter("itemName");
	try {
		if(dbController.loadItem(itemName) == null){
			System.out.println("Fail to load item " + itemName);
			throw new ItemProcessException("Item Purchase Failed. Item is not in Database");
		}
		bbHandler.processItem(itemName);
		System.out.println("PURCHASE ENDED");
	}catch (ItemProcessException e) {
		e.printStackTrace();
	}
%>