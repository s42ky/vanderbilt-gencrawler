package com.crawljax.core.state;

import net.jcip.annotations.GuardedBy;

import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.DirectedMultigraph;

import com.crawljax.core.CrawljaxException;
import com.crawljax.core.plugin.CrawljaxPluginsUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The State-Flow Graph is a directed graph with states on the vertices and clickables on the edges.
 * 
 * @author mesbah
 * @version $Id: StateFlowGraph.java 446 2010-09-16 09:17:24Z slenselink@google.com $
 */
public class StateFlowGraph {
	private static final Logger LOGGER = Logger.getLogger(StateFlowGraph.class.getName());

	private final DirectedGraph<StateVertix, Eventable> sfg;
	
	private static HashMap<String, StateVertix> stateForwardMap = new HashMap<String,StateVertix>();
	

	/**
	 * Intermediate counter for the number of states, not relaying on getAllStates.size() because of
	 * Thread-safety.
	 */
	private final AtomicInteger stateCounter = new AtomicInteger(1);

	/**
	 * Empty constructor.
	 */
	public StateFlowGraph() {
		sfg = new DirectedMultigraph<StateVertix, Eventable>(Eventable.class);
	}

	/**
	 * The constructor.
	 * 
	 * @param initialState
	 *            the state to start from.
	 */
	public StateFlowGraph(StateVertix initialState) {
		this();
		sfg.addVertex(initialState);
		CrawljaxPluginsUtil.runOnAddStateVertixPlugins(initialState, this);
	}

	/**
	 * Adds a state (as a vertix) to the State-Flow Graph if not already present. More formally,
	 * adds the specified vertex, v, to this graph if this graph contains no vertex u such that
	 * u.equals(v). If this graph already contains such vertex, the call leaves this graph unchanged
	 * and returns false. In combination with the restriction on constructors, this ensures that
	 * graphs never contain duplicate vertices. Throws java.lang.NullPointerException - if the
	 * specified vertex is null. This method automatically updates the state name to reflect the
	 * internal state counter.
	 * 
	 * @param stateVertix
	 *            the state to be added.
	 * @return the clone if one is detected null otherwise.
	 * @see org.jgrapht.Graph#addVertex(Object)
	 */
	public StateVertix addState(StateVertix stateVertix) {
		return addState(stateVertix, true);
	}

	/**
	 * Adds a state (as a vertix) to the State-Flow Graph if not already present. More formally,
	 * adds the specified vertex, v, to this graph if this graph contains no vertex u such that
	 * u.equals(v). If this graph already contains such vertex, the call leaves this graph unchanged
	 * and returns false. In combination with the restriction on constructors, this ensures that
	 * graphs never contain duplicate vertices. Throws java.lang.NullPointerException - if the
	 * specified vertex is null.
	 * 
	 * @param stateVertix
	 *            the state to be added.
	 * @param correctName
	 *            if true the name of the state will be corrected according to the internal state
	 *            counter.
	 * @return the clone if one is detected null otherwise.
	 * @see org.jgrapht.Graph#addVertex(Object)
	 */
	@GuardedBy("sfg")
	public StateVertix addState(StateVertix stateVertix, boolean correctName) {
		synchronized (sfg) {
			StateVertix pluginsClone = CrawljaxPluginsUtil.runDetectCloneStatePlugins(stateVertix);
			if (pluginsClone != null) {
				return pluginsClone;
			} else if (!sfg.addVertex(stateVertix)) {
				// Graph already contained the vertix
				return this.getStateInGraph(stateVertix);
			} else {
				/**
				 * A new State has been added so check to see if the name is correct, remember this
				 * is the only place states can be added and we are now locked so getAllStates.size
				 * works correctly.
				 */
				if (correctName) {
					// the -1 is for the "index" state.
					int totalNumberOfStates = this.getAllStates().size() - 1;
					String correctedName =
					        makeStateName(totalNumberOfStates, stateVertix.isGuidedCrawling());
					if (!stateVertix.getName().equals("index")
					        && !stateVertix.getName().equals(correctedName)) {
						LOGGER.info("Correcting state name from  " + stateVertix.getName()
						        + " to " + correctedName);
						stateVertix.setName(correctedName);
					}
				}
				
				CrawljaxPluginsUtil.runOnAddStateVertixPlugins(stateVertix, this);
			}
			stateCounter.set(this.getAllStates().size() - 1);
		}
		return null;
	}

