package com.j256.springrequestdoclet.collector;

import java.util.List;

/**
 * Method information which refines the request path and adds other narrowing fields.
 * 
 * @author graywatson
 */
public class MethodInfo implements Comparable<MethodInfo> {

	private final String javaMethodName;
	private final String javaDoc;
	private final String javaDocFirstSentence;
	private final String[] paths;
	private final String[] methods;
	private final String[] params;
	private final String[] headers;
	private final String[] consumes;
	private final String[] produces;
	private final List<ParamInfo> paramInfos;
	private final ContentsInfo requestInfo;
	private final ContentsInfo responseInfo;

	public MethodInfo(String javaMethodName, String javaDoc, String javaDocFirstSentence, String[] paths,
			String[] methods, String[] params, String[] headers, String[] consumes, String[] produces,
			List<ParamInfo> paramInfos, ContentsInfo requestInfo, ContentsInfo responseInfo) {
		this.javaMethodName = javaMethodName;
		this.paths = paths;
		this.javaDoc = javaDoc;
		this.javaDocFirstSentence = javaDocFirstSentence;
		this.methods = methods;
		this.params = params;
		this.headers = headers;
		this.consumes = consumes;
		this.produces = produces;
		this.paramInfos = paramInfos;
		this.requestInfo = requestInfo;
		this.responseInfo = responseInfo;
	}

	public String getJavaMethodName() {
		return javaMethodName;
	}

	public String getJavaDoc() {
		return javaDoc;
	}

	public String getJavaDocFirstSentence() {
		return javaDocFirstSentence;
	}

	public String[] getPaths() {
		return paths;
	}

	public String[] getMethods() {
		return methods;
	}

	public String[] getParams() {
		return params;
	}

	public String[] getHeaders() {
		return headers;
	}

	public String[] getConsumes() {
		return consumes;
	}

	public String[] getProduces() {
		return produces;
	}

	public List<ParamInfo> getParamInfos() {
		return paramInfos;
	}

	public ContentsInfo getRequestInfo() {
		return requestInfo;
	}

	public ContentsInfo getResponseInfo() {
		return responseInfo;
	}

	@Override
	public int compareTo(MethodInfo other) {
		return this.javaMethodName.compareTo(other.javaMethodName);
	}
}
