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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.sf.jfuf.fd.AttributeValuePair;

import org.xml.sax.InputSource;

/**
 * Main OpenSchema class. The schema is created from an XML file. At construction, the strategy for selecting local
 * choices is also specified. Once created, the schema is instantiated with a frameset and an ontology. The result of an
 * instantiated schema is a document plan.
 * 
 * @author Pablo Ariel Duboue <pablo.duboue@gmail.com>
 */

public class OpenSchemaPlanner {
	/** Verbosity flag, defaults to off. */
	public static boolean verbose = true;
	/**
	 * Top-level node of the schema, here is where the instantiation starts. The whole network is constructed from the
	 * XML in the build method.
	 */
	protected Node top;
	/**
	 * Definition of the rhetorical predicates. The keys are names ( <tt>String</tt>), the values are instances of the
	 * inner class <tt>Predicate</tt>.
	 */
	protected Map<String, Predicate> predicates;
	/**
	 * LocalChooser, decides which node to continue the instantiation of the schema.
	 */
	protected LocalChooser chooser;

	protected static Frame EMPTY_FOCUS = new Frame() {

		@Override
		public Object getType() {
			return Boolean.TRUE;
		}

		@Override
		public void setType(Object type) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getID() {
			return "EMPTY_FOCUS";
		}

		@Override
		public void setID(String id) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<Object> get(String key) {
			return Collections.emptyList();
		}

		@Override
		public void set(String key, Object value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void set(String key, List<Object> values) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void add(String key, Object value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean containsKey(String key) {
			return false;
		}

		@Override
		public Set<String> keySet() {
			return Collections.emptySet();
		}

	};

	/**
	 * Constructor from an SAX <tt>InputSource</tt>.
	 * 
	 * @param source
	 *            A SAX <tt>InputSource</tt>, can read from a <tt>Reader</tt> or an <tt>InputStream</tt>.
	 * @param chooser
	 *            An instance of the abstract class <tt>LocalChooser</tt>, it should select among the different
	 *            continuation nodes during instantiation.
	 */
	public OpenSchemaPlanner(InputSource source, LocalChooser chooser) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance("net.sf.openschema:net.sf.jfuf.fd");
		Unmarshaller unmarshaller = context.createUnmarshaller();
		assembleSchema((net.sf.openschema.OpenSchema) unmarshaller.unmarshal(source));
		this.chooser = chooser;
	}

	/**
	 * Main method, takes data to instantiate the schema, an initial variable mapping and an ontology and returns a
	 * DocumentPlan.
	 * 
	 * @param frames
	 *            a class implementing the <tt>FrameSet</tt> interface, for example <tt>RDFFrameSet</tt> containing the
	 *            data to fill the schema.
	 * @param initialMapping
	 *            a mapping from variable names to values (<tt>Frame</tt>s).
	 * @param ontology
	 *            a class implementing the <tt>Ontology</tt> interface, for example <tt>RDFOntology</tt>.
	 */
	public DocumentPlan instantiate(FrameSet frames, Map<String, Frame> initialMapping, Ontology ontology) {
		// initialize variables
		DocumentPlan result = new DocumentPlan();

		Cache cache = new Cache();
		Map<String, Frame> varMapping = new HashMap<String, Frame>(initialMapping);
		List<Frame> focusHistory = new ArrayList<Frame>();
		Frame currentFocus = initialMapping.size() == 0 ? EMPTY_FOCUS : initialMapping.values().iterator().next();
		List<Frame> potentialFoci = new ArrayList<Frame>();

		// main cycle
		Node currentNode = top;
		while (currentNode != null) {
			if (verbose)
				System.err.println("CURRENT NODE=" + currentNode);
			// find which nodes are reachable from current node
			List<DecoratedNode> confusionSet = computeConfusionSet(currentNode, varMapping, cache, ontology, frames);
			if (verbose) {
				System.err.println("confusionSet.size()==" + confusionSet.size());
				if (confusionSet.size() > 1)
					System.err.println(confusionSet);
			}
			if (confusionSet.isEmpty()) {
				currentNode = null;
				break;
			}

			// transform the confusion set to something suitable for the chooser to process
			List<Map<String, Object>> fds = new ArrayList<Map<String, Object>>(confusionSet.size());
			List<Frame> defaultFoci = new ArrayList<Frame>(confusionSet.size());
			// extract the FDs and the default focus for the decorated nodes (nodes decorated with aggr. and par.
			// boundaries)
			for (DecoratedNode decoratedNode : confusionSet) {
				List<Map<Object, Frame>> values = cache.fetch(decoratedNode.getNode(), varMapping);
				Map<Object, Frame> valueMapping = values.get(0);

				fds.add(instantiateClause(decoratedNode.getNode().getPredicate().getOutput(), valueMapping));
				defaultFoci.add(valueMapping.get(decoratedNode.getNode().getPredicate().getDefaultFocus()));
			}
			// choose
			LocalChooser.Decision decision = chooser.choose(fds, defaultFoci, currentFocus, potentialFoci,
					focusHistory, frames);
			// understand and process the decision
			DecoratedNode decoratedNode = confusionSet.get(decision.getPosition());
			currentFocus = decision.getCurrentFocus();
			potentialFoci = decision.getPotentialFoci();
			int positionInHistory = focusHistory.lastIndexOf(currentFocus);
			if (positionInHistory == -1)
				focusHistory.add(currentFocus);
			else
				focusHistory = focusHistory.subList(0, positionInHistory + 1);

			// create the clause
			Map<String, Object> clause = instantiatePredicate(decoratedNode.getNode(), varMapping, cache, true);

			if (decoratedNode.isAggrBoundary())
				result.addAggrBoundary();
			if (decoratedNode.isParBoundary())
				result.addParBoundary();

			currentNode = decoratedNode.getNode();

			// add the clause
			if (verbose)
				System.err.println("Adding clause: " + clause);
			// (focus info is recorded as it may be used by referring expression generators, etc.)
			clause.put("focus", ((Frame) currentFocus).getID());
			List<Map<String, Object>> focusStack = new ArrayList<Map<String, Object>>();
			for (int i = focusHistory.size() - 1; i >= 0; i--) {
				Map<String, Object> focusPair = new HashMap<String, Object>();
				focusPair.put("focus", ((Frame) focusHistory.get(i)).getID());
				focusStack.add(focusPair);
			}
			clause.put("focus-stack", focusStack);
			List<Map<String, Object>> potentialFocusList = new ArrayList<Map<String, Object>>();
			for (int i = 0; i < potentialFoci.size(); i++) {
				Map<String, Object> focusPair = new HashMap<String, Object>();
				focusPair.put("focus", ((Frame) potentialFoci.get(i)).getID());
				potentialFocusList.add(focusPair);
			}
			clause.put("potential-focus-list", potentialFocusList);

			result.addClause(clause);
		}
		return result;
	}

	/**
	 * Compute all the nodes that are reachable from current node without passing through another Predicate node. The
	 * possible nodes should be able to be instantiated, that is to say, there must be sets of values satisfying the
	 * node's predicate properties in the cache. This method invokes the <tt>canBeInstantiated</tt> method, that will
	 * fill the cache the first time it is executed.
	 * 
	 * @param node
	 *            the current node to start the spanning tree.
	 * @param varMapping
	 *            the current variable-values mapping.
	 * @param cache
	 *            the current values cache.
	 * @param ontology
	 *            used to update the cache.
	 * @param frames
	 *            used to update the cache.
	 * @return a list of <tt>DecoratedNode</tt> that can be reached and instantiated from the current node.
	 */
	protected List<DecoratedNode> computeConfusionSet(Node node, Map<String, Frame> varMapping, Cache cache,
			Ontology ontology, FrameSet frames) {
		List<DecoratedNode> result = new ArrayList<DecoratedNode>();

		// enumerate boundary nodes
		List<DecoratedNode> boundaryNodes = new LinkedList<DecoratedNode>();
		Set<DecoratedNode> seenNodes = new HashSet<DecoratedNode>();
		List<DecoratedNode> newNodes = new ArrayList<DecoratedNode>();
		// newNodes.add(new DecoratedNode(node));
		for (Node og : node.getOutgoing())
			newNodes.add(new DecoratedNode(og));

		while (!newNodes.isEmpty()) {
			DecoratedNode currentNode = newNodes.remove(0);
			if (seenNodes.contains(currentNode))
				continue;
			seenNodes.add(currentNode);
			boolean isPar = currentNode.isParBoundary();
			boolean isAggr = currentNode.isAggrBoundary();
			if (currentNode.getNode().isPredicate())
				boundaryNodes.add(currentNode);
			if (currentNode.getNode().isAggrBoundary())
				isAggr = true;
			if (currentNode.getNode().isParBoundary())
				isPar = true;
			if (!currentNode.getNode().isPredicate()) {
				for (Node o : currentNode.getNode().getOutgoing())
					newNodes.add(new DecoratedNode(o, isAggr, isPar));
			}
		}
		if (verbose)
			System.err.println("Checking " + boundaryNodes.size() + "...");
		// check whether they can be instantiated
		for (DecoratedNode decoratedNode : boundaryNodes)
			if (canBeInstantiated(decoratedNode.getNode(), varMapping, cache, ontology, frames))
				result.add(decoratedNode);

		return result;
	}

	/**
	 * Check whether there is valid data to instantiate a node or not, given an initial variable mapping. This function
	 * relies heavily in the cache, and will populate it in case of cache miss.
	 * 
	 * @param node
	 *            the current node, to analyze the predicate and global-to-local variable mapping.
	 * @param varMapping
	 *            the global variables.
	 * @param cache
	 *            the cache to be queried/populated.
	 * @param ontology
	 *            used to populate the cache.
	 * @param frames
	 *            used to populate the cache.
	 * @return whether or not there's data to instantiate this node.
	 */
	protected boolean canBeInstantiated(Node node, Map<String, Frame> varMapping, Cache cache, Ontology ontology,
			FrameSet frames) {
		List<Map<Object, Frame>> values = cache.fetch(node, varMapping);
		if (values == null) { // cache miss, populate
			// search for values
			values = searchValues(node, varMapping, ontology, frames);
			if (verbose)
				System.err.println("Values found: " + values.size());
			cache.populate(node, varMapping, values); // populate
		}
		return !values.isEmpty();
	}

	/**
	 * Search for values that satisfy the node properties and the given mapping of variables.
	 * 
	 * @param node
	 *            the schema node, containing the properties that the values should satisfy and the mapping between
	 *            local and global variables.
	 * @param varMapping
	 *            global variables, mapping from name to value.
	 * @param ontology
	 *            the ontology, employed to restrict the search over values of a certain type.
	 * @param frames
	 *            the set of frames over where to perform the search.
	 * @return List of associations of local variables to values.
	 * @see {edu.columbia.openschema.OpenSchema.Node.Property}
	 */
	protected List<Map<Object, Frame>> searchValues(Node node, Map<String, Frame> varMapping, Ontology ontology,
			FrameSet frames) {
		// key=name of the local variable
		// value=Set of possible values the variable can take
		if (verbose) {
			System.err.println("searchValues:");
			for (Map.Entry<String, Frame> entry : varMapping.entrySet())
				System.err.println("\t" + entry.getKey() + "="
						+ (entry.getValue() == null ? "null" : ((Frame) entry.getValue()).getID()));
		}

		Map<Object, List<Frame>> allVars = new HashMap<Object, List<Frame>>();
		// initialize allVars with the global vars, if given
		Map<String, String> nodeVars = node.getVars();
		for (Map.Entry<String, String> entry : nodeVars.entrySet()) {
			Object predVar = entry.getKey();
			Object globalVar = entry.getValue();
			if (varMapping.containsKey(globalVar))
				allVars.put(predVar, Collections.singletonList(varMapping.get(globalVar)));
		}
		// or all the values of the target type, otherwise
		if (node.getPredicate() == null)
			System.out.println(node);
		Map<String, String> predVars = node.getPredicate().getVars();
		for (Map.Entry<String, String> entry : predVars.entrySet()) {
			Object predVar = entry.getKey();
			Object type = entry.getValue();
			List<Frame> l = framesUnderType(type, ontology, frames);
			if (verbose) {
				System.err.println("Frames under '" + type + "'");
				for (Frame ff : l)
					System.err.println("\t" + ff.getID());
			}
			if (!allVars.containsKey(predVar))
				allVars.put(predVar, framesUnderType(type, ontology, frames));
		}
		// now check the predicates over the set multiplication of all
		// sets on the variable mappings and filter the ones who
		// satisfy the properties
		long allSets = 1;
		Object[] varOrder = allVars.keySet().toArray();
		int[] allVarSize = new int[varOrder.length];
		long[] setMultiplier = new long[varOrder.length];
		for (int i = 0; i < varOrder.length; i++) {
			setMultiplier[i] = allSets;
			allVarSize[i] = allVars.get(varOrder[i]).size();
			allSets = allSets * (long) allVarSize[i];
		}
		long currentSet = 0;
		// reuse Map to reduce GC
		Map<Object, Frame> currentAssignment = new HashMap<Object, Frame>();
		Set<Property> failedProperties = new HashSet<Property>();
		// reuse Set to reduce GC
		int[] currentPos = new int[varOrder.length]; // reuse array to reduce GC
		// int[] otherPos = new int[varOrder.length]; // reuse array to reduce
		// GC
		// reuse array to reduce GC
		// boolean[] isFree = new boolean[varOrder.length];

		List<Map<Object, Frame>> result = new LinkedList<Map<Object, Frame>>();
		while (currentSet < allSets) {
			// make the map corresponding to the number 'currentSet'
			currentAssignment.clear();
			long leftOver2 = currentSet;
			for (int i = varOrder.length - 1; i >= 0; i--) {
				int pos = (int) (leftOver2 % allVarSize[i]);
				leftOver2 = leftOver2 / allVarSize[i];
				currentPos[i] = pos;
				currentAssignment.put(varOrder[i], allVars.get(varOrder[i]).get(pos));
			}

			// now see if it checks
			failedProperties.clear();
			for (Property property : node.getPredicate().getProperties()) {
				if (verbose) {
					System.err.println("Checking property: " + property);
					for (Map.Entry<Object, Frame> entry : currentAssignment.entrySet())
						System.err.println("\t" + entry.getKey() + "="
								+ (entry.getValue() == null ? "null" : ((Frame) entry.getValue()).getID()));
				}
				if (!property.check(currentAssignment, ontology)) {
					failedProperties.add(property);
					if (verbose)
						System.err.println("\t\tFALSE");
				} else if (verbose)
					System.err.println("\t\tTRUE");
			} // look for the failed property that will advance
			if (failedProperties.isEmpty()) { // good assignment, output
				result.add(new HashMap<Object, Frame>(currentAssignment));
				currentSet++;
			} else {
				currentSet++;
				/*
				 * // look for the failed property that will advance more // positions Property pickedProperty=null;
				 * long toAdvance=0; Iterator fp=failedProperties.iterator(); while(fp.hasNext()){ Property
				 * property=(Property)fp.next(); long thisToAdvance=1; Set propertyVariables=property.variables();
				 * for(int i=0;i<varOrder.length;i++) if(!propertyVariables.contains(varOrder[i]))
				 * thisToAdvance=thisToAdvance*(long)allVarSize[i]; if(thisToAdvance>toAdvance) pickedProperty=property;
				 * } // now advance all the positions that can be done with // this ordering Set freeVariables=new
				 * HashSet(allVars.keySet()); freeVariables.removeAll(pickedProperty.variables()); for(int
				 * i=0;i<varOrder.length;i++) isFree[i]=freeVariables.contains(varOrder[i]); boolean allFree=true; long
				 * newCurrent=currentSet; do{ newCurrent++; long leftOver3=currentSet; for(int
				 * i=varOrder.length-1;i>=0;i--){ int pos=(int) (leftOver3%allVarSize[i]);
				 * leftOver3=leftOver3/allVarSize[i]; if(pos!=currentPos[i]&&!isFree[i]) allFree=false; }
				 * }while(allFree&&newCurrent<allSets); currentSet=newCurrent;
				 */
			}
		}
		// order the associations somehow (alphabetically by now

		Collections.sort(result, new Comparator<Map<Object, Frame>>() {

			private Map<Integer, String> cache = new HashMap<Integer, String>();

			private String mapToString(Map<Object, Frame> m) {
				int hash = System.identityHashCode(m);
				if (cache.containsKey(m))
					return cache.get(m);
				List<Object> allKeys = new ArrayList<Object>(m.keySet());
				Collections.sort(allKeys, new Comparator<Object>() {
					@Override
					public int compare(Object o1, Object o2) {
						return o1.toString().compareTo(o2.toString());
					}
				});
				StringBuilder sb = new StringBuilder();
				for (Object k : allKeys)
					sb.append(k).append('=').append(m.get(k).getID()).append(';');
				String result = sb.toString();
				cache.put(hash, result);
				return result;
			}

			@Override
			public int compare(Map<Object, Frame> o1, Map<Object, Frame> o2) {
				return mapToString(o1).compareTo(mapToString(o2));
			}
		});

		return result;
	}

	/**
	 * Retrieve all the frames in a <tt>FrameSet</tt> which type is a subtype of a given type.
	 * 
	 * @param type
	 *            the parent type.
	 * @param ontology
	 *            the ontology where to check for the relations.
	 * @param frames
	 *            the frameset to look for the data.
	 * @return a list of frames.
	 */
	protected static List<Frame> framesUnderType(Object type, Ontology ontology, FrameSet frames) {
		List<Frame> result = new ArrayList<Frame>();
		for (Frame frame : frames.getFrames())
			if (ontology.isA(frame.getType(), type))
				result.add(frame);

		return result;
	}

	/**
	 * Instantiate a predicate by fetching the values from the cache and resolving variables in the output FD of the
	 * predicate.
	 * 
	 * @param node
	 *            the node being instantiated.
	 * @param varMapping
	 *            the global variables.
	 * @param commit
	 *            whether or not to remove the values from the cache.
	 * @return the instantiated FD.
	 */
	protected Map<String, Object> instantiatePredicate(Node node, Map<String, Frame> varMapping, Cache cache,
			boolean commit) {
		List<Map<Object, Frame>> values = cache.fetch(node, varMapping); // cache
																			// access
		if (values == null) // cache miss is not an option at this point
			throw new IllegalStateException("Trying to instantiate an uninitialized node.");
		if (values.isEmpty()) // neither an empty node
			throw new IllegalStateException("Trying to instantiate an exhaust node.");
		Map<Object, Frame> valueMapping = commit ? values.remove(0) : values.get(0);
		if (commit) {
			for (Map.Entry<String, String> entry : node.getVars().entrySet()) {
				String predVar = entry.getKey();
				String globalVar = entry.getValue();
				varMapping.put(globalVar, valueMapping.get(predVar));
			}
		}
		return instantiateClause(node.getPredicate().getOutput(), valueMapping);
	}

	/**
	 * Instantiate a given FD by changing all the variable references via a provided variable mapping. Recursive
	 * function.
	 * 
	 * @param fd
	 *            the functional description to operate on.
	 * @param valueMapping
	 *            variable-to-value mapping.
	 * @return a new functional description, with the variables replaced.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Map<String, Object> instantiateClause(Map<String, Object> fd, Map<Object, Frame> valueMapping) {
		Map<String, Object> result = new HashMap<String, Object>();
		for (Map.Entry<String, Object> entry : fd.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof Map)
				value = instantiateClause((Map<String, Object>) value, valueMapping);
			else if (value instanceof VarRef) {
				Frame frame = valueMapping.get(((VarRef) value).getRef());
				if (value instanceof PathRef) {
					List<Object> list = new ArrayList<Object>();
					list.add(frame);
					String[] path = ((PathRef) value).getPath();
					for (int i = 0; i < path.length; i++) {
						// iterate over current frames
						List<Object> list2 = new ArrayList<Object>();
						for (Object o : list)
							if (o instanceof Frame)
								list2.addAll(((Frame) o).get(path[i]));
						list = list2;
					}
					if (list.size() == 0)
						value = null;
					else if (list.size() == 1)
						value = list.get(0);
					else
						value = new ArrayList<Map<String, Object>>((List) list);
				} else
					value = frame;
			}
			if (value instanceof Frame) {
				Map<String, Object> pair = new HashMap<String, Object>();
				pair.put("object-id", ((Frame) value).getID());
				value = pair;
			}
			result.put(entry.getKey(), value);
		}
		return result;
	}

	/**
	 * Construct the nodes network from a JAXB generated schema class.
	 * 
	 * @param schema
	 *            the JAXB class, used to deal with the XML.
	 */
	protected void assembleSchema(OpenSchema schema) {
		// start with the predicates
		this.predicates = new HashMap<String, Predicate>();
		for (OpenSchema.Predicate predicate : schema.getPredicate()) {
			String id = predicate.getID();
			String defaultFocus = null;
			Map<String, String> vars = new HashMap<String, String>();
			Set<String> requiredVars = new HashSet<String>();
			for (OpenSchema.Predicate.Variable variable : predicate.getVariable()) {
				if (defaultFocus == null || variable.isDefaultFocus())
					defaultFocus = variable.getID();
				vars.put(variable.getID(), variable.getType());
				if (variable.isRequired())
					requiredVars.add(variable.getID());
			}
			List<Property> properties = new ArrayList<Property>();
			for (OpenSchema.Predicate.Property property : predicate.getProperty())
				properties.add(Property.parse(property.getValue(), vars.keySet()));
			Map<String, Object> output = assembleFD(predicate.getOutput().getFD().getV(), vars.keySet());
			predicates.put(predicate.getID(), new Predicate(id, defaultFocus, vars, requiredVars, properties, output));
		}
		// now build the state machine
		this.top = new Node("top-");
		Node exitNode = new Node("exit-");
		build(top, exitNode, schema.getSchema().getNode(), false);
	}

	/**
	 * Create a functional description from the JAXB code. All defined variables are changed for <tt>VarRef</tt> or
	 * <tt>PathRef</tt>, accordingly.
	 * 
	 * @param vs
	 *            the JAXB instances being processed.
	 * @param vars
	 *            predicate variables, to replace the variable references.
	 * @return a newly created FD.
	 */
	protected Map<String, Object> assembleFD(List<AttributeValuePair> vs, Set<String> vars) {
		Map<String, Object> result = new HashMap<String, Object>();
		for (AttributeValuePair avPair : vs) {
			String key = avPair.getN();
			Object value = null;
			if (avPair.getG() == null)
				value = assembleFD(avPair.getV(), vars);
			else {
				String ground = avPair.getG();
				if (vars.contains(ground))
					value = new VarRef(ground);
				else if (ground.indexOf('.') != -1) {
					PathRef pathRef = PathRef.parse(ground);
					if (vars.contains(pathRef.getRef()))
						value = pathRef;
					else
						value = ground;
				} else
					value = ground;
			}
			result.put(key, value);
		}
		return result;
	}

	/**
	 * Construct the network for a given construction in the schema language. Most constructions can be taken as a
	 * sequence with an input node and an output node, but the choices. A flag signals whether to construct a choice or
	 * otherwise.
	 * 
	 * @param inNode
	 *            the input to this subnetwork.
	 * @param outNode
	 *            the output of the subnetwork.
	 * @param schemaNodes
	 *            the JAXB instances for this construction.
	 * @param isChoice
	 *            whether to link each node one after the other or all from inNode to outNode.
	 */
	protected void build(Node inNode, Node outNode, List<SchemaNode.Node> schemaNodes, boolean isChoice) {
		Node previous = inNode;
		for (SchemaNode.Node node : schemaNodes) {
			Node current = null;
			// resolve according to node type. Most likely recurse.
			if (node.getPredicate() != null) {
				SchemaNode.Node.Predicate predicate = node.getPredicate();
				Map<String, String> vars = new HashMap<String, String>();
				for (SchemaNode.Node.Predicate.Variable variable : predicate.getVariable())
					vars.put(variable.getName(), variable.getValue());

				current = new Node(predicates.get(predicate.getName()), vars);
				previous.linkTo(current);
			} else if (node.getAggrBoundary() != null) {
				current = new Node(AGGRBOUNDARY_NODE, "aggr-");
				previous.linkTo(current);
			} else if (node.getParBoundary() != null) {
				current = new Node(PARBOUNDARY_NODE, "par-");
				previous.linkTo(current);
			} else if (node.getSequence() != null || node.getOptional() != null || node.getKleeneStar() != null
					|| node.getKleenePlus() != null) {
				current = new Node("recurse-");
				List<SchemaNode.Node> recurse = null;
				if (node.getSequence() != null)
					recurse = node.getSequence().getNode();
				else if (node.getKleeneStar() != null)
					recurse = node.getKleeneStar().getNode();
				else if (node.getKleenePlus() != null)
					recurse = node.getKleenePlus().getNode();
				else
					recurse = node.getOptional().getNode();

				build(previous, current, recurse, false);
				if (node.getKleeneStar() != null || node.getKleenePlus() != null)
					current.linkTo(previous);
				if (node.getKleeneStar() != null || node.getOptional() != null)
					previous.linkTo(current);
			} else if (node.getChoice() != null) {
				current = new Node("option-");
				build(previous, current, node.getChoice().getNode(), true);
			} else {
				current = new Node("other-");
			}
			if (!isChoice) {
				// create a buffer between each element of the sequence to avoid
				// far-reaching backward jumps
				Node sequence = new Node("extra-");
				current.linkTo(sequence);
				previous = sequence;
			} else
				current.linkTo(outNode);
		}
		if (!isChoice)
			previous.linkTo(outNode);
	}

	/** Show the network (for debugging purposes) */
	public String dump() {
		return dump(false);
	}

	/** Show the network (for debugging purposes) */
	public String dump(boolean asDot) {
		StringBuilder result = new StringBuilder();
		if (asDot)
			result.append("digraph schema {\n");

		dump(result, top, new HashSet<Node>(), asDot);
		if (asDot)
			result.append("}\n");
		return result.toString();
	}

	/** Recursive method for dump. */
	protected void dump(StringBuilder dump, Node current, Set<Node> seen, boolean asDot) {
		if (seen.contains(current))
			return;
		seen.add(current);
		if (asDot) {
			dump.append("\tn_").append(current.name.replaceAll("-", "_")).append(" [label=\"");
			if (current.type == EMPTY_NODE)
				dump.append(current.name).append("\", shape=ellipse");
			else
				dump.append(current).append("\", shape=box");
			dump.append("];\n");
		} else {
			dump.append(current.toString());
			dump.append("\n");
			dump.append("connects to:\n");
		}
		List<Node> outGoing = current.getOutgoing();
		for (Node og : outGoing) {
			if (asDot)
				dump.append("\tn_").append(current.name.replaceAll("-", "_")).append("->n_")
						.append(og.name.replaceAll("-", "_")).append(";\n");
			else {
				dump.append("\t");
				dump.append(og);
				dump.append("\n");
			}
		}
		for (Node n : outGoing)
			dump(dump, n, seen, asDot);
	}

	/** Predicate inner class */
	protected class Predicate {
		/** Predicate ID. */
		public String id;
		/** Default focus (variable name). */
		public String defaultFocus;
		/** Mapping from local variable names to types. */
		public Map<String, String> vars;
		/** Set of names of required variables. */
		public Set<String> requiredVars;
		/** Properties to check upon the variables. */
		public List<Property> properties;
		/**
		 * FD to use as clause, after changing the variables to actual values.
		 */
		public Map<String, Object> output;

		/** Full constructor, this is an immutable class. */
		public Predicate(String id, String defaultFocus, Map<String, String> vars, Set<String> requiredVars,
				List<Property> properties, Map<String, Object> output) {
			this.id = id;
			this.defaultFocus = defaultFocus;
			this.vars = vars;
			this.requiredVars = requiredVars;
			this.properties = properties;
			this.output = output;
		}

		/** ID accessor. */
		public String getID() {
			return id;
		}

		/** Default focus (variable name) accessor. */
		public String getDefaultFocus() {
			return defaultFocus;
		}

		/**
		 * Accessor to the FD to be used as clause, after changing the variables to actual values.
		 */
		public Map<String, Object> getOutput() {
			return this.output;
		}

		/** Mapping from variable names to types. */
		public Map<String, String> getVars() {
			return this.vars;
		}

		/** Properties that the variables should hold. */
		public List<Property> getProperties() {
			return this.properties;
		}
	}

	// NODE TYPE CONSTANTS
	/** Empty node, only used for structural purposes. */
	protected static final int EMPTY_NODE = 0;
	/**
	 * Predicate node, contains a predicate reference plus global-to-local mapping.
	 */
	protected static final int PREDICATE_NODE = 1;
	/** Aggregation boundary node. */
	protected static final int AGGRBOUNDARY_NODE = 2;
	/** Paragraph boundary node. */
	protected static final int PARBOUNDARY_NODE = 3;

	private static int nodeCounter = 0;

	/** Node inner class. */
	protected class Node {

		/** Type of the node (EMPTY_NODE, PREDICATE_NODE, etc.). */
		public int type;
		/** Name, for debugging purposes */
		public String name;
		/** Nodes that can be accessed from the current one. */
		public List<Node> outgoing;
		/** Predicate held by this node (if any). */
		public Predicate predicate;
		/** Mapping from global-to-local variable names (if any). */
		public Map<String, String> vars;

		/** Create an EMPTY_NODE. */
		public Node() {
			this(EMPTY_NODE);
		}

		/** Create an EMPTY_NODE. */
		public Node(String name) {
			this(EMPTY_NODE, name);
		}

		/** Create a node of a given type. */
		public Node(int type) {
			this(type, "");
		}

		/** Create a node of a given type. */
		public Node(int type, String name) {
			this.type = type;
			this.outgoing = new LinkedList<Node>();
			this.predicate = null;
			this.vars = null;
			this.name = name + String.valueOf(nodeCounter++);
		}

		/** Create a PREDICATE_NODE. */
		public Node(Predicate predicate, Map<String, String> vars) {
			this(PREDICATE_NODE);
			this.predicate = predicate;
			this.vars = vars;
		}

		/**
		 * Add a link from the current node to the destination node.
		 */
		public void linkTo(Node destination) {
			outgoing.add(destination);
		}

		/** Node type accessor. */
		public int getType() {
			return this.type;
		}

		/** Check whether the type of the node is PREDICATE_NODE. */
		public boolean isPredicate() {
			return this.type == PREDICATE_NODE;
		}

		/** Check whether the type of the node is AGGRBOUNDARY_NODE. */
		public boolean isAggrBoundary() {
			return this.type == AGGRBOUNDARY_NODE;
		}

		/** Check whether the type of the node is PARBOUNDARY_NODE. */
		public boolean isParBoundary() {
			return this.type == PARBOUNDARY_NODE;
		}

		/** Access all nodes accessable from this node. */
		public List<Node> getOutgoing() {
			return this.outgoing;
		}

		/** Access the global-to-local variable names mapping (if any). */
		public Map<String, String> getVars() {
			return this.vars;
		}

		/** Access the predicate referenced by this node (if any). */
		public Predicate getPredicate() {
			return this.predicate;
		}

		/** String rendering (for debugging purposes). */
		public String toString() {
			return "Node@"
					+ name
					+ "["
					+ (type == PREDICATE_NODE ? "PRED" : (type == AGGRBOUNDARY_NODE ? "AGGR"
							: (type == PARBOUNDARY_NODE ? "PAR" : (type == EMPTY_NODE ? "EMPTY" : "?")))) + "/"
					+ outgoing.size() + (predicate != null ? "|" + predicate.getID() + ":" + vars : "") + "]";
		}
	}

	/** A decorated node inner class. */
	protected static class DecoratedNode {
		/** The node being decorated. */
		protected Node node;
		/** Crossed an aggregation boundary? */
		protected boolean aggrBoundary;
		/** Crossed a paragraph boundary? */
		protected boolean parBoundary;

		/** Default constructor */
		public DecoratedNode(Node node) {
			this(node, false, false);
		}

		/** Initialized constructor. */
		public DecoratedNode(Node node, boolean aggrBoundary, boolean parBoundary) {
			this.node = node;
			this.aggrBoundary = aggrBoundary;
			this.parBoundary = parBoundary;
		}

		/** Get the node being decorated. */
		public Node getNode() {
			return node;
		}

		/**
		 * Whether or not the path to get to the decorated node crossed an aggregation boundary.
		 */
		public boolean isAggrBoundary() {
			return aggrBoundary;
		}

		/**
		 * Whether or not the path to get to the decorated node crossed a paragraph boundary.
		 */
		public boolean isParBoundary() {
			return parBoundary;
		}

		/** String rendering (for debugging purposes). */
		public String toString() {
			return "Decorated" + node.toString() + (aggrBoundary ? "{A}" : "") + (parBoundary ? "{P}" : "");
		}

		public int hashCode() {
			return 3 * node.hashCode() + (aggrBoundary ? 1 : 0) + (parBoundary ? 7 : 0);
		}

		public boolean equals(Object o) {
			if (!(o instanceof DecoratedNode))
				return false;
			DecoratedNode other = (DecoratedNode) o;
			if (!other.node.equals(this.node))
				return false;
			if (other.aggrBoundary != this.aggrBoundary)
				return false;
			if (other.parBoundary != this.parBoundary)
				return false;
			return true;
		}
	}

	/** Values cache. */
	protected static class Cache {
		/** The cache itself, as a map from nodes to Entry. */
		protected Map<Node, List<Entry>> cache;

		/** Construct an empty cache. */
		public Cache() {
			this.cache = new HashMap<Node, List<Entry>>();
		}

		/** Entry inner class */
		protected static class Entry {
			/** The node which values are being cached. */
			protected Node node;
			/** The mapping from global variable names to values. */
			protected Map<String, Frame> vars;
			/** The values that satisty the predicate properties. */
			protected List<Map<Object, Frame>> values;

			/** Construct a new entry for a given node and global variables. */
			public Entry(Node node, Map<String, Frame> vars) {
				this.node = node;
				this.values = null;
				this.vars = new HashMap<String, Frame>();
				// look for which variables from the global mapping are
				// associated by the predicate in this node and record the
				// association in the entry
				for (String globalVar : node.getVars().values())
					if (vars.containsKey(globalVar))
						this.vars.put(globalVar, vars.get(globalVar));
			}

			/** Check when the node and the variables are the same. */
			public boolean equals(Node node, Map<String, Frame> vars) {
				if (this.node != node)
					return false;
				for (Map.Entry<String, Frame> entry : this.vars.entrySet()) {
					if (!vars.containsKey(entry.getKey()))
						return false;
					if (!vars.get(entry.getKey()).equals(entry.getValue()))
						return false;
				}
				return true;
			}

			/** Access the values list. */
			public List<Map<Object, Frame>> getValues() {
				return this.values;
			}

			/** Set the values list. */
			public void setValues(List<Map<Object, Frame>> values) {
				this.values = values;
			}
		}

		/**
		 * Access the cache by searching for the list of values for this node and variables. Returns null on miss.
		 */
		public List<Map<Object, Frame>> fetch(Node node, Map<String, Frame> vars) {
			if (!cache.containsKey(node))
				cache.put(node, new LinkedList<Entry>());
			List<Entry> entries = cache.get(node);
			Entry theEntry = null;
			for (Entry entry : entries)
				if (entry.equals(node, vars)) {
					theEntry = entry;
					break;
				}

			if (theEntry == null) {
				theEntry = new Entry(node, vars);
				entries.add(theEntry);
			}
			return theEntry.getValues();
		}

		/**
		 * Populate the cache entry for a node and variables with the values.
		 */
		public void populate(Node node, Map<String, Frame> vars, List<Map<Object, Frame>> values) {
			List<Entry> entries = cache.get(node);
			for (Entry entry : entries)
				if (entry.equals(node, vars)) {
					entry.setValues(values);
					return;
				}
			throw new IllegalStateException("populate(..) executed before fetch(..).");
		}
	}
}
