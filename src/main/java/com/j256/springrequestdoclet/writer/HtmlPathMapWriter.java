package com.j256.springrequestdoclet.writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.j256.springrequestdoclet.collector.ClassInfo;
import com.j256.springrequestdoclet.collector.ContentsInfo;
import com.j256.springrequestdoclet.collector.EndPoint;
import com.j256.springrequestdoclet.collector.FieldInfo;
import com.j256.springrequestdoclet.collector.MethodInfo;
import com.j256.springrequestdoclet.collector.ParamInfo;

/**
 * Writes out a HTML file describing the path information.
 * 
 * @author graywatson
 */
public class HtmlPathMapWriter implements EndPointMapWriter {

	private static final String CLASS_SUBDIR = "classes";
	private static final String METHOD_SUBDIR = "methods";
	private static final String CLASS_METHOD_SUBDIR = CLASS_SUBDIR + File.separatorChar + METHOD_SUBDIR;
	private static final String CLASS_METHOD_SUBDIR_HTML = CLASS_SUBDIR + '/' + METHOD_SUBDIR;
	private static final String CLASS_SUMMARY_FILE = "classes.html";
	private static final String METHOD_NAME_SUFFIX = "(...)";
	private static final Pattern JAVADOC_CLEANUP_PATTERN = Pattern.compile("(?sm)^\\s*[@]");

	private Map<String, String> classNameMap = new HashMap<String, String>();
	private Map<String, String> methodNameMap = new HashMap<String, String>();
	private Set<String> classPathSet = new HashSet<String>();
	private Set<String> methodPathSet = new HashSet<String>();

	@Override
	public void write(Map<String, List<EndPoint>> endPointMap) throws IOException {
		// write a file for each class method, we do this first to build the methodNameMap
		writeMethodFiles(endPointMap.values());
		// write a path summary into our index.html
		writePathSummary(endPointMap, new File("index.html"));
		// write an index.html for all of the paths linking to path details
		writeClassSummary(endPointMap, new File(CLASS_SUMMARY_FILE));
		// write a file for each class
		writeClassFiles(endPointMap);
	}

	private void writePathSummary(Map<String, List<EndPoint>> endPointMap, File file) throws IOException {
		PrintWriter out = new PrintWriter(file);
		try {
			writePathSummary(endPointMap, out);
		} finally {
			out.close();
		}
	}

