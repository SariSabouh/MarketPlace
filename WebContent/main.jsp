<%@page import="blackboard.base.*"%>
<%@page import="blackboard.data.course.*"%> 				
<%@page import="blackboard.data.user.*"%> 					
<%@page import="blackboard.persist.*"%>
<%@page import="blackboard.persist.content.*"%> 					
<%@page import="blackboard.persist.course.*"%> 	
<%@page import="blackboard.platform.gradebook2.*"%>
<%@page import="blackboard.platform.gradebook2.impl.*"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Scanner"%>
<%@page import="cs499.controllers.*"%>
<%@page import="cs499.itemHandler.Item"%>
<%@page import="cs499.util.Student"%>
<%@page import="java.io.InputStream"%>
<%@page import="blackboard.platform.plugin.PlugInUtil"%>
<%@ taglib uri="/bbData" prefix="bbData"%> 					
<%@ taglib uri="/bbNG" prefix="bbNG"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<bbNG:includedPage ctxId="ctx">

<%
	// get the current user
	User sessionUser = ctx.getUser();
	Id courseID = ctx.getCourseId();		
	String sessionUserRole = ctx.getCourseMembership().getRoleAsString();	
	String sessionUserID = sessionUser.getId().toString();	
	System.out.println("\n\nSession Started for " + sessionUserRole);
	MarketPlaceDAO dbController = new MarketPlaceDAO(false);
	List<Item> itemList = dbController.loadItems();
	if(dbController.loadItem("ITEM_INIT") == null){
		System.out.println("Initializing Database.");
		InputStream in = this.getClass().getClassLoader() .getResourceAsStream("/dataSeed.txt");
		String content = new Scanner(in).useDelimiter("\\Z").next();
		itemList = dbController.initilizeDatabase(content);
	}	
	BlackboardHandler bbHandler = new BlackboardHandler(courseID, sessionUser, itemList);
	application.setAttribute("bbHandler", bbHandler);
	
	// get Gold for student
	boolean isStudent = false;
	String role = sessionUserRole.trim().toLowerCase();
	List<Item> myItems = new ArrayList<Item>();
	int myGold = 0;
	if (role.contains("student")) {
		isStudent = true;
		Student student = bbHandler.getStudent();
		myGold = student.getGold();
		for(Item item: student.getItemList()){
			myItems.add(item);
		}
		pageContext.setAttribute("myItems", myItems);
		
	}
	
	session.setAttribute("itemList", itemList);
	List<Item> allItems = new ArrayList<Item>();
	for(Item item: itemList){
		if(!dbController.isOutOfSupply(item)){
			allItems.add(item);
		}
	}
	pageContext.setAttribute("allItems", allItems);
	List<String> columnNames = bbHandler.getAllColumnsByType("ALL");
	pageContext.setAttribute("columnNames", columnNames);
	String getDurationURL = PlugInUtil.getUri("dt", "MarketPlace", "jsp/GetDurationUtil.jsp");
	String addItemURL = PlugInUtil.getUri("dt", "MarketPlace", "jsp/AddItemUtil.jsp");
	String buyItemURL = PlugInUtil.getUri("dt", "MarketPlace", "jsp/BuyItemUtil.jsp");
	String useItemURL = PlugInUtil.getUri("dt", "MarketPlace", "jsp/UseItemUtil.jsp");
	String TRUNCATEURL = PlugInUtil.getUri("dt", "MarketPlace", "jsp/TRUNCATE.jsp");
	String getListURL = PlugInUtil.getUri("dt", "MarketPlace", "jsp/GetListUtil.jsp");
%>

<!doctype html>

<html lang="en">

<head>

<meta charset="utf-8">

<title>Market Place</title>

<link rel="stylesheet"
	href="//code.jquery.com/ui/1.11.4/themes/blitzer/jquery-ui.css">

<script src="//code.jquery.com/jquery-2.1.4.js"></script>

<script src="//code.jquery.com/ui/1.11.4/jquery-ui.js"></script> 

</head>

