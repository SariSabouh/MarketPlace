jQuery.noConflict();
(function($) {
	var student = $("#isStudent").val();
    $( "#tabs" ).tabs();
    if (student == "false") {
        $('#tabs > ul li:has(a[href="#tabs-1"])').hide();
        $("#tabs").tabs('refresh');
        $("#tabs").tabs('option', 'active', 1);
    }
    
    else{
    	$('#tabs > ul li:has(a[href="#tabs-3"])').hide();
        $("#tabs").tabs('refresh');
        $("#tabs").tabs('option', 'active', 1);
    }
    $(".Items").click(function() {
		var student = $("#isStudent").val();
		if(student == "true"){
			$.ajax({
		    	url: $("#buyItemURL").val(),
	    		type: "GET",
	    		data: {itemName: $(this).text()},
	    		success: function(result){
	    			alert("SUCCESS");
	    		},
	    		error: function(result){
	    			alert("FAIL");
	    		}
		    })
			.done(function(result){
    			alert("DONE");
    		});
		}
		else{
			$.ajax({
		    	url: $("#TRUNCATE").val(),
	    		type: "GET",
	    		data: {itemName: $(this).text()},
	    		success: function(result){
	    			alert("SUCCESS");
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
    		data: {itemName: $(this).text()},
    		success: function(result){
    			alert("SUCCESS");
    			console.log(result.responseText);
    		},
    		error: function(result){
    			alert("FAIL");
    		}
	    });
	});
    $(".Items").hover(function() {
        ($(this).data("tooltip")).css({
            left: e.pageX + 1,
            top: e.pageY + 1
        }).stop().show(100);
    }, function() {
        $((this).data("tooltip")).hide();
    });

})
(jQuery);