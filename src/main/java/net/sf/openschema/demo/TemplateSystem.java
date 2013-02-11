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

package net.sf.openschema.demo;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.openschema.DocumentPlan;
import net.sf.openschema.Frame;
import net.sf.openschema.FrameSet;
import net.sf.openschema.Ontology;
import net.sf.openschema.OpenSchemaPlanner;
import net.sf.openschema.RDFFrameSet;
import net.sf.openschema.RDFOntology;
import net.sf.openschema.SimpleFocusChooser;
import net.sf.openschema.util.CsvToRdfFilterStream;
import net.sf.openschema.util.SchemaToXmlFilterStream;

import org.xml.sax.InputSource;

/**
 * Sample template system. Takes as input and schema and an RDF files and produce as output text. In the schema
 * predicates, the FDs have to be flat, have a "template" entry with running text. Each variable of the form @name.
 * should have a "name" entry in the FD with the value that will be inserted at that position.
 * 
 * @author Pablo Ariel Duboue <pablo.duboue@gmail.com>
 */

public class TemplateSystem {
	/** Verbosity flag, defaults to off. */
	public static boolean verbose = true;

	/** Main. */
	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.err
					.println("Usage: TemplateSystem schema.(xml|schema) data.(rdf|csv) ontology.rdfs attribute value attribute value ...");
			System.exit(0);
		}
		if (verbose)
			System.err.print("Loading ontology... ");
		Ontology ontology = new RDFOntology(new FileInputStream(args[2]), "file://" + args[1]);
		if (verbose)
			System.err.println("Loaded.");
		InputStream schemaIS = new FileInputStream(args[0]);
		if (args[0].endsWith(".schema"))
			schemaIS = new SchemaToXmlFilterStream(schemaIS);
		InputSource inputSource = new InputSource(new InputStreamReader(schemaIS));
		if (verbose)
			System.err.print("Loading schema... ");
		OpenSchemaPlanner schema = new OpenSchemaPlanner(inputSource, new SimpleFocusChooser(ontology));
		// new RandomChooser());
		// new GreedyChooser());
		if (verbose) {
			System.err.println("Loaded.");
			System.err.println(schema.dump(true));
			System.err.print("Loading frames... ");
		}
		InputStream framesIS = new FileInputStream(args[1]);
		if (args[1].endsWith(".csv"))
			framesIS = new CsvToRdfFilterStream(framesIS);
		FrameSet frames = new RDFFrameSet(framesIS, "file://" + args[1]);
		if (verbose) {
			System.err.println("Loaded.");
			for (Frame frame : frames.getFrames())
				System.err.println(frame.getID() + " " + frame.getType());
		}
		Map<String, Frame> varMapping = new HashMap<String, Frame>();
		for (int i = 3; i < args.length; i += 2)
			if (frames.getFrame(args[i + 1]) == null) {
				System.err.println("Argument: '" + args[i] + "', value: '" + args[i + 1] + "' not found.");
				System.exit(1);
			} else
				varMapping.put(args[i], frames.getFrame(args[i + 1]));
		if (verbose)
			System.err.print("Instantiating...");
		DocumentPlan plan = schema.instantiate(frames, varMapping, ontology);
		if (verbose) {
			System.err.println("Instantiated.");
			System.err.println(plan);
		}
		// instantiate the templates
		for (List<List<Map<String, Object>>> aggrSegments : plan.getParagraphs()) {
			// ignore aggregation boundaries
			List<Map<String, Object>> clauses = new ArrayList<Map<String, Object>>();
			for (List<Map<String, Object>> aggr : aggrSegments)
				clauses.addAll(aggr);
			for (Map<String, Object> clause : clauses) {
				if (!clause.containsKey("template"))
					continue; // ignore
				String template = clause.get("template").toString();
				StringBuffer instantiated = new StringBuffer();
				String[] fields = template.split("\\@");
				instantiated.append(fields[0].startsWith("\"") ? fields[0].substring(1) : fields[0]);
				for (int i = 1; i < fields.length; i++) {
					String[] nameRest = fields[i].split("\\.", 2);
					if (clause.containsKey(nameRest[0]) && clause.get(nameRest[0]) != null) {
						String value = clause.get(nameRest[0]).toString();
						instantiated.append(value.startsWith("\"") ? value.substring(1, value.length() - 1) : value);
					}
					instantiated.append(i == fields.length - 1 && nameRest[1].endsWith("\"") ? nameRest[1].substring(0,
							nameRest[1].length() - 1) : nameRest[1]);
				}
				System.out.print(instantiated);
			}
			System.out.println();
			System.out.println();
		}
	}
}