	/**
	 * Adds the specified edge to this graph, going from the source vertex to the target vertex.
	 * More formally, adds the specified edge, e, to this graph if this graph contains no edge e2
	 * such that e2.equals(e). If this graph already contains such an edge, the call leaves this
	 * graph unchanged and returns false. Some graphs do not allow edge-multiplicity. In such cases,
	 * if the graph already contains an edge from the specified source to the specified target, than
	 * this method does not change the graph and returns false. If the edge was added to the graph,
	 * returns true. The source and target vertices must already be contained in this graph. If they
	 * are not found in graph IllegalArgumentException is thrown.
	 * 
	 * @param sourceVert
	 *            source vertex of the edge.
	 * @param targetVert
	 *            target vertex of the edge.
	 * @param clickable
	 *            the clickable edge to be added to this graph.
	 * @return true if this graph did not already contain the specified edge.
	 * @see org.jgrapht.Graph#addEdge(Object, Object, Object)
	 */
	@GuardedBy("sfg")
	public boolean addEdge(StateVertix sourceVert, StateVertix targetVert, Eventable clickable) {
		synchronized (sfg) {
			// TODO Ali; Why is this code (if-stmt) here? Its the same as what happens in sfg.addEge
			// imo (21-01-10 Stefan).
			if (sfg.containsEdge(sourceVert, targetVert)
			        && sfg.getAllEdges(sourceVert, targetVert).contains(clickable)) {
				return false;
			}

			return sfg.addEdge(sourceVert, targetVert, clickable);
		}
	}

	/**
	 * @return the string representation of the graph.
	 * @see org.jgrapht.DirectedGraph#toString()
	 */
	@Override
	public String toString() {
		return sfg.toString();
	}

	/**
	 * Returns a set of all clickables outgoing from the specified vertex.
	 * 
	 * @param stateVertix
	 *            the state vertix.
	 * @return a set of the outgoing edges (clickables) of the stateVertix.
	 * @see org.jgrapht.DirectedGraph#outgoingEdgesOf(Object)
	 */
	public Set<Eventable> getOutgoingClickables(StateVertix stateVertix) {
		return sfg.outgoingEdgesOf(stateVertix);
	}

	/**
	 * Returns a set of all edges incoming into the specified vertex.
	 * 
	 * @param stateVertix
	 *            the state vertix.
	 * @return a set of the incoming edges (clickables) of the stateVertix.
	 * @see org.jgrapht.DirectedGraph#incomingEdgesOf(Object)
	 */
	public Set<Eventable> getIncomingClickable(StateVertix stateVertix) {
		return sfg.incomingEdgesOf(stateVertix);
	}

	/**
	 * Returns the set of outgoing states.
	 * 
	 * @param stateVertix
	 *            the state.
	 * @return the set of outgoing states from the stateVertix.
	 */
	public Set<StateVertix> getOutgoingStates(StateVertix stateVertix) {
		final Set<StateVertix> result = new HashSet<StateVertix>();

		for (Eventable c : getOutgoingClickables(stateVertix)) {
			result.add(sfg.getEdgeTarget(c));
		}

		return result;
	}

	/**
	 * @param clickable
	 *            the edge.
	 * @return the target state of this edge.
	 */
	public StateVertix getTargetState(Eventable clickable) {
		return sfg.getEdgeTarget(clickable);
	}

	/**
	 * Is it possible to go from s1 -> s2?
	 * 
	 * @param source
	 *            the source state.
	 * @param target
	 *            the target state.
	 * @return true if it is possible (edge exists in graph) to go from source to target.
	 */
	@GuardedBy("sfg")
	public boolean canGoTo(StateVertix source, StateVertix target) {
		synchronized (sfg) {
			return sfg.containsEdge(source, target) || sfg.containsEdge(target, source);
		}
	}

	/**
	 * Convenience method to find the Dijkstra shortest path between two states on the graph.
	 * 
	 * @param start
	 *            the start state.
	 * @param end
	 *            the end state.
	 * @return a list of shortest path of clickables from the state to the end
	 */
	public List<Eventable> getShortestPath(StateVertix start, StateVertix end) {
		return DijkstraShortestPath.findPathBetween(sfg, start, end);
	}

	/**
	 * Return all the states in the StateFlowGraph.
	 * 
	 * @return all the states on the graph.
	 */
	public Set<StateVertix> getAllStates() {
		return sfg.vertexSet();
	}

