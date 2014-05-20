package com.microstrategy.yatilibrary.ci.soapuiexecutor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import hudson.model.Action;
import hudson.model.AbstractBuild;
import hudson.model.Hudson;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import static com.microstrategy.yatilibrary.ci.soapuiexecutor.Utils.*;


@ExportedBean(defaultVisibility=2)
public class SoapUISummaryAction implements Action {
	
	    private Map metadata = new LinkedHashMap();
	    private AbstractBuild build;
	    private boolean doNotAddUserName;

	    
	    /* Action methods */
	    public String getUrlName() { return ""; }
	    public String getDisplayName() { return ""; }
	    public String getIconFileName() { return null; }
	    
	    public void setMetadata(Map map){
	    	this.metadata = map;
	    }
	    
	    public void setBuild(AbstractBuild build){
	    	this.build = build;
	    }
	    
	    public void setDoNotAddUserName(boolean doNotAddUserName){
	    	this.doNotAddUserName = doNotAddUserName;
	    }
	    
	    public AbstractBuild getBuild(){
	    	return this.build;
	    }
	    
	    public String getStatus(){
	    	return this.build.getResult().toString();
	    }
	    
	    public Map getBuildVars(){
	    	return this.build.getBuildVariables();
	    }
	    
	    public String getUser(){
	    	return Hudson.getInstance().getAuthentication().getName();
	    }

	    public String getBuildUrl(){
	    	return Hudson.getInstance().getRootUrl()+this.build.getUrl();
	    }
	    
	    public String getJobUrl(){
	    	return Hudson.getInstance().getRootUrl()+this.build.getProject().getUrl();
	    }
	    
	    public String getGeneralComments(){
	    	//String desc = this.build.getDescription();
	    	String desc = getComments();
	    	if(desc!=null){
		    	int index = desc.indexOf('[');
		    	if(index >= 0){
		    		return desc.substring(0, index);
		    	}
		    	else{
		    		return desc;
		    	}
	    	}else{
	    		return null;
	    	}
	    }
	    
	    public List getListComments(){
	    	List list = new ArrayList();
	    	String desc = this.build.getDescription();
	    	String strComm = getComments();
			StringBuffer sb = new StringBuffer();
			if(desc!=null){
				sb.append(desc);
			}
			if(strComm!=null){
				sb.append(strComm);
			}else{
                return getCommentsList();
            }
			for(int i=0; i<sb.length() && i>=0; i++){
				if(sb.charAt(i)=='['){
					for(int j=i; j<sb.length()&& j>=0; j++){
						if(sb.charAt(j)==']'){
							String key = sb.substring(i+1, j);
							for(i=j; i>=0; i++){
								if(i==sb.length()){
									String value = sb.substring(j+1, i);
									Map<String, String> map = new LinkedHashMap<String, String>();
									map.put("key", key);
									map.put("value", value);
									list.add(map);
									i--;
									break;
								}
								if(sb.charAt(i)=='['){
									Map<String, String> map = new LinkedHashMap<String, String>();
									String value = sb.substring(j+1, i);
									map.put("key", key);
									map.put("value", value);
									list.add(map);
									i--;
									break;
								}
							}
							break;
						}
					}
				}
			}
			return list;
	    }
	    
	    @Exported
	    public Map getMetadata() {

                Map condition = new LinkedHashMap();
                condition.put("name", this.build.getProject().getName());
                condition.put("number", this.build.getNumber());
                DBObject rs = Utils.getSoapUIResult(condition);
                if(rs!=null){
                    rs.put("fromDB", "yes");
                    rs.put("_id", "");
                    return rs.toMap();
                }else{
                    return this.metadata;
            }
	    }
	    
	    public boolean getDoNotAddUserName(){
	    	return this.doNotAddUserName;
	    }

	    public String getComments(){
	    	try{
	    		return this.build.getAction(SoapUICommentsAction.class).getComments();
	    	}catch(Exception e){
	    		return null;
	    	}
	    	
	    }

        public List getCommentsList(){
            try{
                return this.build.getAction(SoapUIListCommentsAction.class).getComments();
            }catch(Exception e){
                return null;
            }
        }
}
