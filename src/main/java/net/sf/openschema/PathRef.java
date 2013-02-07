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
 * A path reference, starting from a variable. Used in properties and clauses.
 * 
 * @author Pablo Ariel Duboue <pablo.duboue@gmail.com>
 */

class PathRef extends VarRef {
	protected String[] path;

	public PathRef(String var, String[] path) {
		super(var);
		this.path = path;
	}

	public static PathRef parse(String s) {
		String[] parts = s.split("\\.", 0);
		String var = parts[0];
		String[] path = new String[parts.length - 1];
		System.arraycopy(parts, 1, path, 0, parts.length - 1);
		return new PathRef(var, path);
	}

	public String[] getPath() {
		return path;
	}

	public String toString() {
		StringBuffer result = new StringBuffer(super.toString());
		for (int i = 0; i < path.length; i++) {
			result.append(".");
			result.append(path[i]);
		}
		return result.toString();
	}
}
