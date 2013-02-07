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

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.xml.sax.InputSource;

/**
 * Test suite.
 * 
 * @author Pablo Ariel Duboue <pablo.duboue@gmail.com>
 */

public class MyTestSuite extends TestCase {
	public MyTestSuite(String name) {
		super(name);
	}

	protected String sampleSchemaDef() {
		StringBuffer schemaDef = new StringBuffer();
		schemaDef.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
		schemaDef.append("<OpenSchema xmlns=\"http://openschema.sf.net\" xmlns:fd=\"http://jfuf.sf.net/FD\">");
		schemaDef.append("<Predicate ID=\"single-predicate\">");
		schemaDef.append("<Variable ID=\"Data\" Type=\"c-object\" Required=\"false\" DefaultFocus=\"true\"/>");
		schemaDef.append("<Output><fd:FD><fd:V N=\"obj\"><fd:G>Data.number</fd:G></fd:V></fd:FD></Output>");
		schemaDef.append("</Predicate>");
		schemaDef.append("<Schema>");
		schemaDef.append("<Node><KleeneStar><Node><Predicate Name=\"single-predicate\"/></Node></KleeneStar></Node>");
		schemaDef.append("</Schema>");
		schemaDef.append("</OpenSchema>");
		return schemaDef.toString();
	}

	protected OpenSchemaPlanner sampleSchema() throws Exception {
		Reader reader = new StringReader(sampleSchemaDef());
		InputSource inputSource = new InputSource(reader);
		return new OpenSchemaPlanner(inputSource, new GreedyChooser());
	}

	protected FrameSet sampleFrames() {
		final Frame frame1 = new MapFrame("frame-1", "c-object");
		frame1.add("number", new Integer(5));
		final Frame frame2 = new MapFrame("frame-2", "c-object");
		frame2.add("number", new Integer(7));
		return new FrameSet() {
			public Collection<Frame> getFrames() {
				return Arrays.asList(new Frame[] { frame1, frame2 });
			}

			public Frame getFrame(String id) {
				return id.equals("frame-1") ? frame1 : frame2;
			}
		};
	}

	protected FrameSet sampleFrames2() {
		final Frame frame1 = new MapFrame("frame-1", "c-object");
		frame1.add("number", new Integer(5));
		final Frame frame2 = new MapFrame("frame-2", "c-event");
		frame2.add("number", new Integer(7));
		return new FrameSet() {
			public Collection<Frame> getFrames() {
				return Arrays.asList(new Frame[] { frame1, frame2 });
			}

			public Frame getFrame(String id) {
				return id.equals("frame-1") ? frame1 : frame2;
			}
		};
	}

	protected Ontology sampleOntology() {
		return new Ontology() {
			public boolean isA(Object child, Object parent) {
				return true;
			}

			public double distance(Object concept1, Object concept2) {
				return 0;
			}
		};
	}

	protected Ontology sampleOntology2() {
		return new Ontology() {
			public boolean isA(Object child, Object parent) {
				return child.equals(parent);
			}

			public double distance(Object concept1, Object concept2) {
				return concept1.equals(concept2) ? 0 : 1;
			}
		};
	}

	public void testSchemaInstantiation() throws Exception {
		OpenSchemaPlanner schema = sampleSchema();
		FrameSet frames = sampleFrames();
		Ontology ontology1 = sampleOntology();
		DocumentPlan documentPlan = schema.instantiate(frames, Collections.<String, Frame> emptyMap(), ontology1);
		List<List<List<Map<String, Object>>>> pars = documentPlan.getParagraphs();
		assertTrue("DocumentPlan does not contain one paragraph.", pars.size() == 1);
		List<List<Map<String, Object>>> aggrs = pars.get(0);
		assertTrue("DocumentPlan paragraph does not contain one aggregation segment.", aggrs.size() == 1);
		List<Map<String, Object>> aggrSegm = aggrs.get(0);
		assertTrue("DocumentPlan aggregation segment does not contain two clauses.", aggrSegm.size() == 2);
		Map<String, Object> fd1 = aggrSegm.get(0);
		Map<String, Object> fd2 = aggrSegm.get(1);
		assertTrue("First clause should be (obj 5)", fd1.size() == 1 + 3 && fd1.containsKey("obj")
				&& fd1.get("obj").equals(new Integer(5)));
		assertTrue("Second clause should be (obj 7)", fd2.size() == 1 + 3 && fd2.containsKey("obj")
				&& fd2.get("obj").equals(new Integer(7)));

		FrameSet frames2 = sampleFrames2();

		DocumentPlan documentPlan2 = schema.instantiate(frames2, Collections.<String, Frame> emptyMap(), ontology1);
		// result should be the same
		assertTrue("DocumentPlans differ", documentPlan.toString().equals(documentPlan2.toString()));

		Ontology ontology2 = sampleOntology2();
		DocumentPlan documentPlan3 = schema.instantiate(frames2, Collections.<String, Frame> emptyMap(), ontology2);

		pars = documentPlan3.getParagraphs();
		assertTrue("New DocumentPlan does not contain one paragraph.", pars.size() == 1);
		aggrs = pars.get(0);
		assertTrue("New DocumentPlan paragraph does not contain one aggregation segment.", aggrs.size() == 1);
		aggrSegm = aggrs.get(0);
		assertTrue("New DocumentPlan aggregation segment does not contain two clauses.", aggrSegm.size() == 1);
		Map<String, Object> fd = aggrSegm.get(0);
		assertTrue("Clause should be (obj 5)",
				fd.size() == 1 + 3 && fd.containsKey("obj") && fd.get("obj").equals(new Integer(5)));
	}

	public void testSchemaCreation() throws Exception {
		OpenSchemaPlanner schema = sampleSchema();
		String dump = schema.dump();
		String[] lines = dump.split("\\n");
		assertTrue("Wrong dump (number of lines)", lines.length != 12);
	}

	public static TestSuite suite() {
		return new TestSuite(MyTestSuite.class);
	}

	public static void main(String args[]) {
		junit.textui.TestRunner.run(suite());
	}
}
