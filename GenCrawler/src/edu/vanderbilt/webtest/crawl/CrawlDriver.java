package edu.vanderbilt.webtest.crawl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jdom.JDOMException;

import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.ProxyConfiguration;
import com.crawljax.plugins.crawloverview.CrawlOverview;
import com.crawljax.plugins.webscarabwrapper.WebScarabWrapper;

import edu.vanderbilt.webtest.model.ParameterValueDomain;
import edu.vanderbilt.webtest.plugins.ConstantParameterEstimator;
import edu.vanderbilt.webtest.plugins.PageTreeCloneDetector;
import edu.vanderbilt.webtest.plugins.PageTreeVertixIdentifier;
import edu.vanderbilt.webtest.plugins.ParameterDomainUpdater;
import edu.vanderbilt.webtest.plugins.StateLogger;
import edu.vanderbilt.webtest.plugins.input.InputFiller;
import edu.vanderbilt.webtest.plugins.input.InputFinder;
import edu.vanderbilt.webtest.plugins.input.PrecrawlLogin;
import edu.vanderbilt.webtest.plugins.input.UserDatabasePlugin;
import edu.vanderbilt.webtest.plugins.proxy.ProxyLogger;

public class CrawlDriver {
	public static final Logger LOGGER = Logger.getLogger(CrawlDriver.class.getName());
	
	//Project configuration
	public static final String loggingDir = "/srv/logger/";
	public static final String host = "localhost";
	public static final String project = "scarf";
	public static final String projectDir = loggingDir+project+"/";
	
	//Minor project configuration -- should be fine
	public static final String dbname = project;
	private static String rootURL = "http://"+host+"/"+project+"/";
	public static String sqlLog = "/tmp/rtlog.sql";
		
	//Select mode for which pass
	public static Mode mode = Mode.InputFinder;
	public enum Mode {
		InputFinder,		//Preliminary pass -- find input fields
							//  useful for then specifying input
		
		GeneralCrawling		//Main pass + SQL logging
	};
	
	public static int[] levels = { 0, 1, 2 };
	public static int passesPerLevel = 3;
	
	//General crawler configuration.
	private static int maxStates = 50;    // max crawling states.
	private static int maxDepth = 20;     // max crawling depth.
	private static int maxRunTime = 99999999;
	
	private static int trials = 1;        // number of trials.
	
