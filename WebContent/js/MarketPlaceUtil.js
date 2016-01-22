jQuery.noConflict();
(function($) {
	
	var settingsNames = $("#settingNames").val();
	settingsNames = settingsNames.replace('[', '');
	settingsNames = settingsNames.replace(']', '');
	var settingsNamesArray = settingsNames.split(',');
	var settingsValues = $("#settingValues").val();
	settingsValues = settingsValues.replace('[', '');
	settingsValues = settingsValues.replace(']', '');
	var settingsValuesArray = settingsValues.split(',');
	for(var i = 0; i<settingsNamesArray.length; i++){
		if(settingsValuesArray[i] == "Y"){
			var settingName = '#'+settingsNamesArray[i];
			$(settingName).prop('checked',true);
		}
	}
	
	$("#mySelect").hide();
	$("#newAttributeAffected > option").each(function() {
		$("#newAttributeAffected option:contains("+ this.text+ ")").hide();
	});
	$("#durationSpan").hide();
	$("#customDuration").hide();
	var student = $("#isStudent").val();
    $( "#tabs" ).tabs();
    if (student == "false") {
        $('#tabs > ul li:has(a[href="#tabs-1"])').hide();
        $("#tabs").tabs('refresh');
        $("#tabs").tabs('option', 'active', 1);
    }
    else{
    	$('#tabs > ul li:has(a[href="#tabs-3"])').hide();
    	$('#tabs > ul li:has(a[href="#tabs-4"])').hide();
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
	    			alert("Item Purchased");
	    		},
	    		error: function(result){
	    			alert("Item Purchase FAILED");
	    		}
		    });
		}
	});
    $(".Truncate").click(function() {
    	$.ajax({
	    	url: $("#TRUNCATE").val(),
    		type: "GET",
    		success: function(result){
    			alert("SUCCESS");
    			location.reload();
    		},
    		error: function(result){
    			alert("FAILED");
    		}
	    });
	});
    $("#useItem").click(function() {
    	var nameItemRadio = $('input[name=itemRadio]:checked', '#myRadioButtons').parent().text().trim();
		$.ajax({
	    	url: $("#useItemURL").val(),
    		type: "GET",
    		data: {columnName: $('#mySelect :selected').text().trim(), itemName: nameItemRadio},
    		success: function(result){
    			alert("Item Used");
    		},
    		error: function(result){
    			alert("Item Use Failed. Check Option Selected.");
    		}
	    });
	});
    $('input[name="itemRadio"]').click(function(){
    	$("#mySelect").hide();
    	$("#mySelect > option").each(function() {
    		$("#mySelect option:contains("+ this.text+ ")").show();
    	});
    	var name = $(this).attr("value");
        $.ajax({
	    	url: $("#getListURL").val(),
    		type: "GET",
    		dataType: "json",
    		contentType:"application/json",
    		data: {category : name},
    		success: function(results){
    			var columnString = $("#columnNames").val();
    			columnString = columnString.substring(1, columnString.length-1);
    			var columnNames = columnString.split(',');
    			for (var i = 0; i < columnNames.length; i++) {
    				var exists = false;
    				for(var k = 0; k < results.length; k++) {
    					if (columnNames[i].trim() == results[k].trim()) {
    						exists = true;
    						break;
    					}
    				}
    				if(!exists){
    					var trimmedName = columnNames[i].trim();
    					$("#mySelect option:contains("+ trimmedName +")").hide();
    				}
    			}
    			if(results[0].trim() == "ALL"){
    				$("#mySelect option:contains(ALL)").show();
    			}
                $("#mySelect").show();
    		},
    		error: function(result){
    			$("#mySelect").hide();
    			alert("Category is invalid. Please check the category of the Item.");
    		}
	    });
    });
    $(".Duration").click(function() {
    	$("#newAttributeAffected > option").each(function() {
    		$("#newAttributeAffected option:contains("+ this.text+ ")").hide();
    	});
		$.ajax({
	    	url: $("#getDurationURL").val(),
    		type: "GET",
    		data: {duration: $(this).text()},
    		success: function(results){
				for(var k = 0; k < results.length; k++) {
					if ($("#newAttributeAffected option:contains("+ results[k].trim() +")")) {
						$("#newAttributeAffected option:contains("+ results[k].trim() +")").show();
					}
				}
                $("#newAttributeAffected").show();
                if($('#newItemDuration option:selected' ).text() == "CONTINUOUS"){
                	$("#durationSpan").show();
                	$("#customDuration").show();
                }
                else{
                	$("#durationSpan").hide();
                	$("#customDuration").hide();
                }
    		},
    		error: function(result){
    			alert("Duration Choice Failed. Contact Admin.");
    		}
	    });
	});
    $("#addItem").click(function () {
        $("#address").focus();
        var duration = $('#newItemDuration option:selected' ).text();
        if(duration == "CONTINUOUS"){
        	duration = $('#customDuration').val();
        }
        $.ajax({
	    	url: $("#addItemURL").val(),
    		type: "GET",
    		data: {name: $('#newItemName').val(), cost: $('#newItemCost').val(), attributeAffected: $('#newAttributeAffected option:selected' ).text(),
    			   effectMagnitude: $('#newItemMagnitude').val(), supply: $('#newItemSupply').val(),
    			   assessmentType: $('#newItemAssessment option:selected' ).text(), duration: duration},
    		success: function(results){
				alert("Item Added");
				location.reload();
    		},
    		error: function(result){
    			alert("Failed To Add Item. Make Sure All Fields Are Filled. If It Failed, Contact Admin.");
    		}
	    });
    });
    $("#addGold").click(function() {
		$.ajax({
	    	url: $("#addGoldURL").val(),
    		type: "GET",
    		data: {gold: $('#addGoldField').val()},
    		success: function(result){
    			alert("Gold Added");
    			location.reload();
    		},
    		error: function(result){
    			alert("Gold Adding Failed. Contact Admin.");
    		}
	    });
	});
    $('.CheckBoxes').change(function() {
        var $check = $(this);
        var flag = "N";
        if ($check.prop('checked')) {
        	flag = "Y";
        }
        $.ajax({
	    	url: $("#checkBoxesURL").val(),
    		type: "GET",
    		data: {name: $(this).attr("id"), value: flag},
    		success: function(result){
    			alert("Setting Updated");
    			location.reload();
    		},
    		error: function(result){
    			alert("Setting Updating Failed. Contact Admin.");
    		}
	    });
    });
})
(jQuery);