	private void writePathSummary(Map<String, List<EndPoint>> endPointMap, PrintWriter out) {

		writeHeader("Path Summary", out);

		out.println("<p> The following is a request path summary for the classes in this library. "
				+ "The request narrowing fields give details about how the requests are routed "
				+ "to the various different classes and methods to be handled.  More documentation "
				+ "is available at the class level which shows parameter details. </p>");

		List<String> pathList = new ArrayList<String>(endPointMap.keySet());
		Collections.sort(pathList);
		out.println("<table>");
		out.println("<tr><th colspan='4'> Request Narrowing </th>"
				+ "<th rowspan='2'> Class </th><th rowspan='2'> Method </th>"
				+ "<th rowspan='2'> Description </th></tr>");
		out.println("<tr><th> Path </th><th> GET/POST </th><th> Param(s) </th><th> Other </th>");
		for (String path : pathList) {

			// maybe we should break down the paths by element if there are a lot of them
			// /auth/foo + /auth/bar -> /auth/ page and the foo and bar

			List<EndPoint> endPoints = endPointMap.get(path);
			boolean first = true;
			for (EndPoint endPoint : endPoints) {
				out.write("<tr>");
				if (first) {
					out.write("<td rowspan='" + endPoints.size() + "'> " + htmlEscape(path) + "</td>");
					first = false;
				}
				out.write("<td> ");
				MethodInfo methodInfo = endPoint.getMethodInfo();
				String[] methods = methodInfo.getHttpMethods();
				printArray(out, null, methods);
				out.write("</td><td> ");
				String[] params = methodInfo.getParams();
				printArray(out, null, params);
				out.write("</td><td> ");
				String[] headers = methodInfo.getHeaders();
				boolean firstOther = true;
				if (!isEmpty(headers)) {
					printArray(out, "Headers: ", headers);
					firstOther = false;
				}
				String[] consumes = methodInfo.getConsumes();
				if (!isEmpty(consumes)) {
					if (!firstOther) {
						out.write(" <br />");
					}
					printArray(out, "Consumes: ", consumes);
					firstOther = false;
				}
				String[] produces = methodInfo.getProduces();
				if (!isEmpty(produces)) {
					if (!firstOther) {
						out.write(" <br />");
					}
					printArray(out, "Produces: ", produces);
					firstOther = false;
				}
				out.write("</td><td> ");
				ClassInfo classInfo = endPoint.getClassInfo();
				String classFilePath = javaClassNameToPath(classInfo);
				out.write("<a href='" + CLASS_SUBDIR + File.separatorChar + classFilePath + ".html'>"
						+ htmlEscape(classInfo.getClassName()) + "</a>");
				out.write("</td><td> ");
				String classMethodFileName = javaClassMathodNameToPath(classInfo, methodInfo);
				out.write("<a href='" + CLASS_METHOD_SUBDIR_HTML + File.separatorChar + classMethodFileName + ".html'>"
						+ htmlEscape(methodInfo.getJavaMethodName()) + METHOD_NAME_SUFFIX + "</a>");
				out.write("</td><td> ");
				writeIfNotNull(out, methodInfo.getJavaDocFirstSentence(), "&nbsp;");
				out.println("</td></tr>");
			}
		}

		out.println("</table>");
		writeTrailer(out, null);
	}

	private void writeClassSummary(Map<String, List<EndPoint>> endPointMap, File file) throws IOException {
		PrintWriter out = new PrintWriter(file);
		try {
			writeClassSummary(endPointMap, out);
		} finally {
			out.close();
		}
	}

	private void writeClassSummary(Map<String, List<EndPoint>> endPointMap, PrintWriter out) {

		writeHeader("Class Summary", out);

		out.println("<p> The following is a class summary showing the classes and their "
				+ "associated path handling. </p>");

		// make a map of class -> paths
		Map<ClassInfo, Set<String>> classInfoMap = new HashMap<ClassInfo, Set<String>>();
		for (Entry<String, List<EndPoint>> entry : endPointMap.entrySet()) {
			for (EndPoint endPoint : entry.getValue()) {
				ClassInfo classInfo = endPoint.getClassInfo();
				Set<String> pathList = classInfoMap.get(classInfo);
				if (pathList == null) {
					pathList = new LinkedHashSet<String>();
					classInfoMap.put(classInfo, pathList);
				}
				pathList.add(entry.getKey());
			}
		}

		// sort by class name
		List<ClassInfo> classInfoList = new ArrayList<ClassInfo>(classInfoMap.keySet());
		Collections.sort(classInfoList);

		out.println("<table>");
		out.println("<tr><th> Class </th><th> Paths </th><th> Description </th></tr>");
		for (ClassInfo classInfo : classInfoList) {
			String classFilePath = javaClassNameToPath(classInfo);
			out.write("<tr><td><a href='" + CLASS_SUBDIR + File.separatorChar + classFilePath + ".html'>"
					+ htmlEscape(classInfo.getClassName()) + "</a></td>");
			out.write("<td>");
			boolean first = true;
			for (String path : classInfoMap.get(classInfo)) {
				if (first) {
					first = false;
				} else {
					out.write(", ");
				}
				out.write(path);
			}
			out.println("</td><td>");
			writeIfNotNull(out, htmlEscape(classInfo.getJavaDocFirstSentence()), "&nbsp;");
			out.println("</td></tr>");
		}
		out.println("</table>");
		writeTrailer(out, null);
	}

