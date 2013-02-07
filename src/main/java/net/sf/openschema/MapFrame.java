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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A <tt>Frame</tt> implementation using <tt>java.util.Map</tt>
 * 
 * @author Pablo Ariel Duboue <pablo.duboue@gmail.com>
 */

public class MapFrame extends HashMap<String, Object> implements Frame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Construct a frame with a given id and type. The id is stored in the map
	 * as the value for the key "#ID", while the type for the key "#TYPE".
	 */
	public MapFrame(String id, Object type) {
		super();
		this.setID(id);
		this.setType(type);
	}

	/** Access the type of the frame. */
	public Object getType() {
		return super.get("#TYPE");
	}

	/** Modify the type of the frame. */
	public void setType(Object type) {
		super.put("#TYPE", type);
	}

	/** Access the ID of the frame. */
	public String getID() {
		return (String) super.get("#ID");
	}

	/** Modify the type of the frame. */
	public void setID(String id) {
		super.put("#ID", id);
	}

	/**
	 * Access a value in the frame. Returns an EMPTY_LIST if the key is
	 * undefined.
	 */
	@SuppressWarnings("unchecked")
	public List<Object> get(String key) {
		if (key.equals("#TYPE"))
			return Collections.singletonList(super.get(key));
		return super.containsKey(key) ? (List<Object>) super.get(key) : Collections.EMPTY_LIST;
	}

	/** Set the value of the key (previous values are erased). */
	public void set(String key, Object value) {
		super.put(key, new ArrayList<Object>(Collections.singletonList(value)));
	}

	/** Set the values of the key (previous values are erased). */
	public void set(String key, List<Object> values) {
		super.put(key, values);
	}

	/** Add a value to the existing values of a key. */
	@SuppressWarnings("unchecked")
	public void add(String key, Object value) {
		if (!super.containsKey(key))
			super.put(key, new ArrayList<Object>());
		((List<Object>) super.get(key)).add(value);
	}

	/** Check whether or not a key is defined. */
	public boolean containsKey(String key) {
		return super.containsKey(key);
	}

	/** Retrieve the set of all keys. */
	public Set<String> keySet() {
		Set<String> keySet = new HashSet<String>(super.keySet());
		keySet.remove("#ID");
		keySet.remove("#TYPE");
		return keySet;
	}

	// need to redefine hashCode as Maps cannot be circular and hash
	public int hashCode() {
		return super.get("#ID").hashCode();
	}
}
