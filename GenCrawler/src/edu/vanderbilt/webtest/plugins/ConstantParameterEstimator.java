package edu.vanderbilt.webtest.plugins;

import org.apache.log4j.Logger;

import edu.vanderbilt.webtest.model.Parameter;
import edu.vanderbilt.webtest.model.ParameterEstimator;

public class ConstantParameterEstimator extends ParameterEstimator {
	public final static Logger LOGGER = Logger.getLogger(ConstantParameterEstimator.class.toString());
	
	int threshold;
	
	public ConstantParameterEstimator(int limit) {
		threshold = limit;
	}

	@Override
	public void newParamValue(Parameter p) {
		if(p.getValueCount()>threshold) {
			LOGGER.info("Passed threshold. Changing "+p.getName()+" to unbounded");
			changeToUnbounded(p);
		}
	}

}
