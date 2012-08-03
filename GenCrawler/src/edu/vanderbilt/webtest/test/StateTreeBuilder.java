package edu.vanderbilt.webtest.test;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;
import org.apache.log4j.Logger;

import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.plugin.PreStateCrawlingPlugin;

import edu.vanderbilt.webtest.model.PageTree;

public class StateTreeBuilder implements PreStateCrawlingPlugin, PostCrawlingPlugin {
	public final Logger LOGGER = Logger.getLogger(StateTreeBuilder.class.getName());
	private static SimpleTree apt_tree;
	
	public StateTreeBuilder() {
		//TODO Init
		apt_tree = new SimpleTree();
	}
	
	public void preStateCrawling(CrawlSession session, List<CandidateElement> candidateElements) {
		
		PageTree pageTree = new PageTree();
		
		for(CandidateElement ce : candidateElements) {
			String dom = ce.getIdentification().getValue(); //XPath
			//For now, we'll generalize XPath by removing the [##]s
			dom = dom.replaceAll("\\[\\d+\\]", "");
			
			String tag = ce.getElement().getTagName();
			
			Vector<String> v = new Vector<String>();
			v.add(dom);
			
			if(tag.equalsIgnoreCase("a")) {
				//grab href
				String href = ce.getElement().getAttribute("href");
				pageTree.addLink(dom, href);
			} else if(tag.equalsIgnoreCase("input")) {
				//TODO get form method
				v.add("FORM");
				
				LOGGER.debug("Adding form pageTree: "+v.toString());
				pageTree.add(v, -1);
			} else {
				String action = "click";
				v.add(action);
				
				LOGGER.debug("Adding unknown pageTree: "+v.toString());
				pageTree.add(v, -1);
			}
		}
		
		Vector<String> pageVector = pageTree.toPageVector();
		
		for(String s : pageVector){
			LOGGER.debug("Page Vector :: "+s);
		}
		
		LOGGER.debug("Page Tree:\n"+pageTree.toFormattedString()+"\n");
		LOGGER.debug("Adding to APT: "+pageVector.toString());
		apt_tree.add(pageVector);
	}

	public void postCrawling(CrawlSession session) {
		LOGGER.info(apt_tree.toString());
	}
}

class SimpleTree {
	private SortedMap<String, SimpleTree> children;
	
	public SimpleTree() {
		children = new TreeMap<String, SimpleTree>();
	}
	
	public SimpleTree(Vector<String> v) {
		this();
		if(v.size()==0) return;
		
		//Recurse
		String val = v.remove(0);
		
		children.put(val, new SimpleTree(v));
	}
	
	public void add(Vector<String> v) {
		if(v.size()==0) return;
		
		String key = v.remove(0);
		if(children.containsKey(key))
			children.get(key).add(v);
		else 
			children.put(key, new SimpleTree(v));
	}
	
	public String getValAtLevel(int level) {
		if(level<0) return ""; //Safety
		
		StringBuilder sb = new StringBuilder("");
		//Base case -- we want this level
		if(level==0) {
			for(String k : children.keySet()) {
				if(k.length()!=0 && sb.length()!=0)
					sb.append(", ");
				sb.append(k);
			}
			return sb.toString();
		}
		
		//Recurse
		for(String k : children.keySet()) {
			String v = children.get(k).getValAtLevel(level-1);
			
			if(v.length()!=0 && sb.length()!=0)
				sb.append(", ");
			sb.append(v);
		}
		
		return sb.toString();
	}
	
	@Override
	public String toString() {
		String fs = toFormattedString(0);
		if(fs.equals("")) return "<NULL>";
		return fs;
	}
	
	public String toFormattedString(int depth) {
		if(children.size()==0) return "";//return pre+"<NULL>\n";
		
		String pre = new String("");
		for(int i=0; i<depth; ++i) pre += "  ";
		
		StringBuilder val_comp = new StringBuilder("");
		for(String s : children.keySet()) {
			val_comp.append(pre);
			val_comp.append(s);
			//Recurse
			String v = children.get(s).toFormattedString(depth+1);
			if(v.length()>0) {
				val_comp.append(":\n");
				val_comp.append(v);
			} else val_comp.append("\n");
		}
		
		return val_comp.toString();
		
	}
}