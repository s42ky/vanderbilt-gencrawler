package com.crawljax.core.state.identifier;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;

import com.crawljax.core.CandidateCrawlAction;
import com.crawljax.core.Crawler;
import com.crawljax.core.state.StateVertix;

public class StrippedDomVertixIdentifier implements StateVertixIdentifier {
	private static final Logger LOGGER = Logger.getLogger(StrippedDomVertixIdentifier.class.getName());
	private String strippedDom = null;
	private String dom = null;
	
	@Override
	public void init(StateVertix node, Crawler crawler) {
		strippedDom = crawler.getController().getStrippedDom(crawler.getBrowser());
		dom = crawler.getBrowser().getDom();
		LOGGER.debug("Main init.");
		if(strippedDom==null) LOGGER.debug("strippedDom IS NULL");
	}
	
	public void init(StateVertix node, String dom) {
		LOGGER.debug("Init through alt entry.");
		this.dom = dom;
		this.strippedDom = dom;
	}

	@Override
	public boolean equals(Object obj) {
		LOGGER.debug("Entering EQUALS");
		if (!(obj instanceof StrippedDomVertixIdentifier)) {
			LOGGER.debug("Not right instance.");
			return false;
		}

		if (this == obj) {
			LOGGER.debug("Reflexive true");
			return true;
		}
		final StrippedDomVertixIdentifier rhs = (StrippedDomVertixIdentifier) obj;
		
		if(strippedDom!=null) {
			LOGGER.debug("Going for .equals on strippedDom");
			return strippedDom.equals(rhs.strippedDom);
		} else {
			LOGGER.debug("Going for null comparison");
			return (rhs.strippedDom == null);
		}
	}
	
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		if (strippedDom == null || "".equals(strippedDom)) {
			builder.append(dom);
		} else {
			builder.append(strippedDom);
		}

		return builder.toHashCode();
	}
	
	public StrippedDomVertixIdentifier getNew() {
		return new StrippedDomVertixIdentifier();
	}

	//Only need equals()
	public boolean equivalent(Object o) {
		return equals(o);
	}

	public String getIdentifierString() {
		return strippedDom;
	}

	@Override
	public boolean screenAction(CandidateCrawlAction action, Crawler crawler) {
		//Always accept the action
		return false;
	}
}
