package com.crawljax.core.plugin;

import com.crawljax.core.state.StateVertix;

public interface DetectCloneStatePlugin extends Plugin {
	
	/**
	 * Find an existing equivalent state. Provided for enabling
	 * 		faster lookups than the default JGraphT lookup
	 * @param state
	 * @return NULL if state does not exist
	 * 		   the StateVertix object if it does exist
	 */
	abstract StateVertix findCloneState(StateVertix state);
}