	private void writeClassFiles(Map<String, List<EndPoint>> endPointMap) throws IOException {
		File classSubdir = new File(CLASS_SUBDIR);
		classSubdir.mkdirs();
		Map<ClassInfo, List<EndPoint>> classInfoMap = new HashMap<ClassInfo, List<EndPoint>>();
		for (List<EndPoint> endPoints : endPointMap.values()) {
			for (EndPoint endPoint : endPoints) {
				ClassInfo classInfo = endPoint.getClassInfo();
				List<EndPoint> entPointList = classInfoMap.get(classInfo);
				if (entPointList == null) {
					entPointList = new ArrayList<EndPoint>();
					classInfoMap.put(classInfo, entPointList);
				}
				entPointList.add(endPoint);
			}
		}

		for (Entry<ClassInfo, List<EndPoint>> entry : classInfoMap.entrySet()) {
			writeClassFile(entry.getKey(), entry.getValue());
		}
	}

	private void writeClassFile(ClassInfo classInfo, List<EndPoint> endPoints) throws IOException {
		String classFilePath = javaClassNameToPath(classInfo);
		PrintWriter out = new PrintWriter(CLASS_SUBDIR + File.separatorChar + classFilePath + ".html");
		try {
			writeClassFile(classInfo, endPoints, out);
		} finally {
			out.close();
		}
	}

	private void writeClassFile(ClassInfo classInfo, List<EndPoint> endPoints, PrintWriter out) {

		writeHeader("Class " + classInfo.getClassName(), out);

		// gather up the methods so we can sort them
		List<MethodInfo> methodInfoList = new ArrayList<MethodInfo>();
		Map<MethodInfo, String> methodPathMap = new HashMap<MethodInfo, String>();
		for (EndPoint endPoint : endPoints) {
			methodInfoList.add(endPoint.getMethodInfo());
			methodPathMap.put(endPoint.getMethodInfo(), endPoint.getPath());
		}
		// sort by class name
		Collections.sort(methodInfoList);

		String javaDoc = classInfo.getJavaDoc();
		if (javaDoc != null && javaDoc.isEmpty()) {
			javaDoc = null;
		}

		out.print("<p> The following is documentation for a single class.");
		if (javaDoc != null) {
			out.print("The full javadocs are the bottom.");
		}
		out.println("</p>");

		String javaDocFirst = classInfo.getJavaDocFirstSentence();
		if (javaDocFirst != null && !javaDocFirst.isEmpty()) {
			// NOTE: javadoc might have html which we hope is ok
			out.println("<p style='margin-left: 2em; margin-right: 2em;'> Javadoc summary: " + javaDocFirst + "</p>");
		}

		writeMethodInfo(out, classInfo, methodInfoList, methodPathMap, METHOD_SUBDIR);

		if (javaDoc != null && !javaDoc.equals(javaDocFirst)) {
			// NOTE: javadoc might have html which we hope is ok
			printJavaDocs(out, javaDoc);
		}

		writeTrailer(out, "../");
	}

	private void writeMethodFiles(Collection<List<EndPoint>> pathEndPoints) throws FileNotFoundException {
		File methodSubdir = new File(CLASS_METHOD_SUBDIR);
		methodSubdir.mkdirs();
		for (List<EndPoint> pathEndPoint : pathEndPoints) {
			for (EndPoint endPoint : pathEndPoint) {
				String classMethodPath = javaClassMathodNameToPath(endPoint.getClassInfo(), endPoint.getMethodInfo());
				PrintWriter out = new PrintWriter(CLASS_METHOD_SUBDIR + File.separatorChar + classMethodPath + ".html");
				try {
					writeMethodFile(endPoint, out);
				} finally {
					out.close();
				}
			}
		}
	}

