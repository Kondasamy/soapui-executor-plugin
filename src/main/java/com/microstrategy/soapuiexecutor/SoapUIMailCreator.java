package com.microstrategy.yatilibrary.ci.soapuiexecutor;

import hudson.FilePath;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SoapUIMailCreator {
	private StringBuffer content = new StringBuffer();
	private PrintStream log;
	private Map rs;
	private boolean isTerminated;
	
	public SoapUIMailCreator(Map rs, boolean isTerminated, PrintStream log){
		this.rs = rs;
		this.log = log;
		this.isTerminated = isTerminated;
	}
	
	public boolean createHtmlMail(){
		try{
			if(((List)rs.get("projects")).size() == 0){
	    		log.println(Utils.getTimeString()+" [SoapUI Executor]No test results are found!");
	    		this.content.append("<html><body><h1>No Test Result Data Found!</h1><br><p>The problem could be caused by:<br/>");
	    		this.content.append("1. None of the SoapUI projects are enabled, so that no projects are executed.<br/>");
	    		this.content.append("2. There are errors in the SoapUI project <b>setup script</b>, so that none of the test cases are executed.<br/>");
	    		this.content.append("3. There are errors in the SoapUI project <b>teardown script</b>, which is to export result data via soapui-helper.</p>");
	    		this.content.append("<hr size=\"1\">Network QE Team @ MicroStrategy Beijing<br/>Any problems/suggestions, please contact fbai@microstrategy.com</center></body></html>");
	    	}else{
		    	this.content.append("<html><head><title>soapUI Test Results</title><style type=\"text/css\">body { font:normal 80% verdana,arial,helvetica;color:#000000;}");
		    	this.content.append("table tr td, table tr th {font-size: 94%;table-layout:fixed;word-break:break-all;vertical-align: top;} table.details tr th{font-weight: bold;text-align:left;background:#a6caf0;table-layout:fixed; word-break:break-all;}");
		    	this.content.append("table.details tr td{background:#eeeee0;table-layout:fixed;word-break:break-all;} table.newF tr th{font-weight: bold;text-align:left;background:#FF8080;table-layout:fixed; word-break:break-all;}");
		    	this.content.append("table.newF tr td{background:#eeeee0;table-layout:fixed;word-break:break-all;}p {line-height:1.5em;margin-top:0.5em; margin-bottom:1.0em;}");
		    	this.content.append("h1 {margin: 0px 0px 5px; font: 165% verdana,arial,helvetica} h2 {margin-top: 1em; margin-bottom: 0.5em; font: bold 125% verdana,arial,helvetica}");
		    	this.content.append("h3 {margin-bottom: 0.5em; font: bold 115% verdana,arial,helvetica} .Success {font-weight:bold; color:green;}");
		    	this.content.append(".Failure {font-weight:bold; color:red;}.Properties {text-align:right;}.maindiv {}</style></head><body><div style=\"width: 1200px;vertical-align:middle;display:block;\"><center>");
		
		    	this.content.append("<h1><a href=\"");
		    	this.content.append(rs.get("url"));
		    	this.content.append("\">");
		    	this.content.append(rs.get("name")+"</a> - <a class=\"");
		    	if(isTerminated){
		    		this.content.append("Failure");
		    		this.content.append("\" >TERMINATED by Vital Failures");
		    	}else if(rs.get("status").toString().equals("FINISHED")){
		    		this.content.append("Success");
		    		this.content.append("\" >");
		    		if((Boolean)rs.get("hasFailure")){
		    			this.content.append("PASSED with Known Failures");
		    		}else{
		    			this.content.append("ALL PASSED");
		    		}
		    	}else{
		    		this.content.append("Failure");
		    		this.content.append("\" >FAILED");
		    	}
		    	this.content.append("</a></h1>");
		    	if(isTerminated){
		    		this.content.append("<h2>This is usually due to the problems of deployment or testing accounts/environment.</h2>");
		    	}
		    	this.content.append("<hr align=\"center\" width=\"1200\" size=\"1\">");
		    	this.content.append("<b>");
		    	this.content.append(rs.get("trigger"));
		    	this.content.append("</b> | Start Time:<b>");
		    	this.content.append(rs.get("startTime"));
		    	this.content.append("</b> | Duration:<b>");
		    	this.content.append(rs.get("timetaken"));
		    	this.content.append("</b> | Test Machine:<b>");
		    	this.content.append(rs.get("nodeName"));
		    	this.content.append("</b><br/>");
		    	Map buildVars = (Map)rs.get("buildVars");
		    	if(buildVars!=null){
		    		this.content.append("<hr align=\"center\" width=\"1200\" size=\"1\">| ");
		    		for (String key : (Set<String>)buildVars.keySet()){
		    			if("mail_recipients".equals(key)){
		    				continue;
		    			}
		    			this.content.append(key);
		    			this.content.append(":<b>");
		    			Object value = buildVars.get(key);
		    			if(value == null || "".equals(value)){
		    				this.content.append("N/A");
		    			}else{
		    				this.content.append(value);
		    			}
		    			this.content.append("</b> | ");
		    		}
		    		this.content.append("<br/>");
		    	}
		    	List unstableSuites = (List)rs.get("unstableSuites");
		    	if(unstableSuites !=null && unstableSuites.size()!=0){
		    		this.content.append("<hr align=\"center\" width=\"1200\" size=\"1\">"
		    				+ "The test suites below failed during the original execution and passed during the re-run process.<br/> | ");
		    		for (String unstableSuite : (List<String>)unstableSuites){
		    			this.content.append("<b>");
		    			this.content.append(unstableSuite);
		    			this.content.append("</b> | ");
		    		}
		    		this.content.append("<br/>");
		    	}
		    	
		    	this.content.append("<h2>Projects</h2><hr align=\"center\" width=\"1200\" size=\"1\"><table width=\"1200\" cellspacing=\"2\" cellpadding=\"5\" border=\"0\" class=\"details\" ><tr valign=\"top\">");
		    	this.content.append("<th width=\"840\">SoapUI Projects</th><th width=\"90\">TestCases</th><th width=\"80\">Failures</th><th nowrap width=\"80\">Status</th><th nowrap width=\"120\">Timetaken</th></tr>");
		    	
		    	for(Map project : (List<Map>)rs.get("projects")){
		    		if("FINISHED".equals(project.get("status").toString())){
		    			this.content.append("<tr valign=\"top\" class=\"Success\"><td>");
		    			this.content.append(project.get("name"));
		    			this.content.append("</td><td>");
		    			this.content.append(project.get("caseCount"));
		    			this.content.append("</td><td>");
		    			this.content.append(project.get("failedCaseCount"));
		    			this.content.append("</td><td>");
		    			this.content.append("PASSED");
		    			this.content.append("</td><td>");
		    			this.content.append(project.get("timetaken"));
		    			this.content.append("</td></tr>");
		    		}else{
		    			this.content.append("<tr valign=\"top\"><td class=\"Failure\">");
		    			this.content.append(project.get("name"));
		    			this.content.append("</td><td>");
		    			this.content.append(project.get("caseCount"));
		    			this.content.append("</td><td class=\"Failure\">");
		    			this.content.append(project.get("failedCaseCount"));
		    			this.content.append("</td><td class=\"Failure\">");
		    			this.content.append("FAILED");
		    			this.content.append("</td><td>");
		    			this.content.append(project.get("timetaken"));
		    			this.content.append("</td></tr>");
		    		}
		    	}
		    	this.content.append("</table>");
	
		    	if("true".equals(rs.get("hasFailure").toString())){
		    		this.content.append("<h3>Failure List</h3>");
		    		this.content.append("<hr align=\"center\" width=\"1200\" size=\"1\">");
		    		if("true".equals(rs.get("hasNewFailure").toString())){
		    			this.content.append("<table width=\"1200\" cellspacing=\"2\" cellpadding=\"5\" border=\"0\" class=\"newF\" >");
		    			this.content.append("<tr><th width=\"200\">Comments</th><th width=\"660\">New Failures</th><th width=\"80\">Status</th><th width=\"120\">Time Taken</th><th width=\"60\">Steps</th><th width=\"80\">Failures</th><th width=\"100\">Last PASS</th></tr>");
		    			for(Map project : (List<Map>)rs.get("projects")){
		    				for(Map testSuite : (List<Map>)project.get("testsuites")){
		    					for(Map testCase : (List<Map>)testSuite.get("testcases")){
		    						if(testCase.containsKey("newF") && testCase.get("newF").toString().equals("true")){
		    			    			this.content.append("<tr valign=\"top\"><td class=\"Failure\"><pre>");
		    			    			if(testCase.containsKey("comm")){
		    			    				this.content.append(testCase.get("comm"));
		    			    			}
		    			    			this.content.append("</pre></td><td class=\"Failure\">");
		    			    			this.content.append(testSuite.get("name"));
		    			    			this.content.append(" - ");
		    			    			this.content.append(testCase.get("name"));
		    			    			this.content.append("</td><td class=\"Failure\">");
		    			    			this.content.append(testCase.get("status"));
		    			    			this.content.append("</td><td>");
		    			    			this.content.append(testCase.get("timetaken"));
		    			    			this.content.append("</td><td>");
		    			    			this.content.append(testCase.get("stepCount"));
		    			    			this.content.append("</td><td class=\"Failure\">");
		    			    			this.content.append(testCase.get("failedStepCount"));
		    			    			if((Integer)testCase.get("lastPass") != 0){
			    			    			this.content.append("</td><td>#<a href='");
			    			    			this.content.append(rs.get("jobUrl"));
			    			    			this.content.append(testCase.get("lastPass"));
			    			    			this.content.append("'>");
			    			    			this.content.append(testCase.get("lastPass"));
			    			    			this.content.append("</a></td></tr>");
		    			    			}else{
		    			    				this.content.append("</td><td>N/A</td></tr>");
		    			    			}
		    						}
		    					}
		    				}
		    			}
		    			this.content.append("</table>");
		    		}
		    		if("true".equals(rs.get("hasOldFailure").toString())){
		    			this.content.append("<table width=\"1200\" cellspacing=\"2\" cellpadding=\"5\" border=\"0\" class=\"details\" >");
		    			this.content.append("<tr><th width=\"200\">Comments</th><th width=\"660\">Known/Ignored Failures</th><th width=\"80\">Status</th><th width=\"120\">Time Taken</th><th width=\"60\">Steps</th><th width=\"80\">Failures</th><th width=\"100\">Last PASS</th></tr>");
		    			for(Map project : (List<Map>)rs.get("projects")){
		    				for(Map testSuite : (List<Map>)project.get("testsuites")){
		    					for(Map testCase : (List<Map>)testSuite.get("testcases")){
		    						if(testCase.containsKey("oldF") && testCase.get("oldF").toString().equals("true")){
		    			    			this.content.append("<tr valign=\"top\"><td class=\"Failure\" width=\"200\"><pre>");
		    			    			if(testCase.containsKey("comm")){
		    			    				this.content.append(testCase.get("comm"));
		    			    			}
		    			    			this.content.append("</pre></td><td>");
		    			    			this.content.append(testSuite.get("name"));
		    			    			this.content.append(" - ");
		    			    			this.content.append(testCase.get("name"));
		    			    			this.content.append("</td><td class=\"Failure\">");
		    			    			this.content.append(testCase.get("status"));
		    			    			this.content.append("</td><td>");
		    			    			this.content.append(testCase.get("timetaken"));
		    			    			this.content.append("</td><td>");
		    			    			this.content.append(testCase.get("stepCount"));
		    			    			this.content.append("</td><td class=\"Failure\">");
		    			    			this.content.append(testCase.get("failedStepCount"));
		    			    			this.content.append("</td><td>-");
		    			    			this.content.append("</td></tr>");
		    						}
		    					}
		    				}
		    			}
		    			this.content.append("</table>");
		    		}
		    	}
		    	
		    	this.content.append("<h3>Test Case List</h3>");
		    	this.content.append("<hr align=\"center\" width=\"1200\" size=\"1\">");
		    	this.content.append("<table width=\"1200\" cellspacing=\"2\" cellpadding=\"5\" border=\"0\" class=\"details\" >");
				this.content.append("<tr><th width=\"860\">SoapUI TestCase</th><th width=\"80\">Status</th><th width=\"120\">Time Taken</th><th width=\"60\">Steps</th><th width=\"80\">Failures</th></tr>");
				for(Map project : (List<Map>)rs.get("projects")){
					for(Map testSuite : (List<Map>)project.get("testsuites")){
						for(Map testCase : (List<Map>)testSuite.get("testcases")){
							if(testCase.get("status").toString().equals("FINISHED")){
				    			this.content.append("<tr valign=\"top\" class=\"Success\"><td>");
				    			this.content.append(testSuite.get("name"));
				    			this.content.append(" - ");
			    				this.content.append(testCase.get("name"));
				    			this.content.append("</td><td>");
				    			this.content.append("PASSED");
				    			this.content.append("</td><td>");
				    			this.content.append(testCase.get("timetaken"));
				    			this.content.append("</td><td>");
				    			this.content.append(testCase.get("stepCount"));
				    			this.content.append("</td><td>");
				    			this.content.append(testCase.get("failedStepCount"));
				    			this.content.append("</td></tr>");
							}else{
				    			this.content.append("<tr valign=\"top\"><td class=\"Failure\">");
				    			this.content.append(testSuite.get("name"));
				    			this.content.append(" - ");
			    				this.content.append(testCase.get("name"));
				    			this.content.append("</td><td class=\"Failure\">");
				    			this.content.append(testCase.get("status"));
				    			this.content.append("</td><td>");
				    			this.content.append(testCase.get("timetaken"));
				    			this.content.append("</td><td>");
				    			this.content.append(testCase.get("stepCount"));
				    			this.content.append("</td><td class=\"Failure\">");
				    			this.content.append(testCase.get("failedStepCount"));
				    			this.content.append("</td></tr>");
							}
						}
					}
				}
				this.content.append("</table>");
		    	
		    	
				this.content.append("<h3>API Health List</h3>");
				this.content.append("<hr align=\"center\" width=\"1200\" size=\"1\">");
		    	this.content.append("<table width=\"1200\" cellspacing=\"2\" cellpadding=\"5\" border=\"0\" class=\"details\">");
		    	this.content.append("<tr><th width=\"930\">API URL</th><th width=\"150\">Avg. Time</th><th width=\"120\">Status</th></tr>");
		    	for(Map fapi : (List<Map>)rs.get("apis")){
		    		if(fapi.get("status").toString().equals("FAILED")){
		    			this.content.append("<tr valign=\"top\" class=\"Failure\"><td>");
						this.content.append(fapi.get("url"));
						this.content.append("</td><td>");
		    			this.content.append(fapi.get("avg"));
		    			this.content.append("</td><td>");
		    			this.content.append(fapi.get("status"));
		    			this.content.append("</td></tr>");
		    		}
		    	}
		    	for(Map api : (List<Map>)rs.get("apis")){
		        	if(!api.get("status").toString().equals("FAILED")){
		        		this.content.append("<tr valign=\"top\" class=\"Success\"><td>");
		    			this.content.append(api.get("url"));
		    			this.content.append("</td><td>");
		    			this.content.append(api.get("avg"));
		        		this.content.append("</td><td>");
		        		this.content.append(api.get("status"));
		        		this.content.append("</td></tr>");
		        	}
		        }
		    	this.content.append("</table><br/><br/>");
		    	
		    	this.content.append("<hr align=\"center\" width=\"1200\" size=\"1\">Network QE Team @ MicroStrategy Beijing<br/>Any problems/suggestions, please contact fbai@microstrategy.com</center></div></body></html>");
	    	}
		}catch(Exception e){
			log.println(Utils.getTimeString()+" [SoapUI Executor]"+e.getMessage());
			return false;
		}
		return true;
	}
	
	public boolean exportToFile(FilePath filePath){
		try {
			filePath.write(this.content.toString(), "UTF-8");
		} catch (IOException e) {
			log.println(Utils.getTimeString()+" [SoapUI Executor]"+e.getMessage());
			return false;
		} catch (InterruptedException e) {
			log.println(Utils.getTimeString()+" [SoapUI Executor]"+e.getMessage());
			return false;
		}
    	log.println(Utils.getTimeString()+" [SoapUI Executor]mail.html created successfully.");
    	return true;
	}
}
