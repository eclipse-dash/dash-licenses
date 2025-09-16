/*************************************************************************
 * Copyright (c) 2023 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version implements Comparable<Version> {
	private static Pattern SemanticVersionPattern = Pattern.compile("^(?<major>\\d+)(?:\\.(?<minor>\\d+)(?:\\.(?<service>\\d+))?([_\\-\\.]+.*)?)?$");

	private String value;
	private Matcher matcher;
	

	public Version(String value) {
		this.value = value;
		
		var matcher = SemanticVersionPattern.matcher(value);
		if (matcher.matches() ) {
			this.matcher = matcher;
		}
	}

	private String[] getBits() {
		return value.split("[\\.\\-]");
	}
	
	private int compare(String[] a, String[] b, int index) {
		if (index >= a.length) return 0;
		
		try {
			var left = Integer.parseInt(a[index]);
			var right = index < b.length ? Integer.parseInt( b[index]) : 0;
			
			if (left == right) return compare(a, b, index+1);
			
			return left-right;
		} catch (NumberFormatException e) {
			if (index >= b.length) return -1;
			
			var left = a[index];
			var right = b[index];
			
			return left.compareTo(right);
		}
	}
	
	@Override
	public int compareTo(Version version) {
		
		var a = getBits();
		var b = version.getBits();
		
		if (b.length > a.length) return -compare(b,a,0);
		return compare(a, b, 0);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (Version.class.isInstance(obj)) 
			return compareTo((Version)obj) == 0;
		return false;
	}
	
	@Override
	public int hashCode() {
		return value.hashCode();
	}

	public int major() {
		return Integer.parseInt(matcher.group("major"));
	}

	public int minor() {
		return matcher.group("minor") != null ? Integer.parseInt(matcher.group("minor")) : 0;
	}
	
	public int service() {
		return matcher.group("service") != null ? Integer.parseInt(matcher.group("service")) : 0;
	}
	
	@Override
	public String toString() {
		return value;
	}
	
	public boolean isSemantic() {
		return matcher != null;
	}
}