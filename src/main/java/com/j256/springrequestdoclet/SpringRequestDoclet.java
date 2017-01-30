package com.j256.springrequestdoclet;

import java.io.File;
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

	private static final String ROOT_DIR_ARG = "-r";

	private static HtmlPathMapWriter writer = new HtmlPathMapWriter();

	private static String rootDirPath;

	/**
	 * Actually do the processing of the variable class information so we can general the documentation output.
	 * 
	 * @see Doclet#start(RootDoc)
	 */
	public static boolean start(RootDoc root) {
		// run our collector to convert the root doc information
		EndPointCollector collector = new EndPointCollector();
		for (ClassDoc classDoc : root.classes()) {
			collector.processClass(classDoc);
		}

		File rootDocDir = null;
		if (rootDirPath != null) {
			rootDocDir = new File(rootDirPath);
			if (!rootDocDir.isDirectory()) {
				System.err.println("Could not find root directory: " + rootDirPath);
				String userDirProp = System.getProperty("user.dir");
				System.err.println("user.dir property = " + userDirProp);
				return false;
			}
		}

		// now write out all of the documentation we've collected
		Map<String, List<EndPoint>> endPointMap = collector.getPathInfoMap();
		try {
			writer.write(endPointMap, rootDocDir);
			return true;
		} catch (IOException ioe) {
			// print out the exception and return error
			ioe.printStackTrace();
			return false;
		}
	}

	/**
	 * This method may be necessary to expose show generic arguments in the class information.
	 * 
	 * @see Doclet#languageVersion()
	 */
	public static LanguageVersion languageVersion() {
		return LanguageVersion.JAVA_1_5;
	}

	/**
	 * This method is necessary otherwise syntax errors because of invalid options is generated. At least "return 1;" is
	 * required. "return 0;" is option unknown. There may be options that this specific doclet uses but there is also
	 * other options that are part of the javadoc calls -- at least when done via maven.
	 * 
	 * @see Doclet#optionLength(String)
	 */
	public static int optionLength(String option) {
		if (ROOT_DIR_ARG.equals(option)) {
			// param + argument
			return 2;
		} else {
			// this allows other unknown options
			return 1;
			// return 0; means option unknown
		}
	}

	/**
	 * This method is necessary otherwise syntax errors are generated because of invalid options is generated. This is
	 * also how the options and any arguments are processed. There is where we could set various output flags or maybe
	 * choose a different collector or writer.
	 * 
	 * @see Doclet#validOptions(String[][], DocErrorReporter)
	 */
	public static boolean validOptions(String[][] options, DocErrorReporter docErrorReporter) {
		for (int optCount = 0; optCount < options.length; optCount++) {
			if (ROOT_DIR_ARG.equals(options[optCount][0])) {
				if (options[optCount].length < 2) {
					docErrorReporter.printError("No argument specified for: " + ROOT_DIR_ARG);
					return false;
				}
				rootDirPath = options[optCount][1];
			}
		}
		return true;
	}
}
