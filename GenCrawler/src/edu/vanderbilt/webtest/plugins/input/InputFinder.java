package edu.vanderbilt.webtest.plugins.input;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.state.Identification;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.PostCrawlingPlugin;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class InputFinder implements OnNewStatePlugin, PostCrawlingPlugin {
	private static SortedSet<String> elementSignatures = new TreeSet<String>();
	public final Logger LOGGER = Logger.getLogger(InputFinder.class.getName());
	
	private String logfile;
	
	public InputFinder(String log) {
		logfile = log;
	}
		
	public void postCrawling(CrawlSession session) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(logfile, false));
			for(String input : InputFinder.elementSignatures) {
				bw.write(input + "\n");
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void onNewState(CrawlSession session) {
		Identification rootPath = new Identification(Identification.How.xpath, "/html");
		WebElement root = session.getBrowser().getWebElement(rootPath);
		List<WebElement> forms = root.findElements(By.tagName("form"));
		
		List<WebElement> allInputs = root.findElements(By.tagName("input"));
		
		for(WebElement cur_form : forms) {
			List<WebElement> cur_inputs = cur_form.findElements(By.tagName("input"));
			allInputs.removeAll(cur_inputs);
			
			//if(LOGGER.isInfoEnabled()) {
			for(WebElement input : cur_inputs) {
				String log_msg = "{URL: "+session.getBrowser().getCurrentUrl()+" "; 
				log_msg += "Form ["+cur_form.getAttribute("name")+" #"+cur_form.getAttribute("id")+"] ";
				log_msg += cur_form.getAttribute("method") + " to " + cur_form.getAttribute("action") + " || ";
				log_msg += "Input["+input.getAttribute("type")+"] element @ "+input.getLocation().toString()+" size "+input.getSize().toString();
				log_msg += " Name: "+input.getAttribute("name") + " Id: "+input.getAttribute("id")+" DefValue: "+input.getAttribute("value");
				LOGGER.info(log_msg);
				elementSignatures.add(log_msg);
			//}
			}
			
		}
		
		//if(LOGGER.isInfoEnabled()) {
			for(WebElement input : allInputs) {
				String log_msg = "{URL: "+session.getBrowser().getCurrentUrl()+" "; 
				log_msg += "Form [NULL FORM] || ";
				log_msg += "Input["+input.getAttribute("type")+"] element @ "+input.getLocation().toString()+" size "+input.getSize().toString();
				log_msg += " Name: "+input.getAttribute("name") + " Id: "+input.getAttribute("id")+" DefValue: "+input.getAttribute("value");
				LOGGER.info(log_msg);
				elementSignatures.add(log_msg);
			}
		//}
	}

}
