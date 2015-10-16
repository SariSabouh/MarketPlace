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
	
	BlackboardHandler bbHandler = new BlackboardHandler();
	
	// use the GradebookManager to get the gradebook data
	GradebookManager gradebookManager = GradebookManagerFactory.getInstanceWithoutSecurityCheck();
	BookData bookData = gradebookManager.getBookData(new BookDataRequest(courseID));
	List<GradableItem> gradableItemList = gradebookManager.getGradebookItems(courseID);
	// it is necessary to execute these two methods to obtain calculated students and extended grade data
	bookData.addParentReferences();
	bookData.runCumulativeGrading();
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
	// get a list of all the students in the class
	List <CourseMembership> cmlist = CourseMembershipDbLoader.Default.getInstance().loadByCourseIdAndRole(courseID, CourseMembership.Role.STUDENT, null, true);
	Iterator<CourseMembership> i = cmlist.iterator();
	List<Student> students = bbHandler.getStudentsList(i);
	
	// assign gold for grade
	
	// calculate gold per grade
	boolean userCanSeeGold = false;
	String role = sessionUserRole.trim().toLowerCase();
	int myGold = 0;
	if (role.contains("student")  ) {
		userCanSeeGold = true;
		for(Student student : students){
			if(student.getId().getExternalString().equals(sessionUser.getId().getExternalString())){
				for (GradableItem gradeItem : gradableItemList) {
					GradeWithAttemptScore gwas2 = bookData.get(student.getId(), gradeItem.getId());
					Grade grade = (Grade) gwas2;
					if (!gradeItem.getCategory().isEmpty() && !grade.isNullGrade()) {
						if(bbHandler.passesCondition(grade.getScoreValue(), grade)){
							student.setGold(grade.getGoldWorth());
						}
						
					}
				}
				myGold = student.getGold();
			}
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
	href="//code.jquery.com/ui/1.11.4/themes/blitzer/jquery-ui.css">

<script src="//code.jquery.com/jquery-2.1.4.js"></script>

<script src="//code.jquery.com/ui/1.11.4/jquery-ui.js"></script>

<link rel="stylesheet" href="/resources/demos/style.css">


<script>
jQuery.noConflict()
(function($) {
    $( "#tabs" ).tabs();
	var student = <%=userCanSeeGold %>
    if (!student) {
        $('#tabs > ul li:has(a[href="#tabs-1"])').hide()
        $("#tabs").tabs('refresh');
        $("#tabs").tabs('option', 'active', 1);
    }
    
    else{
    	$('#tabs > ul li:has(a[href="#tabs-3"])').hide()
    	$('#tabs > ul li:has(a[href="#tabs-4"])').hide()
        $("#tabs").tabs('refresh');
        $("#tabs").tabs('option', 'active', 1);
    }
  })(jQuery);

</script>

</head>

<body>



	<div id="tabs">

		<ul>

			<li><a href="#tabs-1">My Items</a></li>

			<li><a href="#tabs-2">Market Place</a></li>
			
			<li><a href="#tabs-3">Add Item</a></li>
			
			<li><a href="#tabs-4">Assign Gold</a></li>

		</ul>

		<div id="tabs-1">

			<p>No Items!</p>
			<div style="position: absolute; bottom: 0; right: 0; width: 100px; text-align:right;">
				 My Gold: <% out.print(myGold); %> 
			</div>

		</div>

		<div id="tabs-2">

			<p><% out.print(allItems); %></p>

		</div>
		
		<div id="tabs-3">

			<p>add</p>

		</div>
		
		<div id="tabs-4">

			<p>AA</p>

		</div>


	</div>

</bbNG:includedPage>

</body>

</html>

