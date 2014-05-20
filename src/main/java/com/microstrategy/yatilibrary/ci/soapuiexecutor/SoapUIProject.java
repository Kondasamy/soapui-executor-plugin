package com.microstrategy.yatilibrary.ci.soapuiexecutor;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.AbstractDescribableImpl;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

public class SoapUIProject extends AbstractDescribableImpl<SoapUIProject>{
	private final String xmlFile;
	private final String jsonFile;
	private final boolean dynamic;
	private final boolean enable;
	private final String disableItems;
	private final boolean doCustomConfig;
	private final boolean parallel;
	private final boolean terminate;
	private boolean isExecuted = false;
	private int partNum;
	private Proc proc;
	private String scriptName;
	private int exitCode = 0;
	
    @DataBoundConstructor
    public SoapUIProject(boolean enable, String xmlFile, String jsonFile,
    		boolean dynamic, String disableItems, boolean doCustomConfig, boolean parallel, boolean terminate) {
		this.xmlFile = xmlFile;
		this.jsonFile = jsonFile;
		this.dynamic = dynamic;
		this.enable = enable;
		this.disableItems = disableItems;
		this.doCustomConfig = doCustomConfig;
		this.parallel = parallel;
		this.terminate = terminate;
    }
	
    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
	public String getXmlFile(){
		return xmlFile;
	}
	
	public String getJsonFile(){
		return jsonFile;
	}

	public boolean getDynamic(){
		return dynamic;
	}

	public boolean getEnable(){
		return enable;
	}
	
	public boolean getParallel(){
		return parallel;
	}
	
	public boolean getTerminate(){
		return terminate;
	}
	
	public String getDisableItems(){
		return disableItems;
	}
	
	public boolean getDoCustomConfig(){
		return doCustomConfig;
	}
	
	public int getPartNum(){
		return partNum;
	}
	
	public boolean isExecuted(){
		return this.isExecuted;
	}
	
	public void setPartNum(int partNum){
		this.partNum = partNum;
	}
	
	public int getExitCode(){
		return this.exitCode;
	}
	
	public void killProcess(){
		try {
			this.proc.kill();
		} catch (IOException e) {
			
		} catch (InterruptedException e) {
			
		} catch (NullPointerException e){
			
		}
	}

	public int execute(Launcher launcher, BuildListener listener, AbstractBuild build, String runner, String soapUISettings) throws IOException, InterruptedException{
		if (!this.enable){
    		return 0;
    	}
    	listener.getLogger().println(getTime()+"[SoapUI Executor]Running SoapUI project "+this.xmlFile);

		/** 
		 * fileName refers to the SoapUI project file which is in XML format. Required.
		 * configFile is a json file contains parameters needed. Optional.
		 */

    	FilePath ws = build.getWorkspace();
		FilePath projectFile = ws.child(this.xmlFile);
		FilePath configFile = ws.child(this.jsonFile);

        FilePath projectWs = ws.child("ws").child(projectFile.getBaseName()+"-"+this.partNum);
		projectWs.mkdirs();

		if(!configFile.exists()){
			listener.fatalError(getTime()+"[SoapUI Executor]Config file "+configFile.getName()+" not found.");
			return 2;
		}

		if(!projectFile.exists()){
			listener.fatalError(getTime()+"[SoapUI Executor]Project file "+projectFile.getName()+" not found.");
			return 3;
		}

		listener.getLogger().println(getTime()+"[SoapUI Executor]Creating rttmp file for dynamic configuration.");
		FilePath tmpConfigFile = projectWs.child("runtimeaction.rttmp");
		try{
			if(this.doCustomConfig && !"".equals(this.disableItems)){
				JSONObject newConfigs = (JSONObject) JSONSerializer.toJSON("{"+this.disableItems+"}");
				tmpConfigFile.write(newConfigs.toString(), "UTF-8");
			}
		}catch(Exception e){
			if(tmpConfigFile.exists()){
	        	tmpConfigFile.delete();
	        }
			listener.getLogger().println(getTime()+"[SoapUI Executor]Error in enable/disable configuration.");
			listener.getLogger().println(getTime()+"[SoapUI Executor]Skip "+this.xmlFile);
			return 4;
		}
        
        exportProjectIndex(projectWs);
        
        EnvVars envVars = build.getEnvironment(listener);

		/**
         * Generate a batch file to run this project
         */
		listener.getLogger().println(getTime()+"[SoapUI Executor]Creating batch file to launch SoapUI.");
        FilePath script = null;
        StringBuffer commands = new StringBuffer("\""); 
        commands.append(runner);
        commands.append("\" -a -j -r");
        commands.append(" -f \"");
        commands.append(projectWs.getRemote());
        commands.append("\" -PXMLPATH=\"\" -PCONFIG=\"");
        if(this.dynamic){
            commands.append("OW_");
        }
        commands.append(configFile.getName());
        commands.append("\" ");
        
        if(soapUISettings != null && !"".equals(soapUISettings)){
           	commands.append("-t\"");
           	commands.append(soapUISettings);
           	commands.append("\" ");
        }
        
        String seqGroup = envVars.get("SEQGROUP");
        if(seqGroup!=null && !seqGroup.equals("")){
        	commands.append("-PSEQGROUP=\"");
        	commands.append(seqGroup);
        	commands.append("\" ");
        }
        
        commands.append("\"");
        commands.append(projectFile.getRemote());
        commands.append("\"");
        script = createScriptFile(projectWs, projectFile.getBaseName(), commands.toString(), launcher.isUnix());
        
        /**
         * The RUN process!
         * 
         */
        listener.getLogger().println(getTime()+"[SoapUI Executor]Starting execution.");
        Launcher.ProcStarter starter = launcher.launch().cmds(buildCommandLine(script, launcher.isUnix())).envs(envVars).pwd(projectWs).stdout(listener);
        Proc p = launcher.launch(starter);
        
        this.isExecuted = true;
        
        if(this.jsonFile == null || "".equals(this.jsonFile)){
        	listener.getLogger().println(getTime()+"[SoapUI Executor]No configuration file.");
        }
        else{
          	listener.getLogger().println(getTime()+"[SoapUI Executor]"+this.jsonFile+" is loaded.");
        }
            
        this.scriptName = script.getName();
        this.proc = p;
        if(!this.parallel){
			this.exitCode = p.join();
			return this.exitCode;
        }
		return 0;
	}
	
