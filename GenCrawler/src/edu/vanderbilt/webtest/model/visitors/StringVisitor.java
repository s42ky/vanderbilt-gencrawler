package edu.vanderbilt.webtest.model.visitors;

import edu.vanderbilt.webtest.model.PageTree;
import edu.vanderbilt.webtest.model.PageTree.PageTreeVisitor;

public class StringVisitor implements PageTreeVisitor {
	private int depth;
	private boolean useSpacing = true;
	private StringBuilder sb = new StringBuilder();
	
	public StringVisitor() {}
	public StringVisitor(boolean inline) {
		useSpacing = !inline;
	}
	
	public void visit(PageTree pt) {
		depth++;
		for(String s : pt.getChildren().keySet()) {
			//Indent to depth
			if(useSpacing) {
				for(int i=1; i<depth; ++i)
					sb.append("  ");
			}
			
			//if(pt.is_parameter) sb.append("[V] ");
			//else sb.append("[_] ");
			
			sb.append(s);
			PageTree next = pt.getChildren().get(s);
			
			if(next.getChildren().isEmpty()) {
				if(useSpacing) sb.append("\n");
			} else {
				sb.append(":");
				if(useSpacing) sb.append("\n");
				next.accept(this);
			}
		}
		depth--;
	}
	
	public String getResult() {
		return sb.toString();
	}
}