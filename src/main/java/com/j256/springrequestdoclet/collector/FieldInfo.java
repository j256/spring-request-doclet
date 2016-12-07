package com.j256.springrequestdoclet.collector;

/**
 * Information about an object fields that help us 
 * 
 * @author graywatson
 */
public class FieldInfo {

	private final String fieldName;
	private final String typeName;
	private final String javaDoc;

	public FieldInfo(String fieldName, String typeName, String javaDoc) {
		this.fieldName = fieldName;
		this.typeName = typeName;
		this.javaDoc = javaDoc;
	}

	public String getFieldName() {
		return fieldName;
	}

	public String getTypeName() {
		return typeName;
	}

	public String getJavaDoc() {
		return javaDoc;
	}
}
