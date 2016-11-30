package com.j256.springrequestdoclet.collector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;
import com.sun.javadoc.AnnotationValue;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;

/**
 * Collector that processed the class documentation and extracts and processes the information.
 * 
 * @author graywatson
 */
public class EndPointCollector {

	private static final String REQUEST_MAPPING_ANNOTATION_NAME = "RequestMapping";
	private static final String REQUEST_PARAM_ANNOTATION_NAME = "RequestParam";
	private static final String PATH_VARIABLE_ANNOTATION_NAME = "PathVariable";
	private static final String REQUEST_HEADER_ANNOTATION_NAME = "RequestHeader";

	private static final String REQUEST_METHOD_PACKAGE_PREFIX =
			"org.springframework.web.bind.annotation.RequestMethod.";
	private static final Pattern JAVADOC_PARAM_PATTERN = Pattern.compile("(?s)@param\\s+([^\\s]+)\\s+([^@]+)");

	private final Map<String, List<EndPoint>> pathInfoMap = new HashMap<String, List<EndPoint>>();

	public void processClass(ClassDoc classDoc) {
		AnnotationDesc requestMapping = findAnnotation(classDoc.annotations(), REQUEST_MAPPING_ANNOTATION_NAME);
		if (requestMapping == null) {
			return;
		}

		// @RequestMapping(value = { "/auth/oauth" })
		String[] paths = findAnnotationFieldValues(requestMapping, "value");
		String javaDoc = classDoc.getRawCommentText();
		ClassInfo classInfo = new ClassInfo(classDoc.name(), classDoc.qualifiedTypeName(), javaDoc,
				javaDocFirstSentence(javaDoc), paths);
		for (MethodDoc methodDoc : classDoc.methods()) {
			handleMethod(classInfo, methodDoc);
		}
	}

	public Map<String, List<EndPoint>> getPathInfoMap() {
		return pathInfoMap;
	}

	/**
	 * Process the annotations from each of the methods looking for a @RequestMapping and/or @RequestMethod.
	 */
	private void handleMethod(ClassInfo classInfo, MethodDoc methodDoc) {

		AnnotationDesc requestMapping = findAnnotation(methodDoc.annotations(), REQUEST_MAPPING_ANNOTATION_NAME);
		if (requestMapping == null) {
			return;
		}

		// @RequestMapping(value = { "/auth/oauth" })
		String[] paths = findAnnotationFieldValues(requestMapping, "value");
		// @RequestMapping(method = { RequestMethod.GET })
		String[] methods = findAnnotationFieldValues(requestMapping, "method");
		if (methods != null) {
			for (int i = 0; i < methods.length; i++) {
				if (methods[i].startsWith(REQUEST_METHOD_PACKAGE_PREFIX)) {
					methods[i] = methods[i].substring(REQUEST_METHOD_PACKAGE_PREFIX.length());
				}
			}
		}
		// @RequestMapping(params = { "schoolId", "user=12234" })
		String[] params = findAnnotationFieldValues(requestMapping, "params");
		// @RequestMapping(headers = { "content-type=text/*" })
		String[] headers = findAnnotationFieldValues(requestMapping, "headers");
		// @RequestMapping(consumes = { "content-type=application/json" })
		String[] consumes = findAnnotationFieldValues(requestMapping, "consumes");
		// @RequestMapping(produces = { "content-type=text/plain" })
		String[] produces = findAnnotationFieldValues(requestMapping, "produces");

		String methodJavaDoc = methodDoc.getRawCommentText();

		List<ParamInfo> paramInfos = new ArrayList<ParamInfo>();
		for (Parameter param : methodDoc.parameters()) {
			ParamInfo paramInfo = handleParam(param, methodJavaDoc);
			if (paramInfo != null) {
				paramInfos.add(paramInfo);
			}
		}

		if (paramInfos.isEmpty()) {
			paramInfos = null;
		}
		MethodInfo methodInfo = new MethodInfo(methodDoc.name(), javaDocFirstSentence(methodJavaDoc), paths, methods,
				params, headers, consumes, produces, paramInfos);

		if (classInfo.getPaths() == null) {
			addClassPathInfo(classInfo, methodInfo, null);
		} else {
			for (String classPath : classInfo.getPaths()) {
				addClassPathInfo(classInfo, methodInfo, classPath);
			}
		}
	}

	private void addClassPathInfo(ClassInfo classInfo, MethodInfo methodInfo, String classPath) {
		classPath = pathNoQuotes(classPath);
		if (methodInfo.getPaths() == null) {
			addPathInfo(classInfo, methodInfo, classPath);
			return;
		}

		for (String methodPath : methodInfo.getPaths()) {
			String path = calculatePath(classPath, methodPath);
			addPathInfo(classInfo, methodInfo, path);
		}
	}

	private String calculatePath(String classPath, String methodPath) {
		classPath = pathNoQuotes(classPath);
		methodPath = pathNoQuotes(methodPath);
		if (classPath == null || classPath.isEmpty()) {
			return methodPath;
		} else if (methodPath.isEmpty()) {
			return classPath;
		}
		StringBuilder sb = new StringBuilder(classPath.length() + methodPath.length() + 1);
		sb.append(classPath);
		// make sure we don't at least one but not multiple '/'
		if (classPath.charAt(classPath.length() - 1) != '/' && methodPath.charAt(0) != '/') {
			sb.append('/');
		}
		sb.append(methodPath);
		return sb.toString();
	}

