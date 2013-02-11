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
 * A random continuation policy. Useful for debugging or demostration purposes.
 * 
 * @author Pablo Ariel Duboue <pablo.duboue@gmail.com>
 */

public class RandomChooser extends LocalChooser {
	protected java.util.Random rnd = new java.util.Random();

	public Decision choose(List<Map<String, Object>> fds, List<Frame> defaultFoci, Object currentFocus,
			List<Frame> potentialFoci, List<Frame> focusStack, FrameSet frames) {
		int pos = fds.size() == 0 ? 0 : rnd.nextInt(fds.size());
		List<Frame> newPotentialFoci = extractPotentialFoci(fds.get(pos), frames);
		Frame focus = newPotentialFoci.size() == 0 ? defaultFoci.get(pos) : newPotentialFoci.get(rnd
				.nextInt(newPotentialFoci.size()));
		return new Decision(pos, focus, newPotentialFoci);
	}
}
