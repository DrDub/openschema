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
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * A <tt>Frame</tt> implementation using <tt>java.util.Map</tt>
 *
 * @author Pablo Ariel Duboue (pablo@cs.columbia.edu)
 * @version $Revision: 1.1.1.1 $, $Date: 2004/07/12 09:34:06 $
 */

public class MapFrame extends HashMap implements Frame{
    /** Construct a frame with a given id and type. The id is stored
     * in the map as the value for the key "#ID", while the type for
     * the key "#TYPE". */
    public MapFrame(String id,Object type){
        super();
        this.setID(id);
        this.setType(type);
    }
    /** Access the type of the frame. */
    public Object getType(){return super.get("#TYPE");}
    /** Modify the type of the frame. */
    public void setType(Object type){super.put("#TYPE",type);}
    /** Access the ID of the frame. */
    public String getID(){return (String)super.get("#ID");}
    /** Modify the type of the frame. */
    public void setID(String id){super.put("#ID",id);}
    /** Access a value in the frame. Returns an EMPTY_LIST if the key
     * is undefined. */
    public List get(String key){
        if(key.equals("#TYPE"))
            return Collections.singletonList(super.get(key));
        return super.containsKey(key)?
            (List)super.get(key):
            Collections.EMPTY_LIST;
    }
    /** Set the value of the key (previous values are erased). */
    public void set(String key,Object value){
        super.put(key,new ArrayList(Collections.singletonList(value)));
    }
    /** Set the values of the key (previous values are erased). */
    public void set(String key,List values){super.put(key,values);}
    /** Add a value to the existing values of a key. */
    public void add(String key,Object value){
        if(!super.containsKey(key))
            super.put(key,new ArrayList());
        ((List)super.get(key)).add(value);
    }
    /** Check whether or not a key is defined. */
    public boolean containsKey(String key){return super.containsKey(key);}
    /** Retrieve the set of all keys. */
    public Set keySet(){
        Set keySet=new HashSet(super.keySet());
        keySet.remove("#ID");
        keySet.remove("#TYPE");
        return keySet;
    }
    // need to redefine hashCode as Maps cannot be circular and hash
    public int hashCode(){return super.get("#ID").hashCode();}
}
        
