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

import java.util.List;
import java.util.ArrayList;

import edu.columbia.fuf.FD;

/**
 * A document plan, the output of document structuring schemata.  In
 * this case, a document plan is a list of paragraph. Each paragraph
 * is a list of aggregation segments. Each aggregation segment is a
 * list of clauses.
 *
 * @author Pablo Ariel Duboue (pablo@cs.columbia.edu)
 * @version $Revision: 1.1 $, $Date: 2004/07/12 09:34:04 $
 */

public class DocumentPlan{
    /** The document plan itself. */
    protected List paragraphs;
    /** Empty constructor. */
    public DocumentPlan(){
        this.paragraphs=new ArrayList();
        List aggr=new ArrayList();
        aggr.add(new ArrayList());
        this.paragraphs.add(aggr);
    }
    /** Adds a new clause to the open paragraph and aggregation segment. */
    public void addClause(FD clause){
        List lastPar=(List)paragraphs.get(paragraphs.size()-1);
        List lastAggr=(List)lastPar.get(lastPar.size()-1);
        lastAggr.add(clause);
    }
    /** Closes the current aggregation segment (deletes it if it's
     * empty), closes the current paragraph and creates a new
     * paragraph with a new aggregation segment inside. */
    public void addParBoundary(){
        List lastPar=(List)paragraphs.get(paragraphs.size()-1);
        List lastAggr=(List)lastPar.get(lastPar.size()-1);
        if(lastAggr.size()==0)
            lastPar.remove(lastPar.size()-1);
        if(lastPar.size()>0){
            List aggr=new ArrayList();
            aggr.add(new ArrayList());
            this.paragraphs.add(aggr);
        }
    }
    /** Closes the current aggregation segment and creates a new
     * one. */
    public void addAggrBoundary(){
        List lastPar=(List)paragraphs.get(paragraphs.size()-1);
        List lastAggr=(List)lastPar.get(lastPar.size()-1);
        if(lastAggr.size()>0)
            lastPar.add(new ArrayList());
    }
    /** Obtain the list of paragraphs. */
    public List getParagraphs(){return this.paragraphs;}
    /** String rendering, for debugging purposes. */
    public String toString(){
        return "DocumentPlan"+paragraphs.toString();
    }
}
