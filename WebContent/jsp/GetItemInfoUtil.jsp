<%@page import="cs499.controllers.MarketPlaceDAO"%>
<%@page import="com.google.gson.JsonArray"%>
<%@page import="cs499.object.Item"%>
<%@ taglib uri="/bbNG" prefix="bbNG"%>
<%@ taglib uri="/bbData" prefix="bbData"%>
<%
	System.out.println("In GetItemInfoUtil");
	MarketPlaceDAO marketPlaceDao = new MarketPlaceDAO(false, (String) application.getAttribute("courseId"));
	Item item = null;
	try{
		item = marketPlaceDao.loadItem(request.getParameter("name"));
	}catch(NullPointerException e){
		e.printStackTrace();
		response.setStatus(500);
	}
	JsonArray json = new JsonArray();
	json.add(item.getCost());
	json.add(item.getDuration());
	json.add(item.getEffectMagnitude());
	json.add(item.getSupply());
	json.add(item.getAttributeAffected().toString());
	json.add(item.getType().toString());
	response.setContentType("application/json"); 
	response.setCharacterEncoding("utf-8"); 
	out.print(json.toString());
%>