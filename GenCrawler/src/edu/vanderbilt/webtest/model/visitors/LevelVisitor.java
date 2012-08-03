package edu.vanderbilt.webtest.model.visitors;

import org.apache.log4j.Logger;

import edu.vanderbilt.webtest.model.PageTree;
import edu.vanderbilt.webtest.model.PageTree.PageTreeVisitor;

public class LevelVisitor implements PageTreeVisitor {
	public static final Logger LOGGER = Logger.getLogger(LevelVisitor.class.getName());
	private int cur_depth;
	private int target_depth;
	private StringBuilder sb = new StringBuilder("");
	
	public LevelVisitor(int depth) {
		cur_depth = 0;
		target_depth = depth;
	}
	
	public void visit(PageTree pt) {
		//Safeguard
		if(cur_depth > target_depth) return;
		if(cur_depth==0) sb.append("[");
		
		boolean first = true;
		if(cur_depth==target_depth) {
			//We want this depth
			for(String s : pt.getChildren().keySet()) {
				if(first) first=false;
				else sb.append(",");
				sb.append(s);
			}
		} else {
			//Go deeper
			cur_depth++;
			for(String s : pt.getChildren().keySet()) {
				if(first) first=false;
				//else sb.append(" || ");
				sb.append("[");
				pt.getChildren().get(s).accept(this);
				sb.append("]");
			}
			cur_depth--;
		}
		if(cur_depth==0) sb.append("]");
	}
	
	public String getResult() {
		//Make sure we have content
		String res = sb.toString();
		if(res.matches(".*[^\\[\\]].*"))
			return res;
		else
			return "";
	}
}