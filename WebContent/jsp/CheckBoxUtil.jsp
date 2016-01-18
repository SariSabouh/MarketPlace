<%@page import="cs499.controllers.MarketPlaceDAO"%>
<%@page import="cs499.util.Setting"%>
<%@ taglib uri="/bbNG" prefix="bbNG"%>
<%@ taglib uri="/bbData" prefix="bbData"%>
<%
	System.out.println("In CheckBoxUtil");
	MarketPlaceDAO marketPlaceDAO = new MarketPlaceDAO(false);
	try{
		String name = request.getParameter("name");
		String value = request.getParameter("value");
		Setting setting = new Setting();
		setting.setName(name);
		setting.setValue(value);
		marketPlaceDAO.updateSetting(setting);
	}catch(Exception e){
		e.printStackTrace();
		response.setStatus(500);
	}
%>