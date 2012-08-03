/*
	CrawlOverview is a plugin for Crawljax that generates a nice HTML
	report to visually see the inferred state graph.
    Copyright (C) 2010  crawljax.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/
package com.crawljax.plugins.crawloverview;

import java.awt.Dimension;
import java.awt.Point;

import org.w3c.dom.Element;

import com.crawljax.core.CandidateElement;

public class RenderedCandidateElement extends CandidateElement {

	private final org.openqa.selenium.Point location;
	private final org.openqa.selenium.Dimension size;

	protected RenderedCandidateElement(Element element, String xpath, org.openqa.selenium.Point location2,
	        org.openqa.selenium.Dimension size2) {
		super(element, xpath);
		this.location = location2;
		this.size = size2;
	}

	/**
	 * @return the location
	 */
	protected org.openqa.selenium.Point getLocation() {
		return location;
	}

	/**
	 * @return the size
	 */
	protected org.openqa.selenium.Dimension getSize() {
		return size;
	}
}
