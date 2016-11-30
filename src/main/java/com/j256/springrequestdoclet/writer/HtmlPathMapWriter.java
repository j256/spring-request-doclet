package com.j256.springrequestdoclet.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.j256.springrequestdoclet.collector.ClassInfo;
import com.j256.springrequestdoclet.collector.EndPoint;
import com.j256.springrequestdoclet.collector.MethodInfo;
import com.j256.springrequestdoclet.collector.ParamInfo;

/**
 * Writes out a HTML file describing the path information.
 * 
 * @author graywatson
 */
public class HtmlPathMapWriter implements PathMapWriter {

	private static final String CLASS_SUBDIR = "classes";
	private static final String CLASS_SUMMARY_FILE = "classes.html";

	private Map<String, String> classNameMap = new HashMap<String, String>();
	private Set<String> classFileNameSet = new HashSet<String>();

	@Override
	public void write(Map<String, List<EndPoint>> endPointMap) throws IOException {
		// write an index.html for all of the paths linking to path details
		writePathSummary(endPointMap, "Path Summary", new File("index.html"));
		// write an index.html for all of the paths linking to path details
		writeClassSummary(endPointMap, "Class Summary", new File(CLASS_SUMMARY_FILE));
		// write a file for each class
		writeClassFiles(endPointMap);
	}

