package edu.vanderbilt.webtest.model;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import edu.vanderbilt.webtest.model.visitors.LevelVisitor;
import edu.vanderbilt.webtest.model.visitors.PageVectorVisitorIgnoreValues;
import edu.vanderbilt.webtest.model.visitors.StringVisitor;
import edu.vanderbilt.webtest.model.visitors.StringVisitorIgnoreValues;

public class PageTree {
	public static final Logger LOGGER = Logger.getLogger(PageTree.class.getName());
	private SortedMap<String, PageTree> children;
	private boolean is_parameter = false;
	
	/**
	 * Default constructor
	 */
	public PageTree() {
		children = new TreeMap<String, PageTree>();
	}
	
	/**
	 * Constructor that adds a Parameter/Value to the PageTree
	 * @param v vector to add to PageTree
	 */
	@Deprecated
	public PageTree(Vector<String> v) {
		this();
		addParamAndValue(v);
	}
	
	public PageTree(Vector<String> v, int paramFromEnd) {
		this();
		add(v, paramFromEnd);
	}
	
	/**
	 * Constructor that adds a Parameter to the PageTree
	 * 		Allows setting whether has a value or not
	 * @param v vector to add to PageTree
	 * @param hasValue True if there is a value for the parameter
	 * 		If only the parameter, set to false
	 * Note: Omitting <hasValue> is the same as setting <hasValue> to true
	 */
	public PageTree(Vector<String> v, boolean hasValue) {
		this();
		if(hasValue)
			addParamAndValue(v);
		else
			addParameter(v);
	}
	
	/**
	 * Visitor interface for the PageTree
	 * @author sky
	 */
	public interface PageTreeVisitor {
		public void visit(PageTree pt);
	}
	
	/**
	 * Acceptor for the visitor
	 * @param PageTreeVisitor that is visiting the PageTree
	 */
	public void accept(PageTreeVisitor ptv) {
		ptv.visit(this);
	}
	
	/**
	 * @deprecated
	 * Use <addParamAndValue> or <addParameter> instead
	 */
	@Deprecated
	public void add(Vector<String> v) {
		addParamAndValue(v);
	}
	
	public void addLink(String xpath, String uri) {
		Vector<String> linkVector = new Vector<String>();
		linkVector.add(xpath);
				
		if(uri==null) {
			add(linkVector, -1);
			return;
		}
		
		processParameters(linkVector, uri);
	}
	
	public void addForm(String xpath, WebElement form) {
		String method = form.getAttribute("method");
		String action = form.getAttribute("action");
		
		Vector<String> linkVector = new Vector<String>();
		linkVector.add(xpath);
		linkVector.add("FORM");
		linkVector.add(method);
		processParameters(linkVector, action);
		linkVector.remove(method);
		
		List<WebElement> paramElements = new LinkedList<WebElement>();
		paramElements.addAll(form.findElements(By.cssSelector("input[type='hidden']")));
		paramElements.addAll(form.findElements(By.cssSelector("input[type='button']")));
		paramElements.addAll(form.findElements(By.cssSelector("input[type='submit']")));
		
		for(WebElement e : paramElements) {
			String name = e.getAttribute("name");
			if(name==null) continue;
			
			linkVector.add(name);
			String val = e.getAttribute("value");
			if(val==null) {
				add(linkVector,0);
			} else {
				linkVector.add(val);
				add(linkVector,1);
				linkVector.remove(val);
			}
			linkVector.remove(name);
		}
	}
	
	private void processParameters(Vector<String> linkVector, String uri) {
		if(uri==null) return;
		
		String last = "";
		StringTokenizer st = new StringTokenizer(uri, "/");
		//TODO Little quick, may need to make more robust
		while(st.hasMoreElements()) {
			if(!last.equals("")) linkVector.add(last);
			last = st.nextToken();
		}
		
		String params = "";
		//page
		if(last.contains("?")) {
			params = last.substring(last.indexOf("?")+1);
			last = last.substring(0,last.indexOf("?"));
		}
		linkVector.add(last);
		
		if(!params.equals("")) {
			Pattern param_regex = Pattern.compile("([^&=?]+)=([^&=]+)");
			
			Matcher m = param_regex.matcher(params);
			
			//TODO add null params (eg ?key1&key2)
			
			while(m.find()) {
		        linkVector.add(m.group(1));
		        linkVector.add(m.group(2));
		        //LOGGER.debug("ADD vector with param: "+m.group(1)+"="+m.group(2));
		        add(linkVector, 1);
		        
		        //Undo for next parameter
		        linkVector.removeElement(m.group(2));
		        linkVector.removeElement(m.group(1));
			}
		} else {
			LOGGER.debug("Adding vector without params:"+linkVector.toString());
			add(linkVector, -1);
		}
	}
	
	/**
	 * @see add(Vector<String> v, int parameterFromEnd)
	 */
	@Deprecated
	public void addParamAndValue(Vector<String> v) {
		add(v, 1);
	}
	
	/**
	 * @see add(Vector<String> v, int parameterFromEnd)
	 */
	@Deprecated
	public void addParameter(Vector<String> v) {
		add(v, 0);
	}
	
	/**
	 * @see add(Vector<String> v, int parameterFromEnd)
	 */
	@Deprecated
	public void addNoParameter(Vector<String> v) {
		add(v, -1);
	}
	
	/**
	 * Adds vector to PageTree and marks one element as a parameter
	 * 		from the added vector
	 * @param v  - vector to add
	 * @param parameterFromEnd  - index from the end that is a parameter
	 * 		If set outside vector bounds, none will be used as a parameter
	 * 			Use -1 if this is intended
	 * 		Ex: if 1, marks 2nd to last item in <v> as parameter
	 * This is used for ignoring values, and all descendants are considered values
	 */
	public void add(Vector<String> v, int parameterFromEnd) {
		//LOGGER.debug("Adding ("+Integer.toString(parameterFromEnd)+"): "+v.toString());
		if(v.size() == parameterFromEnd) {
			is_parameter = true;
		}
		
		if(v.size()==0) return;
		
		String key = v.remove(0);
		
		if(children.containsKey(key))
			children.get(key).add(v, parameterFromEnd);
		else 
			children.put(key, new PageTree(v, parameterFromEnd));
	}
	
	/**
	 * @return string representation of the PageTree
	 * Is equivalent to toFormattedString without formatting whitespace
	 */
	@Override
	public String toString() {
		StringVisitor sv = new StringVisitorIgnoreValues(true);
		this.accept(sv);
		return sv.getResult();
	}
	
	/**
	 * @return print formatted string
	 * Each entry is on its own line and is indented to depth
	 */
	public String toFormattedString() {
		StringVisitor sv = new StringVisitor();
		this.accept(sv);
		return sv.getResult();
	}
	
	public boolean isParameter() {
		return is_parameter;
	}
	
	public SortedMap<String,PageTree> getChildren() {
		return children;
	}
	
	/**
	 * @return PageVector - Vector where v[k] is a string form of the kth level of the tree
	 * 
	 * Also, v[k] is equivalent to levelToString(k)
	 */
	public Vector<String> toPageVector() {
		PageVectorVisitorIgnoreValues pvv = new PageVectorVisitorIgnoreValues();
		this.accept(pvv);
		return pvv.getResult();
	}
	
	/**
	 * @param level -- which level to return. 0 is top level
	 * @return all items in a level as a joined string
	 */
	public String getLevelAsString(int level) {
		LevelVisitor lv = new LevelVisitor(level);
		this.accept(lv);
		return lv.getResult();
	}
}