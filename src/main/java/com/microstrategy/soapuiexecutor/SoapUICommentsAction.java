package com.microstrategy.yatilibrary.ci.soapuiexecutor;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.io.PrintWriter;
import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import hudson.FilePath;
import hudson.model.Action;
import hudson.model.AbstractBuild;
import hudson.model.DirectoryBrowserSupport;
import hudson.model.Run;

@ExportedBean(defaultVisibility=3)
public class SoapUICommentsAction implements Action{

	private final AbstractBuild build;
	private String comments = "";
	
	public SoapUICommentsAction(AbstractBuild build){
		this.build = build;
		File dir =getBuildArchiveDir(build);
		if(!dir.exists()){
			dir.mkdirs();
		}
	}

    public final AbstractBuild<?,?> getOwner() {
    	return build;
    }
	
	public String getIconFileName() {
		return null;
	}

	public String getDisplayName() {
		return null;
	}

	public String getUrlName() {
		return "submitComments";
	}
	
	@Exported
	public String getComments(){
		return this.comments;
	}
	
	public void setComments(String comments) throws IOException, InterruptedException {
        //this.build.checkPermission(this.build.UPDATE);
        this.comments = comments;
        this.build.save();
    }
	
	public void addComments(String comments) throws IOException, InterruptedException {
        //this.build.checkPermission(this.build.UPDATE);
        this.comments = this.comments + comments;
        this.build.save();
    }

	//	public void doSubmitComments(StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException, InterruptedException {
//        DirectoryBrowserSupport dbs = new DirectoryBrowserSupport(this, new FilePath(this.dir()), "Comments", "folder.png", true);
//        net.sf.json.JSONObject params = (JSONObject) net.sf.json.JSONSerializer.toJSON(req.getInputStream());
//        if(!params.isEmpty()){
//        	setComments(params.toString());
//        }
//        
//		setComments(req.getParameter("comments"));
//        rsp.sendRedirect(".");  // go to the top page
//    }

    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, InterruptedException {
        DirectoryBrowserSupport dbs = new DirectoryBrowserSupport(this, new FilePath(dir()), null, null, false);
        Map params = req.getParameterMap();
        if(req.hasParameter("comments")){
        	setComments(req.getParameter("comments"));
        }
        rsp.sendRedirect(".");
        //dbs.generateResponse(req, rsp, null);
//        dbs.setIndexFileName("data.txt");
        
    }
    
    protected File dir() {
        return getBuildArchiveDir(this.build);
    }
    
    private File getBuildArchiveDir(Run run) {
        return new File(run.getRootDir(), "submitComments");
    }

}
