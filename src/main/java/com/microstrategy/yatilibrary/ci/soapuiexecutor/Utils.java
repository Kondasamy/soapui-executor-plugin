package com.microstrategy.yatilibrary.ci.soapuiexecutor;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import hudson.model.BuildListener;
import com.mongodb.*;

public class Utils {
    private static Mongo mongoClient = null;

	public static String getTimeString(){
    	return new SimpleDateFormat("HH:mm:ss").format(new Date());
	}
	
	public static long getTimeLong(){
		return Calendar.getInstance().getTime().getTime();
	}
	
	public static String toReadableTime(int t){
		StringBuffer rs = new StringBuffer();
		if(t<0){
			rs.append("N/A");
		}
		else if(t<1000){
			rs.append(t);
			rs.append("ms");
		}
		else if(t<60*1000){
			rs.append(t/1000);
			rs.append("s");
		}
		else if(t<3600*1000){
			int min = t/(60*1000);
			rs.append(min);
			rs.append("min ");
			rs.append(t/1000-min*60);
			rs.append("s");
		}
		else{
			int h = t/(3600*1000);
			int min = t/(60*1000) - h*60;
			int s = t/1000 - h*3600 - min*60;
			rs.append(h);
			rs.append("h ");
			rs.append(min);
			rs.append("min ");
			rs.append(s);
			rs.append("s");
		}
		return rs.toString();
	}

    public static Mongo getMongoClient(){
        if(mongoClient == null){
            mongoClient = createMongoClient();
        }
        return mongoClient;
    }

    public static boolean insertSoapUIResult(Map rs){
        DB db = getMongoClient().getDB("jenkins");
        if(!db.isAuthenticated()){
            db.authenticate("jenkins_rw", "jenkins_rw".toCharArray());
        }
        BasicDBObject dbo = new BasicDBObject(rs);
        if(db.collectionExists("soapui_results")){
            DBCollection coll = db.getCollection("soapui_results");
            coll.insert(dbo, WriteConcern.NORMAL);
            return true;
        }else{
            return false;
        }
    }

    public static DBObject getSoapUIResult(Map condition){
        DB db = getMongoClient().getDB("jenkins");
        if(!db.isAuthenticated()){
            db.authenticate("jenkins_rw", "jenkins_rw".toCharArray());
        }
        if(db.collectionExists("soapui_results")){
            DBCollection coll = db.getCollection("soapui_results");
            return coll.findOne(new BasicDBObject(condition));
        }else{
            return null;
        }
    }

    private static Mongo createMongoClient(){
        try {
            return new Mongo("localhost");
        } catch (UnknownHostException e) {
            return null;
        }
    }
}
