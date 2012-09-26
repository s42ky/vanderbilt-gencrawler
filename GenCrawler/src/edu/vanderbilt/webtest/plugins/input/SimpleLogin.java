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

public class SimpleLogin implements PreCrawlingPlugin {
	public final static Logger LOGGER = Logger.getLogger(SimpleLogin.class.getName().toString());
	
	//Fields
	private final String loginPage;

	private String _loginField;
	private String _passField;
	private String _submitButton;
	
	private String _user;
	private String _pass;
	
	public SimpleLogin(String loginURI, String loginField, String passField, String submitButton, String user, String pwd) {
		loginPage = loginURI;
		_loginField = loginField; 
		_passField = passField;
		_submitButton = submitButton;
		
		_user = user;
		_pass = pwd;
	}
	
	public void preCrawling(EmbeddedBrowser browser) {
		browser.goToUrl(loginPage);
		
		browser.getWebElement(new Identification(How.name, _loginField)).sendKeys(_user);

		browser.getWebElement(new Identification(How.name, _passField)).sendKeys(_pass);
		
		Identification rootPath = new Identification(Identification.How.xpath, "/html");
		WebElement root = browser.getWebElement(rootPath);
		List<WebElement> forms = root.findElements(By.tagName("form"));
		LOGGER.info("Num of forms = "+forms.size());
		for(WebElement e : forms){
			LOGGER.info("action = "+e.getAttribute("action"));
			if( e.getAttribute("action").contains(_submitButton) ){
				e.submit();
				return;
			}
		}
		
		WebElement form = browser.getWebElement(new Identification(How.tag, "form"));
		if(form==null) {
			LOGGER.warn("Could not find form for submit call.");
			return;
		}
		form.submit();
		
	}
	
}
