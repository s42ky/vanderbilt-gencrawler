package com.crawljax.core.state;

import com.crawljax.core.CandidateCrawlAction;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CandidateElementExtractor;
import com.crawljax.core.CrawlQueueManager;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.Crawler;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.TagElement;
import com.crawljax.core.plugin.CrawljaxPluginsUtil;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.core.state.identifier.StateVertixIdentifier;
import com.crawljax.core.state.identifier.StrippedDomVertixIdentifier;
import com.crawljax.util.Helper;

import net.jcip.annotations.GuardedBy;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The state vertix class which represents a state in the browser. This class implements the
 * Iterable interface because on a StateVertix it is possible to iterate over the possible
 * CandidateElements found in this state. When iterating over the possible candidate elements every
 * time a candidate is returned its removed from the list so it is a one time only access to the
 * candidates.
 * 
 * @author mesbah
 * @version $Id: StateVertix.java 435 2010-09-13 10:31:18Z slenselink@google.com $
 */
public class StateVertix implements Serializable {
	//DEBUGGING
	private static AtomicInteger ctr;
	private int unique_id;
	
	private static final long serialVersionUID = 123400017983488L;
	private static final Logger LOGGER = Logger.getLogger(StateVertix.class);
	private long id;
	private String name;
	private String dom;
	private final String url;
	private boolean guidedCrawling = false;
	
	//Additional per-Vertix storage (single object)
	private StateVertixIdentifier extension;
	
	private boolean haveRunPreStateCrawlingPlugins = false;

	/**
	 * This list is used to store the possible candidates. If it is null its not initialised if it's
	 * a empty list its empty.
	 */
	private LinkedBlockingDeque<CandidateCrawlAction> candidateActions;

	private final ConcurrentHashMap<Crawler, CandidateCrawlAction> registerdCandidateActions =
	        new ConcurrentHashMap<Crawler, CandidateCrawlAction>();
	private final ConcurrentHashMap<Crawler, CandidateCrawlAction> workInProgressCandidateActions =
	        new ConcurrentHashMap<Crawler, CandidateCrawlAction>();

	private final Object candidateActionsSearchLock = new Object();

	private final LinkedBlockingDeque<Crawler> registeredCrawlers =
	        new LinkedBlockingDeque<Crawler>();

	/**
	 * Default constructor to support saving instances of this class as an XML.
	 */
	public StateVertix() {
		this.url = "";
	}

	/**
	 * Creates a current state without an url and the stripped dom equals the dom.
	 * 
	 * @param name
	 *            the name of the state
	 * @param dom
	 *            the current DOM tree of the browser
	 */
	@Deprecated
	public StateVertix(String name, String dom) {
		this.url = null;
		this.name = name;
		this.dom = dom;
		//Have to force to DOM default if use this constructor
		//(why set this as Deprecated
		this.extension = new StrippedDomVertixIdentifier();
		((StrippedDomVertixIdentifier) this.extension).init(this, dom);
		
		if(ctr==null) ctr = new AtomicInteger(10000);
		unique_id = ctr.getAndIncrement();
	}

	/**
	 * Defines a State.
	 * 
	 * @param url
	 *            the current url of the state
	 * @param name
	 *            the name of the state
	 * @param controller
	 *            the current Crawljax controller
	 */
	public StateVertix(String url, String name, Crawler crawler) {
		this.url = url;
		this.name = name;
		this.dom = crawler.getBrowser().getDom();
		
		this.extension = crawler.getController().getConfigurationReader()
				.getCrawlSpecificationReader().getStateVertixIdentifier().getNew();
		this.extension.init(this, crawler);
		
		if(ctr==null) ctr = new AtomicInteger(10000);
		unique_id = ctr.getAndIncrement();
	}

	/**
	 * Retrieve the name of the StateVertix.
	 * 
	 * @return the name of the stateVertix
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retrieve the DOM String.
	 * 
	 * @return the dom for this state
	 */
	public String getDom() {
		return dom;
	}

	/**
	 * @return the stripped dom by the oracle comparators
	 */
	@Deprecated
	public String getStrippedDom() {
		//return strippedDom;
		return dom;
	}

	public StateVertixIdentifier getIdentifier() {
		return extension;
	}
	
	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Returns a hashcode. Uses reflection to determine the fields to test.
	 * 
	 * @return the hashCode of this StateVertix
	 */
	@Override
	public int hashCode() {
		return extension.hashCode();
	}

