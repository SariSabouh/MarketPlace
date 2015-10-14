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
<%@page import="controllers.*"%>	
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
	GradebookManager gradebookManager = GradebookManagerFactory.getInstanceWithoutSecurityCheck();
	BookData bookData = gradebookManager.getBookData(new BookDataRequest(courseID));
	List<GradableItem> gradableItemList = gradebookManager.getGradebookItems(courseID);
	// it is necessary to execute these two methods to obtain calculated students and extended grade data
	bookData.addParentReferences();
	bookData.runCumulativeGrading();
	// get a list of all the students in the class
	List <CourseMembership> cmlist = CourseMembershipDbLoader.Default.getInstance().loadByCourseIdAndRole(courseID, CourseMembership.Role.STUDENT, null, true);
	Iterator<CourseMembership> i = cmlist.iterator();
	List<Student> students = new ArrayList<Student>();
	while(i.hasNext()){
		CourseMembership selectedMember = (CourseMembership) i.next();
		User currentUser = selectedMember.getUser();
		Student student = new Student();
		student.setFirstName(currentUser.getGivenName());
		student.setLastName(currentUser.getFamilyName());
		student.setStudentID(currentUser.getStudentId());
		student.setUserName(currentUser.getUserName());
		students.add(student);
	}
	
	// instructors will see student names
	boolean canSeeGold = false;
	String role = sessionUserRole.trim().toLowerCase();
	int myGold = 0;
	if (role.contains("student")  ) {
		canSeeGold = true;
		for(Student student : students){
			if(student.getStudentID().equals(sessionUser.getStudentId())){
				myGold = student.getGold();
			}
		}
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
	href="//code.jquery.com/ui/1.11.4/themes/blitzer/jquery-ui.css">

<script src="//code.jquery.com/jquery-2.1.4.js"></script>

<script src="//code.jquery.com/ui/1.11.4/jquery-ui.js"></script>

<link rel="stylesheet" href="/resources/demos/style.css">


<script>
jQuery.noConflict()
(function($) {
    $( "#tabs" ).tabs();
	var student = <%=canSeeGold %>
    if (!student) {
        $('#tabs > ul li:has(a[href="#tabs-1"])').hide()
        $("#tabs").tabs('refresh');
        $("#tabs").tabs('option', 'active', 1);
    }
    
    else{
    	$('#tabs > ul li:has(a[href="#tabs-3"])').hide()
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
			
			<li><a href="#tabs-3">Add Item/Assign Gold</a></li>

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


	</div>

</bbNG:includedPage>

</body>

</html>

