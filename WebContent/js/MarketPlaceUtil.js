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
    	$('#tabs > ul li:has(a[href="#tabs-5"])').hide();
        $("#tabs").tabs('refresh');
        $("#tabs").tabs('option', 'active', 1);
    }
    
    if($("#communityItemExists").val() == "false"){
    	$("#communityItemActive").hide();
    }
    else{
    	$("#noCommunityItemActive").hide();
    }
    
    $("#buyItem").click(function() {
		var student = $("#isStudent").val();
		if(student == "true"){
			$.ajax({
		    	url: $("#buyItemURL").val(),
	    		type: "GET",
	    		data: {itemName: $('input[name=buyItemRadio]:checked', '#storeRadioButtons').val()},
	    		success: function(result){
	    			alert("Item Purchased");
	    			location.reload();
	    		},
	    		error: function(result){
	    			alert("Item Purchase FAILED");
	    		}
		    });
		}
	});
    $(".Truncate").click(function() {
    	if (confirm('Are you sure you want to erase all the infromation and reset to Preset?')) {
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
    	} else {
    	    
    	}
	});
    $("#useItem").click(function() {
    	var nameItemRadio = $('input[name=itemRadio]:checked', '#myRadioButtons').parent().text().trim();
    	var columnNameUsed = $('#mySelect :selected').text().trim();
    	if(columnNameUsed == "NONE"){
    		alert("Item Use Failed. Check Option Selected.");
    	}
    	else{
			$.ajax({
		    	url: $("#useItemURL").val(),
	    		type: "GET",
	    		data: {columnName: columnNameUsed, itemName: nameItemRadio},
	    		success: function(result){
	    			alert("Item Used");
	    			location.reload();
	    		},
	    		error: function(result){
	    			alert("Item Use Failed. Check Option Selected.");
	    		}
		    });
    	}
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
    				$("#mySelect option:contains('ALL')").show();
    			}
    			else{
    				$("#mySelect option:contains('ALL')").hide();
    			}
                $("#mySelect").show();
    		},
    		error: function(result){
    			$("#mySelect").hide();
    			alert("Category is invalid. Please check the category of the Item.");
    		}
	    });
        $.ajax({
	    	url: $("#getItemDescriptionURL").val(),
    		type: "GET",
    		dataType: "json",
    		contentType:"application/json",
    		data: {name : name},
    		success: function(results){
    			$('#myItemDescription').text(results[0]);
    		},
    		error: function(result){
    			alert("Could Not Get Information About The Item. Please Contact Admin.");
    		}
	    });
    });
    $(".Duration").click(function() {
    	$("#newAttributeAffected > option").each(function() {
    		$("#newAttributeAffected option:contains("+ this.text+ ")").hide();
    	});
    	$("#editAttributeAffected > option").each(function() {
    		$("#editAttributeAffected option:contains("+ this.text+ ")").hide();
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
					if ($("#editAttributeAffected option:contains("+ results[k].trim() +")")) {
						$("#editAttributeAffected option:contains("+ results[k].trim() +")").show();
					}
				}
                $("#newAttributeAffected").show();
                if($('#newItemDuration option:selected' ).text() == "CONTINUOUS"){
                	$("#customDuration").show();
                }
                else{
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
    $("#editItem").click(function () {
        $("#address").focus();
        var duration = $('#editItemDuration option:selected' ).text();
        if(duration == "CONTINUOUS"){
        	duration = $('#editCustomDuration').val();
        }
        $.ajax({
	    	url: $("#editItemURL").val(),
    		type: "GET",
    		data: {name: $('#editItemName').val(), cost: $('#editItemCost').val(), attributeAffected: $('#editAttributeAffected option:selected' ).text(),
    			   effectMagnitude: $('#editItemMagnitude').val(), supply: $('#editItemSupply').val(),
    			   assessmentType: $('#editItemAssessment option:selected' ).text(), duration: duration},
    		success: function(results){
				alert("Item updated");
				location.reload();
    		},
    		error: function(result){
    			alert("Failed To Edit Item. Make Sure All Fields Are Filled. If It Failed again, Contact Admin.");
    		}
	    });
    });
    
    $(".EditItemClass").click(function () {
        $.ajax({
	    	url: $("#getItemInfoURL").val(),
    		type: "GET",
    		data: {name: $('#editItemName option:selected' ).text()},
    		dataType: "json",
    		success: function(results){
    			$('#editItemCost').val(results[0]);
    			var duration = results[1];
    			if(duration == 0){
    				duration = "ONCE";
    			}
    			else if(duration == -1){
    				duration = "PASSIVE";
    			}
    			else{
    				duration = "CONTINUOUS";
    			}
    			$("#editItemDuration").val(duration);
    			$('#editItemMagnitude').val(results[2]);
    			$('#editItemSupply').val(results[3]);
    			$('#editAttributeAffected').val(results[4]);
    			$('#editItemAssessment').val(results[5]);
    		},
    		error: function(result){
    			alert("Failed To prefill Item info. Make Sure All Fields Are Filled. If It Failed again, Contact Admin.");
    		}
	    });
    });
    $('input[name="buyItemRadio"]').click(function(){
        $.ajax({
	    	url: $("#getItemDescriptionURL").val(),
    		type: "GET",
    		dataType: "json",
    		contentType:"application/json",
    		data: {name : $(this).attr("value")},
    		success: function(results){
    			$('#itemDescription').text(results[0]);
    		},
    		error: function(result){
    			alert("Could Not Get Information About The Item. Please Contact Admin.");
    		}
	    });
    });
    $("#directEditItem").click(function () {
    	var itemName = $('input[name=buyItemRadio]:checked', '#storeRadioButtons').val();
    	$.ajax({
	    	url: $("#getItemInfoURL").val(),
    		type: "GET",
    		data: {name: itemName},
    		dataType: "json",
    		success: function(results){
    			var index = $('#tabs a[href="#tabs-5"]').parent().index();
    			$('#tabs').tabs("option", "active", index);
    			$('#editItemCost').val(results[0]);
    			var duration = results[1];
    			if(duration == 0){
    				duration = "ONCE";
    			}
    			else if(duration == -1){
    				duration = "PASSIVE";
    			}
    			else{
    				duration = "CONTINUOUS";
    			}
    			$("#editItemName").val(itemName);
    			$("#editItemDuration").val(duration);
    			$('#editItemMagnitude').val(results[2]);
    			$('#editItemSupply').val(results[3]);
    			$('#editAttributeAffected').val(results[4]);
    			$('#editItemAssessment').val(results[5]);
    		},
    		error: function(result){
    			alert("Failed To prefill Item info. Make Sure All Fields Are Filled. If It Failed again, Contact Admin.");
    		}
    	});
    });
    $("#communityItemButton").click(function() {
		var index = $('#tabs a[href="#tabs-6"]').parent().index();
		var name = $('input[name=buyItemRadio]:checked').val();
		$('#tabs').tabs("option", "active", index);
		$("#communityItemsSelect").val(name);
		$("#communityItemColumnSelect").hide();
    	$("#communityItemColumnSelect > option").each(function() {
    		$("#communityItemColumnSelect option:contains("+ this.text+ ")").show();
    	});
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
    					$("#communityItemColumnSelect option:contains("+ trimmedName +")").hide();
    				}
    			}
    			if(results[0].trim() == "ALL"){
    				$("#communityItemColumnSelect option:contains('ALL')").show();
    			}
    			else{
    				$("#communityItemColumnSelect option:contains('ALL')").hide();
    			}
                $("#communityItemColumnSelect").show();
    		},
    		error: function(result){
    			$("#communityItemColumnSelect").hide();
    			alert("Category is invalid. Please check the category of the Item.");
    		}
	    });
	});
    $("#communityItemsSelect").change(function() {
		var name = $('#communityItemsSelect :selected').text().trim();
		$("#communityItemColumnSelect").hide();
    	$("#communityItemColumnSelect > option").each(function() {
    		$("#communityItemColumnSelect option:contains("+ this.text+ ")").show();
    	});
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
    					$("#communityItemColumnSelect option:contains("+ trimmedName +")").hide();
    				}
    			}
    			if(results[0].trim() == "ALL"){
    				$("#communityItemColumnSelect option:contains('ALL')").show();
    			}
    			else{
    				$("#communityItemColumnSelect option:contains('ALL')").hide();
    			}
                $("#communityItemColumnSelect").show();
    		},
    		error: function(result){
    			$("#communityItemColumnSelect").hide();
    			alert("Category is invalid. Please check the category of the Item.");
    		}
	    });
	});
    $("#communityItemBuy").click(function() {
    	var student = $("#isStudent").val();
    	var columnNameUsed = $('#communityItemColumnSelect :selected').text().trim();
		if(student == "true"){
			if(columnNameUsed == "NONE"){
	    		alert("Item Use Failed. Check Option Selected.");
	    	}
			$.ajax({
		    	url: $("#useCommunityItemURL").val(),
	    		type: "GET",
	    		data: {itemName: $('input[name=buyItemRadio]:checked', '#storeRadioButtons').val(),
	    			downPayment: $("#communityDownPayment").val(), columnUsed: columnNameUsed},
	    		success: function(result){
	    			alert("Community Item Purchased");
	    			location.reload();
	    		},
	    		error: function(result){
	    			alert("Item Purchase FAILED. Check down payment total. It has to be more than 1 GOLD!");
	    		}
		    });
		}
	});
    $("#payGold").click(function() {
    	var student = $("#isStudent").val();
    	var pay = $('#payGoldField').val();
		if(student == "true"){
			$.ajax({
		    	url: $("#payGoldURL").val(),
	    		type: "GET",
	    		data: {goldPaid: pay},
	    		success: function(result){
	    			alert("Payment Processed");
	    			location.reload();
	    		},
	    		error: function(result){
	    			alert("Payment Failed. Make Sure You Can Afford.");
	    		}
		    });
		}
	});
})
(jQuery);