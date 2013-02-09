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

package net.sf.openschema.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A filter stream that transform a list of triples in comma separated format
 * into RDF.
 * 
 * @author Pablo Ariel Duboue <pablo.duboue@gmail.com>
 */

public class CsvToRdfFilterStream extends FilterInputStream {

	public CsvToRdfFilterStream(InputStream in) {
		super(new ByteArrayInputStream(transform(in)));
	}

	// all magic happens here
	private static byte[] transform(InputStream in) {
		try {
			Model model = ModelFactory.createDefaultModel();

			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line = br.readLine();

			List<String[]> triples = new ArrayList<String[]>();
			Set<String> seen = new HashSet<String>();

			while (line != null) {
				String[] parts = line.split(",", 3);
				// resource, property, value
				seen.add(parts[0]);
				triples.add(parts);
				line = br.readLine();
			}
			br.close();

			for (String[] triple : triples) {
				Resource resource = model.createResource("http://local/" + triple[0]);
				if (seen.contains(triple[2]))
					resource.addProperty(model.createProperty("http://local/", triple[1]),
							model.createResource("http://local/" + triple[2]));
				else
					resource.addProperty(model.createProperty("http://local/", triple[1]), triple[2]);
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			model.write(baos);
			baos.close();

			return baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new byte[0];
	}
}
