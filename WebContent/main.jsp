<%@page import="blackboard.base.*"%>
<%@page import="blackboard.data.course.*"%> 				
<%@page import="blackboard.data.user.*"%> 					
<%@page import="blackboard.persist.*"%>
<%@page import="blackboard.persist.content.*"%> 					
<%@page import="blackboard.persist.course.*"%> 	
<%@page import="blackboard.platform.gradebook2.*"%>
<%@page import="blackboard.platform.gradebook2.impl.*"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Scanner"%>
<%@page import="cs499.controllers.*"%>
<%@page import="cs499.itemHandler.Item"%>
<%@page import="cs499.util.Student"%>
<%@page import="java.io.InputStream"%>
<%@page import="blackboard.platform.plugin.PlugInUtil"%>
<%@ taglib uri="/bbData" prefix="bbData"%> 					
<%@ taglib uri="/bbNG" prefix="bbNG"%>
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
	String myItems = "";
	int myGold = 0;
	if (role.contains("student")) {
		isStudent = true;
		Student student = bbHandler.getStudent();
		myGold = student.getGold();
		for(Item item : student.getItemList()){
			myItems += item.getName() + "<br />";
		}
		
	}
	
	session.setAttribute("itemList", itemList);
	String allItems = "";
	for(Item item: itemList){
		allItems += item.toString() + "<br /><br />";
	}
	String buyItemURL = PlugInUtil.getUri("dt", "MarketPlace",	"jsp/BuyItemUtil.jsp");
	String useItemURL = PlugInUtil.getUri("dt", "MarketPlace",	"jsp/UseItemUtil.jsp");

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
<input type="hidden" id="buyItemURL" name="buyItemURL" value="<%=buyItemURL%>"/>
<input type="hidden" id="useItemURL" name="useItemURL" value="<%=useItemURL%>"/>

	<div id="tabs">

		<ul>

			<li><a href="#tabs-1">My Items</a></li>

			<li><a href="#tabs-2">Market Place</a></li>
			
			<li><a href="#tabs-3">Add Item</a></li>

		</ul>

		<div id="tabs-1">

			<p><% out.print(myItems); %></p>
			<a class="MyItems" name="PickAxe" href="#">USE</a>
			<div style="position: absolute; bottom: 0; right: 0; width: 100px; text-align:right;">
				 My Gold: <% out.print(myGold); %> 
			</div>

		</div>

		<div id="tabs-2">

			<p><% out.print(allItems); %></p>
			<a class="Items" name="PickAxe" href="#">BUY</a>

		</div>
		
		<div id="tabs-3">

			<p>add</p>

		</div>


	</div>
	
	<script type="text/javascript">
		<jsp:include page="js/MarketPlaceUtil.js" />
	</script>

</bbNG:includedPage>

</body>

</html>

