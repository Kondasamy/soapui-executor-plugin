package com.microstrategy.yatilibrary.ci.soapuiexecutor;

import hudson.Launcher;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.model.Result;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.*;

import java.io.IOException;
import java.lang.Exception;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;

import net.sf.json.JSONSerializer;

/**
 * SoapUI Executor plugin for Jenkins
 * 
 * You need to set properties or environment variables listed below:
 * 1."SOAPUIRUNNER" - Required. For instance: "c:\CI\SoapUI\bin\testrunner.bat"
 * 2."SOAPUISETTINGS" - Optional. For instance: "C:\CI\SoapUI\soapui-settings.xml"
 * 
 * Date: Sep 4th, 2012
 * @author fbai
 *
 * LastUpdate: May 31th, 2013
 */

public class SoapUIExecutorBuilder extends Builder{

    public final ArrayList<SoapUIProject> projects;
    private final boolean doCallBack;
    private final boolean doNotAddUserName;
    private final boolean doSaveOkTxts;
    private final boolean expressMode;

    @DataBoundConstructor
    public SoapUIExecutorBuilder(List<SoapUIProject> projects, boolean doCallBack, boolean doNotAddUserName, boolean doSaveOkTxts, boolean expressMode) {
        this.projects = projects != null ? new ArrayList<SoapUIProject>(projects) : new ArrayList<SoapUIProject>();
        this.doCallBack = doCallBack;
        this.doNotAddUserName = doNotAddUserName;
        this.doSaveOkTxts = doSaveOkTxts;
        this.expressMode = expressMode;
    }
    
    public ArrayList<SoapUIProject> getProjects() {
        return this.projects;
    }
    
	public boolean getDoNotAddUserName(){
		return this.doNotAddUserName;
	}
	
	public boolean getDoSaveOkTxts(){
		return this.doSaveOkTxts;
	}
    
    public boolean getDoCallBack(){
    	return this.doCallBack;
    }

    public boolean getExpressMode(){
        return this.expressMode;
    }

	
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException{
    	listener.getLogger().println("");
    	listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Starting to run...");
    	
    	if(this.projects == null){
    		listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]No SoapUI projects to run!");
    		listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Exit.");
    		return true;
    	}
    	
		FilePath ws = build.getWorkspace();

		
		/**
		 * Get envVars
		 */
		
        EnvVars envVars = build.getEnvironment(listener);

        /**
    	 * Parameters
    	 */
    	String testProject = null;
    	if(envVars.containsKey("project")){
    		testProject = envVars.get("project");
    	}
    	
    	
    	Map<String, String> buildVars = build.getBuildVariables();
    	for(String key: (Set<String>)buildVars.keySet()){
    		listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Build parameter: "+key+"="+buildVars.get(key));
    	}

		for(SoapUIProject project : projects){
			if(project.getDynamic()){
                //FilePath owConfig = ws.child("OW_"+project.getJsonFile());
                FilePath config = ws.child(project.getJsonFile());
                FilePath owConfig = config.sibling("OW_"+config.getName());
                if(!owConfig.exists()){
                    generateConf(config, owConfig, buildVars, testProject, listener);
                }
			}
		}
    	
        /**
         * prepare dir
         */
        
    	/*listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Deleting old contents in workspace.");
    	
    	String[] subfolders = {"xmlReport", "htmlReport", "txts"};
    	
    	for(String subfolder : subfolders){
    		FilePath subdir = ws.child(subfolder);
    		if(subdir.exists()){
    			subdir.deleteContents();
    			listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Old contents in directory "+subfolder+" cleared.");
    		}
    		else{
    			subdir.mkdirs();
    			listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]A new directory "+subfolder+" is created.");
    		}
    	}*/
    	
