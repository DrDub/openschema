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

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Collections;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.xml.sax.InputSource;

import net.sf.openschema.SchemaNode;
import net.sf.jfuf.fd.AttributeValuePair;
import net.sf.jfuf.fd.FDType;
import edu.columbia.fuf.FD;
import edu.columbia.fuf.FDDistinct;
import edu.columbia.fuf.impl.hashfd.FDH;
import edu.columbia.fuf.impl.hashfd.FDDistinctH;

/**
 * Main OpenSchema class.  The schema is created from an XML file.  At
 * construction, the strategy for selecting local choices is also
 * specified.  Once created, the schema is instantiated with a
 * frameset and an ontology.  The result of an instantiated schema is
 * a document plan.
 *
 * @author Pablo Ariel Duboue (pablo@cs.columbia.edu)
 * @version $Revision: 1.1.1.1 $, $Date: 2004/07/12 09:34:06 $
 */

public class OpenSchema{
    /** Verbosity flag, defaults to off.*/
    public static boolean verbose=false;
    /** Top-level node of the schema, here is where the instantiation
     * starts. The whole network is constructed from the XML in the
     * build method.
     */
    protected Node top;
    /** Definition of the rhetorical predicates. The keys are names
     * (<tt>String</tt>), the values are instances of the inner class
     * <tt>Predicate</tt>.
     */
    protected Map predicates;
    /** LocalChooser, decides which node to continue the instantiation
     * of the schema.
     */
    protected LocalChooser chooser;
    /** 
     * Constructor from an SAX <tt>InputSource</tt>.
     * @param source A SAX <tt>InputSource</tt>, can read from a
     * <tt>Reader</tt> or an <tt>InputStream</tt>.
     * @param chooser An instance of the abstract class
     * <tt>LocalChooser</tt>, it should select among the different
     * continuation nodes during instantiation.
     */
    public OpenSchema(InputSource source,LocalChooser chooser)throws JAXBException{
        JAXBContext context=JAXBContext.newInstance("net.sf.openschema:net.sf.jfuf.fd");
        Unmarshaller unmarshaller=context.createUnmarshaller();
        unmarshaller.setValidating(false);
        assembleSchema((net.sf.openschema.OpenSchema)unmarshaller.unmarshal(source));
        this.chooser=chooser;
    }
    /** 
     * Main method, takes data to instantiate the schema, an initial
     * variable mapping and an ontology and returns a DocumentPlan.
     * @param frames a class implementing the <tt>FrameSet</tt>
     * interface, for example <tt>RDFFrameSet</tt> containing the data
     * to fill the schema.
     * @param initialMapping a mapping from variable names to values
     * (<tt>Frame</tt>s).
     * @param ontology a class implementing the <tt>Ontology</tt>
     * interface, for example <tt>RDFOntology</tt>.
     */
    public DocumentPlan instantiate(FrameSet frames,Map initialMapping,Ontology ontology){
        // initialize variables
        DocumentPlan result=new DocumentPlan();

        Cache cache=new Cache();
        Map varMapping=new HashMap(initialMapping);
        List focusHistory=new ArrayList();
        Object currentFocus=initialMapping.size()==0?
            Boolean.TRUE:initialMapping.values().iterator().next();
        List potentialFoci=new ArrayList();

        // main cycle
        Node currentNode=top;
        while(currentNode!=null){
            if(verbose)System.err.println("CURRENT NODE="+currentNode);
            // find which nodes are reachable from current node
            List confusionSet=computeConfusionSet(currentNode,varMapping,cache,ontology,frames);
            if(verbose)System.err.println("confusionSet.size()=="+confusionSet.size());
            if(confusionSet.isEmpty()){
                currentNode=null;
                break;
            }

            // transform the confusion set to something suitable for
            // the chooser to process
            List fds=new ArrayList(confusionSet.size());
            List defaultFoci=new ArrayList(confusionSet.size());
            // extract the FDs and the default focus for the decorated
            // nodes (nodes decorated with aggr. and par. boundaries)
            Iterator cs=confusionSet.iterator();
            while(cs.hasNext()){
                DecoratedNode decoratedNode=(DecoratedNode)cs.next();
                List values=cache.fetch(decoratedNode.getNode(),varMapping);
                Map valueMapping=(Map)values.get(0);

                fds.add(instantiateClause(decoratedNode.getNode().getPredicate().getOutput(),
                                          valueMapping));
                defaultFoci.add(valueMapping.get(decoratedNode.getNode().getPredicate().getDefaultFocus()));
            }
            // choose
            LocalChooser.Decision decision=chooser.choose(fds,defaultFoci,currentFocus,
                                                          potentialFoci,focusHistory,frames);
            // understand and process the decision
            DecoratedNode decoratedNode=(DecoratedNode)confusionSet.get(decision.getPosition());
            currentFocus=decision.getCurrentFocus();
            potentialFoci=decision.getPotentialFoci();
            int positionInHistory=focusHistory.lastIndexOf(currentFocus);
            if(positionInHistory==-1)
                focusHistory.add(currentFocus);
            else
                focusHistory=focusHistory.subList(0,positionInHistory+1);

            // create the clause
            FD clause=instantiatePredicate(decoratedNode.getNode(),varMapping,cache,true);

            if(decoratedNode.isAggrBoundary())result.addAggrBoundary();
            if(decoratedNode.isParBoundary())result.addParBoundary();

            currentNode=decoratedNode.getNode();

            // add the clause
            if(verbose)System.err.println("Adding clause: "+clause);
            // (focus info is recorded as it may be used by referring
            // expression generators, etc.)
            clause.put("focus",((Frame)currentFocus).getID());
            FDDistinct focusStack=new FDDistinctH();
            for(int i=focusHistory.size()-1;i>=0;i--){
                FD focusPair=new FDH();
                focusPair.put("focus",((Frame)focusHistory.get(i)).getID());
                focusStack.add(focusPair);
            }
            clause.put("focus-stack",focusStack);
            FDDistinct potentialFocusList=new FDDistinctH();
            for(int i=0;i<potentialFoci.size();i++){
                FD focusPair=new FDH();
                focusPair.put("focus",((Frame)potentialFoci.get(i)).getID());
                potentialFocusList.add(focusPair);
            }
            clause.put("potential-focus-list",potentialFocusList);

            result.addClause(clause);
        }
        return result;
    }
    /** 
     * Compute all the nodes that are reachable from current node
     * without passing through another Predicate node.  The possible
     * nodes should be able to be instantiated, that is to say, there
     * must be sets of values satisfying the node's predicate
     * properties in the cache. This method invokes the
     * <tt>canBeInstantiated</tt> method, that will fill the cache the
     * first time it is executed.
     * @param node the current node to start the spanning tree.
     * @param varMapping the current variable-values mapping.
     * @param cache the current values cache.
     * @param ontology used to update the cache.
     * @param frames used to update the cache.
     * @return a list of <tt>DecoratedNode</tt> that can be reached
     * and instantiated from the current node.
     */
    protected List computeConfusionSet(Node node,Map varMapping,Cache cache,
                                      Ontology ontology,FrameSet frames){
        List result=new ArrayList();
        
        // enumerate boundary nodes
        List boundaryNodes=new LinkedList();
        Set seenNodes=new HashSet();
        List newNodes=new ArrayList();
        newNodes.add(new DecoratedNode(node));
        Iterator og=node.getOutgoing().iterator();
        while(og.hasNext())
            newNodes.add(new DecoratedNode((Node)og.next()));
        
        while(!newNodes.isEmpty()){
            DecoratedNode currentNode=(DecoratedNode)newNodes.remove(0);
            if(seenNodes.contains(currentNode))
                continue;
            seenNodes.add(currentNode);
            boolean isPar=currentNode.isParBoundary();
            boolean isAggr=currentNode.isAggrBoundary();
            if(currentNode.getNode().isPredicate())
                boundaryNodes.add(currentNode);
            if(currentNode.getNode().isAggrBoundary())
                isAggr=true;
            if(currentNode.getNode().isParBoundary())
                isPar=true;
            if(!currentNode.getNode().isPredicate()){
                Iterator o=currentNode.getNode().getOutgoing().iterator();
                while(o.hasNext())
                    newNodes.add(new DecoratedNode((Node)o.next(),isAggr,isPar));
            }
        }
        if(verbose)System.err.println("Checking "+boundaryNodes.size()+"...");
        // check whether they can be instantiated
        Iterator bn=boundaryNodes.iterator();
        while(bn.hasNext()){
            DecoratedNode decoratedNode=(DecoratedNode)bn.next();
            if(canBeInstantiated(decoratedNode.getNode(),varMapping,cache,ontology,frames))
                result.add(decoratedNode);
        }

        return result;
    }
    /**
     * Check whether there is valid data to instantiate a node or not,
     * given an initial variable mapping. This function relies heavily
     * in the cache, and will populate it in case of cache miss.
     * @param node the current node, to analyze the predicate and
     * global-to-local variable mapping.
     * @param varMapping the global variables.
     * @param cache the cache to be queried/populated.
     * @param ontology used to populate the cache.
     * @param frames used to populate the cache.
     * @return whether or not there's data to instantiate this node.
     */
    protected boolean canBeInstantiated(Node node,Map varMapping,Cache cache,
                                        Ontology ontology,FrameSet frames){
        List values=cache.fetch(node,varMapping);
        if(values==null){ // cache miss, populate
             // search for values
            values=searchValues(node,varMapping,ontology,frames);
            if(verbose)System.err.println("Values found: "+values.size());
            cache.populate(node,varMapping,values); // populate
        }
        return !values.isEmpty();
    }
    /** 
     * Search for values that satisfy the node properties and the
     * given mapping of variables. 
     * @param node the schema node, containing the properties that the
     * values should satisfy and the mapping between local and global
     * variables.
     * @param varMapping global variables, mapping from name to value.
     * @param ontology the ontology, employed to restrict the search
     * over values of a certain type.
     * @param frames the set of frames over where to perform the
     * search.
     * @return List of associations of local variables to values.
     * @see {edu.columbia.openschema.OpenSchema.Node.Property}
     */
    protected List searchValues(Node node,Map varMapping,Ontology ontology,FrameSet frames){
        // key=name of the local variable
        // value=Set of possible values the variable can take
        if(verbose){
            System.err.println("searchValues:");
            Iterator ee=varMapping.entrySet().iterator();
            while(ee.hasNext()){
                Map.Entry entry=(Map.Entry)ee.next();
                System.err.println("\t"+entry.getKey()+"="+(entry.getValue()==null?"null":
                                                            ((Frame)entry.getValue()).getID()));
            }
        }

        Map allVars=new HashMap();
        // initialize allVars with the global vars, if given
        Map nodeVars=node.getVars();
        Iterator nv=nodeVars.entrySet().iterator();
        while(nv.hasNext()){
            Map.Entry entry=(Map.Entry)nv.next();
            Object predVar=entry.getKey();
            Object globalVar=entry.getValue();
            if(varMapping.containsKey(globalVar))
                allVars.put(predVar,Collections.singletonList(varMapping.get(globalVar)));
        }
        // or all the values of the target type, otherwise
        Map predVars=node.getPredicate().getVars();
        Iterator pv=predVars.entrySet().iterator();
        while(pv.hasNext()){
            Map.Entry entry=(Map.Entry)pv.next();
            Object predVar=entry.getKey();
            Object type=entry.getValue();
            List l=framesUnderType(type,ontology,frames);
            if(verbose){
                System.err.println("Frames under '"+type+"'");
                Iterator ll=l.iterator();
                while(ll.hasNext())System.err.println("\t"+((Frame)ll.next()).getID());
            }
            if(!allVars.containsKey(predVar))
                allVars.put(predVar,framesUnderType(type,ontology,frames));
        }
        // now check the predicates over the set multiplication of all
        // sets on the variable mappings and filter the ones who
        // satistfy the properties
        long allSets=1;
        Object[]varOrder=allVars.keySet().toArray();
        int[]allVarSize=new int[varOrder.length];
        long[]setMultiplier=new long[varOrder.length];
        for(int i=0;i<varOrder.length;i++){
            setMultiplier[i]=allSets;
            allVarSize[i]=((Collection)allVars.get(varOrder[i])).size();
            allSets=allSets*(long)allVarSize[i];
        }
        long currentSet=0;
        Map currentAssignment=new HashMap(); // reuse Map to reduce GC
        Set failedProperties=new HashSet(); // reuse Set to reduce GC
        int[]currentPos=new int[varOrder.length]; // reuse array to reduce GC
        int[]otherPos=new int[varOrder.length]; // reuse array to reduce GC
        boolean[]isFree=new boolean[varOrder.length]; // reuse array to reduce GC

        List result=new LinkedList();
        while(currentSet<allSets){
            // make the map corresponding to the number 'currentSet'
            currentAssignment.clear();
            long leftOver2=currentSet;
            for(int i=varOrder.length-1;i>=0;i--){
                int pos=(int)(leftOver2%allVarSize[i]);
                leftOver2=leftOver2/allVarSize[i];
                currentPos[i]=pos;
                currentAssignment.put(varOrder[i],
                                      ((List)allVars.get(varOrder[i])).get(pos));
            }
            
            // now see if it checks
            failedProperties.clear();
            Iterator p=node.getPredicate().getProperties().iterator();
            while(p.hasNext()){
                Property property=(Property)p.next();
                if(verbose){
                    System.err.println("Checking property: "+property);
                    Iterator e=currentAssignment.entrySet().iterator();
                    while(e.hasNext()){
                        Map.Entry entry=(Map.Entry)e.next();
                        System.err.println("\t"+entry.getKey()+"="+((Frame)entry.getValue()).getID());
                    }
                }
                if(!property.check(currentAssignment,ontology)){
                    failedProperties.add(property);
                    if(verbose)System.err.println("\t\tFALSE");
                }else if(verbose)
                    System.err.println("\t\tTRUE");
            } // look for the failed property that will advance
            if(failedProperties.isEmpty()){ // good assignment, output
                result.add(new HashMap(currentAssignment));
                currentSet++;
            }else{
                currentSet++;
                /*
                // look for the failed property that will advance more
                // positions
                Property pickedProperty=null;
                long toAdvance=0;
                Iterator fp=failedProperties.iterator();
                while(fp.hasNext()){
                    Property property=(Property)fp.next();
                    long thisToAdvance=1;
                    Set propertyVariables=property.variables();
                    for(int i=0;i<varOrder.length;i++)
                        if(!propertyVariables.contains(varOrder[i]))
                            thisToAdvance=thisToAdvance*(long)allVarSize[i];
                    if(thisToAdvance>toAdvance)
                        pickedProperty=property;
                }
                // now advance all the positions that can be done with
                // this ordering
                Set freeVariables=new HashSet(allVars.keySet());
                freeVariables.removeAll(pickedProperty.variables());
                for(int i=0;i<varOrder.length;i++)
                    isFree[i]=freeVariables.contains(varOrder[i]);
                boolean allFree=true;
                long newCurrent=currentSet;
                do{
                    newCurrent++;
                    long leftOver3=currentSet;
                    for(int i=varOrder.length-1;i>=0;i--){
                        int pos=(int) (leftOver3%allVarSize[i]);
                        leftOver3=leftOver3/allVarSize[i];
                        if(pos!=currentPos[i]&&!isFree[i])
                            allFree=false;
                    }
                }while(allFree&&newCurrent<allSets);
                currentSet=newCurrent;
                */
            }
        }
        return result;
    }
    /**
     * Retrieve all the frames in a <tt>FrameSet</tt> which type is a
     * subtype of a given type.
     * @param type the parent type.
     * @param ontology the ontology where to check for the relations.
     * @param frames the frameset to look for the data.
     * @return a list of frames.
     */
    protected static List framesUnderType(Object type,Ontology ontology,FrameSet frames){
        List result=new ArrayList();
        Iterator s=frames.getFrames().iterator();
        while(s.hasNext()){
            Frame frame=(Frame)s.next();
            if(ontology.isA(frame.getType(),type))
                result.add(frame);
        }
        return result;
    }
    /**
     * Instantiate a predicate by fetching the values from the cache
     * and resolving variables in the output FD of the predicate.
     * @param node the node being instantiated.
     * @param varMapping the global variables.
     * @param commit whether or not to remove the values from the
     * cache.
     * @return the instantiated FD.
     */
    protected FD instantiatePredicate(Node node,Map varMapping,Cache cache,boolean commit){
        List values=cache.fetch(node,varMapping); // cache access
        if(values==null) // cache miss is not an option at this point
            throw new IllegalStateException("Trying to instantiate an uninitialized node.");
        if(values.isEmpty()) // neither an empty node
            throw new IllegalStateException("Trying to instantiate an exhaust node.");
        Map valueMapping=
            commit?(Map)values.remove(0):(Map)values.get(0);
        if(commit){
            Iterator e=node.getVars().entrySet().iterator();
            while(e.hasNext()){
                Map.Entry entry=(Map.Entry)e.next();
                Object predVar=entry.getKey();
                Object globalVar=entry.getValue();
                varMapping.put(globalVar,valueMapping.get(predVar));
            }
        }
        return instantiateClause(node.getPredicate().getOutput(),valueMapping);
    }
    /**
     * Instantiate a given FD by changing all the variable references
     * via a provided variable mapping. Recursive function.
     * @param fd the functional description to operate on.
     * @param valueMapping variable-to-value mapping.
     * @return a new functional description, with the variables
     * replaced.
     */
    protected FD instantiateClause(FD fd,Map valueMapping){
        FD result=new FDH();
        Iterator e=fd.entrySet().iterator();
        while(e.hasNext()){
            Map.Entry entry=(Map.Entry)e.next();
            Object value=entry.getValue();
            if(value instanceof FD)
                value=instantiateClause((FD)value,valueMapping);
            else if(value instanceof VarRef){
                Frame frame=(Frame)valueMapping.get(((VarRef)value).getRef());
                if(value instanceof PathRef){
                    List list=new ArrayList();
                    list.add(frame);
                    String[]path=((PathRef)value).getPath();
                    for(int i=0;i<path.length;i++){
                        Iterator l=list.iterator();
                        list=new ArrayList();
                        while(l.hasNext()){
                            Object o=l.next();
                            if(o instanceof Frame)
                                list.addAll(((Frame)o).get(path[i]));
                        }
                    }
                    if(list.size()==0)
                        value=null;
                    else if(list.size()==1)
                        value=list.get(0);
                    else
                        value=new FDDistinctH(list);
                }else
                    value=frame;
            }
            if(value instanceof Frame){
                FD pair=new FDH();
                pair.put("object-id",((Frame)value).getID());
                value=pair;
            }
            result.put(entry.getKey(),value);
        }
        return result;
    }
    /**
     * Construct the nodes network from a JAXB generated schema class.
     * @param schema the JAXB class, used to deal with the XML.
     */
    protected void assembleSchema(net.sf.openschema.OpenSchema schema){
        // start with the predicates
        this.predicates=new HashMap();
        Iterator p=schema.getPredicate().iterator();
        while(p.hasNext()){
            net.sf.openschema.OpenSchemaType.PredicateType predicateType=
                (net.sf.openschema.OpenSchemaType.PredicateType)p.next();
            String id=predicateType.getID();
            String defaultFocus=null;
            Map vars=new HashMap();
            Set requiredVars=new HashSet();
            Iterator v=predicateType.getVariable().iterator();
            while(v.hasNext()){
                net.sf.openschema.OpenSchemaType.PredicateType.VariableType variableType=
                    (net.sf.openschema.OpenSchemaType.PredicateType.VariableType)v.next();
                if(defaultFocus==null||variableType.isDefaultFocus())
                    defaultFocus=variableType.getID();
                vars.put(variableType.getID(),variableType.getType());
                if(variableType.isRequired())
                    requiredVars.add(variableType.getID());
            }
            List properties=new ArrayList();
            Iterator pr=predicateType.getProperty().iterator();
            while(pr.hasNext())
                properties.add(Property.parse
                               (((net.sf.openschema.OpenSchemaType.PredicateType.PropertyType)
                                 pr.next()).getValue(),
                                vars.keySet()));
            FD output=assembleFD(predicateType.getOutput().getFD().getV(),vars.keySet());
            predicates.put(predicateType.getID(),new Predicate(id,defaultFocus,vars,
                                                               requiredVars,properties,output));
        }
        // now build the state machine
        this.top=new Node();
        Node exitNode=new Node();
        build(top,exitNode,schema.getSchema().getNode(),false);
    }
    /**
     * Create a functional description from the JAXB code. All defined
     * variables are changed for <tt>VarRef</tt> or <tt>PathRef</tt>,
     * accordingly.
     * @param vs the JAXB instances being processed.
     * @param vars predicate variables, to replace the variable
     * references.
     * @return a newly created FD.
     */
    protected FD assembleFD(List vs,Set vars){
        FD result=new FDH();
        Iterator v=vs.iterator();
        while(v.hasNext()){
            AttributeValuePair avPair=(AttributeValuePair)v.next();
            String key=avPair.getN();
            Object value=null;
            if(avPair.getG()==null)
                value=assembleFD(avPair.getV(),vars);
            else{
                String ground=avPair.getG();
                if(vars.contains(ground))
                    value=new VarRef(ground);
                else if(ground.indexOf('.')!=-1){
                    PathRef pathRef=PathRef.parse(ground);
                    if(vars.contains(pathRef.getRef()))
                        value=pathRef;
                    else
                        value=ground;
                }else
                    value=ground;
            }
            result.put(key,value);
        }
        return result;
    }
    /**
     * Construct the network for a given construction in the schema
     * language. Most constructions can be taken as a sequence with an
     * input node and an output node, but the choices. A flag signals
     * whether to construct a choice or otherwise.
     * @param inNode the input to this subnetwork.
     * @param outNode the output of the subnetwork.
     * @param schemaNodes the JAXB instances for this construction.
     * @param isChoice whether to link each node one after the other
     * or all from inNode to outNode.
     */
    protected void build(Node inNode,Node outNode,List schemaNodes,boolean isChoice){
        Iterator n=schemaNodes.iterator();
        Node previous=inNode;
        while(n.hasNext()){
            SchemaNode.NodeType nodeType=(SchemaNode.NodeType)n.next();
            Node current=null;
            // resolve according to node type. Most likely recurse.
            if(nodeType.getPredicate()!=null){
                SchemaNode.NodeType.PredicateType predicateType=nodeType.getPredicate();
                Map vars=new HashMap();
                Iterator v=predicateType.getVariable().iterator();
                while(v.hasNext()){
                    SchemaNode.NodeType.PredicateType.VariableType variableType=
                        (SchemaNode.NodeType.PredicateType.VariableType)v.next();
                    vars.put(variableType.getName(),variableType.getValue());
                }
                
                current=new Node((Predicate)predicates.get(predicateType.getName()),vars);
                previous.linkTo(current);
            }else if(nodeType.getAggrBoundary()!=null){
                current=new Node(AGGRBOUNDARY_NODE);
                previous.linkTo(current);
            }else if(nodeType.getParBoundary()!=null){
                current=new Node(PARBOUNDARY_NODE);
                previous.linkTo(current);
            }else if(nodeType.getSequence()!=null||
                     nodeType.getOptional()!=null||
                     nodeType.getKleeneStar()!=null||
                     nodeType.getKleenePlus()!=null){
                current=new Node();
                build(previous,current,
                      (nodeType.getSequence()!=null?
                       nodeType.getSequence().getNode():
                       (nodeType.getKleeneStar()!=null?
                        nodeType.getKleeneStar().getNode():
                        (nodeType.getKleenePlus()!=null?
                         nodeType.getKleenePlus().getNode():
                         nodeType.getOptional().getNode()))),false);
                if(nodeType.getKleeneStar()!=null||
                     nodeType.getKleenePlus()!=null)
                    current.linkTo(previous);
                if(nodeType.getKleeneStar()!=null||
                   nodeType.getOptional()!=null)
                    previous.linkTo(current);
            }else if(nodeType.getChoice()!=null){
                current=new Node();
                build(previous,current,nodeType.getChoice().getNode(),true);
            }else{
                current=new Node();
            }
            if(!isChoice){
                // create a buffer between each element of the
                // sequence to avoid far-reaching backward jumps
                Node sequence=new Node();
                current.linkTo(sequence);
                previous=sequence;
            }else
                current.linkTo(outNode);
        }
        if(!isChoice)
            previous.linkTo(outNode);
    }
    /** Show the network (for debugging purposes) */
    public String dump(){
        StringBuffer result=new StringBuffer();
        dump(result,top,new HashSet());
        return result.toString();
    }
    /** Recursive method for dump. */
    protected void dump(StringBuffer dump,Node current,Set seen){
        if(seen.contains(current))return;
        seen.add(current);
        dump.append(current.toString());
        dump.append("\n");
        dump.append("connects to:\n");
        List outGoing=current.getOutgoing();
        Iterator og=outGoing.iterator();
        while(og.hasNext()){
            dump.append("\t");
            dump.append(og.next());
            dump.append("\n");
        }
        Iterator n=outGoing.iterator();
        while(n.hasNext())
            dump(dump,(Node)n.next(),seen);
    }
    /** Predicate inner class */
    protected class Predicate{
        /** Predicate ID. */
        public String id;
        /** Default focus (variable name). */
        public String defaultFocus;
        /** Mapping from local variable names to types. */
        public Map vars;
        /** Set of names of required variables. */
        public Set requiredVars;
        /** Properties to check upon the variables. */
        public List properties;
        /** FD to use as clause, after changing the variables to
         * actual values. */
        public FD output;
        /** Full constructor, this is an immutable class. */
        public Predicate(String id,String defaultFocus,
                         Map vars,Set requiredVars,List properties,FD output){
            this.id=id;
            this.defaultFocus=defaultFocus;
            this.vars=vars;
            this.requiredVars=requiredVars;
            this.properties=properties;
            this.output=output;
        }
        /** ID accessor. */
        public String getID(){return id;}
        /** Default focus (variable name) accessor. */
        public String getDefaultFocus(){return defaultFocus;}
        /** Accessor to the FD to be used as clause, after changing the variables to
         * actual values. */
        public FD getOutput(){return this.output;}
        /** Mapping from variable names to types. */
        public Map getVars(){return this.vars;}
        /** Properties that the variables should hold. */
        public List getProperties(){return this.properties;}
    }
    // NODE TYPE CONSTANTS
    /** Empty node, only used for structural purposes. */
    protected static final int EMPTY_NODE=0;
    /** Predicate node, contains a predicate reference plus
     * global-to-local mapping. */
    protected static final int PREDICATE_NODE=1;
    /** Aggregation boundary node. */
    protected static final int AGGRBOUNDARY_NODE=2;
    /** Paragraph boundary node. */
    protected static final int PARBOUNDARY_NODE=3;
    /** Node inner class. */
    protected class Node{
        /** Type of the node (EMPTY_NODE, PREDICATE_NODE, etc.). */
        public int type;
        /** Nodes that can be accessed from the current one. */
        public List outgoing;
        /** Predicate held by this node (if any). */
        public Predicate predicate;
        /** Mapping from global-to-local variable names (if any). */
        public Map vars;
        /** Create an EMPTY_NODE. */
        public Node(){
            this(EMPTY_NODE);
        }
        /** Create a node of a given type. */
        public Node(int type){
            this.type=type;
            this.outgoing=new LinkedList();
            this.predicate=null;
            this.vars=null;
        }
        /** Create a PREDICATE_NODE. */
        public Node(Predicate predicate,Map vars){
            this(PREDICATE_NODE);
            this.predicate=predicate;
            this.vars=vars;
        }
        /** Add a link from the current node to the destination
         * node. */
        public void linkTo(Node destination){
            outgoing.add(destination);
        }
        /** Node type accessor. */
        public int getType(){return this.type;}
        /** Check whether the type of the node is PREDICATE_NODE. */
        public boolean isPredicate(){return this.type==PREDICATE_NODE;}
        /** Check whether the type of the node is AGGRBOUNDARY_NODE. */
        public boolean isAggrBoundary(){return this.type==AGGRBOUNDARY_NODE;}
        /** Check  whether the type of the node is PARBOUNDARY_NODE. */
        public boolean isParBoundary(){return this.type==PARBOUNDARY_NODE;}
        /** Access all nodes accessable from this node. */
        public List getOutgoing(){return this.outgoing;}
        /** Access the global-to-local variable names mapping (if any). */
        public Map getVars(){return this.vars;}
        /** Access the predicate referenced by this node (if any). */
        public Predicate getPredicate(){return this.predicate;}
        /** String rendering (for debugging purposes). */
        public String toString(){
            return "Node@"+Integer.toHexString(super.hashCode())+"["+(type==PREDICATE_NODE?"PRED":
                            (type==AGGRBOUNDARY_NODE?"AGGR":
                             (type==PARBOUNDARY_NODE?"PAR":
                              (type==EMPTY_NODE?"EMPTY":"?"))))+
                "/"+outgoing.size()+(predicate!=null?"|"+predicate.getID()+":"+vars:"")+"]";
        }
    }
    /** A decorated node inner class. */
    protected static class DecoratedNode{
        /** The node being decorated. */
        protected Node node;
        /** Crossed an aggregation boundary? */
        protected boolean aggrBoundary;
        /** Crossed a paragraph  boundary? */
        protected boolean parBoundary;
        /** Default constructor */
        public DecoratedNode(Node node){
            this(node,false,false);
        }
        /** Initialized constructor. */
        public DecoratedNode(Node node,boolean aggrBoundary,boolean parBoundary){
            this.node=node;
            this.aggrBoundary=aggrBoundary;
            this.parBoundary=parBoundary;
        }
        /** Get the node being decorated. */
        public Node getNode(){return node;}
        /** Whether or not the path to get to the decorated node
         * crossed an aggregation boundary. */
        public boolean isAggrBoundary(){return aggrBoundary;}
        /** Whether or not the path to get to the decorated node
         * crossed a paragraph boundary. */
        public boolean isParBoundary(){return parBoundary;}
        /** String rendering (for debugging purposes). */
        public String toString(){return "Decorated"+node.toString()+(aggrBoundary?"{A}":"")+
                                     (parBoundary?"{P}":"");}
        public int hashCode(){return 3*node.hashCode()+(aggrBoundary?1:0)+(parBoundary?7:0);}
        public boolean equals(Object o){
            if(!(o instanceof DecoratedNode))return false;
            DecoratedNode other=(DecoratedNode)o;
            if(!other.node.equals(this.node))return false;
            if(other.aggrBoundary!=this.aggrBoundary)return false;
            if(other.parBoundary!=this.parBoundary)return false;
            return true;
        }
    }
    /** Values cache. */
    protected static class Cache{
        /** The cache itself, as a map from nodes to Entry. */
        protected Map cache;
        /** Construct an empty cache. */
        public Cache(){
            this.cache=new HashMap();
        }
        /** Entry inner class */
        protected static class Entry{
            /** The node which values are being cached. */
            protected Node node;
            /** The mapping from global variable names to values. */
            protected Map vars;
            /** The values that satisty the predicate properties. */
            protected List values;
            /** Construct a new entry for a given node and global variables. */
            public Entry(Node node,Map vars){
                this.node=node;
                this.values=null;
                this.vars=new HashMap();
                // look for which variables from the global mapping
                // are associated by the predicate in this node and
                // record the association in the entry
                Iterator v=node.getVars().values().iterator();
                while(v.hasNext()){
                    Object globalVar=v.next();
                    if(vars.containsKey(globalVar))
                        this.vars.put(globalVar,vars.get(globalVar));
                }
            }
            /** Check when the node and the variables are the same. */
            public boolean equals(Node node,Map vars){
                if(this.node!=node)
                    return false;
                Iterator e=this.vars.entrySet().iterator();
                while(e.hasNext()){
                    Map.Entry entry=(Map.Entry)e.next();
                    if(!vars.containsKey(entry.getKey()))
                        return false;
                    if(!vars.get(entry.getKey()).equals(entry.getValue()))
                        return false;
                }
                return true;
            }
            /** Access the values list. */
            public List getValues(){return this.values;}
            /** Set the values list. */
            public void setValues(List values){this.values=values;}
        }
        /** Access the cache by searching for the list of values for
         * this node and variables. Returns null on miss. */
        public List fetch(Node node,Map vars){
            if(!cache.containsKey(node))
                cache.put(node,new LinkedList());
            List entries=(List)cache.get(node);
            Iterator e=entries.iterator();
            Entry theEntry=null;
            while(e.hasNext()){
                Entry entry=(Entry)e.next();
                if(entry.equals(node,vars)){
                    theEntry=entry;
                    break;
                }
            }
            if(theEntry==null){
                theEntry=new Entry(node,vars);
                entries.add(theEntry);
            }
            return theEntry.getValues();
        }
        /** Populate the cache entry for a node and variables with the
         * values. */
        public void populate(Node node,Map vars,List values){
            List entries=(List)cache.get(node);
            Iterator e=entries.iterator();
            while(e.hasNext()){
                Entry entry=(Entry)e.next();
                if(entry.equals(node,vars)){
                    entry.setValues(values);
                    return;
                }
            }
            throw new IllegalStateException("populate(..) executed before fetch(..).");
        }
    }
}
