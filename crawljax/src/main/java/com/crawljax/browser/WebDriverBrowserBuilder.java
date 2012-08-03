package com.crawljax.browser;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

import com.crawljax.core.configuration.CrawljaxConfigurationReader;

/**
 * This class represents the default Crawljax used implementation of the BrowserBuilder. It's based
 * on the WebDriver implementations offered by Crawljax.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id: WebDriverBrowserBuilder.java 465 2010-11-05 23:22:45Z amesbah $
 */
public class WebDriverBrowserBuilder implements EmbeddedBrowserBuilder {

	/**
	 * Build a new WebDriver based EmbeddedBrowser.
	 * 
	 * @see EmbeddedBrowserBuilder#buildEmbeddedBrowser(CrawljaxConfigurationReader)
	 * @param configuration
	 *            the configuration object to read the config values from
	 * @return the new build WebDriver based embeddedBrowser
	 */
	@Override
	public EmbeddedBrowser buildEmbeddedBrowser(CrawljaxConfigurationReader configuration) {
		// Retrieve the config values used
		List<String> filterAttributes = configuration.getFilterAttributeNames();
		Map<Pattern, String> regexFilters = configuration.getRegexFilters();
		int crawlWaitReload = configuration.getCrawlSpecificationReader().getWaitAfterReloadUrl();
		int crawlWaitEvent = configuration.getCrawlSpecificationReader().getWaitAfterEvent();

		// Determine the requested browser type
		switch (configuration.getBrowser()) {
			case firefox:
				if (configuration.getProxyConfiguration() != null) {
					FirefoxProfile profile = new FirefoxProfile();

					profile.setPreference("network.proxy.http", configuration
					        .getProxyConfiguration().getHostname());
					profile.setPreference("network.proxy.http_port", configuration
					        .getProxyConfiguration().getPort());
					profile.setPreference("network.proxy.type", configuration
					        .getProxyConfiguration().getType().toInt());
					/* use proxy for everything, including localhost */
					profile.setPreference("network.proxy.no_proxies_on", "");

					return WebDriverBackedEmbeddedBrowser.withDriver(new FirefoxDriver(profile),
					        filterAttributes, regexFilters, crawlWaitReload, crawlWaitEvent);
				}

				return WebDriverBackedEmbeddedBrowser.withDriver(new FirefoxDriver(),
				        configuration.getFilterAttributeNames(), configuration.getRegexFilters(), configuration
				                .getCrawlSpecificationReader().getWaitAfterEvent(), configuration
				                .getCrawlSpecificationReader().getWaitAfterReloadUrl());

			case ie:
				return WebDriverBackedEmbeddedBrowser.withDriver(new InternetExplorerDriver(),
				        configuration.getFilterAttributeNames(), configuration.getRegexFilters(), configuration
				                .getCrawlSpecificationReader().getWaitAfterEvent(), configuration
				                .getCrawlSpecificationReader().getWaitAfterReloadUrl());

			case chrome:
				return WebDriverBackedEmbeddedBrowser.withDriver(new ChromeDriver(),
				        configuration.getFilterAttributeNames(), configuration.getRegexFilters(), configuration
				                .getCrawlSpecificationReader().getWaitAfterEvent(), configuration
				                .getCrawlSpecificationReader().getWaitAfterReloadUrl());

			case remote:
				return WebDriverBackedEmbeddedBrowser.withRemoteDriver(configuration
				        .getRemoteHubUrl(), configuration.getFilterAttributeNames(), configuration.getRegexFilters(),
				        configuration.getCrawlSpecificationReader().getWaitAfterEvent(),
				        configuration.getCrawlSpecificationReader().getWaitAfterReloadUrl());

			case htmlunit:
				return WebDriverBackedEmbeddedBrowser.withDriver(new HtmlUnitDriver(true),
				        configuration.getFilterAttributeNames(), configuration.getRegexFilters(), configuration
				                .getCrawlSpecificationReader().getWaitAfterEvent(), configuration
				                .getCrawlSpecificationReader().getWaitAfterReloadUrl());

			default:
				return WebDriverBackedEmbeddedBrowser.withDriver(new FirefoxDriver(),
				        configuration.getFilterAttributeNames(), configuration.getRegexFilters(), configuration
				                .getCrawlSpecificationReader().getWaitAfterEvent(), configuration
				                .getCrawlSpecificationReader().getWaitAfterReloadUrl());
		}
	}
}
