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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Property;

/**
 * A RDF-based implementation of the interface <tt>FrameSet</tt>.
 *
 * @author Pablo Ariel Duboue (pablo@cs.columbia.edu)
 * @version $Revision: 1.1.1.1 $, $Date: 2004/07/12 09:34:06 $
 */

public class RDFFrameSet implements FrameSet{
    /** The frames themselves, map from frame name to <tt>MapFrame</tt>. */
    protected Map frames;

    /** Construct a FrameSet from an existing RDF model. */
    public RDFFrameSet(Model model){
        extractTriples(model);
    }
    /** Load the RDF Model from an InputStream. */
    public RDFFrameSet(java.io.InputStream in,String base)throws java.io.IOException{
        Model model=ModelFactory.createDefaultModel();
        model.read(in,base);
        extractTriples(model);
    }
    /** Load the RDF Model from an URL. */
    public RDFFrameSet(String url,String lang){
        Model model=ModelFactory.createDefaultModel();
        model.read(url,lang);
        extractTriples(model);
    }
    /** Extract the frames from the RDF model. */
    protected void extractTriples(Model model){
        this.frames=new HashMap();
        StmtIterator st=model.listStatements();
        while(st.hasNext()){
            Statement statement=st.nextStatement();
            Resource subject=statement.getSubject();
            RDFNode object=statement.getObject();
            Property property=statement.getPredicate();

            String key=property.getLocalName();
            String frameID=subject.getLocalName();
            Object value=object instanceof Resource?
                value=findOrCreate(((Resource)object).getLocalName()):
                object.toString();
            Frame frame=findOrCreate(frameID);
            if(key.equals("TYPE"))
                frame.setType(object);
            else if(key.equals("ID"))
                frame.setID(object.toString());
            else
                frame.add(key,value);
        }
    }
    /** Find a frame with a given id or creates it anew if it is not
     * found. */
    protected Frame findOrCreate(String id){
        if(frames.containsKey(id))
            return (Frame)frames.get(id);
        Frame frame=new MapFrame(id,null);
        frames.put(id,frame);
        return frame;
    }

    /** Get all the frames. */
    public Collection getFrames(){return frames.values();}
    /** Get a frame with a given name. */
    public Frame getFrame(String id){return (Frame)frames.get(id);}
}
