package edu.vanderbilt.webtest.plugins;

import java.util.Iterator;

import org.apache.log4j.Logger;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.plugin.DetectCloneStatePlugin;
import com.crawljax.core.plugin.OnAddStateVertixPlugin;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertix;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

public class PageTreeCloneDetector implements DetectCloneStatePlugin, OnAddStateVertixPlugin, PostCrawlingPlugin {
	public static final Logger LOGGER = Logger.getLogger(PageTreeCloneDetector.class.toString());

	//Structure for looking up which node (StateVertix) a given page refers to
	private static Table<String, HashMultimap<String,String>, StateVertix> stateVertixLookup = HashBasedTable.create();
	
	/*
	 * (non-Javadoc)
	 * @see com.crawljax.core.plugin.DetectCloneStatePlugin#findCloneState(com.crawljax.core.state.StateVertix)
	 */
	public StateVertix findCloneState(StateVertix state) {
		PageTreeVertixIdentifier vertixID = (PageTreeVertixIdentifier) state.getIdentifier();
		String pageKey = vertixID.getParameterlessLookup();
		
		//Get local copy
		HashMultimap<String,String> paramKey = HashMultimap.create(vertixID.getValueMap());
		//Remove all bounded parameters
		for(String paramLookup : vertixID.getValueMap().keySet()) {
			if(ParameterDomainUpdater.getPVD().lookup(paramLookup)!=null
			  && !ParameterDomainUpdater.getPVD().lookup(paramLookup).isBounded()) {
				paramKey.removeAll(paramLookup);
			}
		}

		if(stateVertixLookup.contains(pageKey, paramKey))
			LOGGER.debug("Clone detected via stateVertixLookup");
		
		return stateVertixLookup.get(pageKey, paramKey);
	}
	
	public PageTreeCloneDetector() {
		reset();
	}
	
	public static void reset() {
		//Use between calls
		stateVertixLookup.clear();
	}
	
	private StateFlowGraph lastGraph = null;
	/*
	 * (non-Javadoc)
	 * @see com.crawljax.core.plugin.OnAddStateVertixPlugin#onAddVertix(com.crawljax.core.state.StateVertix)
	 */
	public void onAddVertix(StateVertix newState, StateFlowGraph callingGraph) {
		LOGGER.debug("calling PageTreeCloneDetector.onAddVertix for "+newState.getName());
		PageTreeVertixIdentifier vertixID = (PageTreeVertixIdentifier) newState.getIdentifier();
		
		//Get local copy
		HashMultimap<String,String> paramKey = HashMultimap.create(vertixID.getValueMap());
		//Remove all bounded parameters
		for(String paramLookup : vertixID.getValueMap().keySet()) {
			if(!ParameterDomainUpdater.getPVD().lookup(paramLookup).isBounded())
				paramKey.removeAll(paramLookup);
		}
				
		stateVertixLookup.put(vertixID.getParameterlessLookup(), paramKey, newState);
		LOGGER.debug("Adding state "+newState.getName()+" to stateVertixLookup. Now size "+stateVertixLookup.size());
		
		lastGraph = callingGraph;
	}

	/*
	 * (non-Javadoc)
	 * @see com.crawljax.core.plugin.PostCrawlingPlugin#postCrawling(com.crawljax.core.CrawlSession)
	 */
	public void postCrawling(CrawlSession session) {
		// TODO Auto-generated method stub
		if(LOGGER.isDebugEnabled()) {
			
			LOGGER.debug("FIN -- stateVertixLookup\n"+toString());
			LOGGER.debug(stateVertixLookup.toString());
			
			StringBuilder sb = new StringBuilder("");
			
			for(StateVertix vtx : lastGraph.getAllStates()) {
				sb.append("\n");
				sb.append(vtx.getName());
				sb.append(" :: Edges in ");
				sb.append(lastGraph.getIncomingClickable(vtx).size());
				sb.append(" / out ");
				sb.append(lastGraph.getOutgoingClickables(vtx).size());
				sb.append("\n\t");
				sb.append(vtx.getIdentifier().getIdentifierString());
				
				PageTreeVertixIdentifier ptvi = (PageTreeVertixIdentifier) vtx.getIdentifier();
				sb.append("\n\t");
				sb.append(ptvi.getParameterlessLookup());
			}
			
			LOGGER.debug("FIN -- states"+sb.toString());
		}
	}
	
	@Override
	public String toString() {
		return genString();
	}
	
