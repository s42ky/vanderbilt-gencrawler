package com.crawljax.core.state.identifier;

import com.crawljax.core.CandidateCrawlAction;
import com.crawljax.core.Crawler;
import com.crawljax.core.state.StateVertix;

/**
 * Class is mainly to add per-node storage
 * and to replace the identification means
 * 
 * Each StateVertix has one StateVertixIdentifier
 * 
 * All StateVertixes in a session will use the same
 * 		StateVertixIdentifier class
 */
public interface StateVertixIdentifier {
	/**
	 * Initialize and return a new SVI object
	 * @param  node this will be associated with (1:1 relation)
	 * 		   crawler - crawler that created the 
	 * @return Initialized object
	 */
	public abstract void init(StateVertix node, Crawler crawler);
	
	/**
	 * Comparison function
	 * @param  rhs  Object to compare
	 * @return True if objects are equal
	 */
	public abstract boolean equals(Object obj);
	
	/**
	 * Hash function
	 * @return Hash for the object
	 * 
	 * May be less specific than equals()
	 * Formally, a.equals(b) implies a.hashCode() == b.hashCode()
	 * but a.equals(b) does not imply a.hashCode() == b.hashCode()
	 */
	public abstract int hashCode();
	
	/**
	 * Equivalence function
	 * @return True if objects are equivalent
	 * 
	 * Provided to allow dynamic comparison
	 * 
	 * If unwanted, have use equals()
	 * 
	 * Formal properties:
	 * a.equals(b) implies a.equivalent(b)
	 * a.equivalent(b) does not imply a.equals(b)
	 * a.equivalent(b) implies b.equivalent(a)
	 * 
	 * No relation between hashCode() and equivalent()
	 * Two objects with different hashCodes may be equivalent
	 * Two equivalent objects may have different hashCodes
	 */
	
	public abstract boolean equivalent(Object o);
	
	/**
	 * @return identification string used by equals()
	 * 		(or that could be used by equals)
	 * a.equals(b)
	 * if and only if
	 * a.getIdentifierString().equals(b.getIdentifierString())
	 * and a and b are of the same class
	 */
	public abstract String getIdentifierString();
	
	
	/**
	 * @return a new uninitialized object of this class
	 */
	public abstract StateVertixIdentifier getNew();

	/**
	 * Screens action to determine whether to continue on it
	 * @param action
	 * @param requestingCrawler 
	 * @return true if need to find a different action
	 * NOTE: should always return false is action is null
	 */
	public abstract boolean screenAction(CandidateCrawlAction action, Crawler requestingCrawler);
}
