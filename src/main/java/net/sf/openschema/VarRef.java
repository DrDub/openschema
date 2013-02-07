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
 * Reference to a variable, used in properties and clauses.
 * 
 * @author Pablo Ariel Duboue <pablo.duboue@gmail.com>
 * @version $Revision: 1.1 $, $Date: 2004/07/12 09:34:06 $
 */

class VarRef {
	protected String var;

	public VarRef(String var) {
		this.var = var;
	}

	public String getRef() {
		return var;
	}

	public String toString() {
		return "@" + var;
	}
}
