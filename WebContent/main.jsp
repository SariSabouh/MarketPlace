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
<%@page import="cs499.object.Item"%>
<%@page import="cs499.object.CommunityItem"%>
<%@page import="cs499.object.Student"%>
<%@page import="cs499.object.Setting"%>
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
	MarketPlaceDAO dbController = new MarketPlaceDAO(false, courseID.getExternalString());
	List<Item> itemList = dbController.loadItems();
	if(dbController.loadItem("ITEM_INIT") == null){
		System.out.println("Initializing Database.");
		InputStream in = this.getClass().getClassLoader() .getResourceAsStream("/dataSeed.txt");
		String content = new Scanner(in).useDelimiter("\\Z").next();
		itemList = dbController.initilizeDatabase(content);
	}	
	BlackboardHandler bbHandler = new BlackboardHandler(courseID, sessionUser, itemList);
	application.setAttribute("bbHandler", bbHandler);
	application.setAttribute("courseId", courseID.getExternalString());
	
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
	CommunityItem communityItem = dbController.getCurrentCommunityItem();
	boolean communityItemExists = false; 
	if(!communityItem.getName().equals("NO$ITEM")){
		communityItemExists = true; 
	}
	List<String> settingNames = new ArrayList<String>();
	List<String> settingValues = new ArrayList<String>();
	for(Setting setting : dbController.getDefaultSettings()){
		settingNames.add(setting.getName());
		settingValues.add(setting.getValue());
	}
	String getDurationURL = PlugInUtil.getUri("jsu", "MarketPlace", "jsp/GetDurationUtil.jsp");
	String addItemURL = PlugInUtil.getUri("jsu", "MarketPlace", "jsp/AddItemUtil.jsp");
	String editItemURL = PlugInUtil.getUri("jsu", "MarketPlace", "jsp/EditItemUtil.jsp");
	String buyItemURL = PlugInUtil.getUri("jsu", "MarketPlace", "jsp/BuyItemUtil.jsp");
	String useItemURL = PlugInUtil.getUri("jsu", "MarketPlace", "jsp/UseItemUtil.jsp");
	String TRUNCATEURL = PlugInUtil.getUri("jsu", "MarketPlace", "jsp/TRUNCATE.jsp");
	String getListURL = PlugInUtil.getUri("jsu", "MarketPlace", "jsp/GetListUtil.jsp");
	String addGoldURL = PlugInUtil.getUri("jsu", "MarketPlace", "jsp/AddGoldUtil.jsp");
	String checkBoxesURL = PlugInUtil.getUri("jsu", "MarketPlace", "jsp/CheckBoxUtil.jsp");
	String getItemInfoURL = PlugInUtil.getUri("jsu", "MarketPlace", "jsp/GetItemInfoUtil.jsp");
	String getItemDescriptionURL = PlugInUtil.getUri("jsu", "MarketPlace", "jsp/GetItemDescriptionUtil.jsp");
	String useCommunityItemURL = PlugInUtil.getUri("jsu", "MarketPlace", "jsp/UseCommunityItemUtil.jsp");
	
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

