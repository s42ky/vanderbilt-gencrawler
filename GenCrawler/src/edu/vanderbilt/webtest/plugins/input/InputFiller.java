package edu.vanderbilt.webtest.plugins.input;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.plugin.OnRevisitStatePlugin;
import com.crawljax.core.plugin.PreStateCrawlingPlugin;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.StateVertix;

public class InputFiller implements PreStateCrawlingPlugin, OnRevisitStatePlugin {
	public final static Logger LOGGER = Logger.getLogger(InputFiller.class.getName());
	
	public interface InputFillerPlugin {

		//Initial setup. Provided with XML Element from config file
		public void configure(Element e);
		
		//Return a value to input into a form field
		public String getValue(String arg);
	}
	
	//Map from Page to InputDescriptor	
	static HashSet<InputDescriptor> specifications;
	HashMap<String, Element> pluginConfig;
	
	public static HashMap<String, HashSet<String>> dataLists;
	public static HashMap<String, InputFillerPlugin> plugins;

	public static void filter(HashSet<InputDescriptor> candidates, String form_attr, String filter) {
		for(Iterator<InputDescriptor> it = candidates.iterator(); it.hasNext(); ) {
			InputDescriptor next = it.next();
			if(!next.inFilter(form_attr,  filter))
				it.remove();
		}
	}
	
	public static HashSet<InputDescriptor> filterOnPage(String page) {
		//Iterator<String> it_key =  specifications.keySet().iterator();
		//Iterator<InputDescriptor> it_val = specifications.values().iterator();
		
		HashSet<InputDescriptor> filteredSet = new HashSet<InputDescriptor>();
		/*
		while(it_key.hasNext() && it_val.hasNext()) {
			String key = it_key.next();
			if(key.equals(InputDescriptor.ANY_STRING) || key.equals(page)) {
				filteredSet.add(it_val.next());
			} else it_val.next(); //Throw away
		}//*/
		
		filteredSet.addAll(specifications);
		return filteredSet;
	}
	
	public InputFiller(String configFile) {
		//Init
		specifications = new HashSet<InputDescriptor>();
		pluginConfig = new HashMap<String,Element>();
		dataLists = new HashMap<String, HashSet<String>>();
		plugins = new HashMap<String, InputFillerPlugin>();
		
		//Load configuration
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
					if(e.getName().equals("input_specification"))
					{
						processSpecification(e);
					}
				}
			}

			//Let's see what we have
			LOGGER.info("Results of configuration import:");
			LOGGER.info("Specifications:\n"+specifications.toString());
			LOGGER.info("\nPlugins:\n"+pluginConfig.toString());
			LOGGER.info("\nLists:\n"+dataLists.toString());
		} catch (JDOMException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public void addPlugin(String name, InputFillerPlugin p) {
		p.configure(pluginConfig.get(name));
		plugins.put(name, p);
	}
	
	@SuppressWarnings("rawtypes")
	private void processSpecification(Element specE) {
		List l = specE.getContent();
		for(Object o : l)
		{
			if(o instanceof Element)
			{
				Element e = (Element)o;
				if(e.getName().equals("plugins"))
					processSecondLevel(e);
				else if(e.getName().equals("lists"))
					processSecondLevel(e);
				else if(e.getName().equals("inputs"))
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
				else if(e.getName().equals("plugin"))
					addPluginConfiguration(e);
				else if(e.getName().equals("list"))
					addInputList(e);
			}
		}
	}
	
	private void addPluginConfiguration(Element pluginE) {
		String name = pluginE.getAttributeValue("name");
		if(name==null) {
			LOGGER.error("Couldn't add unnamed plugin.");
			return;
		} else if(pluginConfig.containsKey(name)) {
			LOGGER.warn("Duplicate plugin for '"+name+"'. Only first added.");
			return;
		}
		
		//Forward whole plugin element to the plugin itself
		//That way it can configure itself (when added)
		pluginConfig.put(name, pluginE);
	}
	
	private void addInputList(Element listE) {
		String name = listE.getAttributeValue("name");
		if(name==null) {
			LOGGER.error("Couldn't add unnamed list.");
			return;
		} else if(dataLists.containsKey(name)) {
			LOGGER.warn("Warning: Duplicate list for '"+name+"'. Combined both.");
		} else {
			dataLists.put(name, new HashSet<String>());
		}
		
		@SuppressWarnings("rawtypes")
		List l = listE.getChildren();
		for(Object o : l) {
			if(o instanceof Element) {
				Element e = (Element) o;
				if(e.getName().equals("value"))
					dataLists.get(name).add(e.getTextTrim());
			}
		}
	}
	
	private void addInputItem(Element inputE) {
		//attrs: name, valtype, value
		//sub elements: form_attr [ name, value ]
		
		String name = inputE.getAttributeValue("name");
		if(name==null) {
			LOGGER.warn("Couldn't add rule for unnamed input element.");
			return;
		}
		//Build specification
		String valtype = inputE.getAttributeValue("valtype");
		InputDescriptor.ValType value_type = InputDescriptor.ValType.TEXT;
		if(valtype!=null) {
			if(valtype.equals("plugin")) value_type = InputDescriptor.ValType.PLUGIN;
			if(valtype.equals("list")) value_type = InputDescriptor.ValType.LIST;
		} //Otherwise defaults to TEXT
		
		String value = inputE.getAttributeValue("value");
		if(value==null) value = ""; //Better than nothing...
		if(value_type==InputDescriptor.ValType.TEXT && value.equals("")) {
			LOGGER.warn("Cannot set blank TEXT value for input specification.");
			return;
		}
		
		//Get form filters
		@SuppressWarnings("unchecked")
		List<Element> l = inputE.getChildren("form_attr");
		
		HashMap<String,String> attrs = new HashMap<String,String>();
		for(Element fa : l) {
			String key = fa.getAttributeValue("name");
			String val = fa.getAttributeValue("value");
			if(key==null || val==null) continue;
			attrs.put(key,  val);
		}
		
		String params = inputE.getAttributeValue("arg");
		//Create spec
		InputDescriptor spec = new InputDescriptor(name, value_type, value, params, attrs);
		//TODO maybe add url filtering
		specifications.add(spec);
	}
	
	public void preStateCrawling(CrawlSession session,
			List<CandidateElement> candidateElements) {
		run(session);
	}
	
	public void onRevisitState(CrawlSession session, StateVertix currentState) {
		run(session);
	}
	
	//@Override
	public static void run(CrawlSession session) {
		
		LOGGER.debug("Running input filler plugin. Page: "+session.getBrowser().getCurrentUrl());
		
		HashSet<InputDescriptor> checkSet = filterOnPage(session.getBrowser().getCurrentUrl());
		
		Identification htmlId = new Identification(Identification.How.xpath, "/html");
		WebElement html = session.getBrowser().getWebElement(htmlId);
		
		//List<WebElement> allFormElements = html.findElements(By.tagName("input"));
		List<WebElement> forms = html.findElements(By.tagName("form"));
		
		//forms.add(null);
		
		for(WebElement form : forms) {
			LOGGER.debug("Trying form "+form.toString());
			
			HashSet<InputDescriptor> candidates = checkSet;
			filter(candidates, "action", form.getAttribute("action"));
			filter(candidates, "id", form.getAttribute("id"));
			filter(candidates, "method", form.getAttribute("method"));
			filter(candidates, "name", form.getAttribute("name"));
			
			//Fill in candidates for each form
			for(InputDescriptor field : candidates) {
				WebElement cur = null;
				cur = form.findElement(By.name(field.getFieldName()));
				
				if(cur != null) {
					cur.sendKeys(field.getValue());
					LOGGER.debug("Typing in field "+cur.getAttribute("name")+": "+field.getValue());
				}
			}
		}

		//TODO Process NULL form (elements not in a form)
	}

	public static void run(WebElement form) {
		HashSet<InputDescriptor> checkSet = filterOnPage("");
		
		HashSet<InputDescriptor> candidates = checkSet;
		filter(candidates, "action", form.getAttribute("action"));
		filter(candidates, "id", form.getAttribute("id"));
		filter(candidates, "method", form.getAttribute("method"));
		filter(candidates, "name", form.getAttribute("name"));
		
		//Fill in candidates for each form
		for(InputDescriptor field : candidates) {
			WebElement cur = null;
			cur = form.findElement(By.name(field.getFieldName()));
			
			if(cur != null) cur.sendKeys(field.getValue());
			
			LOGGER.debug("Typing in field "+cur.getAttribute("name")+": "+field.getValue());
		}
	}


}

