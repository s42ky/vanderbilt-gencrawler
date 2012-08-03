package edu.vanderbilt.webtest.model;

import org.apache.log4j.Logger;

import edu.vanderbilt.webtest.plugins.PageTreeCloneDetector;

public abstract class ParameterEstimator {
	public final static Logger LOGGER = Logger.getLogger(ParameterEstimator.class.toString());
	private ParameterValueDomain parameterValueDomain;

	/**
	 * @param parameterValueDomain
	 */

	public abstract void newParamValue(Parameter p);
	
	public void setPVD(ParameterValueDomain pvd) {
		parameterValueDomain = pvd;
	}
	
	public void changeToUnbounded(Parameter p) {
		LOGGER.info("Changing parameter "+p.getName()+" to unbounded.");
		
		p.setUnbounded();
		//The PageTreeCloneDetector will handle merging states
		PageTreeCloneDetector.setParameterAsUnbounded(parameterValueDomain.getParameters().inverse().get(p));
	}
}