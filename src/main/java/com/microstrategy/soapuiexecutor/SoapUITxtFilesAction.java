package com.microstrategy.yatilibrary.ci.soapuiexecutor;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.FilePath;
import hudson.model.Action;
import hudson.model.AbstractBuild;
import hudson.model.DirectoryBrowserSupport;
import hudson.model.Run;

public class SoapUITxtFilesAction extends AbstractSoapUITxtFilesAction{
	private final AbstractBuild<?, ?> build;

    public SoapUITxtFilesAction(AbstractBuild<?, ?> build) {
        this.build = build;
    }

    public final AbstractBuild<?,?> getOwner() {
    	return build;
    }

    @Override
    protected String getTitle() {
        return this.build.getDisplayName() + " txts";
    }

    @Override
    protected File dir() {
        return getBuildArchiveDir(this.build);
    }
    
    private File getBuildArchiveDir(Run run) {
        return new File(run.getRootDir(), "txts");
    }

}