	public void teardown(Launcher launcher, BuildListener listener, AbstractBuild build, boolean saveOkTxts, boolean expressMode) throws IOException, InterruptedException{
		if((!this.enable) || (!this.isExecuted)){
			return;
		}
       	if(this.proc!=null){
       		listener.getLogger().println(getTime()+"[SoapUI Executor]"+this.xmlFile+"-"+this.partNum+" Process join.");
       		this.exitCode = proc.join();
       	}
       	this.proc = null;
       	FilePath projectWs = build.getWorkspace().child("ws").child(build.getWorkspace().child(this.xmlFile).getBaseName()+"-"+this.partNum);
		listener.getLogger().println(getTime()+"[SoapUI Executor]Execution finished with return value of "+this.exitCode);
//        listener.getLogger().println(getTime()+"[SoapUI Executor]Deleting temp batch&rttmp files.");
//
//        FilePath runnerScript = projectWs.child(this.scriptName);
//        if(runnerScript != null && runnerScript.exists()){
//			runnerScript.delete();
//		}
//
//        FilePath tmpConfigFile = projectWs.child("runtimeaction.rttmp");
//        if(tmpConfigFile!=null && tmpConfigFile.exists()){
//        	tmpConfigFile.delete();
//        }
        
        listener.getLogger().println(getTime()+"[SoapUI Executor]Moving result files.");
		
		FilePath dcScript = null;
		
		String dcCommands = "";
        if(expressMode){
            if(launcher.isUnix()){
                dcCommands = "mv *.jsondata ../../xmlReport/"
                            + "\r\nmv *.xml ../../xmlReport/\r\nmv *.unstable ../../xmlReport/";
            }else{
                dcCommands = "@move *.jsondata ../../xmlReport/ > nul"
                            + "\r\n@move *.xml ../../xmlReport/ > nul\r\n@move *.unstable ../../xmlReport/ > nul";
            }
        }else{
            if(launcher.isUnix()){
                if(saveOkTxts){
                    dcCommands = "mv *-OK.txt\r\nmv *-UNKNOWN.txt ../../txts/\r\nmv *-FAILED.txt ../../txts/"
                            + "\r\nmv *.jd.txt ../../txts/\r\nmv *.jsondata ../../xmlReport/"
                            + "\r\nmv *.xml ../../xmlReport/\r\nmv *.unstable ../../xmlReport/";
                }else{
                    dcCommands = "rm *-OK.txt\r\nmv *-UNKNOWN.txt ../../txts/\r\nmv *-FAILED.txt ../../txts/"
                            + "\r\nmv *.jd.txt ../../txts/\r\nmv *.jsondata ../../xmlReport/"
                            + "\r\nmv *.xml ../../xmlReport/\r\nmv *.unstable ../../xmlReport/";
                }
            }else{
                if(saveOkTxts){
                    dcCommands = "@move *-OK.txt > nul\r\n@move *-UNKNOWN.txt ../../txts/ > nul\r\n@move *-FAILED.txt ../../txts/ > nul"
                            + "\r\n@move *.jd.txt ../../txts/ > nul\r\n@move *.jsondata ../../xmlReport/ > nul"
                            + "\r\n@move *.xml ../../xmlReport/ > nul\r\n@move *.unstable ../../xmlReport/ > nul";
                }else{
                    dcCommands = "@del *-OK.txt > nul\r\n@move *-UNKNOWN.txt ../../txts/ > nul\r\n@move *-FAILED.txt ../../txts/ > nul"
                            + "\r\n@move *.jd.txt ../../txts/ > nul\r\n@move *.jsondata ../../xmlReport/ > nul"
                            + "\r\n@move *.xml ../../xmlReport/ > nul\r\n@move *.unstable ../../xmlReport/ > nul";
                }
            }
        }
		
		EnvVars envVars = build.getEnvironment(listener);
		dcScript = createScriptFile(projectWs, "delete", dcCommands, launcher.isUnix());
        try{
            Launcher.ProcStarter dcStarter = launcher.launch().cmds(buildCommandLine(dcScript, launcher.isUnix())).envs(envVars).pwd(projectWs).stdout(listener);
            launcher.launch(dcStarter).join();
            if(dcScript!=null && dcScript.exists()){
                dcScript.delete();
            }
        }catch(Exception e){
            listener.getLogger().println(getTime()+"[SoapUI Executor]Error occurs when moving the result files.");
        }
	}
	
