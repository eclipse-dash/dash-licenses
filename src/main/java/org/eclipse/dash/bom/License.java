package org.eclipse.dash.bom;

public class License {

	private String code;

	public License(String code) {
		this.code = code;
	}
	
	@Override
	public String toString() {
		return code;
	}

}
