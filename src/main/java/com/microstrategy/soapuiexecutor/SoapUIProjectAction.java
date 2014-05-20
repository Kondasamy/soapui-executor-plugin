package com.microstrategy.yatilibrary.ci.soapuiexecutor;

import hudson.model.Hudson;
import hudson.model.ProminentProjectAction;
import hudson.model.AbstractItem;
import hudson.model.AbstractProject;
import hudson.model.Project;

public class SoapUIProjectAction implements ProminentProjectAction{
	private final AbstractProject project;

    public SoapUIProjectAction(AbstractProject project) {
        this.project = project;
    }
   
    public String getUrlName() {
    	Integer buildNum = null;
    	try{
    		buildNum = new Integer(this.project.getLastCompletedBuild().getNumber());
    	}catch (NullPointerException e){
    		return null;
    	}
        return buildNum.toString();
    }

    public String getDisplayName() {
        return "Latest Result";
    }

    public String getIconFileName() {
        return "/plugin/SoapUIExecutor/images/48x48/soapui.png";
    }
}
