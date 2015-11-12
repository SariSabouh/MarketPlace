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
		    	url: $("#buyItemURL").val(),
	    		type: "GET",
	    		data: {itemName: "PickAxe"},
	    		success: function(result){
	    			alert("SUCCESS");
	    			console.log(result.responseText);
	    		},
	    		error: function(result){
	    			alert("FAIL");
	    		}
		    })
			.done(function(result){
    			alert("DONE");
    		});
		}
	});
    $(".MyItems").click(function() {
		$.ajax({
	    	url: $("#useItemURL").val(),
    		type: "GET",
    		data: {itemName: "PickAxe"},
    		success: function(result){
    			alert("SUCCESS");
    			console.log(result.responseText);
    		},
    		error: function(result){
    			alert("FAIL");
    		}
	    })
	});
})
(jQuery);