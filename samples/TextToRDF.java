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

import java.io.*;
import java.util.*;
import com.hp.hpl.jena.rdf.model.*;

/**
 * Helper class to transform a list of triples to RDF. Used by
 * scripts/text-to-rdf.sh.
 *
 * @author Pablo Ariel Duboue (pablo@cs.columbia.edu)
 * @version $Revision: 1.1 $, $Date: 2004/07/12 09:34:02 $
 */

public class TextToRDF{
    /** Main. */
    public static void main(String[]args)throws IOException{
        Model model=ModelFactory.createDefaultModel();
        BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
        String line=br.readLine();
        List triples=new ArrayList();
        Set seen=new HashSet();
        
        while(line!=null){
            String[]parts=line.split(",",3);
            // resource, property, value
            seen.add(parts[0]);
            triples.add(parts);
            line=br.readLine();
        }
        Iterator t=triples.iterator();
        while(t.hasNext()){
            String[]triple=(String[])t.next();
        
            Resource resource=model.createResource("http://local/"+triple[0]);
            if(seen.contains(triple[2]))
                resource.addProperty(model.createProperty("http://local/",triple[1]),
                                     model.createResource("http://local/"+triple[2]));
            else
                resource.addProperty(model.createProperty("http://local/",triple[1]),
                                     triple[2]);
        }
        model.write(System.out);
    }
}
