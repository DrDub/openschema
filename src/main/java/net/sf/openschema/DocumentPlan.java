/***********************************************************************
 * OPENSCHEMA
 * An open source implementation of document structuring schemata.
 *
 * Copyright (C) 2004-2013 Pablo Ariel Duboue <pablo.duboue@gmail.com>
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111, USA.
 ***********************************************************************/

package net.sf.openschema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A document plan, the output of document structuring schemata. In this case, a document plan is a list of paragraph.
 * Each paragraph is a list of aggregation segments. Each aggregation segment is a list of clauses.
 * 
 * @author Pablo Ariel Duboue (pablo.duboue@gmail.com)
 */

public class DocumentPlan {
	/** The document plan itself. */
	protected List<List<List<Map<String, Object>>>> paragraphs;

	/** Empty constructor. */
	public DocumentPlan() {
		this.paragraphs = new ArrayList<List<List<Map<String, Object>>>>();
		List<List<Map<String, Object>>> aggr = new ArrayList<List<Map<String, Object>>>();
		aggr.add(new ArrayList<Map<String, Object>>());
		this.paragraphs.add(aggr);
	}

	/** Adds a new clause to the open paragraph and aggregation segment. */
	public void addClause(Map<String, Object> clause) {
		List<List<Map<String, Object>>> lastPar = paragraphs.get(paragraphs.size() - 1);
		List<Map<String, Object>> lastAggr = lastPar.get(lastPar.size() - 1);
		lastAggr.add(clause);
	}

	/**
	 * Closes the current aggregation segment (deletes it if it's empty), closes the current paragraph and creates a new
	 * paragraph with a new aggregation segment inside.
	 */
	public void addParBoundary() {
		List<List<Map<String, Object>>> lastPar = paragraphs.get(paragraphs.size() - 1);
		List<Map<String, Object>> lastAggr = lastPar.get(lastPar.size() - 1);
		if (lastAggr.size() == 0)
			lastPar.remove(lastPar.size() - 1);
		if (lastPar.size() > 0) {
			List<List<Map<String, Object>>> aggr = new ArrayList<List<Map<String, Object>>>();
			aggr.add(new ArrayList<Map<String, Object>>());
			this.paragraphs.add(aggr);
		}
	}

	/**
	 * Closes the current aggregation segment and creates a new one.
	 */
	public void addAggrBoundary() {
		List<List<Map<String, Object>>> lastPar = paragraphs.get(paragraphs.size() - 1);
		List<Map<String, Object>> lastAggr = lastPar.get(lastPar.size() - 1);
		if (lastAggr.size() > 0)
			lastPar.add(new ArrayList<Map<String, Object>>());
	}

	/** Obtain the list of paragraphs. */
	public List<List<List<Map<String, Object>>>> getParagraphs() {
		return this.paragraphs;
	}

	/** String rendering, for debugging purposes. */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("DocumentPlan:");
		int paraCounter = 0;
		for (List<List<Map<String, Object>>> para : paragraphs) {
			paraCounter++;
			int aggrCounter = 0;
			for (List<Map<String, Object>> aggr : para) {
				aggrCounter++;
				int clauseCounter = 0;
				for (Map<String, Object> clause : aggr) {
					clauseCounter++;
					sb.append(paraCounter).append('.').append(aggrCounter).append('.').append(clauseCounter)
							.append('.').append(clause).append('\n');
				}
			}
		}
		return sb.toString();
	}
}
