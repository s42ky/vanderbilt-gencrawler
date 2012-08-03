package edu.vanderbilt.webtest.plugins.input;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jdom.Element;

import edu.vanderbilt.webtest.plugins.input.InputFiller.InputFillerPlugin;


public class UserDatabasePlugin implements InputFillerPlugin {
	public final static Logger LOGGER = Logger.getLogger(UserDatabasePlugin.class.toString());
	
	//Database configuration
	private String connectionString = "jdbc:mysql://localhost:3306/crawler_users";
	private String usersTable = "_users";
	private String currentTable = "scarf";
	private String dbUserName = "";
	private String dbPassword = "j3ExpHYAKWue5Y5m";
	
	private int defaultLevel = 1;
	//End configuration
	
	private int userLevel = defaultLevel;
	private int userID = 0;
	private HashMap<String,String> userInfo = new HashMap<String,String>();
	
	public void configure(Element e) {
		String level = null;
		
		if(e!=null)
			level = e.getAttributeValue("level");
		
		if(level==null) {
			userLevel = defaultLevel;
			LOGGER.info("Initializing UserDB with user level "+level);
		} else {
			//Try to parse the integer
			try {
				int newLevel = Integer.parseInt(level);
				userLevel = newLevel;
				LOGGER.info("Initialized user level to "+userLevel);
			} catch( NumberFormatException err) {
				userLevel = defaultLevel;
				LOGGER.error("Could not parse the level integer. Defaulting to "+level);
			}
		}
		
		getRandomUser();
	}
	
	public boolean getRandomUser(int newLevel) {
		userLevel = newLevel;
		LOGGER.info("Set user level to "+userLevel);
		return getRandomUser();
	}
	
	
	public boolean getRandomUser() {
		java.sql.Connection conn = null;
		
		Properties props = new Properties();
        props.put("user", dbUserName);
        props.put("password", dbPassword);
        
        try {
            conn = DriverManager.getConnection(connectionString, props);
        
        	String tablesJoin = " "+usersTable+" AS u JOIN "+currentTable+" AS c ON c.uid = u.uid ";
        	Statement s = conn.createStatement();
			s.execute("SELECT FLOOR(RAND() * COUNT(*)) AS `offset` FROM "+ tablesJoin+" WHERE c.level = "+userLevel);
			
			ResultSet r = s.getResultSet();
			r.first();
			String offset = r.getString(1);
			
			s.execute("SELECT * FROM "+tablesJoin+" WHERE c.level = "+userLevel+" LIMIT "+offset+",1");
			r = s.getResultSet();
			if(!r.first()) {
				LOGGER.error("No users found for level "+userLevel);
				return false;
			}
			
			//User info
			ResultSetMetaData meta = r.getMetaData();
			userInfo.clear();
			for(int i=1; i<=meta.getColumnCount(); ++i) {
				userInfo.put(meta.getColumnLabel(i), r.getString(i));
				userInfo.put(meta.getColumnName(i), r.getString(i));
			}
			
			userID = r.getInt("u.uid");	
		} catch (SQLException err) {
			LOGGER.error("SQL error.", err);
		}
        
        LOGGER.info("UserDatabase selected user ID#"+userID);
        LOGGER.debug(userInfo.toString());
        return true;
	}

	public String getValue(String arg) {
		String retval = userInfo.get(arg);
		if(retval==null) {
			LOGGER.warn("No known value for '"+arg+"'");
			return "";
		}
		
		LOGGER.debug("Sending value '"+retval+"' for '"+arg+"'");
		
		return retval;
	}

}
