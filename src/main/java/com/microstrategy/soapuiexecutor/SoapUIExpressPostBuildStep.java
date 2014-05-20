package com.microstrategy.yatilibrary.ci.soapuiexecutor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.EnvVars;
import hudson.Util;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixProject;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Notifier;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by fbai on 4/15/14.
 */
public class SoapUIExpressPostBuildStep extends Notifier {

    @DataBoundConstructor
    public SoapUIExpressPostBuildStep() {

    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        listener.getLogger().println("[SoapUIExecutor] Moving files to server...");
        FilePath txtDir = new FilePath(build.getRootDir()).child("txts");
        FilePath ws = build.getWorkspace();
        FilePath subWs = ws.child("ws");
        for(FilePath projectWs : subWs.list()){
            if(projectWs.isDirectory()){
                FilePath dcScript = null;

                String dcCommands = "";
                if(launcher.isUnix()){
                    dcCommands = "rm *-OK.txt\r\nrm *.log";
                }else{
                    dcCommands = "@del *-OK.txt > nul\r\n@del *.log > nul";

                }
                EnvVars envVars = build.getEnvironment(listener);
                dcScript = createScriptFile(projectWs, "delete", dcCommands, launcher.isUnix());
                Launcher.ProcStarter dcStarter = launcher.launch().cmds(buildCommandLine(dcScript, launcher.isUnix())).envs(envVars).pwd(projectWs).stdout(listener);
                launcher.launch(dcStarter).join();
                if(dcScript!=null && dcScript.exists()){
                    dcScript.delete();
                }

                projectWs.copyRecursiveTo(txtDir);

                if(launcher.isUnix()){
                    dcCommands = "rm *";
                }else{
                    dcCommands = "@del * > nul";

                }
                dcScript = createScriptFile(projectWs, "delete", dcCommands, launcher.isUnix());
                Launcher.ProcStarter dcStarter2 = launcher.launch().cmds(buildCommandLine(dcScript, launcher.isUnix())).envs(envVars).pwd(projectWs).stdout(listener);
                launcher.launch(dcStarter2).join();
                if(dcScript!=null && dcScript.exists()){
                    dcScript.delete();
                }
            }
        }

        return true;
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
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        @Override
        public String getDisplayName() {
            return "SoapUI executor teardown(for express mode only)";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }
}
