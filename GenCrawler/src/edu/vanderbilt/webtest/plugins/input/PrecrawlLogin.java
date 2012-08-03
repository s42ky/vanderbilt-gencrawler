package edu.vanderbilt.webtest.plugins.input;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.plugin.PreCrawlingPlugin;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;

public class PrecrawlLogin implements PreCrawlingPlugin {
	public final static Logger LOGGER = Logger.getLogger(PrecrawlLogin.class.getName().toString());
	
	//Configuration
	private String connectionString = "jdbc:mysql://localhost:3306/crawler_users";
	private static final int defaultUserLevel = 0;
	
	//Fields
	private final String loginPage;
	private final HashMap<String,String> specifications = new HashMap<String,String>();
	private HashMap<String,String> userInfo = new HashMap<String,String>();
	private String applicationTable = null; //Controls auth level and passwords per-Application
	private String databaseUsername = null;
	private String databasePassword = null;
	private String usersTable = "_users";
	private int userLevel = 0;
	private boolean isInitialized = false;
	private boolean isAnonymousUser = false;
	
	public PrecrawlLogin(String loginURI, String configFile) {
		this(loginURI, configFile, defaultUserLevel);
	}
	
	public PrecrawlLogin(String loginURI, String configFile, int level) {
		userLevel = level;
		loginPage = loginURI;
		
		SAXBuilder parser = new SAXBuilder();
		Document doc;
		try {
			doc = parser.build(configFile);
			@SuppressWarnings("rawtypes")
			List l = doc.getContent();
			for(Object o : l)
			{
				if(o instanceof Element)
				{
					Element e = (Element)o;
					if(e.getName().equals("login_specification"))
					{
						processSpecification(e);
					}
				}
			}
			
			getNewUser();
			
			isInitialized = 	(databaseUsername != null)
						&&	(databasePassword != null)
						&&	(applicationTable != null);
			
		} catch (JDOMException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
				
		//Let's see what we have
		LOGGER.info("Results of configuration import:");
		LOGGER.info("Specifications:\n"+specifications.toString());
	}
	
	
	public void preCrawling(EmbeddedBrowser browser) {
		if(!isInitialized || isAnonymousUser) return;
		
		browser.goToUrl(loginPage);
		
		WebElement lastElement = null;
		//Go through specs
		for(Entry<String, String> field : specifications.entrySet()) {
			WebElement e = browser.getWebElement(new Identification(How.name, field.getKey()));
			if(e==null) {
				LOGGER.warn("Could not find element "+field.getKey());
				continue;
			}
			
			String val = userInfo.get(field.getValue());
			if(val==null) {
				LOGGER.warn("No database info for field "+field.getValue());
				continue;
			}
			e.sendKeys(val);
			lastElement = e;
		}
		
		if(lastElement == null) {
			LOGGER.warn("No input fields filled in. Could not log in.");
			return;
		}
		
		WebElement form = browser.getWebElement(new Identification(How.tag, "form"));
		if(form==null) {
			LOGGER.warn("Could not find form for submit call.");
			return;
		}
		form.submit();
	}
	
	@SuppressWarnings("rawtypes")
	private void processSpecification(Element specE) {
		List l = specE.getContent();
		for(Object o : l)
		{
			if(o instanceof Element)
			{
				Element e = (Element)o;
				if(e.getName().equals("database")) {
					applicationTable = e.getAttributeValue("app_table");
					if(applicationTable==null) {
						LOGGER.warn("Null application table.");
					}
					processSecondLevel(e);
				} else if(e.getName().equals("inputs"))
					processSecondLevel(e);
				else LOGGER.warn("Unrecognized element '"+e.getName()+"'");
			}
		}
	}
	
	private void processSecondLevel(Element subE) {
		@SuppressWarnings("rawtypes")
		List l = subE.getContent();
		for(Object o : l)
		{
			if(o instanceof Element)
			{
				Element e = (Element)o;
				if(e.getName().equals("input"))
					addInputItem(e);
				else if(e.getName().equals("login"))
					setLoginInfo(e);
			}
		}
	}
	
	private void addInputItem(Element inputE) {
		String name = inputE.getAttributeValue("name");
		if(name==null) {
			LOGGER.warn("Couldn't add rule for unnamed input element.");
			return;
		}
		String mapping = inputE.getAttributeValue("field");
		if(mapping==null) {
			LOGGER.warn("Blank field. Set to match name.");
			mapping=name;
		}
		specifications.put(name, mapping);
	}
	
	private void setLoginInfo(Element loginE) {
		String uname = loginE.getAttributeValue("user");
		if(uname==null) {
			LOGGER.error("Login not set. Missing login name.");
			return;
		}
		String upass = loginE.getAttributeValue("password");
		if(upass==null) {
			LOGGER.error("Login not set. Missing user password.");
		}
		databaseUsername = uname;
		databasePassword = upass;
	}
	
	public boolean changeLevel(int newLevel) {
		userLevel = newLevel;
		return getNewUser();
	}
		
	public boolean getNewUser() {
		isAnonymousUser = false;
		java.sql.Connection conn = null;
		
		Properties props = new Properties();
        props.put("user", databaseUsername);
        props.put("password", databasePassword);
        
        try {
            conn = DriverManager.getConnection(connectionString, props);
        
        	String tablesJoin = " "+usersTable+" AS u JOIN "+applicationTable+" AS c ON c.uid = u.uid ";
        	
        	Statement s = conn.createStatement();
			s.execute("SELECT FLOOR(RAND() * COUNT(*)) AS `offset` FROM "+ tablesJoin);
			
			ResultSet r = s.getResultSet();
			r.first();
			String offset = r.getString(1);
			
			String query = "SELECT * FROM "+tablesJoin+" WHERE c.level = "+userLevel+" LIMIT "+offset+",1";
			
        	if(applicationTable==null) {
        		query = "SELECT * FROM "+usersTable+" LIMIT "+offset+",1";
        	}
        	
			s.execute(query);
			r = s.getResultSet();
			if(!r.first()) {
				if(userLevel==0) {
					LOGGER.info("No users for level 0. Set to built-in anonymous.");
					userInfo.clear();
					isAnonymousUser = true;
					return true;
				} else {
					LOGGER.error("No users found for level "+userLevel);
					return false;
				}
			}
			
			//User info
			ResultSetMetaData meta = r.getMetaData();
			userInfo.clear();
			for(int i=1; i<=meta.getColumnCount(); ++i) {
				userInfo.put(meta.getColumnLabel(i), r.getString(i));
				userInfo.put(meta.getColumnName(i), r.getString(i));
			}
		} catch (SQLException err) {
			LOGGER.error("SQL error.", err);
		}
        
        LOGGER.info("UserDatabase selected user ID#"+userInfo.get("uid"));
        LOGGER.debug(userInfo.toString());
        return true;
	}
}
