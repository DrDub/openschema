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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract class for deciding which continuation to follow in the schema instantiation process.
 * 
 * @author Pablo Ariel Duboue <pablo.duboue@gmail.com>
 */

public abstract class LocalChooser {
	/**
	 * Decide which continuation to follow in the schema instantiation process.
	 */
	public abstract Decision choose(List<Map<String, Object>> fds, List<Frame> defaultFoci, Object currentFocus,
			List<Frame> potentialFoci, List<Frame> focusStack, FrameSet frames);

	/**
	 * Extract a set of potential foci from a given clause. As the clauses store only frame names but not the frame
	 * themselves, the FrameSet is used to do that mapping.
	 */
	@SuppressWarnings("unchecked")
	public List<Frame> extractPotentialFoci(Map<String, Object> fd, FrameSet frames) {
		List<Frame> result = new LinkedList<Frame>();
		Set<Frame> seen = new HashSet<Frame>();
		for (Object value : fd.values()) {
			if (value instanceof Map) {
				for (Frame frame : extractPotentialFoci((Map<String, Object>) value, frames))
					if (!seen.contains(frame)) {
						result.add(frame);
						seen.add(frame);
					}
			} else if (value instanceof Frame) {
				Frame frame = (Frame) value;
				if (!seen.contains(frame)) {
					result.add(frame);
					seen.add(frame);
				}
			} else if (value != null && frames.getFrame(value.toString()) != null) {
				Frame frame = frames.getFrame(value.toString());
				if (!seen.contains(frame)) {
					result.add(frame);
					seen.add(frame);
				}
			}
		}
		return result;
	}

	/** LocalChooser Decision inner class. */
	public static class Decision {
		/** Position in the original list. */
		protected int pos;
		/** New current focus. */
		protected Frame currentFocus;
		/** New potential focus list. */
		protected List<Frame> potentialFoci;

		/**
		 * Full constructor (this class is immutable).
		 * 
		 * @param pos
		 *            Position in the original list of the chosen continuation.
		 * @param currentFocus
		 *            New current focus.
		 * @param potentialFoci
		 *            New potential focus list.
		 */
		public Decision(int pos, Frame currentFocus, List<Frame> potentialFoci) {
			this.pos = pos;
			this.currentFocus = currentFocus;
			this.potentialFoci = potentialFoci;
		}

		/** Position in the original list of the chosen continuation. */
		public int getPosition() {
			return pos;
		}

		/** New current focus. */
		public Frame getCurrentFocus() {
			return currentFocus;
		}

		/** New potential focus list. */
		public List<Frame> getPotentialFoci() {
			return potentialFoci;
		}
	}
}
