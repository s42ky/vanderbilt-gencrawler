package edu.vanderbilt.webtest.model;

import java.util.Set;

import org.apache.log4j.Logger;

import com.crawljax.core.state.StateVertix;
import com.google.common.collect.HashBiMap;



public class ParameterValueDomain {
	public final static Logger LOGGER = Logger.getLogger(ParameterValueDomain.class.toString());
	/**
	 * Main structure
	 * Key:
	 * 		URI of page + '?' + parameter name
	 * 		Ex: ./admin/edit.php?user
	 * Value: Parameter object
	 */
	private HashBiMap<String, Parameter> parameters = HashBiMap.create();
	private ParameterEstimator paramEstimator = null;
	
	@Deprecated
	public ParameterValueDomain() {};
	
	public ParameterValueDomain(ParameterEstimator pe) {
		LOGGER.info("Creating new ParameterValueDomain.");
		paramEstimator = pe;
		pe.setPVD(this);
	}

	public void addParameter(String uri_base, String name) {
		String key = uri_base+"?"+name;
		if(!parameters.containsKey(key)) {
			LOGGER.debug("Adding NULL value for parameter "+key);
			parameters.put(key, new Parameter(name));
		}
		//TODO figure out null values
	}
	
	public HashBiMap<String, Parameter> getParameters() {
		return parameters;
	}
	
	@Deprecated
	public void addParameter(String uri_base, String name, String value, StateVertix referenceVertix) {
		String key = uri_base+"?"+name;
		addParameterValue(key,value, referenceVertix);
	}
	
	public void addParameterValue(String lookup, String value, StateVertix referenceVertix) {
		if(parameters.containsKey(lookup)) {
			LOGGER.debug("Adding parameter pair "+lookup+"="+value);
			parameters.get(lookup).addValue(value, referenceVertix);
		} else {
			LOGGER.debug("Adding parameter pair "+lookup+"="+value);
			parameters.put(lookup, new Parameter(lookup,value, referenceVertix));
		}
		
		//TODO make required
		if(paramEstimator!=null && parameters.get(lookup).isBounded())
			paramEstimator.newParamValue(parameters.get(lookup));
		else LOGGER.warn("NO PARAMETER ESTIMATOR");
	}
	
	public void addParameterValues(String lookup, Set<String> values, StateVertix referenceVertix) {
		if(!parameters.containsKey(lookup)) {
			parameters.put(lookup, new Parameter(lookup));
			LOGGER.debug("Creating new key for lookup.");
		}
		
		for(String val : values) {
			parameters.get(lookup).addValue(val, referenceVertix);
			LOGGER.debug("Adding parameter pair "+lookup+"="+val);
		}
		
		//TODO make required
		if(parameters.get(lookup).isBounded())
			paramEstimator.newParamValue(parameters.get(lookup));
	}
	
	public Parameter lookup(String uri_base) {
		return parameters.get(uri_base);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("\t");
		boolean first = true;
		for(String param : parameters.keySet()) {
			if(first) first = false;
			else sb.append("\n\t");
			sb.append(parameters.get(param).toString());
		}
		return sb.toString();
	}
}