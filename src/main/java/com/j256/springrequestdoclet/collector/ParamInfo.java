package com.j256.springrequestdoclet.collector;

/**
 * Method parameter information which shows how the parameter values are set.
 * 
 * @author graywatson
 */
public class ParamInfo {

	private final String javaParamName;
	private final String javaTypeName;
	private final ParamRequestKind requestKind;
	private final String requestName;
	private final boolean required;
	private final String defaultValue;
	private final String javaDoc;

	public static ParamInfo fromRequestParam(String javaParamName, String javaTypeName, String queryParamName,
			boolean required, String defaultValue, String javaDoc) {
		return new ParamInfo(javaParamName, javaTypeName, ParamRequestKind.QUERY, queryParamName, required,
				defaultValue, javaDoc);
	}

	public static ParamInfo fromPathVariable(String javaParamName, String javaTypeName, String pathVariableName,
			boolean required, String defaultValue, String javaDoc) {
		return new ParamInfo(javaParamName, javaTypeName, ParamRequestKind.PATH, pathVariableName, required,
				defaultValue, javaDoc);
	}

	public static ParamInfo fromRequestHeader(String javaParamName, String javaTypeName, String requestHeaderName,
			boolean required, String defaultValue, String javaDoc) {
		return new ParamInfo(javaParamName, javaTypeName, ParamRequestKind.HEADER, requestHeaderName, required,
				defaultValue, javaDoc);
	}

	private ParamInfo(String javaParamName, String javaTypeName, ParamRequestKind type, String requestName,
			boolean required, String defaultValue, String javaDoc) {
		this.javaParamName = javaParamName;
		this.javaTypeName = javaTypeName;
		this.requestKind = type;
		this.requestName = requestName;
		this.required = required;
		this.defaultValue = defaultValue;
		this.javaDoc = javaDoc;
	}

	public String getJavaParamName() {
		return javaParamName;
	}

	public String getJavaTypeName() {
		return javaTypeName;
	}

	public ParamRequestKind getRequestKind() {
		return requestKind;
	}

	/**
	 * One of this, path-variable, or header-name will not be null.
	 */
	public String getRequestName() {
		return requestName;
	}

	/**
	 * NOTE: always true for path-variable.
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * NOTE: always null for path-variable.
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	public String getJavaDoc() {
		return javaDoc;
	}

	/**
	 * Type of the parameter.
	 */
	public static enum ParamRequestKind {
		QUERY("Query"),
		HEADER("Header"),
		PATH("Path"),
		// end
		;

		private final String description;

		private ParamRequestKind(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}
	}
}