	private void addPathInfo(ClassInfo classInfo, MethodInfo methodInfo, String path) {
		List<EndPoint> pathInfos = pathInfoMap.get(path);
		if (pathInfos == null) {
			pathInfos = new ArrayList<EndPoint>();
			pathInfoMap.put(path, pathInfos);
		}
		EndPoint pathInfo = new EndPoint(path, classInfo, methodInfo);
		pathInfos.add(pathInfo);
	}

	/**
	 * Process the annotations from each of the methods looking for a @RequestMapping and/or @RequestMethod.
	 */
	private ParamInfo handleParam(Parameter param, String methodJavaDoc) {
		String pathVariableName = null;
		String requestHeaderName = null;

		String javaDoc = extractParamDocs(methodJavaDoc, param.name());

		// @RequestParam("schoolId) long schoolId, ...
		AnnotationDesc requestParam = findAnnotation(param.annotations(), REQUEST_PARAM_ANNOTATION_NAME);
		if (requestParam != null) {
			String queryParamName = findAnnotationFieldValue(requestParam, "value");
			boolean required = true;
			String requiredStr = findAnnotationFieldValue(requestParam, "required");
			if (requiredStr != null) {
				required = Boolean.parseBoolean(requiredStr);
			}
			String defaultValue = findAnnotationFieldValue(requestParam, "defaultValue");
			return ParamInfo.fromRequestParam(param.name(), param.typeName(), queryParamName, required, defaultValue,
					javaDoc);
		}

		// @RequestMapping("/request/{schoolId}") public void request(@PathVariable("schoolId) long schoolId)
		AnnotationDesc pathVariable = findAnnotation(param.annotations(), PATH_VARIABLE_ANNOTATION_NAME);
		if (pathVariable != null) {
			pathVariableName = findAnnotationFieldValue(pathVariable, "value");
			return ParamInfo.fromPathVariable(param.name(), param.typeName(), pathVariableName, true, null, javaDoc);
		}

		// @RequestHeader("Content-Type") String contentType, ...
		AnnotationDesc requestHeader = findAnnotation(param.annotations(), REQUEST_HEADER_ANNOTATION_NAME);
		if (requestHeader != null) {
			requestHeaderName = findAnnotationFieldValue(requestHeader, "value");
			boolean required = true;
			String requiredStr = findAnnotationFieldValue(requestParam, "required");
			if (requiredStr != null) {
				required = Boolean.parseBoolean(requiredStr);
			}
			String defaultValue = findAnnotationFieldValue(requestParam, "defaultValue");
			return ParamInfo.fromRequestHeader(param.name(), param.typeName(), requestHeaderName, required,
					defaultValue, javaDoc);
		}

		return null;
	}

	private String extractParamDocs(String methodJavaDocs, String paramName) {
		// @param ssoVar oauth provider name.
		if (methodJavaDocs == null || methodJavaDocs.isEmpty()) {
			return null;
		}

		int start = 0;
		Matcher matcher = JAVADOC_PARAM_PATTERN.matcher(methodJavaDocs);
		while (true) {
			if (!matcher.find(start)) {
				return null;
			}
			if (matcher.groupCount() == 2 && matcher.group(1).equals(paramName)) {
				return matcher.group(2);
			}
			start = matcher.end();
		}
	}

	/**
	 * Find the specific annotation from the list of annotation descriptions.
	 */
	private AnnotationDesc findAnnotation(AnnotationDesc[] annotations, String annotationName) {
		for (AnnotationDesc annotation : annotations) {
			if (annotationName.equals(annotation.annotationType().name())) {
				return annotation;
			}
		}
		return null;
	}

	/**
	 * Find the specific annotation field by name.
	 * 
	 * @return Values associated with the field or null if not found.
	 */
	private String[] findAnnotationFieldValues(AnnotationDesc annotation, String fieldName) {
		for (ElementValuePair pair : annotation.elementValues()) {
			// String[] value, consumes, produces, headers, method (no s), params
			String name = pair.element().name();
			Object obj = pair.value().value();
			if (fieldName.equals(name) && (obj instanceof AnnotationValue[])) {
				// each of these are the path mapping for the @RequestMapping annotation
				AnnotationValue[] values = (AnnotationValue[]) obj;
				String[] result = new String[values.length];
				for (int i = 0; i < values.length; i++) {
					result[i] = pathNoQuotes(values[i].toString());
				}
				return result;
			}
		}
		return null;
	}

	/**
	 * Find the specific annotation field by name.
	 * 
	 * @return String value associated with the field or null if not found.
	 */
	private String findAnnotationFieldValue(AnnotationDesc annotation, String fieldName) {
		for (ElementValuePair pair : annotation.elementValues()) {
			// String value
			String name = pair.element().name();
			AnnotationValue value = pair.value();
			if (fieldName.equals(name)) {
				return pathNoQuotes(value.toString());
			}
		}
		return null;
	}

	private String javaDocFirstSentence(String javaDoc) {
		if (javaDoc == null) {
			return null;
		}
		for (int i = 0; i < javaDoc.length(); i++) {
			char ch = javaDoc.charAt(i);
			if (ch == '.' || ch == '!' || ch == '?' || ch == '@') {
				return javaDoc.substring(0, i + 1);
			}
		}
		return javaDoc;
	}

	private String pathNoQuotes(String path) {
		if (path != null && path.length() >= 2 && path.charAt(0) == '\"' && path.charAt(path.length() - 1) == '\"') {
			return path.substring(1, path.length() - 1);
		} else {
			return path;
		}
	}
}
