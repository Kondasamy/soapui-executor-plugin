package com.microstrategy.yatilibrary.ci.soapuiexecutor;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Hudson;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONSerializer;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;

public class HttpCaller {

    public void callBack(AbstractBuild build, Map rs, String returnPath, BuildListener listener) throws HttpException, IOException, InterruptedException{

        EnvVars envVars = build.getEnvironment(listener);

        HttpClient client = new HttpClient();
		PostMethod method = new PostMethod(returnPath.toString());

        getNeededParams(listener, method, build.getBuildVariables());
        if(rs.containsKey(("svn_revision"))){
            method.addParameter("svn_revision", rs.get("svn_revision").toString());
        }
        listener.getLogger().println(Utils.getTimeString()+" [SoapUI Executor]Sending test result info to Build Status Server.");
	    listener.getLogger().println(Utils.getTimeString()+" [SoapUI Executor]Server address: "+returnPath);
	    method.addParameter("testurl", Hudson.getInstance().getRootUrl()+build.getUrl());
	    method.addParameter("status", build.getResult().toString());
	    listener.getLogger().println(Utils.getTimeString()+" [SoapUI Executor]URL to Build page: "+Hudson.getInstance().getRootUrl()+build.getUrl());
	    String result_data = JSONSerializer.toJSON(rs).toString(0);
	    method.addParameter("result_data", result_data);
	    listener.getLogger().println(Utils.getTimeString()+" [SoapUI Executor]POST request body size:"+result_data.length()/1024+"KB");
	    client.executeMethod(method);
	    listener.getLogger().println(Utils.getTimeString()+" [SoapUI Executor]POST request is sent successfully.");
    }
    
    private boolean getNeededParams(BuildListener listener, PostMethod method, Map envVars){
    	try{
	    	for (String name : (Set<String>)envVars.keySet()){
                if(!"svn_revision".equals(name)){
                    method.addParameter(name, envVars.get(name).toString());
                    listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Post params: "+name+"="+envVars.get(name).toString());
                }
	    	}
    	}catch (Exception e){
    		//
    	}
    	return true;
    }
}