	/**
	 * Compare this vertix to a other StateVertix.
	 * 
	 * @param obj
	 *            the Object to compare this vertix
	 * @return Return true if equal. Uses reflection.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof StateVertix)) {
			return false;
		}

		if (this == obj) {
			return true;
		}
		final StateVertix rhs = (StateVertix) obj;

		LOGGER.debug("Going to EqualsBuilder()");
		
		return new EqualsBuilder().append(this.extension, rhs.extension).append(
		        this.guidedCrawling, rhs.guidedCrawling).isEquals();
	}

	/**
	 * Returns the name of this state as string.
	 * 
	 * @return a string representation of the current StateVertix
	 */
	@Override
	public String toString() {
		return name;
	}

	/**
	 * Return the size of the DOM in bytes.
	 * 
	 * @return the size of the dom
	 */
	public int getDomSize() {
		return getDom().getBytes().length;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param dom
	 *            the dom to set
	 */
	public void setDom(String dom) {
		this.dom = dom;
	}

	/**
	 * @return if this state is created through guided crawling.
	 */
	public boolean isGuidedCrawling() {
		return guidedCrawling;
	}

	/**
	 * @param guidedCrawling
	 *            true if set through guided crawling.
	 */
	public void setGuidedCrawling(boolean guidedCrawling) {
		this.guidedCrawling = guidedCrawling;
	}

	/**
	 * search for new Candidates from this state. The search for candidates is only done when no
	 * list is available yet (candidateActions == null).
	 * 
	 * @param candidateExtractor
	 *            the CandidateElementExtractor to use.
	 * @param crawlTagElements
	 *            the tag elements to examine.
	 * @param crawlExcludeTagElements
	 *            the elements to exclude.
	 * @param clickOnce
	 *            if true examine each element once.
	 * @return true if the searchForCandidateElemens has run false otherwise
	 */
	@GuardedBy("candidateActionsSearchLock")
	public boolean searchForCandidateElements(CandidateElementExtractor candidateExtractor,
	        List<TagElement> crawlTagElements, List<TagElement> crawlExcludeTagElements,
	        boolean clickOnce) {
		synchronized (candidateActionsSearchLock) {
			if (candidateActions == null) {
				candidateActions = new LinkedBlockingDeque<CandidateCrawlAction>();
			} else {
				return false;
			}
		}
		// TODO read the eventtypes from the crawl elements instead
		List<String> eventTypes = new ArrayList<String>();
		eventTypes.add(EventType.click.toString());

		try {
			List<CandidateElement> candidateList =
			        candidateExtractor.extract(crawlTagElements, crawlExcludeTagElements,
			                clickOnce, this);

			for (CandidateElement candidateElement : candidateList) {
				for (String eventType : eventTypes) {
					if (eventType.equals(EventType.click.toString())) {
						candidateActions.add(new CandidateCrawlAction(candidateElement,
						        EventType.click));
					} else {
						if (eventType.equals(EventType.hover.toString())) {
							candidateActions.add(new CandidateCrawlAction(candidateElement,
							        EventType.hover));
						} else {
							LOGGER.warn("The Event Type: " + eventType + " is not supported.");
						}
					}
				}
			}
		} catch (CrawljaxException e) {
			LOGGER.error(
			        "Catched exception while searching for candidates in state " + getName(), e);
		}
		return candidateActions.size() > 0; // Only notify of found candidates when there are...

	}

	/**
	 * Return a list of UnprocessedCandidates in a List.
	 * 
	 * @return a list of candidates which are unprocessed.
	 */
	public List<CandidateElement> getUnprocessedCandidateElements() {
		List<CandidateElement> list = new ArrayList<CandidateElement>();
		if (candidateActions == null) {
			return list;
		}
		CandidateElement last = null;
		for (CandidateCrawlAction candidateAction : candidateActions) {
			if (last != candidateAction.getCandidateElement()) {
				last = candidateAction.getCandidateElement();
				list.add(last);
			}
		}
		return list;
	}

	/**
	 * @return a Document instance of the dom string.
	 * @throws SAXException
	 *             if an exception is thrown.
	 * @throws IOException
	 *             if an exception is thrown.
	 */
	public Document getDocument() throws SAXException, IOException {
		return Helper.getDocument(this.dom);
	}

	/**
	 * This is the main work divider function, calling this function will first look at the
	 * registeedCandidateActions to see if the current Crawler has already registered itself at one
	 * of the jobs. Second it tries to see if the current crawler is not already processing one of
	 * the actions and return that action and last it tries to find an unregistered candidate. If
	 * all else fails it tries to return a action that is registered by an other crawler and
	 * disables that crawler.
	 * 
	 * @param requestingCrawler
	 *            the Crawler placing the request for the Action
	 * @param manager
	 *            the manager that can be used to remove a crawler from the queue.
	 * @return the action that needs to be performed by the Crawler.
	 */
	public CandidateCrawlAction pollCandidateCrawlAction(Crawler requestingCrawler,
	        CrawlQueueManager manager) {
		CandidateCrawlAction action = registerdCandidateActions.remove(requestingCrawler);
		if (action != null) {
			workInProgressCandidateActions.put(requestingCrawler, action);
			return action;
		}
		action = workInProgressCandidateActions.get(requestingCrawler);
		if (action != null) {
			return action;
		}
		
		do {
			action = candidateActions.pollFirst();
		} while(extension.screenAction(action, requestingCrawler));
		
		if (action != null) {
			workInProgressCandidateActions.put(requestingCrawler, action);
			return action;
		} else {
			Crawler c = registeredCrawlers.pollFirst();
			if (c == null) {
				return null;
			}
			do {
				if (manager.removeWorkFromQueue(c)) {
					LOGGER.info("Crawler " + c + " REMOVED from Queue!");
					action = registerdCandidateActions.remove(c);
					if (action != null) {
						/*
						 * We got a action and removed the registeredCandidateActions for the
						 * crawler, remove the crawler from queue as the first thinng. As the
						 * crawler might just have started the run method of the crawler must also
						 * be added with a check hook.
						 */
						LOGGER.info("Stolen work from other Crawler");
						return action;
					} else {
						LOGGER.warn("Oh my! I just removed " + c
						        + " from the queue with no action!");
					}
				} else {
					LOGGER.warn("FAILED TO REMOVE " + c + " from Queue!");
				}
				c = registeredCrawlers.pollFirst();
			} while (c != null);
		}
		return null;
	}

	/**
	 * Register an assignment to the crawler.
	 * 
	 * @param newCrawler
	 *            the crawler that wants an assignment
	 * @return true if the crawler has an assignment false otherwise.
	 */
	public boolean registerCrawler(Crawler newCrawler) {
		CandidateCrawlAction action;
		do {
			action = candidateActions.pollLast();
		} while(extension.screenAction(action, newCrawler));
		
		if (action == null) {
			return false;
		}
		registeredCrawlers.offerFirst(newCrawler);
		registerdCandidateActions.put(newCrawler, action);
		return true;
	}

	/**
	 * Register a Crawler that is going to work, tell if his must go on or abort.
	 * 
	 * @param crawler
	 *            the crawler to register
	 * @return true if the crawler is successfully registered
	 */
	public boolean startWorking(Crawler crawler) {
		CandidateCrawlAction action = registerdCandidateActions.remove(crawler);
		registeredCrawlers.remove(crawler);
		if (action == null) {
			return false;
		} else {
			workInProgressCandidateActions.put(crawler, action);
			return true;
		}
	}

	/**
	 * Notify the current StateVertix that the given crawler has finished working on the given
	 * action.
	 * 
	 * @param crawler
	 *            the crawler that is finished
	 * @param action
	 *            the action that have been examined
	 */
	public void finishedWorking(Crawler crawler, CandidateCrawlAction action) {
		candidateActions.remove(action);
		registerdCandidateActions.remove(crawler);
		workInProgressCandidateActions.remove(crawler);
		registeredCrawlers.remove(crawler);
	}
	
	/**
	 * Runs the preStateCrawl plugins if appropriate
	 * Will only run the plugins the first time called
	 */
	public void runPreStateCrawlingPlugins(CrawlSession session) {
		if(!haveRunPreStateCrawlingPlugins) {
			// Only execute the preStateCrawlingPlugins when it's the first time
			LOGGER.info("Starting preStateCrawlingPlugins...");
			CrawljaxPluginsUtil.runPreStateCrawlingPlugins(session,
					getUnprocessedCandidateElements());
			haveRunPreStateCrawlingPlugins = true;
		}
	}
	
	//Used this instead of the existing ID, because not sure what that's used for
	public int getUniqueIdentifier() {
		return unique_id;
	}
	
	public String getTextIdentifier() {
		return toString() + "[" + unique_id + "]";
	}
}