	private void writeMethodFile(EndPoint endPoint, PrintWriter out) {

		ClassInfo classInfo = endPoint.getClassInfo();
		MethodInfo methodInfo = endPoint.getMethodInfo();
		writeHeader("Method " + classInfo.getClassName() + ". " + methodInfo.getJavaMethodName() + METHOD_NAME_SUFFIX,
				out);

		String javaDoc = methodInfo.getJavaDoc();
		if (javaDoc != null && javaDoc.isEmpty()) {
			javaDoc = null;
		}

		out.print("<p> The following is documentation for a single method.");
		if (javaDoc != null) {
			out.print("The full javadocs are the bottom.");
		}
		out.println("</p>");

		writeMethodInfo(out, classInfo, Collections.singletonList(methodInfo),
				Collections.singletonMap(methodInfo, endPoint.getPath()), null);
		out.println("<br />\n");
		if (methodInfo.getRequestInfo() == null) {
			writeParamInfo(out, methodInfo);
		} else {
			writeContentsInfo(out, methodInfo, methodInfo.getRequestInfo(), "Request Fields POSTed to Method");
		}

		if (methodInfo.getResponseInfo() != null) {
			out.println("<br />\n");
			writeContentsInfo(out, methodInfo, methodInfo.getResponseInfo(), "Response Fields Returned to Client");
		}

		if (javaDoc != null) {
			// NOTE: javadoc might have html which we hope is ok
			printJavaDocs(out, javaDoc);
		}

		writeTrailer(out, "../../");
	}

	private void printJavaDocs(PrintWriter out, String javaDoc) {
		if (javaDoc == null || javaDoc.isEmpty()) {
			return;
		}

		Matcher matcher = JAVADOC_CLEANUP_PATTERN.matcher(javaDoc);
		out.print("<p style='margin-left: 2em; margin-right: 2em;'>");
		int start = 0;
		while (matcher.find(start)) {
			out.print(javaDoc.substring(start, matcher.start()));
			out.println("<br />");
			start = matcher.start() + 1;
		}
		out.print(javaDoc.substring(start));
		out.println("</p>");
	}

	private void writeMethodInfo(PrintWriter out, ClassInfo classInfo, List<MethodInfo> methodInfoList,
			Map<MethodInfo, String> methodPathMap, String subDir) {
		out.println("<table>");
		out.println("<tr><th colspan='7'> Method Information </th></tr>");
		out.println("<tr><th rowspan='2'> Method </th><th colspan='5'> Request Narrowing </th>"
				+ "<th rowspan='2'> Description </th></tr>");
		out.println("<tr><th> Path(s) </th><th> GET/POST </th><th> Params </th><th> Headers </th>"
				+ "<th> Content Types </th></tr>");
		for (MethodInfo methodInfo : methodInfoList) {
			writeMethodInfoRow(out, classInfo, methodInfo, methodPathMap.get(methodInfo), subDir);
		}
		out.println("</table>");
	}

	private void writeMethodInfoRow(PrintWriter out, ClassInfo classInfo, MethodInfo methodInfo, String methodPath,
			String subDir) {
		out.write("<tr><td>");
		if (subDir != null) {
			String classMethodFileName = javaClassMathodNameToPath(classInfo, methodInfo);
			out.write("<a href='" + subDir + '/' + classMethodFileName + ".html'>");
		}
		out.write(htmlEscape(methodInfo.getJavaMethodName()) + METHOD_NAME_SUFFIX);
		if (subDir != null) {
			out.write("</a>");
		}
		out.write("</td><td>");
		writeIfNotNull(out, methodPath, "&nbsp;");
		out.write("</td><td>");
		String[] methods = methodInfo.getHttpMethods();
		printArray(out, null, methods);
		out.write("</td><td> ");
		String[] params = methodInfo.getParams();
		printArray(out, null, params);
		out.write("</td><td> ");
		String[] headers = methodInfo.getHeaders();
		printArray(out, "Headers: ", headers);
		out.write("</td><td> ");
		String[] consumes = methodInfo.getConsumes();
		boolean firstOther = true;
		if (!isEmpty(consumes)) {
			printArray(out, "Consumes: ", consumes);
			firstOther = false;
		}
		String[] produces = methodInfo.getProduces();
		if (!isEmpty(produces)) {
			if (!firstOther) {
				out.write(" <br />");
			}
			printArray(out, "Produces: ", produces);
			firstOther = false;
		}
		out.write("</td><td> ");
		writeIfNotNull(out, methodInfo.getJavaDocFirstSentence(), "&nbsp;");
		out.println("</td></tr>");
	}