    	FilePath txtDir = null;
    	try{
    		txtDir = new FilePath(build.getRootDir()).child("txts");
    		if(txtDir!=null && (!txtDir.exists())){
    			txtDir.mkdirs();
    		}
    	}catch(Exception e){
    		listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Failed to create txts folder in build folder."+e.getMessage());
    	}
		
		
        FilePath delScript = null;
        String delCommands = "";
        listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Clearing workspace...");
        if(launcher.isUnix()){
        	delCommands = "rm *-OK.txt\r\nrm *-UNKNOWN.txt\r\nrm *-FAILED.txt\r\nrm *.rttmp\r\nrm *.log\r\nrm soapui.log.*";
        }else{
        	//delCommands = "@del *-OK.txt > nul\r\n@del *-UNKNOWN.txt > nul\r\n@del *-FAILED.txt > nul\r\n@del *.rttmp > nul\r\n@del *.log > nul\r\n@del soapui.log.* > nul\r\n@rd /s /q ws > nul";
            delCommands = "@rd /s /q ws > nul\r\n@rd /s /q xmlReport > nul\r\n@rd /s /q htmlReport > nul\r\n@rd /s /q txts > nul\r\n@mkdir ws > nul\r\n@mkdir xmlReport > nul\r\n@mkdir htmlReport > nul\r\n@mkdir txts > nul";
        }
        delScript = createScriptFile(ws, "delete", delCommands, launcher.isUnix());
        Launcher.ProcStarter delStarter = launcher.launch().cmds(buildCommandLine(delScript, launcher.isUnix())).envs(envVars).pwd(ws).stdout(listener);
        launcher.launch(delStarter).join();
        if(delScript!=null && delScript.exists()){
        	delScript.delete();
        }

		/**
         *  Jenkins must pre-set a system property named "SOAPUIRUNNER" referring to
         *  testrunner.bat of SoapUI.
         */
        String runner = getSoapuiRunner(envVars);
        if (runner == null){
        	listener.fatalError(Utils.getTimeString()+"[SoapUI Executor]System property SOAPUIRUNNER not found.");
        	return false;
        }
        else{
        	FilePath runnerPath = build.getBuiltOn().createPath(runner);
        	if(!runnerPath.exists()){
        		listener.fatalError(Utils.getTimeString()+"[SoapUI Executor]SoapUI not found. "+runner);
        		return false;
        	}
        }

