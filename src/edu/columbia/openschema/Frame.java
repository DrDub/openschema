/***********************************************************************
 * OPENSCHEMA
 * An open source implementation of document structuring schemata.
 *
 * Copyright (C) 2004 Pablo Ariel Duboue
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

package edu.columbia.openschema;

import java.util.Set;
import java.util.List;

/**
 * Knowledge frames, part of the <tt>FrameSet</tt>, the input to the
 * document structuring schemata.
 *
 * @author Pablo Ariel Duboue (pablo@cs.columbia.edu)
 * @version $Revision: 1.1.1.1 $, $Date: 2004/07/12 09:34:04 $
 */

public interface Frame{
    /** Access the type of the frame. */
    public Object getType();
    /** Modify the type of the frame. */
    public void setType(Object type);
    /** Access the ID of the frame. */
    public String getID();
    /** Modify the type of the frame. */
    public void setID(String id);
    /** Access a value in the frame. Returns an EMPTY_LIST if the key
     * is undefined. */
    public List get(String key);
    /** Set the value of the key (previous values are erased). */
    public void set(String key,Object value); // singleton list
    /** Set the values of the key (previous values are erased). */
    public void set(String key,List values);
    /** Add a value to the existing values of a key. */
    public void add(String key,Object value);
    /** Check whether or not a key is defined. */
    public boolean containsKey(String key);
    /** Retrieve the set of all keys. */
    public Set keySet();
}
