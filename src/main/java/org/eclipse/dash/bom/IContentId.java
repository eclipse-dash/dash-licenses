package org.eclipse.dash.bom;

public interface IContentId {

	String getNamespace();

	String getName();

	String getVersion();

	String getType();

	String getSource();

	boolean isValid();

}