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
 * A random continuation policy. Useful for debugging or demostration
 * purposes.
 *
 * @author Pablo Ariel Duboue (pablo@cs.columbia.edu)
 * @version $Revision: 1.1 $, $Date: 2004/07/12 09:34:06 $
 */

public class RandomChooser extends LocalChooser{
    protected java.util.Random rnd=new java.util.Random();
    public Decision choose(List fds,List defaultFoci,Object currentFocus,
                           List potentialFoci,List focusStack,FrameSet frames){
        int pos=fds.size()==0?0:rnd.nextInt(fds.size());
        List newPotentialFoci=new ArrayList(extractPotentialFoci((FD)fds.get(pos),frames));
        Object focus=
            newPotentialFoci.size()==0?defaultFoci.get(pos):
            newPotentialFoci.get(rnd.nextInt(newPotentialFoci.size()));
        return new Decision(pos,focus,newPotentialFoci);
    }
}
