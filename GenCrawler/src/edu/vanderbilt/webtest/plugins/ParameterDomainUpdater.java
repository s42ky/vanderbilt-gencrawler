package edu.vanderbilt.webtest.plugins;

import org.apache.log4j.Logger;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.plugin.OnAddStateVertixPlugin;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertix;

import edu.vanderbilt.webtest.model.ParameterEstimator;
import edu.vanderbilt.webtest.model.ParameterValueDomain;

public class ParameterDomainUpdater implements OnAddStateVertixPlugin, PostCrawlingPlugin {
	public final static Logger LOGGER = Logger.getLogger(ParameterDomainUpdater.class.toString());
	private static ParameterValueDomain pvd;
	
	@Deprecated
	public ParameterDomainUpdater() {
		pvd = new ParameterValueDomain();
	}
	
	public ParameterDomainUpdater(ParameterEstimator pe) {
		pvd = new ParameterValueDomain(pe);
	}
	
	public static ParameterValueDomain getPVD() {
		return pvd;
	}
	
	private static StateFlowGraph callingGraph = null;
	public static StateFlowGraph getGraph() { return callingGraph; }
	
	public void onAddVertix(StateVertix newState, StateFlowGraph graph) {
		callingGraph = graph;
		LOGGER.debug("calling ParameterDomainUpdater.onAddVertix for "+newState.getName());
		
		//Visitor will add each parameter it finds
		PageTreeVertixIdentifier vtx = (PageTreeVertixIdentifier) newState.getIdentifier();
		
		LOGGER.debug("Looking through "+vtx.getParameterIdentifiers().size()+" parameters.");
		
		for(String lookup : vtx.getParameterIdentifiers()) {
			LOGGER.debug("Adding parameters "+lookup+"::"+vtx.getValuesForParameter(lookup).toString());
			pvd.addParameterValues(lookup, vtx.getValuesForParameter(lookup), newState);
		}
		
		callingGraph = null;
	}

	public void postCrawling(CrawlSession session) {
		LOGGER.info("Final PVD:\n"+pvd.toString());
	}
}