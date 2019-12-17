package org.eclipse.dash.bom;

public class InvalidContentId implements IContentId {

	private String value;

	public InvalidContentId(String value) {
		this.value = value;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String toString() {
		return "# " + value;
	}

	@Override
	public String getNamespace() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

}
