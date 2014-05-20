package com.microstrategy.yatilibrary.ci.soapuiexecutor;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.Cause;
import hudson.model.Hudson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.sf.json.JSONObject;

public class SoapUIReportParser {
	private Map mapResult;
	
	public boolean parseResults(AbstractBuild build, BuildListener listener, long startTimestamp, long finishTimestamp,
			EnvVars envVars, FilePath ws, boolean isPassed) throws IOException, InterruptedException{

		listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Parsing json result data.");
		Map mapResult = new LinkedHashMap();
		Properties buildProperties = new Properties();
		buildProperties.put("name", build.getProject().getName());
		buildProperties.put("timetaken", Utils.toReadableTime((int) (finishTimestamp - startTimestamp)));
        buildProperties.put("longtimetaken", (finishTimestamp - startTimestamp));
		buildProperties.put("status", "FINISHED");
		buildProperties.put("hostName", build.getBuiltOn().toComputer().getHostName());
		buildProperties.put("nodeName", build.getBuiltOn().getDisplayName());
		SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strFinishTime = timeFormat.format(finishTimestamp);
		String strStartTime = timeFormat.format(startTimestamp);
        buildProperties.put("timestamp", startTimestamp);
		buildProperties.put("finishTime", strFinishTime);
		buildProperties.put("startTime", strStartTime);
		buildProperties.put("trigger", build.getCause(Cause.class).getShortDescription());
		buildProperties.put("url", Hudson.getInstance().getRootUrl()+build.getUrl());
		buildProperties.put("jobUrl", Hudson.getInstance().getRootUrl()+build.getProject().getUrl());
        buildProperties.put("number", build.getNumber());
        if(build.getBuildVariables().containsKey("BUILD_TAG")){
            buildProperties.put("unique_tag", build.getBuildVariables().get("BUILD_TAG"));
        }

		if(envVars.containsKey("SVN_REVISION")){
			buildProperties.put("test_revision", envVars.get("SVN_REVISION"));
			listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]test_revision="+envVars.get("SVN_REVISION"));
		}else{
			Integer maxRev = 1;
			for(int svnIndex=1; svnIndex<33; svnIndex++){
				if(envVars.containsKey(("SVN_REVISION_"+svnIndex).toString())){
					int rev = new Integer(envVars.get(("SVN_REVISION_"+svnIndex).toString()));
					if(rev > maxRev){maxRev = rev;}
				}else{
					break;
				}
			}
			if(maxRev > 1){
				buildProperties.put("test_revision", maxRev.toString());
				listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]test_revision="+maxRev);
			}else{
				listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Cannot find SVN_REVISION in env vars.");
			}
		}
		
		/*Properties paramProperties = new Properties();
		paramProperties.putAll(build.getBuildVariables());
		OutputStream propOS = null;
		OutputStream paramOS = null;
		try{
			propOS = ws.child("xmlReport/buildInfo.properties").write();
			buildProperties.store(propOS, "Build Info");
		}catch (Exception e){
			listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Error. "+e.getMessage());
		}finally{
			if(propOS!=null){
				propOS.close();
			}
		}
		try{
			paramOS = ws.child("xmlReport/buildParam.properties").write();
			paramProperties.store(paramOS, "Build Params");
		}catch (Exception e){
			listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Error. "+e.getMessage());
		}finally{
			if(paramOS!=null){
				paramOS.close();
			}
		}
		*/
		
		if(isPassed){
			mapResult.put("status", "FINISHED");
		}else{
			mapResult.put("status", "FAILED");
		}
		
		mapResult.putAll(buildProperties);
		mapResult.put("buildVars", build.getBuildVariables());
		List<Map> mapApis = new ArrayList(); 
		FilePath jsonDataDir = ws.child("xmlReport");
		List projectsList = new ArrayList();
		
		List<String> unstableSuites = new ArrayList<String>();
		FilePath[] unstables = jsonDataDir.list("*.unstable");
		
		for(FilePath unstableFile : unstables){
			listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Parsing "+unstableFile.getName());
			net.sf.json.JSONObject jsonUnstableSuites = null;
			InputStream is = null;
			try{
				is = unstableFile.read();
				net.sf.json.groovy.JsonSlurper jsonSlurper = new net.sf.json.groovy.JsonSlurper();
				jsonUnstableSuites = (JSONObject) jsonSlurper.parse(is);
				//jsonProjectResult = (JSONObject) net.sf.json.JSONSerializer.toJSON(jsonFile.readToString());
			}catch(Exception e){
				listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Failed to parse "+unstableFile.getName());
			}finally{
				if(is!=null){
					is.close();
				}
			}
			if(jsonUnstableSuites != null){
				unstableSuites.addAll((List<String>)jsonUnstableSuites.get("unstableSuites"));
			}
		}
		mapResult.put("unstableSuites", unstableSuites);
		
		FilePath[] jsonFiles = jsonDataDir.list("*.jsondata");
		if(jsonFiles.length == 0){
			isPassed = false;
		}
		
		for(FilePath jsonFile : jsonFiles){
			listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Parsing "+jsonFile.getName());
			net.sf.json.JSONObject jsonProjectResult = null;
			InputStream is = null;
			try{
				is = jsonFile.read();
				net.sf.json.groovy.JsonSlurper jsonSlurper = new net.sf.json.groovy.JsonSlurper();
				jsonProjectResult = (JSONObject) jsonSlurper.parse(is);
				//jsonProjectResult = (JSONObject) net.sf.json.JSONSerializer.toJSON(jsonFile.readToString());
			}catch(Exception e){
				listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Failed to parse "+jsonFile.getName());
			}finally{
				if(is!=null){
					is.close();
				}
			}
			if(jsonProjectResult != null){
				if(!"FINISHED".equals(jsonProjectResult.get("status"))){
					mapResult.put("status", "FAILED");
					isPassed = false;
				}
			}
			mapApis.addAll((List)jsonProjectResult.get("apis"));
			jsonProjectResult.remove("apis");
			projectsList.add(jsonProjectResult);
		}
		
		mergeDuplicatedApis(mapApis);
		
		mapResult.put("apis", mapApis);
		mapResult.put("projects", projectsList);
		this.mapResult = mapResult;
		return isPassed;
	}
	
	public Map getResult(){
		return this.mapResult;
	}
	
    private void mergeDuplicatedApis(List apis){
    	for(int i=0; i<apis.size(); i++){
    		Map api = (Map)apis.get(i);
    		for(int j=i+1; j<apis.size(); j++){
    			Map apj = (Map)apis.get(j);
    			if(api.get("url").equals(apj.get("url"))){
    				selectNum("max", api, apj);
    				selectNum("min", api, apj);
    				selectNum("avg", api, apj);
    				
   					if(isWorseThan(apj.get("status").toString(), api.get("status").toString())){
   						api.put("status", apj.get("status"));
   					}
   					
					List itxts = null;
   					List jtxts = null;
   					try{
   						itxts = (List)api.get("txts");
   					}catch (Exception e){
   						//
   					}
   					try{
   						jtxts = (List)apj.get("txts");
   					}catch (Exception e){
   						//
   					}
   					if(itxts!=null && jtxts!=null){
   						itxts.addAll(jtxts);
   						api.put("txts", itxts);
   					}else if(itxts!=null){
   						api.put("txts", itxts);
   					}else if(jtxts!=null){
   						api.put("txts", jtxts);
   					}
   					apis.remove(j--);
    			}
    		}
    	}

    	Integer apiNum = 1;
    	for(Map api : (List<Map>)apis){
    		api.put("n", apiNum.toString());
    		apiNum++;
    	}
    }
    
    private boolean isWorseThan(String a, String b){
    	if("FAILED".equals(a)){
    		return (("UNKNOWN".equals(b) || "OK".equals(b)));
    	}else if("UNKNOWN".equals(a)){
    		return ("OK".equals(b));
    	}else{
    		return false;
    	}
    }
    
    private void selectNum(String key, Map a, Map b){
    	Integer an = new Integer(a.get(key).toString());
    	Integer bn = new Integer(b.get(key).toString());
    	if("max".equals(key)){
	    	if(bn > an){
	    		a.put("max", bn.toString());
	    	}
    	}else if("min".equals(key)){
    		if(bn < an){
    			a.put("min", bn.toString());
    		}
    	}else{
    		a.put("avg", new Integer((an+bn)/2).toString());
    	}
    }
}
