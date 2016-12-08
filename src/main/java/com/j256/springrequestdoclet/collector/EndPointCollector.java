package com.j256.springrequestdoclet.collector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;
import com.sun.javadoc.AnnotationValue;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.Type;

/**
 * Collector that processed the class documentation and extracts and processes the information.
 * 
 * @author graywatson
 */
public class EndPointCollector {

	private static final String REQUEST_MAPPING_ANNOTATION_NAME = "RequestMapping";
	private static final String REQUEST_PARAM_ANNOTATION_NAME = "RequestParam";
	private static final String REQUEST_BODY_ANNOTATION_NAME = "RequestBody";
	private static final String PATH_VARIABLE_ANNOTATION_NAME = "PathVariable";
	private static final String REQUEST_HEADER_ANNOTATION_NAME = "RequestHeader";

	private static final String REQUEST_METHOD_PACKAGE_PREFIX =
			"org.springframework.web.bind.annotation.RequestMethod.";
	private static final Pattern JAVADOC_PARAM_PATTERN = Pattern.compile("(?s)@param\\s+([^\\s]+)\\s+([^@]+)");
	private static final Pattern JAVADOC_RETURN_PATTERN = Pattern.compile("(?s)@return\\s+([^@]+)");

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
		Set<String> methodNameSet = new HashSet<String>();
		for (MethodDoc methodDoc : classDoc.methods()) {
			handleMethod(classInfo, methodNameSet, methodDoc);
		}
	}

	public Map<String, List<EndPoint>> getPathInfoMap() {
		return pathInfoMap;
	}

	/**
	 * Process the annotations from each of the methods looking for a @RequestMapping and/or @RequestMethod.
	 */
	private void handleMethod(ClassInfo classInfo, Set<String> methodNameSet, MethodDoc methodDoc) {

		AnnotationDesc requestMapping = findAnnotation(methodDoc.annotations(), REQUEST_MAPPING_ANNOTATION_NAME);
		if (requestMapping == null) {
			return;
		}

		// @RequestMapping(value = { "/auth/oauth" })
		String[] paths = findAnnotationFieldValues(requestMapping, "value");
		// @RequestMapping(method = { RequestMethod.GET })
		String[] httpMethods = findAnnotationFieldValues(requestMapping, "method");
		if (httpMethods != null) {
			for (int i = 0; i < httpMethods.length; i++) {
				if (httpMethods[i].startsWith(REQUEST_METHOD_PACKAGE_PREFIX)) {
					httpMethods[i] = httpMethods[i].substring(REQUEST_METHOD_PACKAGE_PREFIX.length());
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

		// process the parameters looking for a @RequestBody parameter
		ContentsInfo requestInfo = null;
		for (Parameter param : methodDoc.parameters()) {
			ContentsInfo contentsInfo = handleRequestBodyParam(param, methodJavaDoc);
			if (contentsInfo != null) {
				requestInfo = contentsInfo;
				break;
			}
		}

		// process the returned class to see if it is @ResponseBody
		ContentsInfo responseInfo = handleResponseBody(methodDoc);

		if (paramInfos.isEmpty()) {
			paramInfos = null;
		}

		String uniqueName = methodDoc.name();
		for (int i = 2; !methodNameSet.add(uniqueName); i++) {
			uniqueName = methodDoc.name() + i;
		}

		MethodInfo methodInfo =
				new MethodInfo(methodDoc.name(), uniqueName, methodJavaDoc, javaDocFirstSentence(methodJavaDoc), paths,
						httpMethods, params, headers, consumes, produces, paramInfos, requestInfo, responseInfo);

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
		String typeName = generateTypeName(param.type());

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
			return ParamInfo.fromRequestParam(param.name(), typeName, queryParamName, required, defaultValue, javaDoc);
		}

		// @RequestMapping("/request/{schoolId}") public void request(@PathVariable("schoolId) long schoolId)
		AnnotationDesc pathVariable = findAnnotation(param.annotations(), PATH_VARIABLE_ANNOTATION_NAME);
		if (pathVariable != null) {
			pathVariableName = findAnnotationFieldValue(pathVariable, "value");
			return ParamInfo.fromPathVariable(param.name(), typeName, pathVariableName, true, null, javaDoc);
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
			return ParamInfo.fromRequestHeader(param.name(), typeName, requestHeaderName, required, defaultValue,
					javaDoc);
		}

		return null;
	}

	/**
	 * Process the annotations from each of the methods looking for a @RequestMapping and/or @RequestMethod.
	 */
	private ContentsInfo handleRequestBodyParam(Parameter param, String methodJavaDoc) {

		// Ex: public void method(@RequestBody SomeObject someObject)

		AnnotationDesc requestBody = findAnnotation(param.annotations(), REQUEST_BODY_ANNOTATION_NAME);
		if (requestBody == null) {
			return null;
		} else {
			String javaDoc = extractParamDocs(methodJavaDoc, param.name());
			String typeName = generateTypeName(param.type());
			return ContentsInfo.fromRequestBody(param.name(), typeName, javaDoc, extractFieldInfos(param.type()));
		}
	}

	/**
	 * Process the return type from a method marked (probably) with @ResponseBody.
	 */
	private ContentsInfo handleResponseBody(MethodDoc methodDoc) {

		// Ex: public @ResponseBody SomeObject method() {

		Type type = methodDoc.returnType();
		if (type == null || "void".equals(type.typeName())) {
			return null;
		}

		// try to extract the @return javadoc information
		String javaDoc = null;
		if (methodDoc.getRawCommentText() != null) {
			Matcher matcher = JAVADOC_RETURN_PATTERN.matcher(methodDoc.getRawCommentText());
			if (matcher.find()) {
				javaDoc = javaDocFirstSentence(matcher.group(1));
			}
		}

		String typeName = generateTypeName(type);
		return ContentsInfo.fromResponse(typeName, javaDoc, extractFieldInfos(type));
	}

	private String generateTypeName(Type type) {
		StringBuilder sb = new StringBuilder();
		sb.append(type.typeName());
		if (type.dimension() != null) {
			sb.append(type.dimension());
		}
		return sb.toString();
	}

	/**
	 * Extract field information from a type which is either a method parameter or a return object.
	 */
	private List<FieldInfo> extractFieldInfos(Type type) {
		if (type.isPrimitive()) {
			return null;
		}
		String typeName = type.typeName();
		// skip the core objects
		if ("Boolean".equals(typeName) || "Byte".equals(typeName) || "Short".equals(typeName)
				|| "Integer".equals(typeName) || "Long".equals(typeName) || "Float".equals(typeName)
				|| "Double".equals(typeName) || "String".equals(typeName)) {
			return null;
		}

		ClassDoc classDoc = type.asClassDoc();
		if (classDoc == null) {
			return null;
		}
		MethodDoc[] methodDocs = classDoc.methods();
		if (methodDocs == null) {
			return null;
		}

		List<FieldInfo> fieldInfos = new ArrayList<FieldInfo>(methodDocs.length);
		for (MethodDoc methodDoc : methodDocs) {
			String methodName = methodDoc.name();
			String fieldName = null;
			if (methodName.startsWith("get") && methodName.length() > 3) {
				fieldName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
			} else if (methodName.startsWith("is") && methodName.length() > 2) {
				fieldName = Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
			}
			if (fieldName != null) {
				fieldInfos.add(new FieldInfo(fieldName, methodDoc.returnType().typeName(),
						javaDocFirstSentence(methodDoc.getRawCommentText())));
			}
		}
		return fieldInfos;
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
