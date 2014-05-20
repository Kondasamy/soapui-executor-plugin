package com.microstrategy.yatilibrary.ci.soapuiexecutor;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.FilePath;
import hudson.model.Action;
import hudson.model.DirectoryBrowserSupport;

public abstract class AbstractSoapUITxtFilesAction implements Action{
	
	public String getUrlName() {
        return "txts";
    }

    public String getDisplayName() {
        return "Txt Files";
    }

    public String getIconFileName() {
        return "graph.gif";
    }

    /**
     * Serves HTML reports.
     */
    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        DirectoryBrowserSupport dbs = new DirectoryBrowserSupport(this, new FilePath(this.dir()), "Txt Files", "folder.png", true);
        dbs.setIndexFileName("index.html");
        dbs.generateResponse(req, rsp, this);
    }
    
    protected abstract File dir();

	protected abstract String getTitle();

}