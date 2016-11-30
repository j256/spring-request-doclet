This package provides a simple doclet for auto-generating documentation using Java's javadoc generator
specifically for tracking spring's web requests.

For more information, visit the home page:
	http://256stuff.com/sources/spring-request-doclet/

The git repository is:
	https://github.com/j256/spring-request-doclet

Maven packages are published via the central repo:
	http://repo1.maven.org/maven2/com/j256/spring-request-doclet/spring-request-doclet/

Enjoy,
Gray Watson

-------------------------------------------------------------------------------------------------------

HOW TO USE WITH MAVEN

To use this doclet you will need to include something like the following into your Maven pom.xml or
run the javadoc command

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
              <name>Spring Docs</name>
              <doclet>com.j256.springrequestdoclet.SpringRequestDoclet</doclet>
              <docletArtifact>
                <groupId>com.j256.spring-request-doclet</groupId>
                <artifactId>spring-request-doclet</artifactId>
                <version>?.?.?</version>
              </docletArtifact>
              <useStandardDocletOptions>false</useStandardDocletOptions>
              <name>Spring Request Doclet</name>
              <description>Spring Request documentation.</description>
              <destDir>springrequestdocs</destDir>
            </configuration>
          </reportSet>
        </reportSets>
      </plugin>
    </plugins>
  </reporting>
