package edu.vanderbilt.webtest.model;

import java.util.HashSet;
import java.util.Set;

import com.crawljax.core.state.StateVertix;
import com.google.common.collect.HashMultimap;

public class Parameter {
	public static enum DomainType { Bounded, Unbounded };
	
	private String name;
	private HashMultimap<String, StateVertix> valueMap;
	private DomainType type;
	
	private HashSet<String> values;
	private int callCount = 0;
	
	//Lookup for state (bounded)
	//private HashMap<String,Set<String>> boundedStateLookup = null;
	//private Set<String> unboundedStateLookup = null;
	
	
	/**
	 * Create a new parameter
	 * @param parameterName -- Name of the parameter
	 */
	Parameter(String parameterName) {
		name = parameterName;
		valueMap = HashMultimap.create();
		type = DomainType.Bounded;
		values = new HashSet<String>();
	}
	
	/**
	 * Alternate constructor that adds a value as well
	 */
	Parameter(String parameterName, String parameterValue, StateVertix referenceVertix) {
		this(parameterName);
		addValue(parameterValue, referenceVertix);
	}
	
	/**
	 * Adds a value to the values set of the Parameter
	 * @param newValue -- value to add
	 * @param referenceVertix TODO remove this
	 * @return true if value added, false if value existed
	 */
	public boolean addValue(String newValue, StateVertix referenceVertix) {
		/*boolean retval = */values.add(newValue);
		
		callCount++;
		
		if(type.equals(DomainType.Unbounded))
			return true;
				
		//TODO use retval and remove valueMap
		return valueMap.put(newValue, referenceVertix);
	}
		
	/**
	 * @post Changes <type> to <DomainType.Unbounded>
	 * This is an IRREVERSIBLE process 
	 */
	public void setUnbounded() {
		type = DomainType.Unbounded;
		//values.clear(); //Free memory
	}
	
	public boolean hasValue(String testValue) {
		//Unbounded always returns true
		if(type.equals(DomainType.Unbounded))
			return true;
		
		return values.contains(testValue);
		//return valueMap.containsKey(testValue);
	}
	
	public boolean hasValues(Set<String> testValues) {
		if(type.equals(DomainType.Unbounded))
			return true;
		
		return values.containsAll(testValues);
		//Set operation
		//return valueMap.keySet().containsAll(testValues);
	}
	
	public int getValueCount() {
		return values.size();
	}
	
	public int getCallCount() {
		return callCount;
	}
	
	@Deprecated
	public Set<StateVertix> getSetsForValue(String testValue) {
		if(valueMap.containsKey(testValue))
			return valueMap.get(testValue);
		else //Return empty hash set
			return new HashSet<StateVertix>();
	}
	
	public boolean isBounded() {
		return type.equals(DomainType.Bounded);
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("");
		sb.append(name);
		sb.append(" : ");
		if(type.equals(DomainType.Unbounded)) {
			sb.append("[U]");
		} else {
			sb.append("[B]");
		}
		sb.append("|v");
		sb.append(values.size());
		sb.append("/c");
		sb.append(callCount);
		sb.append("|");
		
		if(type.equals(DomainType.Bounded)) {
			sb.append(values.toString());
		}
		return sb.toString();
	}
}