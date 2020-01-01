package org.eclipse.dash.licenses;

public interface IContentId {

	String getNamespace();

	String getName();

	String getVersion();

	String getType();

	String getSource();

	boolean isValid();

}