        /**
         * Jenkins can set a system property named "SOAPUISETTINGS" to provide settings file for SoapUI.
         * This parameter is optional.
         * If it's not set, SoapUI will create a brand new settings for this run.
         */
        String soapUISettings = getSoapuiSettings(envVars);
        if (soapUISettings == null || "".equals(soapUISettings)){
        	listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]No specific SoapUI settings for this run. Use default settings.");
        }
        else{
        	listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]SoapUI is loading settings from "+soapUISettings);
        }
        
        long startTimestamp = Utils.getTimeLong();
		boolean isPassed = true;
		boolean terminated = false;
		int enableProjectCount = 0;
		Map mapProjectIndex = new LinkedHashMap();
		for(SoapUIProject project : projects){
			if(project.getEnable()){
				enableProjectCount++;
				listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Enabled project: "+project.getXmlFile());
			}
			if(mapProjectIndex.containsKey(project.getXmlFile())){
				Integer index = (Integer)mapProjectIndex.get(project.getXmlFile());
				if(index == 0){
					index = 1;
				}
				index++;
				mapProjectIndex.put(project.getXmlFile(), index);
				project.setPartNum(index);
			}else{
				mapProjectIndex.put(project.getXmlFile(), 0);
				project.setPartNum(0);
			}
		}
		for(SoapUIProject project: projects){
			if(mapProjectIndex.containsKey(project.getXmlFile())){
				Integer index = (Integer)mapProjectIndex.get(project.getXmlFile());
				if(index != 0 && project.getPartNum()==0){
					project.setPartNum(1);
				}
			}
		}
		listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]"+projects.size()+" projects in total, "+ enableProjectCount +" projects will be executed.");
        List procList = new ArrayList();
        
        
        // Execution!!
	    try{
	        for(SoapUIProject project : projects){
		    	if(project.getEnable()){
		    		int r = project.execute(launcher, listener, build, runner, soapUISettings);
		    		if (r!=0 && project.getTerminate()){
		    			terminated = true;
		    			break;
		    		}
		    	}
	        }
	    }catch (InterruptedException e){
    		listener.getLogger().println("[SoapUI Executor] Process interrupted!");
    		for(SoapUIProject project: projects){
    			project.killProcess();
    		}
    		build.setResult(Result.ABORTED);
    		return false;
    	}catch (IOException e){
    		listener.getLogger().println("[SoapUI Executor]"+e.getMessage());
    		for(SoapUIProject project: projects){
    			project.killProcess();
    		}
    	}
	    
	    // Teardown

	    try{
	    	for(SoapUIProject project : projects){
	        	if(project.getEnable() && project.isExecuted()){
		        	project.teardown(launcher, listener, build, this.doSaveOkTxts, this.expressMode);
		        	if(project.getExitCode() != 0){
		        		isPassed = false;
		        	}
	        	}
	    	}
    	}catch (InterruptedException e){
    		for(SoapUIProject project: projects){
    			project.killProcess();
    		}
    		listener.getLogger().println("[SoapUI Executor] Process interrupted!");
    		build.setResult(Result.ABORTED);
    		return false;
    	}catch (IOException e){
    		listener.getLogger().println("[SoapUI Executor]"+e.getMessage());
    		for(SoapUIProject project: projects){
    			project.killProcess();
    		}
    	}finally{
    		
    	}
    
    	long finishTimestamp = Utils.getTimeLong();

        if(! this.expressMode){
            try{
                listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Moving txt files to builds folder.");
                //ws.child("txts").copyRecursiveTo(txtDir);
                ws.child("txts").copyRecursiveTo(txtDir);
            }catch (Exception e){
                listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]"+e.getMessage());
            }
        }

        build.addAction(new SoapUIListCommentsAction(build));
    	build.addAction(new SoapUITxtFilesAction(build));

		if(terminated){
			//build.getAction(SoapUICommentsAction.class).addComments("Terminated by vital failures. This is usually caused by the problems"
			//		+ "of deployment or testing account/environment.");
			listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Terminated by vital failures.");
			listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]This is usually caused by the problems"
					+ "of deployment or testing account/environment.");
		}

    	/**
		 * 
		 * HTML Path
		 */

    	SoapUIReportParser srp = new SoapUIReportParser();
    	isPassed = srp.parseResults(build, listener, startTimestamp, finishTimestamp, envVars, ws, isPassed);
		Map mapResult = srp.getResult();
		//List unstableSuites = (List) mapResult.get("unstableSuites");
		//if(unstableSuites != null && unstableSuites.size()>0){
			//build.getAction(SoapUICommentsAction.class).addComments("The test suites below failed during the original execution and passed during the re-run process.<br/>");
		//	for(String unstableSuite : (List<String>)unstableSuites){
				//build.getAction(SoapUICommentsAction.class).addComments(unstableSuite+"  ");
		//	}
	//	}

        findNewFeaturesSuites(build, listener, mapResult);
    	listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Making result comparison.");
		resultCompare(build, listener, mapResult);
			
		listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Inserting result data to build.");
		SoapUISummaryAction action = new SoapUISummaryAction();
		//action.setMetadata(mapResult);
		action.setBuild(build);
		action.setDoNotAddUserName(this.doNotAddUserName);
		//build.getActions().add(action);
		build.addAction(action);

        listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Inserting result data to DB...");
        try{
            if(Utils.insertSoapUIResult(mapResult)){
                listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Success.");
            }else{
                listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Failed.");
            }
        }catch(Exception e){
            listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Error! "+e.getMessage());
        }
        listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Finish writting DB.");
		
		if(isPassed){
			build.setResult(Result.SUCCESS);
		}
		else{
			if(((List)mapResult.get("ignoreList")).size()>0 && mapResult.get("ignoreList").equals(mapResult.get("failureList"))){
				mapResult.put("status", "FINISHED");
				build.setResult(Result.SUCCESS);
			}else{
				build.setResult(Result.FAILURE);
			}
		}
		
		FilePath htmlMail = ws.child("htmlReport/mail.html");
		SoapUIMailCreator mail = new SoapUIMailCreator(mapResult, terminated, listener.getLogger());
		mail.createHtmlMail();
		mail.exportToFile(htmlMail);
		
		if(this.doCallBack){
			String returnPath;
		   	if(getDescriptor().getServerUrl() != null){
		   		returnPath = (getDescriptor().getServerUrl());
		   	}
			else{
				returnPath = ("http://pek-build.labs.microstrategy.com:8080/bjweb/ReturnTestResultServlet");
			}
		   	HttpCaller caller = new HttpCaller();

		   	try{
				caller.callBack(build, mapResult, returnPath, listener);
			}catch (Exception e){
				listener.getLogger().println(Utils.getTimeString()+" [SoapUI Executor]Failed to send result to Build Server."+e.getMessage());
			}
		}

		return true;
	}
    
    private void generateConf(FilePath file, FilePath newFile, Map<String, String>params, String project, BuildListener listener) throws IOException, InterruptedException{
    	try{
    		listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Inserting parameters to conf file:"+newFile.getName());
	    	JSONObject confContents = (JSONObject) JSONSerializer.toJSON(file.readToString());
    		confContents.putAll(params);
    		if(confContents.containsKey("project")){
    			Object projectObj = confContents.get("project");
    			if(projectObj instanceof Map){
    				((Map)projectObj).putAll(params);
    			}
    		}
    		newFile.write(confContents.toString(), "UTF-8");
			listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]New conf file created:"+newFile.getName());
    	}catch (Exception e){
    		listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Failed to insert parameters to conf file:"+newFile.getName());
    	}
    }
    
    
    @Override
    public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
        ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(new SoapUIProjectAction(project));
        return actions;
    }

    private void findNewFeaturesSuites(AbstractBuild build, BuildListener listener, Map mapResult)throws IOException{
        if(build.getBuildVariables().containsKey("new_feature_suites")){
            listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Looking for failed suites of new features.");
            int totalNewFeaFailSuites = 0;
            String[] aSuites = build.getBuildVariables().get("new_feature_suites").toString().split(",");
            listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Defined new features are "+build.getBuildVariables().get("new_feature_suites"));
            for(Map mapProject : (List<Map>)mapResult.get("projects")){
                int newFeaFailSuites = 0;
                for(Map mapSuite: (List<Map>)mapProject.get("testsuites")){
                    for(int i=0; i<aSuites.length; i++){
                        if(aSuites[i].trim().equals(mapSuite.get("name").toString().trim())){
                            newFeaFailSuites ++;
                            totalNewFeaFailSuites ++;
                            mapSuite.put("isNewFeature", true);
                            listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Found new feature failed suite: "+mapSuite.get("name").toString());
                        }
                    }
                }
                mapProject.put("newFeatureFailures", newFeaFailSuites);
            }
            mapResult.put("newFeatureFailures", totalNewFeaFailSuites);
            listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Finish searching for failed suites of new features. Founded "+totalNewFeaFailSuites);
        }
        return;
    }
   
    private void resultCompare(AbstractBuild build, BuildListener listener, Map mapResult) throws IOException{
    	boolean newF = false;
    	boolean oldF = false;
 	
    	List ignoreList = new ArrayList<String>();
    	List ignoreHashList = new ArrayList<Integer>();
    	List failureList = new ArrayList<String>();
    	List failureHashList = new ArrayList<Integer>();
    	
    	Map newComments = new LinkedHashMap();
    	List<Map> recentRs = getRecentRs(build, 10, listener);
        listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Track failures of the recent "+recentRs.size());

    	for(Map mapProject : (List<Map>)mapResult.get("projects")){
            boolean pro_newF = false;
            boolean pro_oldF = false;
    		for(Map mapSuite: (List<Map>)mapProject.get("testsuites")){
    			for(Map mapCase: (List<Map>)mapSuite.get("testcases")){
    				StringBuffer failureFullName = new StringBuffer();
    				String caseName = mapCase.get("name").toString();

    				if("FAILED".equals(mapCase.get("status").toString())){
    					Integer lastPassNum= null;
    					try{
    						AbstractBuild<?, ?> preBuild = build;
    						do{
    							preBuild = (AbstractBuild)preBuild.getPreviousCompletedBuild(); 
    						}while(preBuild!=null && preBuild.getAction(SoapUISummaryAction.class) == null);
    						lastPassNum = (Integer) findSameTestCase(preBuild.getAction(SoapUISummaryAction.class).getMetadata(), mapProject.get("name").toString(), mapSuite.get("name").toString(), caseName).get("lastPass");
    					}catch (Exception e){
                            listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Failed to find the lastPass num.");
    					}
    					if(lastPassNum == null){
    						lastPassNum = 0;
    					}
    					mapCase.put("lastPass", lastPassNum);
    					
    					for(Map mapStep: (List<Map>)mapCase.get("teststeps")){
        					if(mapStep.get("status").toString().equals("FAILED")){
        						failureFullName.append(mapStep.get("name"));
            	    			failureFullName.append('-');
        					}
        				}
    					
    					failureFullName.append(caseName);
    					failureFullName.append('-');
    					failureFullName.append(mapSuite.get("name"));
    					failureList.add(failureFullName.toString());
    					failureHashList.add(failureFullName.toString().hashCode());
    					
    					for(Map recentInfo : recentRs){
    						Map prevComments = (Map)recentInfo.get("comments");
    						if(prevComments == null){
                                listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Failed to find comments map from Build.");
    						}else{
                                List prevFailures = (List)recentInfo.get("failureList");
                                if(prevFailures!=null && prevFailures.contains(failureFullName.toString())){
                                    if(prevComments.containsKey(caseName)){
                                        StringBuffer msg = new StringBuffer();
                                        msg.append(Utils.getTimeString()+"[SoapUI Executor]Copy comment for [");
                                        msg.append(caseName);
                                        msg.append("]:<");
                                        msg.append(prevComments.get(caseName));
                                        msg.append("> from Build #");
                                        msg.append(recentInfo.get("n"));
                                        listener.getLogger().println(msg.toString());
                                        mapCase.put("comm", prevComments.get(caseName).toString());
                                        newComments.put(caseName, prevComments.get(caseName).toString());
//    								try{
//    									build.getAction(SoapUICommentsAction.class).addComments(caseName, prevComments.get(caseName).toString());
//    								}catch (Exception e){
//    									//
//    								}
                                        //comments.append("[");
                                        //comments.append(caseName);
                                        //comments.append("]");
                                        //comments.append(prevComments.get(caseName).toString());
                                        break;
                                    }
                                }
                            }
    					}
    					
    					if(mapCase.containsKey("comm")){
    						if(mapCase.get("comm").toString().contains("IGNORE") || mapCase.get("comm").toString().contains("KNOWN")){
    							mapCase.put("oldF", true);
    							oldF = true;
                                pro_oldF = true;
    							ignoreList.add(failureFullName.toString());
								ignoreHashList.add(failureFullName.toString().hashCode());
	    						mapCase.put("lastPass", build.getNumber());
    						}else{
    							mapCase.put("newF", true);
    							newF = true;
                                pro_newF = true;
    						}
    					}else{
							mapCase.put("newF", true);
							newF = true;
                            pro_newF = true;
						}
    				}else{//SUCCESS CASE
    					mapCase.put("lastPass", build.getNumber());
    				}
    			}
    		}
            mapProject.put("hasOldFailure", pro_oldF);
            mapProject.put("hasNewFailure", pro_newF);
    	}
    	mapResult.put("ignoreList", ignoreList);
    	mapResult.put("failureList", failureList);
    	mapResult.put("ignoreHashList", ignoreHashList);
    	mapResult.put("failureHashList", failureHashList);
    	mapResult.put("hasNewFailure", newF);
    	mapResult.put("hasOldFailure", oldF);
    	mapResult.put("hasFailure", newF || oldF);
        try{
            build.getAction(SoapUIListCommentsAction.class).addMapComments(newComments);
        }catch (InterruptedException e){
            listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Failed to save comments. "+e.getMessage());
        }
    }
    
    private Map findSameTestCase(Map rs, String projectName, String suiteName, String caseName){
    	try{
    		for(Map project : (List<Map>)rs.get("projects")){
    			if(projectName.equals(project.get("name").toString())){
    				for(Map testsuite : (List<Map>)project.get("testsuites")){
    					if(suiteName.equals(testsuite.get("name").toString())){
    						for(Map testcase : (List<Map>)testsuite.get("testcases")){
    							if(caseName.equals(testcase.get("name").toString())){
    								return testcase;
    							}
    						}
    					}
    				}
    			}
    		}
    		return null;
    	}catch (Exception e){
    		return null;
    	}
    }
    
    private List getRecentRs(AbstractBuild build, int n, BuildListener listener){
    	List rs = new ArrayList();
    	try{
    		List<AbstractBuild> builds = build.getProject().getBuilds();
    		for(int i=0; i<n&&i<builds.size(); i++){
    			Map buildInfo = new LinkedHashMap();
    			try{
    				Map buildRs = builds.get(i).getAction(SoapUISummaryAction.class).getMetadata();
    				buildInfo.put("failureList", buildRs.get("failureList"));
    				Map prevComments = null;
    				try{
    					prevComments = getCommentsMapFromList(builds.get(i).getAction(SoapUIListCommentsAction.class).getComments());
    				}catch (Exception e){
    					//
    				}
    				Map mapComments = null;
    				try{
    					mapComments = getCommentsMap(builds.get(i).getAction(SoapUICommentsAction.class).getComments());
    				}catch (Exception e){
    					//
    				}
    				if(mapComments!=null && prevComments!=null){
    					prevComments.putAll(mapComments);
    					buildInfo.put("comments", prevComments);
    				}else if(mapComments !=null){
    					buildInfo.put("comments", mapComments);
    				}else if(prevComments!=null){
    					buildInfo.put("comments", prevComments);
    				}
    				
    				buildInfo.put("n", builds.get(i).getNumber());
    				rs.add(buildInfo);
    			}catch(Exception e){
    			}
    		}
    	}catch(Exception e){
    	}
    	return rs;
    }

    private Map getCommentsMapFromList(List<Map<String, String>> listComments){
        Map<String, String> rs = new LinkedHashMap<String, String>();
        for(Map<String, String> comm: listComments){
            if(!"comments".equals(comm.get("key"))){
                rs.put(comm.get("key"), comm.get("value"));
            }
        }
        return rs;
    }

    private Map getCommentsMap(String strComm){
    	Map map = new LinkedHashMap();
    	StringBuffer sb = new StringBuffer(strComm);
		for(int i=0; i<sb.length() && i>=0; i++){
			if(sb.charAt(i)=='['){
				for(int j=i; j<sb.length()&& j>=0; j++){
					if(sb.charAt(j)==']'){
						String key = sb.substring(i+1, j);
						for(i=j; i>=0; i++){
							if(i==sb.length()){
								String value = sb.substring(j+1, i);
								map.put(key, value);
								i--;
								break;
							}
							if(sb.charAt(i)=='['){
								String value = sb.substring(j+1, i);
								map.put(key, value);
								i--;
								break;
							}
						}
						break;
					}
				}
			}
		}
		return map;
    }
    
    private Map getPreviousComments(AbstractBuild build, Integer buildnum){
    	String desc = null;
    	try{
    		desc = build.getProject().getBuildByNumber(buildnum).getDescription();
    		//desc = build.getPreviousCompletedBuild().getDescription();
    	}catch (Exception e){
    		desc = "";
    	}
   		if(desc == null){
       		desc = "";
       	}
    	return getCommentsMap(desc);
    }
    
	private String getFileNameFromPath(String path){
		int index = path.lastIndexOf('\\');
		if(index != -1){
			return path.substring(index+1);
		}
		else{
			return path;
		}
	}
	
	private String getSoapuiRunner(EnvVars envVars){
        String runner = null;
        if (envVars.containsKey("SOAPUIRUNNER")){
          	runner = envVars.get("SOAPUIRUNNER").toString();
        }
        return runner;
	}
	
	private String getSoapuiSettings(EnvVars envVars){
		String settings = null;
		if (envVars.containsKey("SOAPUISETTINGS")){
        	settings = envVars.get("SOAPUISETTINGS").toString();
        }
		return settings;
	}

	private String doubleSlashPath(String path){
		StringBuffer rs = new StringBuffer(path);
		int index = rs.indexOf("\\");
		while(index != -1){
			rs.insert(index, "\\");
			index = rs.indexOf("\\", index+2);
		}
		return rs.toString();
	}

	public FilePath createScriptFile(FilePath dir, String fileName, String contents, boolean isUnix) throws IOException, InterruptedException {
		if(isUnix){
			return dir.createTextTempFile(fileName, ".sh", contents, true);
		}else{
			return dir.createTextTempFile(fileName, ".bat", contents, true);
		}
    }
	
	public String[] buildCommandLine(FilePath script, boolean isUnix) {
		if(isUnix){
			return new String[] {"bash", script.getRemote()};
		}else{
			return new String[] {"cmd","/c","call",script.getRemote()};
		}
    }
	
    protected static String resolveParametersInString(AbstractBuild<?, ?> build, BuildListener listener, String input) {
        try {
            return build.getEnvironment(listener).expand(input);
        } catch (Exception e) {
            listener.getLogger().println(Utils.getTimeString()+"[SoapUI Executor]Failed to resolve parameters in string \""+
            input+"\" due to following error:\n"+e.getMessage());
        }
        return input;
    }
    
    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Descriptor for {@link HelloWorldBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        private String serverUrl;
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        public String getDisplayName() {
            return "Run SoapUI projects";
        }
        

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            serverUrl = formData.getString("serverUrl");
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req,formData);
        }
        
        public String getServerUrl() {
        	load();
            return serverUrl;
        }
    }
}


