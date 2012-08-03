package edu.vanderbilt.webtest.plugins;

import java.util.concurrent.atomic.AtomicInteger;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.crawlcondition.CrawlConditionChecker;
import com.crawljax.condition.eventablecondition.EventableConditionChecker;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.ExtractorManager;

public class ParamaterDomainExtractorManager implements ExtractorManager {

	private final AtomicInteger counter = new AtomicInteger();
	private final EventableConditionChecker eventableConditionChecker;
	private final CrawlConditionChecker crawlConditionChecker;
	
	public ParamaterDomainExtractorManager(EventableConditionChecker eventableConditionChecker, CrawlConditionChecker crawlConditionChecker) {
		this.eventableConditionChecker = eventableConditionChecker;
		this.crawlConditionChecker = crawlConditionChecker;
	}
	
	public boolean isChecked(String element) {
		//Fix at FALSE. We want to keep all elements
		return false;
	}

	public boolean markChecked(CandidateElement candidateElement) {
		//Fix at TRUE. We want to keep all elements
		return true;
	}

	public void increaseElementsCounter() {
		counter.getAndIncrement();
	}

	public int numberOfExaminedElements() {
		return counter.get();
	}

	public EventableConditionChecker getEventableConditionChecker() {
		return eventableConditionChecker;
	}

	public boolean checkCrawlCondition(EmbeddedBrowser browser) {
		return crawlConditionChecker.check(browser);
	}

}