	// spec configuration.
	//private static File interactSpec = new File("/home/sky/workspace/crawl/spec/scarf/interaction-scarf.xml");
	//private static File loginSpec = null; //new File(workingDir + "login-scarf.xml");
	//private static File inputSpec = null; //new File(workingDir + "input-scarf.xml");
	
	
	public static void main(String[] args){
		/*
		 * log4j Configuration
		 * Setup logging levels		
		 */
		
		LOGGER.setLevel(Level.INFO);
		
		Logger.getLogger("com.crawljax").setLevel(Level.WARN);
		Logger.getLogger("com.crawljax.core.CrawljaxController").setLevel(Level.INFO);
		
		//Logger.getLogger("com.crawljax.core").setLevel(Level.INFO);
		Logger.getLogger("com.crawljax.core.state").setLevel(Level.INFO);
		Logger.getLogger("edu.vanderbilt.webtest").setLevel(Level.WARN);
		
		//Logger.getLogger("edu.vanderbilt.webtest.model").setLevel(Level.DEBUG);
		//Logger.getLogger("edu.vanderbilt.webtest.model.visitors").setLevel(Level.WARN);
		Logger.getLogger(PageTreeCloneDetector.class.toString()).setLevel(Level.INFO);
		//Logger.getLogger(ParameterDomainUpdater.class.toString()).setLevel(Level.DEBUG);
		//Logger.getLogger(ConstantParameterEstimator.class.toString()).setLevel(Level.DEBUG);
		//Logger.getLogger(PageTreeVertixIdentifier.class.toString()).setLevel(Level.DEBUG);
		Logger.getLogger(ParameterValueDomain.class.toString()).setLevel(Level.INFO);
		Logger.getLogger("edu.vanderbilt.webtest.plugins.input").setLevel(Level.INFO);
		//Logger.getLogger(CandidateElementExtractor.class.toString()).setLevel(Level.INFO);
		//Logger.getLogger("com.crawljax.plugins").setLevel(Level.DEBUG);
		//Logger.getLogger("com.crawljax.core.state").setLevel(Level.INFO);
				
		File dir = new File(projectDir);
		if(!dir.exists()) {
			LOGGER.fatal("Project logging directory does not exist.", new RuntimeException());
		}
		
		try {
			run();
		} catch (CrawljaxException e) {
			e.printStackTrace();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void run() throws CrawljaxException, ConfigurationException {
		BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%-5p [%t] %C{1}: %m%n")));
		
		boolean rerun = false;
		
		//Configure once plugins
		InputFiller ifp = new InputFiller(projectDir+"InputSpec.xml");
		
		int proxyPort = 4750;
		//Go
		for(int level : levels) {
			PrecrawlLogin loginPlugin = new PrecrawlLogin(rootURL+"login.php", projectDir+"LoginSpec.xml", level);
			CrawlOverview crawlOverview = new CrawlOverview();
			crawlOverview.setOutputFolder(projectDir+"lvl"+level+"/");
			
			for(int i=0; i<passesPerLevel; ++i) {

				CrawlSpecification specification = new CrawlSpecification(rootURL);
				
				specification.setMaximumStates(maxStates);
				specification.setDepth(maxDepth);
				
				if(maxRunTime != -1)
					specification.setMaximumRuntime(maxRunTime);
				else
					specification.setMaximumRuntime(99999999);
				
				specification.setRandomInputInForms(false);
				
				specification.clickDefaultElements();
				specification.dontClick("a").withText("View Paper");
				specification.dontClick("a").withText("Logout");
				
				specification.setStateVertixIdentifier(new PageTreeVertixIdentifier(0.3));
				
				
				CrawljaxConfiguration configuration = new CrawljaxConfiguration();
				configuration.setCrawlSpecification(specification);
				
				/*
				 * Input specification plugin
				 */
				if(mode == Mode.InputFinder) {
					configuration.addPlugin(new InputFinder(projectDir+"inputs.log"));
				}
				
				configuration.addPlugin(ifp);
				configuration.addPlugin(loginPlugin);
				
				configuration.addPlugin(crawlOverview);
				
				/*
				 * Proxy plugins 
				 */
				ProxyConfiguration proxyConfiguration = new ProxyConfiguration();
				proxyConfiguration.setPort(proxyPort);
				proxyConfiguration.setHostname("localhost");
				configuration.setProxyConfiguration(proxyConfiguration);
				WebScarabWrapper proxy = new WebScarabWrapper();
				//proxy.addPlugin(new ProxyPageTokenizer());
				proxy.addPlugin(new ProxyLogger());
				configuration.addPlugin(proxy);
				configuration.addPlugin(new StateLogger());
				
				/*
				 * PageTree plugins
				 * MUST BE IN THIS ORDER
				 */
				configuration.addPlugin(new ParameterDomainUpdater(new ConstantParameterEstimator(4)));
				configuration.addPlugin(new PageTreeCloneDetector());
				
				
				//Configure log4j
				Logger.getRootLogger().removeAllAppenders();
				BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%-5p %35.35C{2}: %m%n")));
				try {
					BasicConfigurator.configure(new FileAppender(
							new PatternLayout("%-7r %-5p %35.35C{2}: %m%n"),
										projectDir+"crawltrace.log", rerun));
					rerun = true;
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				String brk = "==================================================================";
				LOGGER.info("\n\n"+brk+brk);
				LOGGER.info("Started logging for level "+level+" iteration "+i+"\n");
				
				CrawljaxController crawljax = new CrawljaxController(configuration);
				//Go
				loginPlugin.getNewUser();
				
				try {
					LOGGER.info("Controller:" +crawljax.toString());
					LOGGER.info(crawljax.getSession().getInitialState().getUnprocessedCandidateElements().toString());
				} catch(Exception e) {
					LOGGER.info("Couldn't get UPCE. Good.");
				}
				crawljax.run();
			}
		}
	}
}
