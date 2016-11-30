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
 * Example java doclet generator class.
 * 
 * @author graywatson
 */
public class SpringRequestDoclet extends Doclet {

	private static final SpringRequestDoclet doclet = new SpringRequestDoclet();
	private static HtmlPathMapWriter writer = new HtmlPathMapWriter();

	/**
	 * Starting the
	 */
	public static boolean start(RootDoc root) {
		return doclet.doStart(root);
	}

	/**
	 * Possibly necessary to show generic arguments.
	 */
	public static LanguageVersion languageVersion() {
		return LanguageVersion.JAVA_1_5;
	}

	/**
	 * This is necessary otherwise syntax errors because of invalid options is generated. At least "return 1;" is
	 * required. "return 0;" is option unknown.
	 */
	public static int optionLength(String option) {
		if ("-o".equals(option)) {
			// -o + argument
			return 2;
		} else {
			// this allows other unknown options
			return 1;
		}
		// return 0; means option unknown
	}

	/**
	 * This is necessary otherwise syntax errors because of invalid options is generated.
	 */
	public static boolean validOptions(String[][] options, DocErrorReporter docErrorReporter) {
		for (int optCount = 0; optCount < options.length; optCount++) {
			for (String arg : options[optCount]) {
				if ("-o".equals(arg)) {
					// ...
				}
			}
		}
		return true;
	}

	private boolean doStart(RootDoc root) {

		// start our collector
		EndPointCollector collector = new EndPointCollector();
		for (ClassDoc classDoc : root.classes()) {
			collector.processClass(classDoc);
		}

		// now print out all of the
		Map<String, List<EndPoint>> endPointMap = collector.getPathInfoMap();
		try {
			writer.write(endPointMap);
			return true;
		} catch (IOException ioe) {
			// we just print out the exception and return error
			ioe.printStackTrace();
			return false;
		}
	}
}
