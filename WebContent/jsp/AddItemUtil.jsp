<%@page import="cs499.object.Item.AssessmentType"%>
<%@page import="cs499.object.Item.AttributeAffected"%>
<%@page import="cs499.controllers.MarketPlaceDAO"%>
<%@page import="cs499.object.Item"%>
<%@ taglib uri="/bbNG" prefix="bbNG"%>
<%@ taglib uri="/bbData" prefix="bbData"%>
<%
	System.out.println("In AddItemUtil");
	MarketPlaceDAO marketPlaceDAO = new MarketPlaceDAO(false, (String) application.getAttribute("courseId"));
	Item item = new Item(request.getParameter("name"));
	item.setAttributeAffected(AttributeAffected.valueOf(request.getParameter("attributeAffected")));
	item.setCost(Integer.parseInt(request.getParameter("cost")));
	item.setDuration(request.getParameter("duration"));
	item.setEffectMagnitude(Float.parseFloat(request.getParameter("effectMagnitude")));
	item.setSupply(Integer.parseInt(request.getParameter("supply")));
	item.setType(AssessmentType.valueOf(request.getParameter("assessmentType")));
	try{
		marketPlaceDAO.addItem(item);
	}catch(Exception e){
		e.printStackTrace();
		response.setStatus(500);
	}
%>