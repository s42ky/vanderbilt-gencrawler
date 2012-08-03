package edu.vanderbilt.webtest.plugins;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebElement;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.crawlcondition.CrawlConditionChecker;
import com.crawljax.condition.eventablecondition.EventableConditionChecker;
import com.crawljax.core.CandidateCrawlAction;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CandidateElementExtractor;
import com.crawljax.core.Crawler;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.core.state.StateVertix;
import com.crawljax.core.state.identifier.StateVertixIdentifier;
import com.google.common.collect.HashMultimap;

import edu.vanderbilt.webtest.model.PageTree;
import edu.vanderbilt.webtest.model.PageTree.PageTreeVisitor;
import edu.vanderbilt.webtest.plugins.input.InputFiller;

public class PageTreeVertixIdentifier implements StateVertixIdentifier {
	public static final Logger LOGGER = Logger.getLogger(PageTreeVertixIdentifier.class.getName());
	private static double revisitProbability = 0.05;
	private PageTree pageTree = null;
	private StateVertix _parent;
	
	public PageTreeVertixIdentifier() {
		LOGGER.setLevel(Level.DEBUG);
	}
	
	public PageTreeVertixIdentifier(double revisitChance) {
		LOGGER.setLevel(Level.DEBUG);
		revisitProbability = revisitChance;
		if(revisitChance>1.0) { 
			LOGGER.error("Revisit chance out of bounds. Must be [0.0, 1.0].");
			revisitProbability=1.0;
		}
		if(revisitChance<0.0) { 
			LOGGER.error("Revisit chance out of bounds. Must be [0.0, 1.0].");
			revisitProbability=0.0;
		}
		
		LOGGER.info("Setup with revisit chance of "+revisitProbability);
	}
	
	private HashMultimap<String,String> parameterValues = HashMultimap.create();
	private String parameterlessLookupString = null;
	
	private static CandidateElementExtractor extractor = null;
	
	public void addParameter(String lookupString, String value) {
		parameterValues.put(lookupString, value);
	}
	
	public Set<String> getParameterIdentifiers() {
		//Make sure we've populated
		if(parameterlessLookupString==null) extractParameters();
		
		return parameterValues.keySet();
	}
	
	public Set<String> getValuesForParameter(String paramName) {
		//Make sure we've populated
		if(parameterlessLookupString==null) extractParameters();
		
		return parameterValues.get(paramName);
	}
	
	public void init(StateVertix node, Crawler crawler) {
		_parent = node;
		
		if(extractor==null) {
			ParamaterDomainExtractorManager pdem = new ParamaterDomainExtractorManager(
				new EventableConditionChecker(crawler.getController().getConfigurationReader().getEventableConditions()),
				new CrawlConditionChecker(crawler.getController().getConfigurationReader().getCrawlSpecificationReader().getCrawlConditions()));
			
			extractor = new CandidateElementExtractor(pdem, crawler.getBrowser(), crawler.getFormHandler(),
							crawler.getController().getConfigurationReader().getCrawlSpecificationReader());
		}
		//Run the search for candidates now
		node.searchForCandidateElements(extractor,
			crawler.getController().getConfigurationReader().getTagElements(),
			crawler.getController().getConfigurationReader().getExcludeTagElements(),
			crawler.getController().getConfigurationReader().getCrawlSpecificationReader().getClickOnce());
		
		//All candidate elements unprocessed at this point
		List<CandidateElement> candidateElements = node.getUnprocessedCandidateElements();
		
		pageTree = new PageTree();
		
		for(CandidateElement ce : candidateElements) {
			String exactXPath = ce.getIdentification().getValue().toUpperCase(); //XPathP
			//Generalize XPath by removing the [##]s
			String xpath = exactXPath.replaceAll("\\[\\d+\\]", "");
			
			String tag = ce.getElement().getTagName();
			
			Vector<String> v = new Vector<String>();
			v.add(xpath);
			
			if(tag.equalsIgnoreCase("a")) {
				//grab href
				String href = ce.getElement().getAttribute("href");
				pageTree.addLink(xpath, href);
			} else if(tag.equalsIgnoreCase("input")) {
				//NodeList forms = dom.getElementsByTagName("form");
				
				if(exactXPath.contains("FORM")) {
					int endCut = exactXPath.indexOf("/",exactXPath.lastIndexOf("FORM"));
					String formXPath = exactXPath.substring(0,endCut);
					
					WebElement form = crawler.getBrowser().getWebElement(new Identification(How.xpath, formXPath));
					pageTree.addForm(xpath, form);
					
					LOGGER.debug("Added form to page tree.");
					//LOGGER.debug("Structure:"+pageTree.toFormattedString());
				} else {
					//Button not in a form
					LOGGER.debug("Adding button to pageTree: "+v.toString());
					v.add("BUTTON");
					pageTree.add(v, -1);
					if(ce.getElement().hasAttribute("name")) {
						v.add(ce.getElement().getAttribute("name"));
						if(ce.getElement().hasAttribute("value"))
							v.add(ce.getElement().getAttribute("value"));
					}
				}
				
			} else {
				String action = ":click";
				v.add(action);
				
				LOGGER.warn("Discarding vector for unknown element: "+v.toString());
				//pageTree.add(v, -1);
			}
		}
		
		if(candidateElements.size()==0)
			LOGGER.warn("No candidate elements on page.");
	}
	
