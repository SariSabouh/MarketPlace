<%@page import="cs499.controllers.BlackboardHandler"%>
<%@page import="cs499.dao.DatabaseController"%>
<%@page import="cs499.exceptions.ItemProcessException"%>
<%@ taglib uri="/bbNG" prefix="bbNG"%>
<%@ taglib uri="/bbData" prefix="bbData"%>
<%
	BlackboardHandler bbHandler = (BlackboardHandler) application.getAttribute("bbHandler");
	DatabaseController dbController = new DatabaseController();
	try {
		out.println("request itemName: " + (String) request.getParameter("itemName"));
		if(dbController.loadItem("PickAxe") == null){ // it is supposed to be request.getParameter("itemName")
			out.println("Fail to load item");
			throw new ItemProcessException("Item Purchase Failed. Item is not in Database");
		}
		bbHandler.processItem("PickAxe");
		out.println("PRCESS ENDED");
	}catch (ItemProcessException e) {
		e.printStackTrace();
	}
%>