	/**
	 * Return all the edges in the StateFlowGraph.
	 * 
	 * @return a Set of all edges in the StateFlowGraph
	 */
	public Set<Eventable> getAllEdges() {
		return sfg.edgeSet();
	}

	/**
	 * Retrieve the copy of a state from the StateFlowGraph for a given StateVertix. Basically it
	 * performs v.equals(u).
	 * 
	 * @param state
	 *            the StateVertix to search
	 * @return the copy of the StateVertix in the StateFlowGraph where v.equals(u)
	 */
	private StateVertix getStateInGraph(StateVertix state) {
		Set<StateVertix> states = getAllStates();

		for (StateVertix st : states) {
			if (state.equals(st)) {
				return st;
			}
		}

		return null;
	}

	/**
	 * @return Dom string average size (byte).
	 */
	public int getMeanStateStringSize() {
		final Mean mean = new Mean();

		for (StateVertix state : getAllStates()) {
			mean.increment(state.getDomSize());
		}

		return (int) mean.getResult();
	}

	/**
	 * @return the state-flow graph.
	 */
	public DirectedGraph<StateVertix, Eventable> getSfg() {
		return sfg;
	}

	/**
	 * @param state
	 *            The starting state.
	 * @return A list of the deepest states (states with no outgoing edges).
	 */
	public List<StateVertix> getDeepStates(StateVertix state) {
		final Set<String> visitedStates = new HashSet<String>();
		final List<StateVertix> deepStates = new ArrayList<StateVertix>();

		traverse(visitedStates, deepStates, state);

		return deepStates;
	}

	private void traverse(Set<String> visitedStates, List<StateVertix> deepStates,
	        StateVertix state) {
		visitedStates.add(state.getName());

		Set<StateVertix> outgoingSet = getOutgoingStates(state);

		if ((outgoingSet == null) || outgoingSet.isEmpty()) {
			deepStates.add(state);
		} else {
			if (cyclic(visitedStates, outgoingSet)) {
				deepStates.add(state);
			} else {
				for (StateVertix st : outgoingSet) {
					if (!visitedStates.contains(st.getName())) {
						traverse(visitedStates, deepStates, st);
					}
				}
			}
		}
	}

	private boolean cyclic(Set<String> visitedStates, Set<StateVertix> outgoingSet) {
		int i = 0;

		for (StateVertix state : outgoingSet) {
			if (visitedStates.contains(state.getName())) {
				i++;
			}
		}

		return i == outgoingSet.size();
	}

	/**
	 * This method returns all possible paths from the index state using the Kshortest paths.
	 * 
	 * @param index
	 *            the initial state.
	 * @return a list of GraphPath lists.
	 */
	public List<List<GraphPath<StateVertix, Eventable>>> getAllPossiblePaths(StateVertix index) {
		final List<List<GraphPath<StateVertix, Eventable>>> results =
		        new ArrayList<List<GraphPath<StateVertix, Eventable>>>();

		final KShortestPaths<StateVertix, Eventable> kPaths =
		        new KShortestPaths<StateVertix, Eventable>(this.sfg, index, Integer.MAX_VALUE);
		// System.out.println(sfg.toString());

		for (StateVertix state : getDeepStates(index)) {
			// System.out.println("Deep State: " + state.getName());

			try {
				List<GraphPath<StateVertix, Eventable>> paths = kPaths.getPaths(state);
				results.add(paths);
			} catch (Exception e) {
				// TODO Stefan; which Exception is catched here???Can this be removed?
				LOGGER.error("Error with " + state.toString(), e);
			}

		}

		return results;
	}

	/**
	 * Return the name of the (new)State. By using the AtomicInteger the stateCounter is thread-safe
	 * 
	 * @return State name the name of the state
	 */
	public String getNewStateName() {
		//TODO truly thread-safe with separation of increment and get commands?
		stateCounter.getAndIncrement();
		String state = makeStateName(stateCounter.get(), false);
		return state;
	}

	/**
	 * Make a new state name given its id. Separated to get a central point when changing the names
	 * of states. The automatic state names start with "state" and guided ones with "guide".
	 * 
	 * @param id
	 *            the id where this name needs to be for.
	 * @return the String containing the new name.
	 */
	private String makeStateName(int id, boolean guided) {

		if (guided) {
			return "guided" + id;
		}

		return "state" + id;
	}
	