<body>

<input type="hidden" id="isStudent" name="isStudent" value="<%=isStudent%>"/>
<input type="hidden" id="columnNames" name="columnNames" value="${columnNames}"/>
<input type="hidden" id="buyItemURL" name="buyItemURL" value="<%=buyItemURL%>"/>
<input type="hidden" id="addItemURL" name="addItemURL" value="<%=addItemURL%>"/>
<input type="hidden" id="getDurationURL" name="getDurationURL" value="<%=getDurationURL%>"/>
<input type="hidden" id="useItemURL" name="useItemURL" value="<%=useItemURL%>"/>
<input type="hidden" id="getListURL" name="getListURL" value="<%=getListURL%>"/>
<input type="hidden" id="TRUNCATE" name="TRUNCATE" value="<%=TRUNCATEURL%>"/>


	<div id="tabs">

		<ul>

			<li><a href="#tabs-1">My Items</a></li>

			<li><a href="#tabs-2">Market Place</a></li>
			
			<li><a href="#tabs-3">Add Item</a></li>
			
			<li><a href="#tabs-4">Settings</a></li>

		</ul>

		<div id="tabs-1">
		<form id="myRadioButtons">
			<c:forEach items="${myItems}" var="item">  
				<TR>  
					<td><label>
					<input type="radio" name="itemRadio" value="${item.name}">${item.name}</label></td>
				    <td><p></p></td>
				</TR>
			</c:forEach>
		</form>
			<p></p>
			
			<div id="columnList">
			  <p>Choose Item To Use. Then...</p>
              <label>Please Select From the List. After You Choose It Will Get Activated: 
                <select id="mySelect">
                	<option>NONE</option>
	                <c:forEach items="${columnNames}" var="name">
						<option class="MyItems">${name}</option>
					</c:forEach>
					<option>ALL</option>
                </select>
              </label>
            </div>
						
			<div style="position: absolute; bottom: 0; right: 0; width: 100px; text-align:right;">
				 My Gold: <% out.print(myGold); %> 
			</div>

		</div>

		<div id="tabs-2">
		
			<c:forEach items="${allItems}" var="item">  
				<TR>  
				    <td><a title="${item}" class="Items" href="#" 
						    style="background-color:#FFFFFF;color:#000000;text-decoration:none">${item.name}</a></td>
				    <td><a style="position: absolute; right: 0; text-align:right;">Cost: ${item.cost}</a></td>
				    <td><p></p></td>
				</TR>
			</c:forEach>

		</div>
		
		<div id="tabs-3">
			<p>Add New Item</p>
			Name: <input type="text" id="newItemName"/>
			<p></p>
			Duration: 
            <select id="newItemDuration">
           		<option class="Duration">ONCE</option>
	            <option class="Duration">CONTINUOUS</option>
	            <option class="Duration">PASSIVE</option>
            </select>
            <input value="Duration Length:0" type="text" id="customDuration" style="position: absolute; right:0; text-align:right;">
            <p></p>
            Assessment Type:
            <select id="newItemAssessment">
           		<option>ASSIGNMENT</option>
	            <option>TEST</option>
	            <option>ALL</option>
            </select>
            <p></p>
            Attribute Affected:
            <select id="newAttributeAffected">
           		<option>GRADE</option>
	            <option>DUEDATE</option>
	            <option>NUMATTEMPTS</option>
            </select>
            <p></p>
            Cost: <input type="text" id="newItemCost"/>
            <p></p>
            Effect Magnitude: <input type="text" id="newItemMagnitude"/>
            <p></p>
            Supply: <input type="text" id="newItemSupply"/>
			<p></p>
			<input id="addItem" type="button" value="Add Item">
		</div>
		
		<div id="tabs-4">

			<a class="Truncate" href="#">Erase All Information But The Preset Items.</a>

		</div>


	</div>
	
	<script type="text/javascript">
		<jsp:include page="js/MarketPlaceUtil.js" />
	</script>

</bbNG:includedPage>

</body>

</html>

