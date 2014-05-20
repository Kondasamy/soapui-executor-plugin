function editComments(element){
	CreateCommentBox(element, element.innerHTML);
}

function CreateCommentBox(element, ovalue){
	var editState = element.getAttribute("EditState");
	if(editState != "true"){
	   var textBox = document.createElement("INPUT");
	   textBox.type = "text";
	   //textBox.size = "150";
	   textBox.setAttribute("style", "width:100%;");
	   textBox.className="EditCell_TextBox";
	  
	   if(!ovalue){
		ovalue = element.getAttribute("Value");
	   }  
	   textBox.value = ovalue;
	  
	   textBox.onblur = function (){
		CancelEditCell(this.parentNode, this.value.replace("[","{").replace("]","}"), ovalue, this.value.replace("[","{").replace("]","}"));
	   }
	   
	   textBox.onkeydown = function(event){
		event = event || window.event; 
			if(event.keyCode==13) 
			{ 
				CancelEditCell(this.parentNode, this.value.replace("[","{").replace("]","}"), ovalue, this.value.replace("[","{").replace("]","}"));
			}
	   }

	   ClearChild(element);
	   element.appendChild(textBox);
	   textBox.focus();
	   textBox.select();
	  
	   element.setAttribute("EditState", "true");
	   //element.parentNode.parentNode.setAttribute("CurrentRow", element.parentNode.rowIndex);
	}
}

function EditCell(element, general, user, disable){
	if(disable == 'false'){
		CreateTextBox(element, element.innerHTML, general, user);
	}else{
		CreateTextBox(element, element.innerHTML, general, '');
	}
}

function CreateTextBox(element, ovalue, general, user){
	var editState = element.getAttribute("EditState");
	if(editState != "true"){
	   var textBox = document.createElement("INPUT");
	   textBox.type = "text";
	   textBox.setAttribute("style", "width:100%;");
	   textBox.className="EditCell_TextBox";
	  
	   if(!ovalue){
		ovalue = element.getAttribute("value");
		if(ovalue == null){
			ovalue = '';
		}
	   }

	   if(!ovalue.endsWith('-'+user) && user!=''){
		ovalue = ovalue + ' -'+user;
	   }
	   textBox.value = ovalue;
	  
	   textBox.onblur = function (){
		CancelEditCell(this.parentNode, this.value.replace("[","{").replace("]","}"), ovalue, general);
		//RevertCell(this.parentNode, ovalue);
	   }
	   
	   textBox.onkeydown = function(event){
		event = event || window.event; 
			if(event.keyCode==13) 
			{ 
				CancelEditCell(this.parentNode, this.value.replace("[","{").replace("]","}"), ovalue, general);
			}
	   }

	   ClearChild(element);
	   element.appendChild(textBox);
	   textBox.focus();
	   textBox.select();
	  
	   element.setAttribute("EditState", "true");
	   //element.parentNode.parentNode.setAttribute("CurrentRow", element.parentNode.rowIndex);
	}
}

function CancelEditCell(element, value, old, general){
	element.setAttribute("Value", value);
    element.innerHTML = value;
	if(value != old){
		//post_to_url("submitDescription" , { "description": ''.concat(general, GetData(document.getElementById("failurelist")), GetData(document.getElementById("newfailurelist")), GetData(document.getElementById("apilist"))) });
		//post_to_url('submitComments?comments='.concat(general, GetData(document.getElementById("failurelist")), GetData(document.getElementById("newfailurelist")), GetData(document.getElementById("apilist"))));
		var comments = encodeURIComponent(general + GetData(document.getElementById("failurelist")) + GetData(document.getElementById("newfailurelist")) + GetData(document.getElementById("apilist")));
        post_to_url('submitComments?comments='.concat(comments).concat('&mapComments=true&').concat(encodeURIComponent(element.getAttribute('name'))).concat('=').concat(encodeURIComponent(value)));
	}
	element.setAttribute("EditState", "false");
}

function ClearChild(element){
	element.innerHTML = "";
}

function GetData(table){
	var data = '';
	if (table == null){
	    return '';
	}
	for(var j=1; j<table.rows.length; j++){
	   if("hidden"!=(table.rows[j].getAttribute("class"))){
    	   name = table.rows[j].cells[0].getAttribute("name");
    	   if(name){
    			var value = table.rows[j].cells[0].getAttribute("value");
    			if(!value){
    				value = table.rows[j].cells[0].innerHTML.replace("[","{").replace("]", "}");
    			}
    			if(value!=''){
    				data = data.concat('[', name, ']', value);
    			}
    	   }
        }
	}
	return data;
}

function post_to_url(path, method) {
        method = method || "post";

        var form = document.createElement("form");

        //Move the submit function to another variable
        //so that it doesn't get overwritten.
        form._submit_function_ = form.submit;

        form.setAttribute("method", method);
        form.setAttribute("action", path);
		form.setAttribute("target", "hidden_frame");

//        for(var key in params) {
//            var hiddenField = document.createElement("input");
//            hiddenField.setAttribute("type", "hidden");
//            hiddenField.setAttribute("name", key);
//            hiddenField.setAttribute("value", params[key]);

//            form.appendChild(hiddenField);
//        }

        document.body.appendChild(form);
        form._submit_function_(); //Call the renamed function.
}