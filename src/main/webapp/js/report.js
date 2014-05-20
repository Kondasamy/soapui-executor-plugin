function showElement(id){
        var element = document.getElementById(id);
        element.setAttribute("style", "");
        var showLinkElement = document.getElementById(id+'-showlink');
        var hideLinkElement = document.getElementById(id+'-hidelink');
        showLinkElement.setAttribute("style", "display:none;");
        hideLinkElement.setAttribute("style", "");
}

function hideElement(id){
        var element = document.getElementById(id);
        element.setAttribute("style", "display:none;");
        var showLinkElement = document.getElementById(id+'-showlink');
        var hideLinkElement = document.getElementById(id+'-hidelink');
        showLinkElement.setAttribute("style", "");
        hideLinkElement.setAttribute("style", "display:none;");
}

function openTxt(txt){
        window.open (txt,'test step detail','height=700,width=1000,toolbar=no,menubar=no,location=no, status=no, scrollbars=yes');
}

function loadData(root, id, url){
        showElement(id);
        var element = document.getElementById(id);
        if(element.getAttribute('hasdata') == 'true'){
            return;
        }else{
            try{
            var xmlhttp;
            if (window.XMLHttpRequest)
              {// code for IE7+, Firefox, Chrome, Opera, Safari
              xmlhttp=new XMLHttpRequest();
              }
            else
              {// code for IE6, IE5
              xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
              }

            xmlhttp.onreadystatechange=function()
              {
              if (xmlhttp.readyState==2){
                //document.getElementById(id).innerHTML="<td/><td>Loading...</td>";
              }
              if (xmlhttp.readyState==4)
                {
                    if(xmlhttp.status ==200){
                        var jsonObj = eval("("+xmlhttp.responseText+")");
                        var detail = '';
                        detail = detail + '<td colspan="5"><div style="background-color:white;"><table class="pane bigtable" style="table-layout:fixed;word-break:break-all;white-space:normal;text-align:left;" width="100%" cellspacing="2" cellpadding="5" border="0">';
                        if(jsonObj.name){
                            detail = detail +'<tr><td width="200">Step Name</td><td colspan="4"><pre>'+jsonObj.name+'</pre></td></tr>';
                        }
                        if(jsonObj.desc){
                            detail = detail +'<tr><td width="200">Description</td><td colspan="4"><pre>'+jsonObj.desc+'</pre></td></tr>';
                        }
                        if(jsonObj.timetaken){
                            detail = detail +'<tr><td width="200">Timetaken</td><td colspan="4"><pre>'+jsonObj.timetaken+'</pre></td></tr>';
                        }
                        if(jsonObj.message){
                            detail = detail +'<tr><td width="200">Messages</td><td colspan="4"><pre>'+jsonObj.message+'</pre></td></tr>';
                        }
                        if(jsonObj.apirequest){
                            detail = detail + '<tr><td width="200"></td><td colspan="4"><a href="javascript:callAPI(\''+jsonObj.apirequest+'\', \''+root+'\')">Call This API</a></td></tr>';
                            detail = detail + '<tr><td width="200">API Request</td><td colspan="4"><pre>'+jsonObj.apirequest.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;')+'</pre></td></tr>';
                        }
                        if(jsonObj.apiresponse){
                            detail = detail + '<tr><td width="200">API Response</td><td colspan="4"><pre>'+jsonObj.apiresponse.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;')+'</pre></td></tr>';
                        }
                        if(jsonObj.teststeps){
                            for(var i=0; jsonObj.teststeps.length>0; i++){
                                var subStep = jsonObj.teststeps.shift();
                                detail = detail + '<tr><td width="200">Sub Step</td><td colspan="4">';
                                detail = detail + '<a id="'+id+'-'+subStep.index+'-showlink" href="javascript:loadData(';
                                detail = detail + "'"+root+"','";
                                detail = detail +id+'-'+subStep.index;
                                detail = detail + "'";
                                detail = detail +', ';
                                detail = detail + "'";
                                detail = detail +url+'-'+subStep.index+'-0.jd.txt';
                                detail = detail + "'";
                                detail = detail + ')" style="">+ '+ subStep.name + ' : '+subStep.status+'</a>';
                                detail = detail + '<a id="'+id+'-'+subStep.index+'-hidelink" style="display: none;" href="javascript:hideElement(';
                                detail = detail + "'";
                                detail = detail +id+'-'+subStep.index;
                                detail = detail + "'";
                                detail = detail +', ';
                                detail = detail + "'";
                                detail = detail +url+'-'+subStep.index+'-0.jd.txt';
                                detail = detail + "'";
                                detail = detail +')" style="">- '+ subStep.name + ' : '+subStep.status+'</a>';
                                detail = detail + '</td></tr>';
                                detail = detail + '<tr id="'+id+'-'+subStep.index+'" style="display: none;" class="hidden" hasData="false"/>';
                            }
                        }
                        detail = detail +'</table></div></td>';
                        document.getElementById(id).innerHTML=detail;
                        element.setAttribute('hasdata', 'true');
                        xmlhttp = null;
                    }else{
                        //document.getElementById(id).innerHTML="<td><pre>Failed to get data! Http status:"+xmlhttp.status+"</pre</td>";
                    }
                }
              }
            xmlhttp.open("GET",url,true);
            //xmlhttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
            xmlhttp.send();
            }catch(e){
            
                document.getElementById(id).innerHTML="<td>Failed to get test step details!</td>";
            
            }
        }
}