<%@page import="cs499.controllers.BlackboardHandler"%>
<%@page import="cs499.itemHandler.ItemController"%>
<%@page import="cs499.itemHandler.Item"%>
<%@page import="cs499.dao.DatabaseController"%>
<%@page import="cs499.exceptions.ItemUseException"%>
<%@ taglib uri="/bbNG" prefix="bbNG"%>
<%@ taglib uri="/bbData" prefix="bbData"%>
<%
	BlackboardHandler bbHandler = (BlackboardHandler) application.getAttribute("bbHandler");
	DatabaseController dbController = new DatabaseController();
	ItemController itemController = new ItemController();
	Item item = dbController.loadItem("PickAxe");
	try{
		System.out.println("Item loaded: " + item.getName());
		boolean done = bbHandler.useItem(item);
		if(!done){
			throw new ItemUseException("Item was not successfully used.");
		}
	}catch(ItemUseException e){
		e.printStackTrace();
	}
	out.println("Item Use ENDED");
%>