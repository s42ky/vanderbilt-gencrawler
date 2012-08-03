package com.crawljax.core.plugin;

import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertix;

public interface OnAddStateVertixPlugin extends Plugin {
	/**
	 * Notify event
	 * @param newState
	 * @param callingGraph 
	 */
	abstract public void onAddVertix(StateVertix newState, StateFlowGraph callingGraph);
}
