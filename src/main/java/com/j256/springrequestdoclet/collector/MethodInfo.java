package com.j256.springrequestdoclet.collector;

import java.util.List;

/**
 * Method information which refines the request path and adds other narrowing fields.
 * 
 * @author graywatson
 */
public class MethodInfo implements Comparable<MethodInfo> {

	private final String javaMethodName;
	private final String uniqueName;
	private final String javaDoc;
	private final String javaDocFirstSentence;
	private final String[] paths;
	private final String[] httpMethods;
	private final String[] params;
	private final String[] headers;
	private final String[] consumes;
	private final String[] produces;
	private final List<ParamInfo> paramInfos;
	private final ContentsInfo requestInfo;
	private final ContentsInfo responseInfo;

	public MethodInfo(String javaMethodName, String uniqueName, String javaDoc, String javaDocFirstSentence, String[] paths,
			String[] httpMethods, String[] params, String[] headers, String[] consumes, String[] produces,
			List<ParamInfo> paramInfos, ContentsInfo requestInfo, ContentsInfo responseInfo) {
		this.javaMethodName = javaMethodName;
		this.uniqueName = uniqueName;
		this.paths = paths;
		this.javaDoc = javaDoc;
		this.javaDocFirstSentence = javaDocFirstSentence;
		this.httpMethods = httpMethods;
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

	public String getUniqueName() {
		return uniqueName;
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

	public String[] getHttpMethods() {
		return httpMethods;
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