<input type="hidden" id="communityItemExists" name="communityItemExists" value="<%=communityItemExists%>"/>
<input type="hidden" id="isStudent" name="isStudent" value="<%=isStudent%>"/>
<input type="hidden" id="settingNames" name="settingNames" value="<%=settingNames%>"/>
<input type="hidden" id="settingValues" name="settingValues" value="<%=settingValues%>"/>
<input type="hidden" id="columnNames" name="columnNames" value="${columnNames}"/>
<input type="hidden" id="buyItemURL" name="buyItemURL" value="<%=buyItemURL%>"/>
<input type="hidden" id="addItemURL" name="addItemURL" value="<%=addItemURL%>"/>
<input type="hidden" id="editItemURL" name="editItemURL" value="<%=editItemURL%>"/>
<input type="hidden" id="getDurationURL" name="getDurationURL" value="<%=getDurationURL%>"/>
<input type="hidden" id="useItemURL" name="useItemURL" value="<%=useItemURL%>"/>
<input type="hidden" id="getListURL" name="getListURL" value="<%=getListURL%>"/>
<input type="hidden" id="TRUNCATE" name="TRUNCATE" value="<%=TRUNCATEURL%>"/>
<input type="hidden" id="addGoldURL" name="addGoldURL" value="<%=addGoldURL%>"/>
<input type="hidden" id="checkBoxesURL" name="checkBoxesURL" value="<%=checkBoxesURL%>"/>
<input type="hidden" id="getItemInfoURL" name="getItemInfoURL" value="<%=getItemInfoURL%>"/>
<input type="hidden" id="getItemDescriptionURL" name="getItemDescriptionURL" value="<%=getItemDescriptionURL%>"/>
<input type="hidden" id="useCommunityItemURL" name="useCommunityItemURL" value="<%=useCommunityItemURL%>"/>
	<div id="tabs">

		<ul>

			<li><a href="#tabs-1">My Items</a></li>

			<li><a href="#tabs-2">Market Place</a></li>
			
			<li><a href="#tabs-3">Add Item</a></li>
			
			<li><a href="#tabs-5">Edit Item</a></li>
			
			<li><a href="#tabs-4">Settings</a></li>
			
			<li><a href="#tabs-6">Community Item</a></li>

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
              <label>Please Select Item From the List: 
                <select id="mySelect">
                	<option>NONE</option>
	                <c:forEach items="${columnNames}" var="name">
						<option>${name}</option>
					</c:forEach>
					<option>ALL</option>
                </select>
                </label>
              <br/>
              <input id="useItem" type="button" value="Use Item">
            </div>
            <div>
  				<p id="myItemDescription" style="font-size:75%;"></p>
			</div>
						
			<div style="position: absolute; bottom: 0; right: 0; width: 100px; text-align:right;">
				 My Gold: <% out.print(myGold); %> 
			</div>

		</div>

		<div id="tabs-2">
			<div>
				<form id="storeRadioButtons">
					<c:forEach items="${allItems}" var="item">  
						<TR>
						    <td><label><input type="radio" name="buyItemRadio" value="${item.name}">${item.name}</label></td>
						    <td><a style="float: right;">Cost: ${item.cost}</a></td>
						    <td><p></p></td>
						</TR>
					</c:forEach>
				</form>
			</div>
			<input id="buyItem" type="button" value="Buy Item">
			<input id="directEditItem" type="button" value="Edit Item">
            <input id="communityItemButton" type="button" value="Community Use Item">
			<div>
  				<p id="itemDescription" style="font-size:75%;"></p>
			</div>
		</div>
		
		<div id="tabs-3">
			<p>Add New Item</p>
			Name: <input type="text" id="newItemName"/>
			<p></p>
			Duration: 
            <select id="newItemDuration">
            	<option class="Duration">NONE</option>
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
			<p></p>
			<input id="addGold" type="button" value="Add Gold To All">
			<input type="text" id="addGoldField"/>
			<p></p>
			<input type="checkbox" class="CheckBoxes" id="visible_columns"/>
			<span>Only Show Columns Visible To Students While Using Items</span>
		</div>
		
		<div id="tabs-5">

			<p>Edit Item</p>
			Name: <select id="editItemName">
                	<option>NONE</option>
	                <c:forEach items="${allItems}" var="item">
						<option  class="EditItemClass">${item.name}</option>
					</c:forEach>
                </select>
			<p></p>
			<input value="Duration Length:0" type="text" id="editCustomDuration" style="position: absolute; right:0; text-align:right;">
			Duration: 
            <select id="editItemDuration">
            	<option class="Duration">NONE</option>
           		<option class="Duration">ONCE</option>
	            <option class="Duration">CONTINUOUS</option>
	            <option class="Duration">PASSIVE</option>
            </select>
            <p></p>
            Assessment Type:
            <select id="editItemAssessment">
           		<option>ASSIGNMENT</option>
	            <option>TEST</option>
	            <option>ALL</option>
            </select>
            <p></p>
            Attribute Affected:
            <select id="editAttributeAffected">
           		<option>GRADE</option>
	            <option>DUEDATE</option>
	            <option>NUMATTEMPTS</option>
            </select>
            <p></p>
            Cost: <input type="text" id="editItemCost"/>
            <p></p>
            Effect Magnitude: <input type="text" id="editItemMagnitude"/>
            <p></p>
            Supply: <input type="text" id="editItemSupply"/>
			<p></p>
			<input id="editItem" type="button" value="Edit Item">
			
		</div>

		<div id="tabs-6">
			<div id="noCommunityItemActive">
			Community Item:
				<select id="communityItemsSelect">
					<option>NONE</option>
	                <c:forEach items="${allItems}" var="item">
						<option class="CommunityItemOption">${item.name}</option>
					</c:forEach>
	            </select>
	            <p></p>
				Gold to Pay (More than 1 Gold): <input type="text" id="communityDownPayment"/>
				<p></p>
				Column to Affect: <select id="communityItemColumnSelect">
					<option>NONE</option>
	                <c:forEach items="${columnNames}" var="name">
						<option>${name}</option>
					</c:forEach>
	            </select>
	            <p></p>
	            <input id="communityItemBuy" type="button" value="Community Use Item">
	        </div>
	        <div id="communityItemActive">
				Community Item:<p><%=communityItem.getName()%></p>
				<p>Total amount paid is: <%=communityItem.getPaid() %> out of <%=communityItem.getCost() %>.
				 This item will affect <%=communityItem.getColumnName() %> only!
				<p>To activate this item the total price has to be filled. To participate in the activation please pay some gold for this Community Item
				that will affect the <%=communityItem.getAttributeAffected() %> by <%=communityItem.getEffectMagnitude() %>.</p>
				<input id="payGold" type="button" value="Pay Gold for This Item">
				<input type="text" id="payGoldField"/>
			</div>
		</div>

	</div>
	
	<script type="text/javascript">
		<jsp:include page="js/MarketPlaceUtil.js" />
	</script>

</bbNG:includedPage>

</body>

</html>

