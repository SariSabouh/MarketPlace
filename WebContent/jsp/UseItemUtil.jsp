<%@page import="cs499.controllers.BlackboardHandler"%>
<%@page import="cs499.controllers.MarketPlaceDAO"%>
<%@page import="cs499.itemHandler.Item"%>
<%@page import="cs499.exceptions.ItemUseException"%>
<%@ taglib uri="/bbNG" prefix="bbNG"%>
<%@ taglib uri="/bbData" prefix="bbData"%>
<%
	System.out.println("In UseItemUtil");
	BlackboardHandler bbHandler = (BlackboardHandler) application.getAttribute("bbHandler");
	MarketPlaceDAO dbController = new MarketPlaceDAO(false);
	Item item = dbController.loadItem("Once");
	try{
		System.out.println("Item loaded: " + item.getName());
		boolean done = bbHandler.useItem(item);
		if(!done){
			throw new ItemUseException("Item was not successfully used.");
		}
	}catch(ItemUseException e){
		e.printStackTrace();
	}
	System.out.println("Item Use ENDED");
%>