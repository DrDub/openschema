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
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import edu.columbia.fuf.FD;

/**
 * A <tt>LocalChooser</tt> that implements a simple set of focus
 * heuristics described in McKeown (1985).
 * <p>
 * The heuristics are as follow:
 *
 * <ul><li>Shift focus to a previously mentioned, not in focus object
 * (an object belonging to the previous <b>potential focus
 * list</b>. The rationale is that text is more interesting when it
 * changes focus <i>and</i> if the focus is not changed at that moment
 * the entity will need to be re-introduced later on.</li>
 *
 * <li>Stay put with the current focus (to avoid giving the idea that
 * a topic is finished when it is not).</li>
 *
 * <li>Come back to a topic in the focus stack.</li>
 * </ul>
 *
 * Moreover, the links between the potential focus lists of the
 * previous clause and the current one are also used as a factor to
 * decide continuation.  The most links exist, the most cohesive the
 * text is expected to be.
 *
 * @author Pablo Ariel Duboue (pablo@cs.columbia.edu)
 * @version $Revision: 1.1.1.1 $, $Date: 2004/07/12 09:34:07 $
 */



public class SimpleFocusChooser extends LocalChooser{
    /** Verbosity flag, defaults to off.*/
    public static boolean verbose=false;
    /** Ontology, employed for the potential focus lists linking
     * decision process. */
    protected Ontology ontology;
    /** Constructor, receives the ontology. */
    public SimpleFocusChooser(Ontology ontology){
        this.ontology=ontology;
    }
    /** Choose the appropriate continuation, using some of McKeown
     * (1985) strategies. */
    public Decision choose(List fds,List defaultFoci,Object currentFocus,
                           List potentialFoci,List focusStack,FrameSet frames){
        // first heuristic: choose a shift of focus, if possible
        // CF(new) \in PFL(last)
        List candidatePositions=new ArrayList();
        List candidateDefaultFoci=new ArrayList();
        for(int i=0;i<fds.size();i++)
            if(!defaultFoci.get(i).equals(currentFocus)&&
               !focusStack.contains(defaultFoci.get(i))&&
               potentialFoci.contains(defaultFoci.get(i))){
                candidatePositions.add(new Integer(i));
                candidateDefaultFoci.add(defaultFoci.get(i));
            }
        if(candidatePositions.size()>0)
            return chooseOnPotentialToPotentialLinks(candidatePositions,candidateDefaultFoci,
                                                     fds,potentialFoci,frames);
        // second heuristic: choose to stay on focus
        // CF(new) == CF(last)
        for(int i=0;i<fds.size();i++)
            if(defaultFoci.get(i).equals(currentFocus)){
                candidatePositions.add(new Integer(i));
                candidateDefaultFoci.add(defaultFoci.get(i));
            }
        if(candidatePositions.size()>0)
            return chooseOnPotentialToPotentialLinks(candidatePositions,candidateDefaultFoci,
                                                     fds,potentialFoci,frames);

        // third heuristic: choose from the focus stack
        // CF(new) \in focus-stack
        for(int i=0;i<fds.size();i++)
            if(focusStack.contains(defaultFoci.get(i))){
                candidatePositions.add(new Integer(i));
                candidateDefaultFoci.add(defaultFoci.get(i));
            }
        if(candidatePositions.size()>0)
            return chooseOnPotentialToPotentialLinks(candidatePositions,candidateDefaultFoci,
                                                     fds,potentialFoci,frames);

        // now consider the potential foci for each chance and repeat
        // the heuristics
        List[]newPotentialFoci=new List[fds.size()];
        for(int i=0;i<fds.size();i++)
            newPotentialFoci[i]=new ArrayList(extractPotentialFoci((FD)fds.get(i),frames));
        
        // first heuristic: choose a shift of focus, if possible
        // CF(new) \in PFL(last)
        for(int i=0;i<fds.size();i++)
            for(int j=0;j<newPotentialFoci[i].size();j++)
                if(potentialFoci.contains(newPotentialFoci[i].get(j))){
                    candidatePositions.add(new Integer(i));
                    candidateDefaultFoci.add(newPotentialFoci[i].get(j));
                    break;
                }
        if(candidatePositions.size()>0)
            return chooseOnPotentialToPotentialLinks(candidatePositions,candidateDefaultFoci,
                                                     fds,potentialFoci,frames);
        // second heuristic: choose to stay on focus
        // CF(new) == CF(last)
        for(int i=0;i<fds.size();i++)
            for(int j=0;j<newPotentialFoci[i].size();j++)
                if(newPotentialFoci[i].get(j)==currentFocus){
                    candidatePositions.add(new Integer(i));
                    candidateDefaultFoci.add(newPotentialFoci[i].get(j));
                    break;
                }
        if(candidatePositions.size()>0)
            return chooseOnPotentialToPotentialLinks(candidatePositions,candidateDefaultFoci,
                                                     fds,potentialFoci,frames);

        // third heuristic: choose from the focus stack
        // CF(new) \in focus-stack
        for(int i=0;i<fds.size();i++)
            for(int j=0;j<newPotentialFoci[i].size();j++)
                if(focusStack.contains(newPotentialFoci[i].get(j))){
                    candidatePositions.add(new Integer(i));
                    candidateDefaultFoci.add(newPotentialFoci[i].get(j));
                    break;
                }
        if(candidatePositions.size()>0)
            return chooseOnPotentialToPotentialLinks(candidatePositions,candidateDefaultFoci,
                                                     fds,potentialFoci,frames);
        // greedy
        return new Decision(0,defaultFoci.get(0),
                            new ArrayList(extractPotentialFoci((FD)fds.get(0),frames)));
    }
    /** Final decision on a smaller set of candidates using the
     * potential to potential links. */
    protected Decision chooseOnPotentialToPotentialLinks(List candidatePositions,List candidateDefaultFoci,
                                                         List fds,List potentialFoci,FrameSet frames){
        int maxPos=0;
        double maxLinks=-1;
        Object maxCurrentFocus=null;
        List maxPotentialFoci=null;
        for(int c=0;c<candidatePositions.size();c++){
            int pos=((Integer)candidatePositions.get(c)).intValue();
            double links=0;
            Set pfl=extractPotentialFoci((FD)fds.get(pos),frames);
            Iterator p=pfl.iterator();
            while(p.hasNext()){
                Frame potentialFocus=(Frame)p.next();
                double maxValue=0;
                Frame linkTo=null;
                for(int i=0;i<potentialFoci.size();i++){
                    Frame oldPotentialFocus=(Frame)potentialFoci.get(i);
                    double thisValue=0;
                    if(potentialFocus==oldPotentialFocus){
                        thisValue=1;
                    }else{
                        double dist=ontology.distance(potentialFocus.getType(),
                                                      oldPotentialFocus.getType());
                        if(dist<0.9)
                            thisValue=0.9-dist;
                    }
                    if(i==0||thisValue>maxValue){
                        maxValue=thisValue;
                        linkTo=oldPotentialFocus;
                    }
                }
                if(maxValue>0){
                    if(verbose)
                        System.err.println("link: "+potentialFocus.getID()+" to: "+linkTo.getID());
                    links+=maxValue;
                }
            }
            if(verbose){
                System.err.println("Continuation with PFL:");
                Iterator pp=pfl.iterator();
                while(pp.hasNext())System.err.println("\t"+((Frame)pp.next()).getID());
                System.err.println("links="+links);
            }
            if(links>maxLinks){
                maxPos=pos;
                maxLinks=links;
                maxCurrentFocus=candidateDefaultFoci.get(c);
                maxPotentialFoci=new ArrayList(pfl);
            }
        }
        return new Decision(maxPos,maxCurrentFocus,maxPotentialFoci);
    }
}
