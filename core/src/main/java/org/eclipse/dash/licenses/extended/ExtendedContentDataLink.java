package org.eclipse.dash.licenses.extended;

public class ExtendedContentDataLink extends ExtendedContentDataItem {

	public ExtendedContentDataLink(String label, String value) {
		super(label, value);
	}

	@Override
	public String asMarkdown() {
		return String.format("[%s](%s)", getLabel(), getValue());
	}
}
