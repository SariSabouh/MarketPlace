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
<%@page import="itemHandler.*"%>	
<%@page import="itemLoot.*"%>	
<%@ taglib uri="/bbData" prefix="bbData"%> 					
<%@ taglib uri="/bbNG" prefix="bbNG"%>
<bbNG:includedPage ctxId="ctx">

	<%
	// get the current user
	User sessionUser = ctx.getUser();
	Id courseID = ctx.getCourseId();		
	String sessionUserRole = ctx.getCourseMembership().getRoleAsString();	
	String sessionUserID = sessionUser.getId().toString();
	
	// use the GradebookManager to get the gradebook data
	GradebookManager gm = GradebookManagerFactory.getInstanceWithoutSecurityCheck();
	BookData bookData = gm.getBookData(new BookDataRequest(courseID));
	List<GradableItem> lgm = gm.getGradebookItems(courseID);
	// it is necessary to execute these two methods to obtain calculated students and extended grade data
	bookData.addParentReferences();
	bookData.runCumulativeGrading();
	// get a list of all the students in the class
	List <CourseMembership> cmlist = CourseMembershipDbLoader.Default.getInstance().loadByCourseIdAndRole(courseID, CourseMembership.Role.STUDENT, null, true);
	Iterator<CourseMembership> i = cmlist.iterator();
	
	// instructors will see student names
	boolean canSeeGold = false;
	String role = sessionUserRole.trim().toLowerCase();
	// out.print("dave, role is " + role);
	if (role.contains("student")  ) {
		canSeeGold = true;
	}
	
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
	List<Item> itemList = itemContr.getItemList();
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
	href="http://code.jquery.com/ui/1.11.4/themes/south-street/jquery-ui.css">

<script src="http://code.jquery.com/jquery-2.1.4.js"></script>

<script src="//code.jquery.com/ui/1.11.4/jquery-ui.js"></script>

<link rel="stylesheet" href="/resources/demos/style.css">


<script>
  $(function() {
    $( "#tabs" ).tabs();
	var student = <%=canSeeGold %>
    if (!student) {
        $('#tabs > ul li:has(a[href="#tabs-3"])').hide()
        $("#tabs").tabs('refresh');
        $("#tabs").tabs('option', 'active', 1);
    }
    
    else{
    	$('#tabs > ul li:has(a[href="#tabs-4"])').hide()
        $("#tabs").tabs('refresh');
        $("#tabs").tabs('option', 'active', 1);
    }

  });

</script>

</head>

<body>



	<div id="tabs">

		<ul>

			<li><a href="#tabs-1">My Items</a></li>

			<li><a href="#tabs-2">Market Place</a></li>
			
			<li><a href="#tabs-3">My Gold</a></li>
			
			<li><a href="#tabs-4">Add Item</a></li>

		</ul>

		<div id="tabs-1">

			<p>No Items!</p>

		</div>

		<div id="tabs-2">

			<p><% out.print(allItems); %></p>

		</div>
		
		<div id="tabs-3">

			<p>Gold</p>

		</div>
		
		<div id="tabs-4">

			<p>add</p>

		</div>


	</div>

</bbNG:includedPage>

</body>

</html>

