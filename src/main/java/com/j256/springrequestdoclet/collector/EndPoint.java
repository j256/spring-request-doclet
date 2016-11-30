package com.j256.springrequestdoclet.collector;

/**
 * Request information mapped to a path.
 * 
 * @author graywatson
 */
public class EndPoint {

	private final String path;
	private final ClassInfo classInfo;
	private final MethodInfo methodInfo;

	public EndPoint(String path, ClassInfo classInfo, MethodInfo methodInfo) {
		this.path = path;
		this.classInfo = classInfo;
		this.methodInfo = methodInfo;
	}

	public String getPath() {
		return path;
	}

	public ClassInfo getClassInfo() {
		return classInfo;
	}

	public MethodInfo getMethodInfo() {
		return methodInfo;
	}
}
