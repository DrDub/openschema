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

import edu.columbia.openschema.*;
import java.io.*;
import java.util.*;
import org.xml.sax.InputSource;
import edu.columbia.fuf.FD;

/**
 * Sample template system. Takes as input and schema and an RDF files
 * and produce as output text. In the schema predicates, the FDs have
 * to be flat, have a "template" entry with running text. Each
 * variable of the form @name. should have a "name" entry in the FD
 * with the value that will be inserted at that position.
 *
 * @author Pablo Ariel Duboue (pablo@cs.columbia.edu)
 * @version $Revision: 1.1.1.1 $, $Date: 2004/07/12 09:34:01 $
 */


public class TemplateSystem{
    /** Verbosity flag, defaults to off.*/
    public static boolean verbose=false;
    /** Main. */
    public static void main(String[]args)throws Exception{
        if(args.length<2){
            System.err.println("Usage: TemplateSystem schema.xml data.rdf attribute value attribute value ...");
            System.exit(0);
        }
        if(verbose)System.err.print("Loading ontology... ");
        Ontology ontology=new RDFOntology(new FileInputStream(args[2]),"file://"+args[1]);
        if(verbose)System.err.println("Loaded.");
        InputSource inputSource=new InputSource(new FileReader(args[0]));
        if(verbose)System.err.print("Loading schema... ");
        OpenSchema schema=new OpenSchema(inputSource,
                                         new SimpleFocusChooser(ontology));
                                         //new RandomChooser());
                                         //new GreedyChooser());
        if(verbose){
            System.err.println("Loaded.");
            System.err.println(schema.dump());
            System.err.print("Loading frames... ");
        }
        FrameSet frames=new RDFFrameSet(new FileInputStream(args[1]),"file://"+args[1]);
        if(verbose){
            System.err.println("Loaded.");
            Iterator f=frames.getFrames().iterator();
            while(f.hasNext()){
                Frame frame=(Frame)f.next();
                System.err.println(frame.getID()+" "+frame.getType());
            }
        }
        Map varMapping=new HashMap();
        for(int i=3;i<args.length;i+=2)
            if(frames.getFrame(args[i+1])==null){
                System.err.println("Argument: '"+args[i]+"', value: '"+args[i+1]+"' not found.");
                System.exit(1);
            }else
                varMapping.put(args[i],frames.getFrame(args[i+1]));
        if(verbose)System.err.print("Instantiating...");
        DocumentPlan plan=schema.instantiate(frames,varMapping,ontology);
        if(verbose){
            System.err.println("Instantiated.");
            System.err.println(plan);
        }
        // instantiate the templates
        Iterator paragraphs=plan.getParagraphs().iterator();
        while(paragraphs.hasNext()){
            List aggrSegments=(List)paragraphs.next();
            // ignore aggregation boundaries
            List clauses=new ArrayList();
            Iterator a=aggrSegments.iterator();
            while(a.hasNext())
                clauses.addAll((List)a.next());
            Iterator c=clauses.iterator();
            while(c.hasNext()){
                FD clause=(FD)c.next();
                if(!clause.containsKey("template"))
                    continue; // ignore
                String template=clause.get("template").toString();
                StringBuffer instantiated=new StringBuffer();
                String[]fields=template.split("\\@");
                instantiated.append(fields[0].startsWith("\"")?fields[0].substring(1):fields[0]);
                for(int i=1;i<fields.length;i++){
                    String[]nameRest=fields[i].split("\\.",2);
                    if(clause.containsKey(nameRest[0])&&
                       clause.get(nameRest[0])!=null){
                        String value=clause.get(nameRest[0]).toString();
                        instantiated.append(value.startsWith("\"")?value.substring(1,value.length()-1):
                                            value);
                    }
                    instantiated.append(i==fields.length-1&&nameRest[1].endsWith("\"")?
                                        nameRest[1].substring(0,nameRest[1].length()-1):nameRest[1]);
                }
                System.out.print(instantiated);
            }
            System.out.println();
            System.out.println();
        }
    }
}
