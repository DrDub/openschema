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

/**
 * An implementation of the <tt>Ontology</tt> interface where each concept is in a separate class.
 * 
 * @author Pablo Ariel Duboue <pablo.duboue@gmail.com>
 */

public class EmptyOntology implements Ontology {
	/** Returns true only with child equals parent. */
	public boolean isA(Object child, Object parent) {
		if (child == null)
			return false;
		if (parent == null)
			return false;
		return child.toString().equals(parent.toString());
	}

	/** Return 0.0 only when concept1 equals concept2 (1.0 otherwise) . */
	public double distance(Object concept1, Object concept2) {
		if (concept1 == null)
			return 1.0;
		if (concept2 == null)
			return 1.0;
		return concept1.equals(concept2) ? 0.0 : 1.0;
	}
}