	private static String genString() {
		StringBuilder sb = new StringBuilder("");
		for(String k1 : stateVertixLookup.rowKeySet()) {
			sb.append(k1);
			sb.append(":\n");
			for(HashMultimap<String,String> k2 : stateVertixLookup.row(k1).keySet()) {
				sb.append("\t");
				sb.append(k2.toString());
				sb.append("\n\t\t");
				sb.append(stateVertixLookup.get(k1, k2));
				sb.append("\n");
			}
		}
		
		if(ParameterDomainUpdater.getGraph()!=null) {
			sb.append("States");
			for(StateVertix vtx : ParameterDomainUpdater.getGraph().getAllStates()) {
				sb.append("\n");
				sb.append(vtx.getName());
				sb.append(" :: Edges in ");
				sb.append(ParameterDomainUpdater.getGraph().getIncomingClickable(vtx).size());
				sb.append(" / out ");
				sb.append(ParameterDomainUpdater.getGraph().getOutgoingClickables(vtx).size());
				sb.append("\n\t");
				sb.append(vtx.getIdentifier().getIdentifierString());
				
				PageTreeVertixIdentifier ptvi = (PageTreeVertixIdentifier) vtx.getIdentifier();
				sb.append("\n\t");
				sb.append(ptvi.getParameterlessLookup());
			}
		} else LOGGER.warn("lastGraph is null");
		
		return sb.toString();
	}
	
	public static void setParameterAsUnbounded(String paramLookup) {
		LOGGER.debug("To UNBOUNDED (pre) -- stateVertixLookup\n"+genString());
		Table<String,HashMultimap<String,String>,StateVertix> tmpVertixLookup = HashBasedTable.create();
		int removed = 0;
		
		for(Iterator<Cell<String, HashMultimap<String, String>, StateVertix>> it = stateVertixLookup.cellSet().iterator(); it.hasNext(); ) {
			Cell<String, HashMultimap<String, String>, StateVertix> c = it.next();
			if(c.getColumnKey().containsKey(paramLookup)) {
				LOGGER.debug("Found parameter. Removing from paramKey...");
				HashMultimap<String,String> newParamMap = HashMultimap.create(c.getColumnKey());
				newParamMap.removeAll(paramLookup);
				
				String pageKey = c.getRowKey();
				it.remove();
				removed++;
				
				boolean merged = false;
				if(stateVertixLookup.contains(pageKey, newParamMap)) {
					merged = true;
					if(!c.getValue().getName().equals(stateVertixLookup.get(pageKey, newParamMap).getName())) {
						StateVertix duplicateState = stateVertixLookup.get(pageKey, newParamMap);
						LOGGER.info("TVL MERGE STATES: "+c.getValue().getName()+" and "+duplicateState.getName());
						
						ParameterDomainUpdater.getGraph().mergeStates(duplicateState, c.getValue());
						if(c.getValue().getName().equals("index")) {
							tmpVertixLookup.put(pageKey, newParamMap, c.getValue());
						}
					} else LOGGER.debug("Duplicate -- easy merge on "+stateVertixLookup.get(pageKey, newParamMap).getName()+"/"+c.getValue().getName());
				}
				
				if(tmpVertixLookup.contains(pageKey, newParamMap)) {
					merged = true;
					if(!c.getValue().getName().equals(tmpVertixLookup.get(pageKey, newParamMap).getName())) {
						StateVertix duplicateState = tmpVertixLookup.get(pageKey, newParamMap);
						LOGGER.info("TVL MERGE STATES: "+c.getValue().getName()+" and "+duplicateState.getName());
						
						ParameterDomainUpdater.getGraph().mergeStates(duplicateState, c.getValue());
						if(c.getValue().getName().equals("index")) {
							tmpVertixLookup.put(pageKey, newParamMap, c.getValue());
						}
					} else LOGGER.debug("Duplicate -- easy merge on "+tmpVertixLookup.get(pageKey, newParamMap).getName()+"/"+c.getValue().getName());
				}
				
				if(!merged){
					LOGGER.debug("Removing "+paramLookup+" from "+c.getValue().getName());
					LOGGER.debug("Keeping mapping to state "+c.getValue());
					tmpVertixLookup.put(pageKey, newParamMap, c.getValue());
				}
			}
		}
		LOGGER.info("stateVertixLookup updated. "+removed+" values removed "+tmpVertixLookup.size()+" values added back");
		stateVertixLookup.putAll(tmpVertixLookup);
		LOGGER.debug("To UNBOUNDED (post) -- stateVertixLookup\n"+genString());
	}
}