	private void writePathSummary(Map<String, List<EndPoint>> endPointMap, String title, File file) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		try {
			writePathSummary(endPointMap, title, out);
		} finally {
			out.close();
		}
	}

	private void writePathSummary(Map<String, List<EndPoint>> endPointMap, String title, BufferedWriter out)
			throws IOException {

		writeHeader(title, out);

		List<String> pathList = new ArrayList<String>(endPointMap.keySet());
		Collections.sort(pathList);
		writeLine(out, "<table>");
		writeLine(out,
				"<tr><th rowspan=\"2\"> Path </th><th colspan=\"3\"> Request Narrowing </th>"
						+ "<th rowspan=\"2\"> Class </th><th rowspan=\"2\"> Method </th>"
						+ "<th rowspan=\"2\"> Description </th></tr>");
		writeLine(out, "<tr><th> GET/POST </th><th> Param(s) </th><th> Other </th>");
		for (String path : pathList) {

			// maybe we should break down the paths by element if there are a lot of them
			// /auth/foo + /auth/bar -> /auth/ page and the foo and bar

			List<EndPoint> endPoints = endPointMap.get(path);
			boolean first = true;
			for (EndPoint endPoint : endPoints) {
				out.write("<tr>");
				if (first) {
					out.write("<td rowspan=\"" + endPoints.size() + "\"> " + path + "</td>");
					first = false;
				}
				out.write("<td> ");
				MethodInfo methodInfo = endPoint.getMethodInfo();
				String[] methods = methodInfo.getMethods();
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
				String classFileName = javaClassNameToPath(classInfo);
				out.write("<a href=\"" + classFileName + ".html\">" + htmlEscape(classInfo.getClassName()) + "</a>");
				out.write("</td><td> ");
				out.write(htmlEscape(methodInfo.getJavaMethodName()) + "(...)");
				out.write("</td><td> ");
				writeIfNotNull(out, methodInfo.getJavadoc());
				writeLine(out, "</td></tr>");
			}
		}

		writeLine(out, "</table>");
		writeTrailer(out, null);
	}

	private void writeClassSummary(Map<String, List<EndPoint>> endPointMap, String title, File file)
			throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		try {
			writeClassSummary(endPointMap, title, out);
		} finally {
			out.close();
		}
	}

	private void writeClassSummary(Map<String, List<EndPoint>> endPointMap, String title, BufferedWriter out)
			throws IOException {

		writeHeader(title, out);

		// make a map of class -> paths
		Map<ClassInfo, List<String>> classInfoMap = new HashMap<ClassInfo, List<String>>();
		for (Entry<String, List<EndPoint>> entry : endPointMap.entrySet()) {
			for (EndPoint endPoint : entry.getValue()) {
				ClassInfo classInfo = endPoint.getClassInfo();
				List<String> pathList = classInfoMap.get(classInfo);
				if (pathList == null) {
					pathList = new ArrayList<String>();
					classInfoMap.put(classInfo, pathList);
				}
				pathList.add(entry.getKey());
			}
		}

		// sort by class name
		List<ClassInfo> classInfoList = new ArrayList<ClassInfo>(classInfoMap.keySet());
		Collections.sort(classInfoList);

		writeLine(out, "<table>");
		writeLine(out, "<tr><th> Class </th><th> Paths </th><th> Description </th></tr>");
		for (ClassInfo classInfo : classInfoList) {
			String classPath = javaClassNameToPath(classInfo);
			out.write(
					"<tr><td><a href=\"" + classPath + ".html\">" + htmlEscape(classInfo.getClassName()) + "</a></td>");
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
			writeLine(out, "</td><td>");
			writeIfNotNull(out, htmlEscape(classInfo.getJavaDocFirstSentence()));
			writeLine(out, "</td></tr>");
		}
		writeLine(out, "</table>");
		writeTrailer(out, null);
	}

	private void writeClassFiles(Map<String, List<EndPoint>> endPointMap) throws IOException {
		File classSubdir = new File(CLASS_SUBDIR);
		classSubdir.mkdirs();
		Map<ClassInfo, List<EndPoint>> classInfoMap = new HashMap<ClassInfo, List<EndPoint>>();
		for (Entry<String, List<EndPoint>> entry : endPointMap.entrySet()) {
			for (EndPoint endPoint : entry.getValue()) {
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
		String classPath = javaClassNameToPath(classInfo);
		BufferedWriter out = new BufferedWriter(new FileWriter(classPath + ".html"));
		try {
			writeClassFile(classInfo, endPoints, out);
		} finally {
			out.close();
		}
	}

	private void writeClassFile(ClassInfo classInfo, List<EndPoint> endPoints, BufferedWriter out) throws IOException {

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
		if (javaDoc != null && !javaDoc.isEmpty()) {
			writeLine(out, "<p> ", javaDoc, "</p>");
		}

		writeMethodInfo(out, methodInfoList, methodPathMap);
		writeParamInfo(out, methodInfoList);
		writeTrailer(out, "../");
	}

	private void writeMethodInfo(BufferedWriter out, List<MethodInfo> methodInfoList,
			Map<MethodInfo, String> methodPathMap) throws IOException {
		writeLine(out, "<table>");
		writeLine(out, "<tr><th colspan=\"7\"> Method Information </th></tr>");
		writeLine(out, "<tr><th rowspan=\"2\"> Method </th><th colspan=\"5\"> Request Narrowing </th>"
				+ "<th rowspan=\"2\"> Description </th></tr>");
		writeLine(out, "<tr><th> Path(s) </th><th> GET/POST </th><th> Params </th><th> Headers </th>"
				+ "<th> Content Types </th></tr>");
		for (MethodInfo methodInfo : methodInfoList) {
			out.write("<tr><td>");
			out.write(htmlEscape(methodInfo.getJavaMethodName()) + "(...)");
			out.write("</td><td>");
			writeIfNotNull(out, methodPathMap.get(methodInfo));
			out.write("</td><td>");
			String[] methods = methodInfo.getMethods();
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
			writeIfNotNull(out, methodInfo.getJavadoc());
			writeLine(out, "</td></tr>");
		}
		writeLine(out, "</table><br />");
	}

	private void writeParamInfo(BufferedWriter out, List<MethodInfo> methodInfoList) throws IOException {
		writeLine(out, "<table>");
		writeLine(out, "<tr><th colspan=\"7\"> Method Params </th></tr>");
		writeLine(out, "<tr><th> Method </th><th> Param Name </th><th> Request </th><th> Type </th>"
				+ "<th> Required </th><th> Default </th><th> Param Description </th></tr>");
		for (MethodInfo methodInfo : methodInfoList) {
			List<ParamInfo> paramInfos = methodInfo.getParamInfos();
			if (paramInfos != null && !paramInfos.isEmpty()) {
				boolean first = true;
				for (ParamInfo paramInfo : paramInfos) {
					out.write("<tr>");
					if (first) {
						out.write("<td rowspan=\"" + paramInfos.size() + "\"> "
								+ htmlEscape(methodInfo.getJavaMethodName()) + "(...)");
						first = false;
					}
					out.write(" </td><td> " + htmlEscape(paramInfo.getRequestName()));
					out.write(" </td><td> " + htmlEscape(paramInfo.getRequestKind().getDescription()));
					out.write(" </td><td> " + htmlEscape(paramInfo.getJavaTypeName()));
					out.write(" </td><td> " + (paramInfo.isRequired() ? true : "&nbsp;"));
					out.write(" </td><td> ");
					writeIfNotNull(out, paramInfo.getDefaultValue());
					out.write(" </td><td> ");
					writeIfNotNull(out, paramInfo.getJavaDoc());
					writeLine(out, "</td></tr>");
				}
			}
		}
		writeLine(out, "</table>");
	}

	private void writeHeader(String title, BufferedWriter out) throws IOException {
		writeLine(out, "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>");
		writeLine(out, "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"");
		writeLine(out, "    \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
		writeLine(out, "<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		writeLine(out, "<head>");
		writeLine(out, "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\" />");
		writeLine(out, "<title> ", title, " </title>");
		writeLine(out, "<style>");
		writeLine(out, "  table { border-collapse: collapse; }");
		writeLine(out, "  table, th, td { border: 1px solid black; }");
		writeLine(out, "  th, td { padding: 5px; }");
		// writeLine(out, " tr:hover {background-color: #f5f5f5}");
		writeLine(out, "  tr:nth-child(even) { background-color: #f2f2f2; }");
		writeLine(out, "</style>");
		writeLine(out, "</head>");
		writeLine(out, "<body>");
		writeLine(out, "<h1> ", title, " </h1>");
	}

	private void writeTrailer(BufferedWriter out, String relativePathToRoot) throws IOException {
		if (relativePathToRoot == null) {
			relativePathToRoot = "./";
		}
		writeLine(out, "<p> <a href=\"" + relativePathToRoot + "index.html\">Path summary<a> &nbsp;&nbsp;&nbsp;&nbsp;"
				+ "  <a href=\"" + relativePathToRoot + CLASS_SUMMARY_FILE + "\">Class summary</a> </p>");
		writeLine(out, "</body>");
		writeLine(out, "</html>");
	}

	private void writeLine(BufferedWriter out, String string) throws IOException {
		out.write(string);
		out.newLine();
	}

	private void writeLine(BufferedWriter out, String... strings) throws IOException {
		for (String str : strings) {
			out.write(str);
		}
		out.newLine();
	}

	private boolean isEmpty(String[] array) {
		return (array == null || array.length == 0);
	}

	private void printArray(BufferedWriter out, String prefix, String[] array) throws IOException {
		if (array == null || array.length == 0) {
			return;
		}
		writeIfNotNull(out, prefix);
		for (int i = 0; i < array.length; i++) {
			if (i > 0) {
				out.write(", ");
			}
			out.write(htmlEscape(array[i]));
		}
	}

	private void writeIfNotNull(BufferedWriter out, String string) throws IOException {
		if (string != null) {
			out.write(htmlEscape(string));
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
		String path = classNameMap.get(classInfo.getTypeName());
		if (path != null) {
			return path;
		}
		StringBuilder sb = new StringBuilder(CLASS_SUBDIR);
		sb.append(File.separatorChar);
		for (char ch : classInfo.getClassName().toCharArray()) {
			if (Character.isDigit(ch) || Character.isLetter(ch)) {
				sb.append(ch);
			} else {
				sb.append('_');
			}
		}
		String rawPath = sb.toString();
		path = rawPath;
		int i = 2;
		while (!classFileNameSet.add(path)) {
			path = rawPath + i;
		}
		classNameMap.put(classInfo.getTypeName(), path);
		return path;
	}
}
