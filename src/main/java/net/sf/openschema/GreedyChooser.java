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

import java.util.List;
import java.util.Map;

/**
 * A greedy continuation chooser that always returns the first element of the possible alternatives.
 * 
 * @author Pablo Ariel Duboue <pablo.duboue@gmail.com>
 */

public class GreedyChooser extends LocalChooser {
	private static final boolean verbose = true;

	public Decision choose(List<Map<String, Object>> fds, List<Frame> defaultFoci, Object currentFocus,
			List<Frame> potentialFoci, List<Frame> focusStack, FrameSet frames) {
		if (verbose)
			System.err.println("GreedyChooser called with fds==" + fds);

		return new Decision(0, defaultFoci.get(0), extractPotentialFoci(fds.get(0), frames));
	}
}
