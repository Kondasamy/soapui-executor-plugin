function callAPI(info, root){
    var lines = info.split(' ');
    var method = lines[0];
    var url = lines[1];
    var body = '';
    var url_parts = url.split('?');
    var path = url_parts[0];
    
    if(method=='GET'){
        win = window.open(url,'Call API','height=700,width=1000,toolbar=no,menubar=no,location=no, status=no, scrollbars=yes');
    }else{
    	win = window.open('','Call API','height=700,width=1000,toolbar=no,menubar=no,location=no, status=no, scrollbars=yes');
        win.document.write("POST, PUT and DELETE are not supported now!");
    }
    
    //win.document.write("<head><script type=\"text/javascript\" src=\"http://ajax.microsoft.com/ajax/jquery/jquery-1.4.min.js\"></script><script src=\""+root+"/plugin/SoapUIExecutor/js/request.js\"></script></head>");
    //win.document.write("<table style=\"table-layout:fixed;word-break:break-all;white-space:normal;text-align:left;\" boder=\"1\"><tr><td width=\"200\">Method:</td><td><input type=\"text\" readOnly=\"true\" value=\""+method+"\"/></td></tr><td>URL:</td><td><textarea cols=\"100\" rows=\"8\">"+url+"</textarea></td></tr><tr><td/><td><button onClick=\"send('response', '"+method+"', '"+url+"', '"+body+"')\">Send Request</button><button onClick=\"doChange('response')\">Test</button></td></tr><tr><td>Response</td><td><textarea cols=\"100\" rows=\"16\" id=\"response\"></textarea></td></tr></table>");
    win.focus();
}

function send(id, method, url, body){
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
                  if (xmlhttp.readyState==4)
                    {
                        //var jsonObj = eval("("+xmlhttp.responseText+")");
                        document.write("code:"+xmlhttp.status+" status:"+xmlhttp.statusText+" resp:"+xmlhttp.responseText);
                        //xmlhttp = null;
                    }
              }
            if (method == 'GET'){
                xmlhttp.open("GET",url,true);
                xmlhttp.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
                xmlhttp.setRequestHeader('Cache-Control', 'no-cache');
                xmlhttp.setRequestHeader('Host', 'api.alert.com');
                xmlhttp.setRequestHeader('Accept-Encoding', 'gzip,deflate');
                xmlhttp.setRequestHeader('User-Agent', 'Apache-HttpClient/4.1.1 (java 1.5)');
                
                xmlhttp.send();
            }else if(method == 'POST'){
                xmlhttp.open("POST",url,true);
                xmlhttp.setRequestHeader('Content-Type', 'application/xml');
                xmlhttp.send();
            }

        }catch(e){
            
                win.document.getElementById(id).innerHTML='Error:'+e;
            
        }
}