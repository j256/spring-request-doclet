This package provides a simple doclet for auto-generating documentation using Java's javadoc generator
specifically for tracking spring's web requests.

* For more information, visit the [Spring Request Doclet home page](http://256stuff.com/sources/spring-request-doclet/).	
* Browse the code on the [git repository](https://github.com/j256/spring-request-doclet).  [![CircleCI](https://circleci.com/gh/j256/spring-request-doclet.svg?style=svg)](https://circleci.com/gh/j256/spring-request-doclet)
* Maven packages are published via [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.j256.spring-request-doclet/spring-request-doclet/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/com.j256.spring-request-doclet/spring-request-doclet/)

Enjoy,
Gray Watson

## Sample Output

For sample output, see this hiearchy: http://256stuff.com/sources/spring-request-doclet/sample/

## How to Use With Maven

To use this doclet you will need to include something like the following into your Maven pom.xml and
execute 'mvn site:site'.

	<reporting>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-javadoc-plugin</artifactId>
				<version>?.?.?</version>
				<reportSets>
					<reportSet>
						<id>spring-request-doclet</id>
						<reports>
							<report>javadoc</report>
						</reports>
						<configuration>
							<name>Spring Request Docs</name>
							<description>Spring Request documentation.</description>
							<doclet>com.j256.springrequestdoclet.SpringRequestDoclet</doclet>
							<docletArtifact>
								<groupId>com.j256.spring-request-doclet</groupId>
								<artifactId>spring-request-doclet</artifactId>
								<version>?.?.?</version>
							</docletArtifact>
							<useStandardDocletOptions>false</useStandardDocletOptions>
							<destDir>spring-request-docs</destDir>
						</configuration>
					</reportSet>
				</reportSets>
			</plugin>
		</plugins>
	</reporting>

Once you run site:site, the directory target/site/spring-request-docs will have been created.    You can
also run 'mvn site:site site:jar' to package up a target/*-site.jar with the site documentation.

### Optional Root Documentation

By default the doclet will generate request details which include a path summary, class summary, class
details, and method details files.  If you'd like, you can specify a small hierarchy of custom
documentation that can be used to provide more generic information as an introduction to the requests
handled as opposed to the details from the classes.

To add the custom documentation, you specificy the -r option with a relative directory path.
Something like:

	<reportSet>
		<id>spring-request-doclet</id>
		...
		<configuration>
			<!-- seems to need to be relative to target/site/spring-request-docs -->
			<additionalparam>-r ../../../src/main/doc/spring-request-doclet</additionalparam>
			...
		</configuration>
	</reportSet>

For example, if you have:

	src/main/doc/spring-request-doclet/index.html
	src/main/doc/spring-request-doclet/overview/index.html
	src/main/doc/spring-request-doclet/overview/details.html

then these files will be copied into:

	target/site/spring-request-docs/index.html
	target/site/spring-request-docs/overview/index.html
	target/site/spring-request-docs/overview/details.html

By default the path-summary file is written to index.html but if you specify a replacement index.html
then it will be written to paths.html and you should include a link to it in your replacement index.

The following files are auto-generated by the doclet and should not be overwritten:

	classes.html
	paths.html
	classes/

Any other paths and directories can be used.