	/**
	 * Merge two states.
	 * @TODO TODO figure out how we're going to do naming.
	 * 	Temp fix: leave isolated, empty node in the graph
	 * 
	 * @param stateKeep -- state to keep in the graph
	 * @param stateDiscard -- state to discard from graph after merging
	 * 
	 * @return state kept. This is the same as the first parameter EXCEPT when
	 * the discarded state is the index state. As the index node cannot be removed,
	 * (or even changed: the reference in StateMachine is final), it will be swapped.
	 * This will generate a warning.
	 * 
	 * @note Shouldn't matter which keep/discard, since they should be equivalent
	 *  states. Right now will just generate warning if non equivalent, although may
	 * 	either change to no/op or make throw an exception.
	 * So don't go merging non-equivalent states
	 */
	@GuardedBy("sfg")
	public StateVertix mergeStates(StateVertix stateKeep, StateVertix stateDiscard) {
		if(stateKeep==stateDiscard) { //If same address, we're done
			LOGGER.warn("mergeStates called on single state object.");
			return stateKeep;
		}
		
		LOGGER.setLevel(Level.DEBUG);
		StateVertix vKeep;
		StateVertix vDiscard;
		if(stateDiscard.getName().equals("index")) {
			LOGGER.warn("Swapping states to keep index");
			vKeep = stateDiscard;
			vDiscard = stateKeep;
		} else {
			vKeep = stateKeep;
			vDiscard = stateDiscard;
		}
		
		if(!vKeep.equals(vDiscard)) {
			String msg = "Merging two NON-EQUIVALENT states: "
					+vDiscard.getName()+" into "+vKeep.getName(); 
			LOGGER.warn(msg);
			System.err.println(msg); //Yeah, it's that important
		}
		synchronized(sfg) {
			//Move all incoming edges
			Object[] edges = sfg.incomingEdgesOf(vDiscard).toArray();
			for(Object o : edges) {
				if(!(o instanceof Eventable)) {
					LOGGER.error("Object isn't an eventable but a "+o.getClass().toString());
					continue;
				}
				Eventable e = (Eventable) o;
				copyOver(e, vKeep, true);
				try {
					LOGGER.debug("Removing edge from "+e.getSourceStateVertix()+" to "+e.getTargetStateVertix());
				} catch (CrawljaxException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				sfg.removeEdge(e);
			}
			
			//Move all outgoing edges
			edges = sfg.outgoingEdgesOf(vDiscard).toArray();
			for(Object o : edges) {
				if(!(o instanceof Eventable)) {
					LOGGER.error("Object isn't an eventable but a "+o.getClass().toString());
					continue;
				}
				Eventable e = (Eventable) o;
				
				copyOver(e, vKeep, false);
				try {
					LOGGER.debug("Removing edge from "+e.getSourceStateVertix()+" to "+e.getTargetStateVertix());
				} catch (CrawljaxException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				sfg.removeEdge(e);
			}
		}
		//Note the forwarding
		stateForwardMap.put(vDiscard.getName(), vKeep);
		
		//We need to update map
		for(Entry<String, StateVertix> pair : stateForwardMap.entrySet()) {
			if(pair.getValue().getName().equals(vDiscard.getName()))
				pair.setValue(vKeep);
		}
		
		//Set to a pointless vector for now (so no equals collisions)
		//TODO remove node and update numbering. (inside synchronized)
		vDiscard.setDom("");
		vDiscard.setGuidedCrawling(false);
		vDiscard.setName(vDiscard.getName().replace("state", "delete"));
		
		//Note the (post-rename) forwarding
		stateForwardMap.put(vDiscard.getName(), vKeep);
		
		return vKeep;
	}
	
	private void copyOver(Eventable e, StateVertix stateKeep, boolean isTo) {
		try {
			Eventable e2;
			if(isTo) {
				e2 = sfg.addEdge(e.getSourceStateVertix(), stateKeep);
			} else {
				e2 = sfg.addEdge(stateKeep, e.getTargetStateVertix());
			}
			
			LOGGER.debug("Added edge from "+e2.getSourceStateVertix()+" to "+e2.getTargetStateVertix());
			
			e2.setElement(e.getElement());
			e2.setEventType(e.getEventType());
			e2.setId(e.getId());
			e2.setIdentification(e.getIdentification());
			e2.setRelatedFormInputs(e.getRelatedFormInputs());
		} catch (CrawljaxException err) {
			// TODO Auto-generated catch block
			err.printStackTrace();
		}
		
	}
	
	public static StateVertix forward(StateVertix in) {
		StateVertix out = stateForwardMap.get(in.getName());
		if(out==null) return in;
		else return out;
		
	}
}