class InputDescriptor {
	
	public enum ValType { TEXT, PLUGIN, LIST };
	
	public InputDescriptor(String name, ValType vt, String val, String arg,
			   HashMap<String, String> form_attrs) {
		value = val;
		value_type = vt;
		param = arg;
		fieldName = name;
		formAttrs = form_attrs;
	}
	
	public InputDescriptor(String name, ValType vt, String val,
						   HashMap<String, String> form_attrs) {
		value = val;
		value_type = vt;
		param = null;
		fieldName = name;
		formAttrs = form_attrs;
	}
	
	public InputDescriptor(String name, String val) {
		value = val;
		value_type = ValType.TEXT;
		fieldName = name;
		formAttrs = new HashMap<String, String>();
	}
	
	Boolean inFilter(String form_attr, String str) {
		if(!formAttrs.containsKey(form_attr)) return true;
		
		return formAttrs.get(form_attr).equals(str);
	}
		
	public static String ANY_STRING = "*";
	
	public HashMap<String, String> formAttrs;
	
	private String fieldName;
	private ValType value_type;
	private String value;
	private String param;
	
	/**
	 * @return <false> if all form attrs are STRING_ANY
	 * @return <true> if any form attr is set
	 */
	public Boolean isFormSpecific() {
		return !formAttrs.isEmpty();
	}
	
	/**
	 * @return the value
	 */
	public String getValue() {
		switch(value_type) {
		//Fall through on fail
		case PLUGIN:
			if(InputFiller.plugins.containsKey(value)) {
				return InputFiller.plugins.get(value).getValue(param);
			} else return value + ": " +param; //DEBUG temp -- go ahead and pass parameter through
			
		case LIST:
			if(InputFiller.dataLists.containsKey(value)) {
				//Pick a random element
				int n = InputFiller.dataLists.get(value).size();
				
				Random r = new Random();
				Iterator<String> it = InputFiller.dataLists.get(value).iterator();
				
				int rnd = r.nextInt(n);
				for(int i=0; i<rnd; ++i) it.next();
				return it.next();
			}
			
			
		case TEXT:
		default:
			return value;
		}
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the fieldName
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * @param fieldName the fieldName to set
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	
	@Override
	public String toString() {
		String retval = "\t{"+fieldName+"= [";
		switch(value_type) {
			case PLUGIN:	retval+="PLUGIN";	break;
			case LIST:		retval+="LIST";		break;
			case TEXT:		retval+="TEXT";		break;
		}
		retval+=":"+value;
		if(value_type==ValType.PLUGIN)
			retval+="("+param+")";
		retval+="] | FORM[";
		
		Boolean first = true;
		for(String key : formAttrs.keySet()) {
			if(first) first = false;
			else retval+=",";
			
			retval += key+"='"+formAttrs.get(key)+"'";
		}
		retval+="]}\n";
		
		return retval;
	}
}
