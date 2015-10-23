jQuery.noConflict();
(function($) {
	var student = $("#isStudent").val();
    $( "#tabs" ).tabs();
    if (student == "false") {
        $('#tabs > ul li:has(a[href="#tabs-1"])').hide()
        $("#tabs").tabs('refresh');
        $("#tabs").tabs('option', 'active', 1);
    }
    
    else{
    	$('#tabs > ul li:has(a[href="#tabs-3"])').hide()
        $("#tabs").tabs('refresh');
        $("#tabs").tabs('option', 'active', 1);
    }
	$(".Items").click(function() {
		var student = $("#isStudent").val();
		if(student == "true"){
			$.ajax({
		    	url: "/webapps/dt-MarketPlace/-BBLEARN-bb_bb60/JavaControllerServlet",
	    		type: "POST",
	    		data: $(this).attr("name"),
	    		success: function(result){
	    			alert("SUCCESS");
	    		},
	    		error: function(result){
	    			alert("FAIL");
	    		}
		    });
		}
	});
})
(jQuery);