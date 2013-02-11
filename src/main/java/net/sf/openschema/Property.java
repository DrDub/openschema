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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Properties to be checked among variables in the predicate. Properties should be moved to a XML on a format on their
 * own in the future. However, lot of changes are foreseen here in the immediate future so it's easy to keep them in the
 * current format. Properties are a strings in the openschema xml definition file (OpenSchema.xsd). The class
 * <tt>Property</tt> is abstract and contains a <tt>parse</tt> static method that will translate the string into a tree
 * of Property subclasses, defined with package access rights only.
 * 
 * @author Pablo Ariel Duboue <pablo.duboue@gmail.com>
 */

public abstract class Property {
	/**
	 * Variables this property restricts. Should be used later on to implement optimizations in the CSP code in
	 * OpenSchema.
	 */
	public abstract Set<String> variables();

	/**
	 * Check whether a map of variables to values verifies or not this property. The ontology is employed for
	 * ontological properties, such as "object-1 UNDER c-entity".
	 */
	public abstract boolean check(Map<Object, Frame> vars, Ontology ontology);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Set<Frame> resolveAll(VarRef varRef, Frame frame) {
		Set<Frame> result = new HashSet<Frame>(Collections.singleton(frame));
		if (varRef instanceof PathRef) {
			String[] path = ((PathRef) varRef).getPath();
			for (int i = 0; i < path.length; i++) {
				Iterator<Frame> r = result.iterator();
				result = new HashSet<Frame>();
				while (r.hasNext()) {
					Object o = r.next();
					if (o instanceof Frame)
						result.addAll(((List) ((Frame) o).get(path[i])));
				}
			}
		}
		return result;
	}

	/**
	 * Static parse method, this is the only way to create properties. The possible variables are given as a Set (from
	 * the predicate variables section) to tell variables from ground strings.
	 */
	public static Property parse(String s, Set<String> vars) {
		String operator = null;
		if (s.indexOf("==") != -1)
			operator = "==";
		else if (s.indexOf("!=") != -1)
			operator = "!=";
		else if (s.indexOf("!UNDER") != -1)
			operator = "!UNDER";
		else if (s.indexOf("UNDER") != -1)
			operator = "UNDER";
		else
			throw new IllegalArgumentException("Cannot parse property '" + s + "'");
		String[] operandStr = s.split(operator, 2);
		Object[] operands = new Object[2];
		for (int i = 0; i < 2; i++) {
			operandStr[i] = operandStr[i].trim();
			if (vars.contains(operandStr[i]))
				operands[i] = new VarRef(operandStr[i]);
			else if (operandStr[i].indexOf('.') != -1) {
				PathRef pathRef = PathRef.parse(operandStr[i]);
				if (vars.contains(pathRef.getRef()))
					operands[i] = pathRef;
				else
					operands[i] = operandStr[i];
			} else
				operands[i] = operandStr[i];
		}

		if (operator.equals("=="))
			return new EqualProperty(operands[0], operands[1]);
		else if (operator.equals("!="))
			return new NotProperty(new EqualProperty(operands[0], operands[1]));
		else if (operator.equals("UNDER"))
			return new UnderProperty(operands[0], operands[1].toString());
		else if (operator.equals("!UNDER"))
			return new NotProperty(new UnderProperty(operands[0], operands[1].toString()));
		else
			throw new IllegalStateException("Shouldn't happen.");
	}
}

/** Not property. Employed to implement '!=' and '!UNDER'. */
class NotProperty extends Property {
	protected Property other;

	protected NotProperty(Property other) {
		this.other = other;
	}

	public Set<String> variables() {
		return other.variables();
	}

	public boolean check(Map<Object, Frame> vars, Ontology ontology) {
		return !other.check(vars, ontology);
	}

	public String toString() {
		return "!(" + other.toString() + ")";
	}
}

/** Equals property. Implements '=='. */
class EqualProperty extends Property {
	protected Object leftSide;
	protected Object rightSide;

	protected EqualProperty(Object leftSide, Object rightSide) {
		this.leftSide = leftSide;
		this.rightSide = rightSide;
	}

	public Set<String> variables() {
		Set<String> result = new HashSet<String>();
		if (leftSide instanceof VarRef)
			result.add(((VarRef) leftSide).getRef());
		if (rightSide instanceof VarRef)
			result.add(((VarRef) rightSide).getRef());
		return result;
	}

	public boolean check(Map<Object, Frame> vars, Ontology ontology) {
		Set<?> leftSideValues = leftSide instanceof VarRef ? resolveAll((VarRef) leftSide,
				vars.get(((VarRef) leftSide).getRef())) : Collections.singleton(leftSide);
		Set<?> rightSideValues = rightSide instanceof VarRef ? resolveAll((VarRef) rightSide,
				vars.get(((VarRef) rightSide).getRef())) : Collections.singleton(rightSide);

		// copy because Collections.singleton is immutable
		Set<?> oneSide = new HashSet<Object>(leftSideValues);
		oneSide.retainAll(rightSideValues);
		return !oneSide.isEmpty();
	}

	public String toString() {
		return leftSide.toString() + "==" + rightSide.toString();
	}
}

/** Under property (ontological property). Implements 'UNDER'. */
class UnderProperty extends Property {
	protected Object leftSide;
	protected String concept;

	protected UnderProperty(Object leftSide, String concept) {
		this.leftSide = leftSide;
		this.concept = concept;
	}

	@SuppressWarnings("unchecked")
	public Set<String> variables() {
		if (leftSide instanceof VarRef)
			return Collections.singleton(((VarRef) leftSide).getRef());
		return Collections.EMPTY_SET;
	}

	public boolean check(Map<Object, Frame> vars, Ontology ontology) {
		Set<?> leftSideValues = leftSide instanceof VarRef ? resolveAll((VarRef) leftSide,
				vars.get(((VarRef) leftSide).getRef())) : Collections.singleton(leftSide);
		for (Object leftSideValue : leftSideValues)
			if (leftSideValue instanceof Frame && ontology.isA(((Frame) leftSideValue).getType(), concept))
				return true;
		return false;
	}

	public String toString() {
		return leftSide.toString() + " UNDER " + concept;
	}
}
