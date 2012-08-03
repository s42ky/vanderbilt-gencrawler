package edu.vanderbilt.webtest.model.visitors;

import org.apache.log4j.Logger;

import edu.vanderbilt.webtest.model.PageTree;

public class PageVectorVisitorIgnoreValues extends PageVectorVisitor {
	public static final Logger LOGGER = Logger.getLogger(PageVectorVisitorIgnoreValues.class.getName());
	
	@Override
	public void visit(PageTree pt) {
		//Terminate if this is a parameter
		if(!pt.isParameter()) super.visit(pt);
		else LOGGER.debug("Ignoring: "+pt.getChildren().keySet().toString());
	}
}