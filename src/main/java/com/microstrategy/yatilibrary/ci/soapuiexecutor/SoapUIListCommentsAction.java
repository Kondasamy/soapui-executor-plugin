package com.microstrategy.yatilibrary.ci.soapuiexecutor;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.DirectoryBrowserSupport;
import hudson.model.Run;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.*;

@ExportedBean(defaultVisibility=3)
public class SoapUIListCommentsAction implements Action{

	private final AbstractBuild build;
	private List<Map<String, String>> comments = new ArrayList<Map<String, String>>();

	public SoapUIListCommentsAction(AbstractBuild build){
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
	public List getComments(){
		return this.comments;
	}

    public void addMapComments(Map mapComments)throws IOException, InterruptedException {
        for(String key : (Set<String>)mapComments.keySet()){
            String value = (String)mapComments.get(key);
            boolean overwrite = false;
            for(Map<String, String> comm: this.comments){
                if(key.equals(comm.get("key"))){
                    comm.put("value", value);
                    overwrite = true;
                    break;
                }
            }
            if(!overwrite){
                Map<String, String> map = new LinkedHashMap<String, String>();
                map.put("key", key);
                map.put("value", value);
                this.comments.add(map);
            }
        }
        this.build.save();
    }
	
	public void addComments(Map mapComments) throws IOException, InterruptedException {
        //this.build.checkPermission(this.build.UPDATE);

        for(String key : (Set<String>)mapComments.keySet()){
            String[] value = (String[])mapComments.get(key);
            boolean overwrite = false;
            for(Map<String, String> comm: this.comments){
                if(key.equals(comm.get("key"))){
                    if(value.length > 0)
                        comm.put("value", value[0].toString());
                    overwrite = true;
                    break;
                }
            }
            if(!overwrite){
                Map<String, String> map = new LinkedHashMap<String, String>();
                map.put("key", key);
                if(value.length > 0)
                    map.put("value", value[0].toString());
                this.comments.add(map);
            }
        }
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
        rsp.sendRedirect(".");
        if (req.hasParameter("mapComments")){
            addComments(params);
        }
//        dbs.setIndexFileName("data.txt");
//        dbs.generateResponse(req, rsp, this);
    }
    
    protected File dir() {
        return getBuildArchiveDir(this.build);
    }
    
    private File getBuildArchiveDir(Run run) {
        return new File(run.getRootDir(), "submitComments");
    }

}
