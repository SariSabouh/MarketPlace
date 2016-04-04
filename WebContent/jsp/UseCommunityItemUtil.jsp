<%@page import="cs499.controllers.BlackboardHandler"%>
<%@page import="cs499.controllers.MarketPlaceDAO"%>
<%@page import="cs499.object.CommunityItem"%>
<%@ taglib uri="/bbNG" prefix="bbNG"%>
<%@ taglib uri="/bbData" prefix="bbData"%>
<%
	System.out.println("In UseCommunityItemUtil");
	BlackboardHandler bbHandler = (BlackboardHandler) application.getAttribute("bbHandler");
	MarketPlaceDAO dbController = new MarketPlaceDAO(false, (String) application.getAttribute("courseId"));
	String itemName = request.getParameter("itemName");
	CommunityItem item = new CommunityItem(dbController.loadItem(itemName));
	int downPayment = Integer.parseInt(request.getParameter("downPayment").replaceAll("[^0-9]", ""));
//	if(downPayment < (item.getCost()*0.2)){
	if(downPayment < 1){
		response.setStatus(500);
	}
	String columnName = request.getParameter("columnUsed");
	item.setPaid(downPayment);
	item.setColumnName(columnName);
	System.out.println(columnName + " to be affected");
	try{
		bbHandler.useCommunityItem(item);
	}catch(Exception e){
		e.printStackTrace();
		response.setStatus(500);
	}
	System.out.println("Community Item Set Up ENDED");
%>