	private void exportProjectIndex(FilePath destDir) throws IOException{
		FileWriter fw = null;
		try {
			FilePath projectIndexFile = destDir.child("projectIndex");
			projectIndexFile.write((new Integer(this.partNum)).toString(), "UTF-8");
		} catch (Exception e) {
		} finally{
		}
	}
	
    private static String getTime(){
    	return new SimpleDateFormat("HH:mm:ss ").format(new Date());
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

    @Extension
    public static class DescriptorImpl extends Descriptor<SoapUIProject>{
        public String getDisplayName() { return ""; }

        public ListBoxModel doFillXmlFileItems(@AncestorInPath AbstractProject project) throws IOException, InterruptedException{
            ListBoxModel items = new ListBoxModel();
            FilePath ws = project.getSomeWorkspace();
            List<FilePath> dirs = ws.listDirectories();
            for(FilePath file : ws.list("*.xml")){
            	items.add(file.getBaseName(), file.getName());
            }
            for(FilePath dir : dirs){
            	parseFiles(dir, "*.xml", items, dir.getName());
            }
            return items;
        }
        
        public ListBoxModel doFillJsonFileItems(@AncestorInPath AbstractProject project) throws IOException, InterruptedException{
            ListBoxModel items = new ListBoxModel();
            items.add("-", "");
            FilePath ws = project.getSomeWorkspace();
            List<FilePath> dirs = ws.listDirectories();
            for(FilePath file : ws.list("*.json")){
            	items.add(file.getBaseName(), file.getName());
            }
            for(FilePath dir : dirs){
            	parseFiles(dir, "*.json", items, dir.getName());
            }
            for(FilePath file : ws.list("*.conf")){
            	items.add(file.getBaseName(), file.getName());
            }
            for(FilePath dir : dirs){
            	parseFiles(dir, "*.conf", items, dir.getName());
            }
//            for(FilePath file : ws.list("*.ini")){
//            	items.add(file.getBaseName(), file.getName());
//            }
//            for(FilePath dir : dirs){
//            	parseFiles(dir, "*.ini", items, dir.getName());
//            }
            return items;
        }
        
        private void parseFiles(FilePath ws, String fileNames, ListBoxModel lbm, String fileFolder) throws IOException, InterruptedException{
        	if(ws.getName().equals("docs") || ws.getName().equals("xmlReport")){
        		return;
        	}
        	for(FilePath file : ws.list(fileNames)){
            	lbm.add(fileFolder+"/"+file.getBaseName(), fileFolder+"/"+file.getName());
            }
        	List<FilePath> dirs = ws.listDirectories();
        	for(FilePath dir: dirs){
        		parseFiles(dir, fileNames, lbm, fileFolder+"/"+dir.getName());
        	}
        }
    }
}
