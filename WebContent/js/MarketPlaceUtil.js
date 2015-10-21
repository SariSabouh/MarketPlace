jQuery.noConflict();
(function($) {
	var student = $("#userCanSeeGold")
    $( "#tabs" ).tabs();
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
	$(".Items").click(function() {
		var student = $("#userCanSeeGold")
		if(student){
			$.ajax({
		    	url: "MarketPlace/controller",
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