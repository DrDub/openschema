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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * An implementation of the <tt>Ontology</tt> interface from and RDF Schema (rdfs) file.
 * 
 * @author Pablo Ariel Duboue <pablo.duboue@gmail.com>
 */

public class RDFOntology implements Ontology {
	/**
	 * Parents of a given concept, map from concept name to Set of parent concepts.
	 */
	protected Map<String, Set<String>> parentsOf;

	/** Construct an ontology from an existing RDF Model. */
	public RDFOntology(Model model) {
		extractTriples(model);
	}

	/** Construct an ontology from an InputStream. */
	public RDFOntology(java.io.InputStream in, String base) throws java.io.IOException {
		Model model = ModelFactory.createDefaultModel();
		model.read(in, base);
		extractTriples(model);
	}

	/** Construct an ontology from an URL. */
	public RDFOntology(String url, String lang) {
		Model model = ModelFactory.createDefaultModel();
		model.read(url, lang);
		extractTriples(model);
	}

	/** Extract the triples from the Model into the ontology. */
	protected void extractTriples(Model model) {
		this.parentsOf = new HashMap<String, Set<String>>();
		StmtIterator st = model.listStatements();
		while (st.hasNext()) {
			Statement statement = st.nextStatement();
			Resource subject = statement.getSubject();
			RDFNode object = statement.getObject();
			// Property property = statement.getPredicate();
			String parent = object instanceof Resource ? ((Resource) object).getLocalName() : object.toString();

			String concept = subject.getLocalName();
			// System.err.println("Adding '"+concept+"' is a child of '"+parent+"'.");
			if (!parentsOf.containsKey(concept))
				parentsOf.put(concept, new HashSet<String>());
			parentsOf.get(concept).add(parent);
		}
	}

	/** Whether a child concept is a sub-type of the parent. */
	public boolean isA(Object child, Object parent) {
		// System.err.println("Checking whether '"+child+"' is a child of '"+parent+"'.");
		if (child == null)
			return false;
		if (parent == null)
			return false;
		child = child.toString();
		parent = parent.toString();
		if (child.equals(parent))
			return true;
		if (!parentsOf.containsKey(child))
			return false;
		Set<String> parents = parentsOf.get(child);
		if (parents.contains(parent))
			return true;
		for (String other : parents)
			if (isA(other, parent))
				return true;
		return false;
	}

	/**
	 * Some notion of semantic distance between the two concepts. Currently, it returns Double.MAX_VALUE if any of the
	 * concepts is null or unknown to the ontology or if they are completely unrelated (they don't share any ancestor in
	 * common, something that cannot happen in RDF-based ontologies, all concepts derive from 'Class'). If the two
	 * concepts are the same, the distance is 0. If one is a parent of the other, the distance is 0.5 (nevermind the
	 * distance). Otherwise the distance is 0.5 plus 0.1 for each level up until a common ancestor is found.
	 */
	public double distance(Object concept1, Object concept2) {
		if (concept1 == null)
			return Double.MAX_VALUE;
		if (concept2 == null)
			return Double.MAX_VALUE;

		if (concept1.equals(concept2))
			return 0.0;

		if (!parentsOf.containsKey(concept1))
			return Double.MAX_VALUE;
		if (!parentsOf.containsKey(concept2))
			return Double.MAX_VALUE;

		if (isA(concept1, concept2) || isA(concept2, concept1))
			return 0.5;

		Set<String> parents1 = new HashSet<String>(parentsOf.get(concept1));
		Set<String> parents2 = new HashSet<String>(parentsOf.get(concept2));
		double current = 0.6;
		boolean changes = false;
		do {
			Set<String> intersection = new HashSet<String>(parents1);
			intersection.retainAll(parents2);
			if (!intersection.isEmpty())
				return current;
			current += 0.1;
			int size1 = parents1.size();
			for (String concept : parents1)
				if (parentsOf.containsKey(concept))
					parents1.addAll(parentsOf.get(concept));

			int size2 = parents2.size();
			for (String concept : parents2)
				if (parentsOf.containsKey(concept))
					parents2.addAll(parentsOf.get(concept));

			changes = size1 != parents1.size() || size2 != parents2.size();
		} while (changes);

		return Double.MAX_VALUE;
	}
}