	private static HashSet<String> pagesVisited = new HashSet<String>();
	private Random revisitRandomGenerator = new Random();
	
	private boolean revisitRandomTest() {
		boolean retval = (revisitRandomGenerator.nextDouble()<=revisitProbability);
		if(retval) LOGGER.debug("Random test => revisiting state.");
		//else LOGGER.debug("Random test failed. Not revisiting state.");
		return retval;
	}
	
	public boolean screenAction(CandidateCrawlAction action, Crawler reqCrawler) {
		if(action==null) return false;
		
		CandidateElement element = action.getCandidateElement();
		String tag = element.getElement().getTagName();
		
		LOGGER.debug("Screener processing tag "+tag);
		//System.err.println("Screener processing tag "+tag);
		
		if(tag.equalsIgnoreCase("a")) {
			String href = element.getElement().getAttribute("href");
			String paramString = "";
			//page
			if(href.contains("?")) {
				paramString = href.substring(href.indexOf("?")+1);
				href = href.substring(0,href.indexOf("?"));
			}
			
			if(paramString.equals("")) { //Page without parameters
				LOGGER.debug("Screen: processing page w/o params.");
				boolean newPage = pagesVisited.add(href);
				if(newPage) return false;
				else return !revisitRandomTest();
			} else {
				LOGGER.debug("Screen: processing page with params.");
				Pattern param_regex = Pattern.compile("([^&=?]+)=([^&=]+)");
				Matcher m = param_regex.matcher(paramString);
				
				HashSet<String> unboundedParams = new HashSet<String>();
				while(m.find()) {
					if(ParameterDomainUpdater.getPVD().lookup(href+"?"+m.group(1))==null)
						return false;
					
					if(ParameterDomainUpdater.getPVD().lookup(href+"?"+m.group(1)).isBounded()) {
						LOGGER.debug("Encountered bounded param. Processing.");
						return false;
					}
					
					unboundedParams.add(m.group(1));
				}
				
				String pageID = href+"?"+unboundedParams.toString();
				boolean newPage = pagesVisited.add(pageID);
				if(newPage) return false;
				else return !revisitRandomTest();
			}
		} else if(tag.equalsIgnoreCase("input")) {
			//TODO implement form lookup
			//Go ahead and accept forms for now
			LOGGER.debug("Screen: keeping form.");
			String exactXPath = action.getCandidateElement().getIdentification().getValue().toUpperCase();
			
			
			return false;
		}
		
		LOGGER.debug("Screen: Discarding unknown element.");
		//Discard other elements
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof PageTreeVertixIdentifier)) 
			return false;
		
		PageTreeVertixIdentifier pt = (PageTreeVertixIdentifier) o;
		
		return pageTree.toString().equals(pt.pageTree.toString());
	}
	
	public boolean equivalent(Object o) {
		return equals(o);
	}

	public StateVertixIdentifier getNew() {
		return new PageTreeVertixIdentifier();
	}
	
	public String getIdentifierString() {
		return pageTree.toString();
	}
	
	public String getParameterlessLookup() {
		if(parameterlessLookupString==null)
			extractParameters();
		
		return parameterlessLookupString;
	}
	
	public void visitPageTree(PageTree.PageTreeVisitor v) {
		pageTree.accept(v);
	}
	
	private void extractParameters() {
		LOGGER.debug("Calling extract parameters.");
		ParameterExtractorVisitor pev = new ParameterExtractorVisitor(this);
		LOGGER.debug("Found "+parameterValues.keySet().size()+" keys and "+parameterValues.size()+" values");
		//Populate parameter/value map
		pageTree.accept(pev);
		parameterlessLookupString = pev.getResult();
	}
	
	class ParameterExtractorVisitor implements PageTreeVisitor {
		LinkedList<String> visitPath = new LinkedList<String>();

		 PageTreeVertixIdentifier root;
		
		ParameterExtractorVisitor( PageTreeVertixIdentifier exploreRoot) {
			root = exploreRoot;
		}
		
		private StringBuilder sb = new StringBuilder("");
		
		public void visit(PageTree pt) {
			if(pt.isParameter()) {
				if(visitPath.size()<3) {
					LOGGER.warn("Incomplete Parameter Path");
					if(visitPath.size()<2) return;
				}
				
				//First is Xpath, so ignore
				StringBuilder paramURI = new StringBuilder("");
				for(int i=1; i<visitPath.size()-1; ++i) {
					paramURI.append(visitPath.get(i));
				}
				
				String key = paramURI.toString()+"?"+visitPath.getLast();
				if(pt.getChildren().isEmpty()) {
					LOGGER.debug("Adding NULL for parameter "+key);
					root.addParameter(key, "");
					return;
				} else { //Add values
					for(String value : pt.getChildren().keySet()) {
						root.addParameter(key, value);
						LOGGER.debug("Add param pair "+key+"="+value);
					}
				}
				return;
			} else { //Recurse
				for(String s : pt.getChildren().keySet()) {
					PageTree next = pt.getChildren().get(s);
					
					//String builder for parameterless lookup
					if(!next.isParameter()) {
						sb.append("|");
						sb.append(s);
						sb.append("|");
					}
					
					visitPath.addLast(s);
					next.accept(this);
					visitPath.removeLast();
				}
			}
		}
		
		public String getResult() {
			return sb.toString();
		}
	}

	public HashMultimap<String, String> getValueMap() {
		return parameterValues;
	}
}
