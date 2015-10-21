<%@page import="blackboard.base.*"%>
<%@page import="blackboard.data.course.*"%> 				
<%@page import="blackboard.data.user.*"%> 					
<%@page import="blackboard.persist.*"%>
<%@page import="blackboard.persist.content.*"%> 					
<%@page import="blackboard.persist.course.*"%> 	
<%@page import="blackboard.platform.gradebook2.*"%>
<%@page import="blackboard.platform.gradebook2.impl.*"%>
<%@page import="blackboard.data.content.Content"%>
<%@page import="blackboard.persist.navigation.CourseTocDbLoader"%>
<%@page import="blackboard.data.navigation.CourseToc"%>
<%@page import="java.util.*"%> 								
<%@page import="cs499.controllers.*"%>
<%@page import="cs499.itemHandler.*"%>
<%@ taglib uri="/bbData" prefix="bbData"%> 					
<%@ taglib uri="/bbNG" prefix="bbNG"%>
<bbNG:includedPage ctxId="ctx">

<%
	// get the current user
	User sessionUser = ctx.getUser();
	Id courseID = ctx.getCourseId();		
	String sessionUserRole = ctx.getCourseMembership().getRoleAsString();	
	String sessionUserID = sessionUser.getId().toString();	
	BlackboardHandler bbHandler = new BlackboardHandler(courseID, sessionUser);
	
	session.setAttribute("bbHandler", bbHandler);
	
	// get items list from contents
	ContentDbLoader contentDb = ContentDbLoader.Default.getInstance();
	CourseTocDbLoader cTocLoader = CourseTocDbLoader.Default.getInstance();
	List<CourseToc> tableOfContents = cTocLoader.loadByCourseId(courseID);
	List<Content> tableChildren = new ArrayList<Content>();
	for (CourseToc t : tableOfContents) {
		if (t.getTargetType() == CourseToc.Target.CONTENT) {
			tableChildren.addAll(contentDb.loadChildren(t.getContentId(), false, null));
		}
	}
	ItemController itemContr = new ItemController();
	String test = null;
	for(Content content : tableChildren){
		if(content.getTitle().equals("itemList")){
			String contentText = content.getBody().getText();
			itemContr.createItemListFromContents(contentText);			
		}
	}
	
	// get Gold for student
	boolean userCanSeeGold = false;
	String role = sessionUserRole.trim().toLowerCase();
	String error = "";
	String myItems = "";
	int myGold = 0;
	if (role.contains("student")) {
		userCanSeeGold = true;
		error = bbHandler.setStudentGold();
		Student student = bbHandler.getStudent();
		myGold = student.getGold();
		for(Item item : student.getItemList()){
			myItems += item.toString() + " AA ";
		}
		
	}
	
	List<Item> itemList = itemContr.getItemList();
	bbHandler.setItemList(itemList);
	session.setAttribute("itemList", itemList);
	String allItems = "";
	for(Item item: itemList){
		allItems += item.toString() + "<br /><br />";
	}

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

<input type="hidden" id="userCanSeeGold" name="userCanSeeGold" value="<%=userCanSeeGold%>"/>

	<div id="tabs">

		<ul>

			<li><a href="#tabs-1">My Items</a></li>

			<li><a href="#tabs-2">Market Place</a></li>
			
			<li><a href="#tabs-3">Add Item</a></li>

		</ul>

		<div id="tabs-1">

			<p><% out.print(myItems); %></p>
			<div style="position: absolute; bottom: 0; right: 0; width: 100px; text-align:right;">
				 My Gold: <% out.print(myGold); %> 
			</div>

		</div>

		<div id="tabs-2">

			<p><% out.print(allItems); %></p>
			<a class="Items" name="PickAxe" href="#">PickAxe</a>

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