	private void writeParamInfo(PrintWriter out, MethodInfo methodInfo) {
		List<ParamInfo> paramInfos = methodInfo.getParamInfos();
		if (paramInfos == null || paramInfos.isEmpty()) {
			return;
		}
		out.println("<table>");
		out.println("<tr><th colspan='7'> Method Parameters </th></tr>");
		out.println("<tr><th> Method </th><th> Param Name </th><th> Request </th><th> Data Type </th>"
				+ "<th> Required </th><th> Default </th><th> Description </th></tr>");
		boolean first = true;
		for (ParamInfo paramInfo : paramInfos) {
			out.write("<tr>");
			if (first) {
				out.write("<td rowspan='" + paramInfos.size() + "'> " + htmlEscape(methodInfo.getJavaMethodName())
						+ METHOD_NAME_SUFFIX + "</td>");
				first = false;
			}
			out.write("<td> " + (paramInfo.getRequestName() == null ? "" : htmlEscape(paramInfo.getRequestName()))
					+ "</td>");
			out.write("<td> " + htmlEscape(paramInfo.getRequestKind().getDescription()) + "</td>");
			out.write("<td> " + htmlEscape(paramInfo.getJavaTypeName()) + "</td>");
			out.write("<td> " + (paramInfo.isRequired() ? true : "&nbsp;" + "</td>"));
			out.write("<td> ");
			writeIfNotNull(out, paramInfo.getDefaultValue(), "&nbsp;");
			out.write(" </td><td> ");
			writeIfNotNull(out, paramInfo.getJavaDoc(), "&nbsp;");
			out.println("</td></tr>");
		}
		out.println("</table>");
	}

	private void writeContentsInfo(PrintWriter out, MethodInfo methodInfo, ContentsInfo bodyInfo, String label) {

		out.println("<table>");
		out.println("<tr><th colspan='7'> " + label + " </th></tr>");
		out.println("<tr><th> Method </th><th> Field Name </th><th> Data Type </th><th> Description </th></tr>");
		out.write("<tr>");
		List<FieldInfo> fieldInfos = bodyInfo.getFieldInfos();
		int height = 1;
		if (fieldInfos != null && !fieldInfos.isEmpty()) {
			height = fieldInfos.size();
		}
		out.write("<td rowspan=\"" + height + "\"> " + htmlEscape(methodInfo.getJavaMethodName()) + METHOD_NAME_SUFFIX
				+ "</td>");
		if (fieldInfos == null || fieldInfos.isEmpty()) {
			out.write("<td>&nbsp;</td>");
			out.write("<td> " + htmlEscape(bodyInfo.getJavaTypeName()) + "</td>");
			out.write("<td> ");
			writeIfNotNull(out, bodyInfo.getJavaDoc(), "&nbsp;");
			out.println("</td></tr>");
		} else {
			boolean first = true;
			for (FieldInfo fieldInfo : fieldInfos) {
				if (!first) {
					out.println("<tr>");
				}
				out.write("<td> " + htmlEscape(fieldInfo.getFieldName()) + "</td>");
				out.write("<td> " + htmlEscape(fieldInfo.getTypeName()) + "</td>");
				out.write("<td> ");
				writeIfNotNull(out, fieldInfo.getJavaDoc(), "&nbsp;");
				out.println("</td>");
				out.println("</tr>");
				first = false;
			}
		}
		out.println("</table>");
	}

