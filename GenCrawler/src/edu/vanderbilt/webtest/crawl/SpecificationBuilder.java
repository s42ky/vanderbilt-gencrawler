package edu.vanderbilt.webtest.crawl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.crawljax.condition.browserwaiter.ExpectedElementCondition;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.RegularExpressionVertixParser;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;

public class SpecificationBuilder {
	private File inputFile;
	private CrawlSpecification specification;
	private static final Logger LOGGER = Logger.getLogger(SpecificationBuilder.class);
	private RegularExpressionVertixParser regularExpressionVertixParser = new RegularExpressionVertixParser();
	@SuppressWarnings("unused")
	private long pageLoadTimeout;
	public SpecificationBuilder(CrawlSpecification specification, File inputFile, long pageLoadTimeout) {
		super();
		this.inputFile = inputFile;
		this.specification = specification;
		this.pageLoadTimeout = pageLoadTimeout;
	}
	
	public void constructCrawlSpecification() throws JDOMException, IOException{	
		SAXBuilder parser = new SAXBuilder();
		
		Document doc = parser.build(inputFile);
		
		@SuppressWarnings("rawtypes")
		List l = doc.getContent();
		for(Object o : l)
		{
			if(o instanceof Element)
			{
				Element e = (Element)o;
				if(e.getName().equals("specification"))
				{
					processSpecification(e);
				}
			}
		}
		//specification.setRegularExpressionVertixParser(regularExpressionVertixParser);
		
	}

	private void processSpecification(Element specE) {
		@SuppressWarnings("rawtypes")
		List l = specE.getContent();
		for(Object o : l)
		{
			if(o instanceof Element)
			{
				Element e = (Element)o;
				if(e.getName().equals("click"))
				{
					processClick(e);
				}else if(e.getName().equals("noClick"))
				{
					processNoClick(e);
				}else if(e.getName().equals("matchOverride")){
					processMatchOverride(e);
				}else if (e.getName().equals("waitFor")){
					processWaitFor(e);
				}
			}
		}
		
	}

	private void processWaitFor(Element override) {
		@SuppressWarnings("rawtypes")
		List l = override.getContent();
		String value = null;
		How how = null;
		String url = null;
		for(Object o : l)
		{
			if(o instanceof Element)
			{
				Element e = (Element)o;
				if(e.getName().equals("id"))
				{
					how = How.id;
				}else if (e.getName().equals("text")){
					how = How.text;
				}else if (e.getName().equals("value")){
					value = e.getText();
				}else if (e.getName().equals("url")){
					url = e.getText();
				}
			}
		}
		if(how!=null && value!=null&& url!=null)
			specification.waitFor(url, new ExpectedElementCondition(new Identification(how, value)));
	}

	private void processMatchOverride(Element override) {
		@SuppressWarnings("rawtypes")
		List l = override.getContent();
		for(Object o : l)
		{
			if(o instanceof Element)
			{
				Element e = (Element)o;
				if(e.getName().equals("match"))
				{
					processMatch(e);
				}
			}
		}
		
	}

	private void processMatch(Element match) {
		@SuppressWarnings("rawtypes")
		List l = match.getContent();
		String pattern = null;
		String replace = null;
		for(Object o : l)
		{
			if(o instanceof Element)
			{
				Element e = (Element)o;
				if(e.getName().equals("pattern"))
				{
					pattern = e.getText();
				}else if(e.getName().equals("replace"))
				{
					replace = e.getText();
				}
			}
		}
		
		if(pattern!=null){
			Pattern p = Pattern.compile(pattern);
			LOGGER.info("Adding pattern " + p);
		
			if(replace!=null){
				regularExpressionVertixParser.addReplacePattern(p, replace);
			}else{
				regularExpressionVertixParser.addMatchPattern(p);
			}
		}
		
	}



	private void processNoClick(Element e) {
		String tag = e.getChildText("tag");
		if(tag==null)
			return;
		
		addModifiersNoClick(e,tag);
	}

	private void processClick(Element clickE) {
		String tag = clickE.getChildText("tag");
		if(tag==null)
			return;
	//	CrawlElement crawlElement = specification.click(tag);
		addModifiersClick(clickE,tag);
		
		
		
	}

	private void addModifiersNoClick(Element clickE, String tag) {
		@SuppressWarnings("rawtypes")
		List l = clickE.getContent();
		
		for(Object o : l)
		{
			if(o instanceof Element)
			{
				Element e = (Element)o;
				
				if(e.getName().equals("xpath"))
				{
					specification.dontClick(tag).underXPath(e.getText());
				}else if(e.getName().equals("attribute"))
				{
					specification.dontClick(tag).withAttribute(e.getChildText("name"), e.getChildText("value"));
				}
				else if(e.getName().equals("text"))
				{
					specification.dontClick(tag).withText(e.getText());
				} 
			}
		}
	}
	
	private void addModifiersClick(Element clickE, String tag) {
		@SuppressWarnings("rawtypes")
		List l = clickE.getContent();
		
		for(Object o : l)
		{
			if(o instanceof Element)
			{
				Element e = (Element)o;
				LOGGER.debug(e.getName());
				if(e.getName().equals("xpath"))
				{
					specification.click(tag).underXPath(e.getText());
					
				}else if(e.getName().equals("attribute"))
				{
					specification.click(tag).withAttribute(e.getChildText("name"), e.getChildText("value"));
					if(LOGGER.isDebugEnabled()) {
						LOGGER.debug(tag + " " + e.getChildText("name") + " " + e.getChildText("value"));
					}
				}
				else if(e.getName().equals("text"))
				{
					specification.click(tag).withText(e.getText());
				} 
			}
		}
	}
	
}
