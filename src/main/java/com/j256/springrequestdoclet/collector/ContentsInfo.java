package com.j256.springrequestdoclet.collector;

import java.util.List;

/**
 * Method parameter information which shows how the parameter values are set.
 * 
 * @author graywatson
 */
public class ContentsInfo {

	private final String javaParamName;
	private final String javaTypeName;
	private final String javaDoc;
	private final List<FieldInfo> fieldInfos;

	public static ContentsInfo fromRequestBody(String javaParamName, String javaTypeName, String javaDoc,
			List<FieldInfo> fieldInfos) {
		return new ContentsInfo(javaParamName, javaTypeName, javaDoc, fieldInfos);
	}

	public static ContentsInfo fromResponse(String javaTypeName, String javaDoc, List<FieldInfo> fieldInfos) {
		return new ContentsInfo(null, javaTypeName, javaDoc, fieldInfos);
	}

	private ContentsInfo(String javaParamName, String javaTypeName, String javaDoc, List<FieldInfo> fieldInfos) {
		this.javaParamName = javaParamName;
		this.javaTypeName = javaTypeName;
		this.javaDoc = javaDoc;
		this.fieldInfos = fieldInfos;
	}

	public String getJavaParamName() {
		return javaParamName;
	}

	public String getJavaTypeName() {
		return javaTypeName;
	}

	public String getJavaDoc() {
		return javaDoc;
	}

	public List<FieldInfo> getFieldInfos() {
		return fieldInfos;
	}
}
