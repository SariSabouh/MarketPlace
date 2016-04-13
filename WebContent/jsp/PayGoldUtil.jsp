<%@page import="cs499.controllers.MarketPlaceDAO"%>
<%@page import="cs499.controllers.BlackboardHandler"%>
<%@page import="cs499.object.CommunityItem"%>
<%@page import="cs499.object.Student"%>
<%@ taglib uri="/bbNG" prefix="bbNG"%>
<%@ taglib uri="/bbData" prefix="bbData"%>
<%
	System.out.println("In PayGoldUtil");
	MarketPlaceDAO marketPlaceDAO = new MarketPlaceDAO(false, (String) application.getAttribute("courseId"));
	BlackboardHandler bbHandler = (BlackboardHandler) application.getAttribute("bbHandler");
	int goldPaid = Integer.parseInt(request.getParameter("goldPaid").replaceAll("[^0-9]", ""));
	try{
		CommunityItem item = marketPlaceDAO.getCurrentCommunityItem();
		item.setPaid(goldPaid);
		Student student = bbHandler.getStudent();
		if(student.canAfford(goldPaid)){
			String studentId = student.getStudentID();
			item.setCost(goldPaid);
			student.substractGold(item);
			bbHandler.persistGoldChange(student);
			marketPlaceDAO.addCommunityItemPayment(item, studentId, item.getForeignId());
		}
		else{
			response.setStatus(500);
		}
	}catch(Exception e){
		e.printStackTrace();
		response.setStatus(500);
	}
%>