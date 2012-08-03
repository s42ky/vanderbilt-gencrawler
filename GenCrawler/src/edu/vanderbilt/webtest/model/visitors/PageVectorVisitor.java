package edu.vanderbilt.webtest.model.visitors;

import java.util.ArrayList;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.vanderbilt.webtest.model.PageTree;
import edu.vanderbilt.webtest.model.PageTree.PageTreeVisitor;

/**
 * Builds PageVector of tree
 * 
 * @author sky
 */
public class PageVectorVisitor implements PageTreeVisitor {
	public static final Logger LOGGER = Logger.getLogger(PageVectorVisitor.class.getName());
	
	protected ArrayList<StringBuilder> levelStringBuilders = new ArrayList<StringBuilder>();
	protected StringBuilder cur_prefix = new StringBuilder("");
	protected int cur_depth = 0;
	
	public void visit(PageTree pt) {
		if(levelStringBuilders.size()==cur_depth) {
			levelStringBuilders.add(new StringBuilder(cur_prefix));
		}
		
		boolean first = true;
		appendBelow(cur_depth, "[");
		for(String s : pt.getChildren().keySet()) {
			if(first) first=false;
			else levelStringBuilders.get(cur_depth).append(",");
			levelStringBuilders.get(cur_depth).append(s);
			
			//Recurse
			cur_depth++;
			pt.getChildren().get(s).accept(this);
			cur_depth--;
		}
		appendBelow(cur_depth, "]");
	}
	
	private void appendBelow(int depth, String appendString) {
		cur_prefix.append(appendString);
		for(int i=depth; i<levelStringBuilders.size(); ++i) {
			levelStringBuilders.get(i).append(appendString);
		}
	}
	
	public Vector<String> getResult() {
		Vector<String> v = new Vector<String>();
		
		for(StringBuilder sb : levelStringBuilders) {
			v.add(sb.toString());
			LOGGER.debug("PageVector :: "+sb.toString());
		}
		return v;
	}
}