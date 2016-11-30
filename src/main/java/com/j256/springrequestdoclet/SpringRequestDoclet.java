package com.j256.springrequestdoclet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.j256.springrequestdoclet.collector.EndPoint;
import com.j256.springrequestdoclet.collector.EndPointCollector;
import com.j256.springrequestdoclet.writer.HtmlPathMapWriter;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;

/**
 * Spring request doclet main class. Although it extends @{link Doclet} is really a collection of static methods that (I
 * guess) are called via reflection.
 * 
 * @author graywatson
 */
public class SpringRequestDoclet extends Doclet {

	private static HtmlPathMapWriter writer = new HtmlPathMapWriter();

	/**
	 * Actually do the processing of the variable class information so we can general the documentation output.
	 */
	public static boolean start(RootDoc root) {
		// run our collector to convert the root doc information
		EndPointCollector collector = new EndPointCollector();
		for (ClassDoc classDoc : root.classes()) {
			collector.processClass(classDoc);
		}

		// now write out all of the documentation we've collected
		Map<String, List<EndPoint>> endPointMap = collector.getPathInfoMap();
		try {
			writer.write(endPointMap);
			return true;
		} catch (IOException ioe) {
			// print out the exception and return error
			ioe.printStackTrace();
			return false;
		}
	}

	/**
	 * This method may be necessary to expose show generic arguments in the class information.
	 */
	public static LanguageVersion languageVersion() {
		return LanguageVersion.JAVA_1_5;
	}

	/**
	 * This method is necessary otherwise syntax errors because of invalid options is generated. At least "return 1;" is
	 * required. "return 0;" is option unknown. There are options that this specific doclet uses but there is also other
	 * options that are part of the javadoc calls -- at least when done via maven.
	 */
	public static int optionLength(String option) {
		// this allows other unknown options
		return 1;

		// NOTE: if we were processing arguments
		// if ("-o".equals(option)) {
		// // -o + argument
		// return 2;
		// }
		// return 0; means option unknown
	}

	/**
	 * This method is necessary otherwise syntax errors are generated because of invalid options is generated. This is
	 * also how the options and any arguments are processed. There is where we could set various output flags or maybe
	 * choose a different collector or writer.
	 */
	public static boolean validOptions(String[][] options, DocErrorReporter docErrorReporter) {
		// for (int optCount = 0; optCount < options.length; optCount++) {
		// for (String arg : options[optCount]) {
		// if ("-o".equals(arg)) {
		// ...
		// }
		// }
		// }
		return true;
	}
}