	private void writeHeader(String title, PrintWriter out) {
		out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>");
		out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"");
		out.println("    \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
		out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		out.println("<head>");
		out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\" />");
		out.println("<title> " + htmlEscape(title) + " </title>");
		out.println("<style>");
		out.println("   table { border-collapse: collapse; }");
		out.println("   table, th, td { border: 1px solid black; }");
		out.println("   th, td { padding: 5px; }");
		out.println("   tr:nth-child(even) { background-color: #f2f2f2; }");
		out.println("   body { width: 80%; }");
		out.println("</style>");
		out.println("</head>");
		out.println("<body>");
		out.println("<h1> " + htmlEscape(title) + " </h1>");
	}

	private void writeTrailer(PrintWriter out, String relativePathToRoot) {
		if (relativePathToRoot == null) {
			relativePathToRoot = "./";
		}
		out.println("<p> <a href='" + relativePathToRoot + "index.html'>Path summary<a> &nbsp;&nbsp;&nbsp;&nbsp;"
				+ "  <a href='" + relativePathToRoot + CLASS_SUMMARY_FILE + "'>Class summary</a> </p>");
		out.println("<p style='font-size: 75%;'> Generated by <a "
				+ "href='http://256stuff.com/sources/spring-request-doclet/'>Spring Request Doclet</a> package. </p>");
		out.println("</body>");
		out.println("</html>");
	}

	private boolean isEmpty(String[] array) {
		return (array == null || array.length == 0);
	}

	private void printArray(PrintWriter out, String prefix, String[] array) {
		if (array == null || array.length == 0) {
			return;
		}
		writeIfNotNull(out, prefix, null);
		for (int i = 0; i < array.length; i++) {
			if (i > 0) {
				out.write(", ");
			}
			out.write(htmlEscape(array[i]));
		}
	}

	private void writeIfNotNull(PrintWriter out, String string, String nullString) {
		if (string == null) {
			if (nullString != null) {
				out.print(nullString);
			}
		} else {
			out.print(htmlEscape(string));
		}
	}

	private String htmlEscape(String maybeHtml) {
		if (maybeHtml == null || maybeHtml.isEmpty()) {
			return maybeHtml;
		}
		boolean htmlChar = false;
		for (int i = 0; i < maybeHtml.length(); i++) {
			if (maybeHtml.charAt(i) == '&' || maybeHtml.charAt(i) == '<' || maybeHtml.charAt(i) == '>') {
				htmlChar = true;
			}
		}
		if (!htmlChar) {
			return maybeHtml;
		}
		StringBuilder sb = new StringBuilder(maybeHtml.length() + 10);
		for (int i = 0; i < maybeHtml.length(); i++) {
			char ch = maybeHtml.charAt(i);
			if (ch == '&') {
				sb.append("&amp;");
			} else if (ch == '<') {
				sb.append("&lt;");
			} else if (ch == '>') {
				sb.append("&gt;");
			} else {
				sb.append(ch);
			}
		}
		return sb.toString();
	}

	private String javaClassNameToPath(ClassInfo classInfo) {
		return findUniquePath(classInfo.getTypeName(), classNameMap, classPathSet);
	}

	private String javaClassMathodNameToPath(ClassInfo classInfo, MethodInfo methodInfo) {
		return findUniquePath(classInfo.getTypeName() + '.' + methodInfo.getUniqueName(), methodNameMap, methodPathSet);
	}

	private String findUniquePath(String key, Map<String, String> nameMap, Set<String> pathSet) {
		String path = nameMap.get(key);
		if (path != null) {
			return path;
		}
		char[] classNameChars = key.toCharArray();
		StringBuilder sb = new StringBuilder(classNameChars.length);
		for (char ch : classNameChars) {
			if (Character.isDigit(ch) || Character.isLetter(ch)) {
				sb.append(ch);
			} else {
				sb.append('_');
			}
		}
		String rawPath = sb.toString();
		path = rawPath;
		for (int i = 2; !pathSet.add(path); i++) {
			path = rawPath + i;
		}
		nameMap.put(key, path);
		return path;
	}
}
