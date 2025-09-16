package org.eclipse.dash.licenses.util;

import java.util.Comparator;

public class ProximityComparator implements Comparator<Integer> {
		Integer base;
		
		public ProximityComparator(Integer base) {
			this.base = base;
		}
		
		@Override
		public int compare(Integer o1, Integer o2) {
			if (o1.equals(o2)) return 0;
			
			if (o1.equals(base)) return -1;
			if (o2.equals(base)) return 1;
			
			return Integer.compare(Math.abs(o1 - base), Math.abs(o2 - base));
		}
	}