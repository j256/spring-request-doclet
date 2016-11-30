package com.j256.springrequestdoclet.collector;

/**
 * Information from the class which is usually paths.
 * 
 * @author graywatson
 */
public class ClassInfo implements Comparable<ClassInfo> {

	private final String className;
	private final String typeName;
	private final String javaDoc;
	private final String javaDocFirstSentence;
	private final String[] paths;

	public ClassInfo(String className, String typeName, String javaDoc, String javaDocFirstSentence, String[] paths) {
		this.className = className;
		this.typeName = typeName;
		this.javaDoc = javaDoc;
		this.javaDocFirstSentence = javaDocFirstSentence;
		this.paths = paths;
	}

	public String getClassName() {
		return className;
	}

	public String getTypeName() {
		return typeName;
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

	@Override
	public int compareTo(ClassInfo other) {
		return this.className.compareTo(other.className);
	}

	@Override
	public int hashCode() {
		return ((typeName == null) ? 0 : typeName.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ClassInfo other = (ClassInfo) obj;
		if (typeName == null) {
			return (other.typeName == null);
		} else {
			return typeName.equals(other.typeName);
		}
	}
}
