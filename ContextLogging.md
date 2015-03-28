# Advanced Diagnostic Context Logging in Connector Manager v2.0 #

Google Search Appliance Connector Manager version 2.0 provides enhanced
logging capabilities that makes it easier to troubleshoot installations
employing multiple Connector Instances.  The log records now contain
additional context-specific information, most notably the name of the
connector instance generating the message.

The new logging mechanism implements the Nested Diagnostic Context (NDC)
and Mapped Diagnostic Context (MDC) patterns as described by Neil Harrison
in the article "Patterns for Logging Diagnostic Messages" in the book
"[Pattern Languages of Program Design 3](http://portal.acm.org/citation.cfm?id=273448)".
The
[MDC](http://code.google.com/p/google-enterprise-connector-manager/source/browse/trunk/projects/connector-manager/source/java/com/google/enterprise/connector/logging/MDC.java)
and
[NDC](http://code.google.com/p/google-enterprise-connector-manager/source/browse/trunk/projects/connector-manager/source/java/com/google/enterprise/connector/logging/NDC.java)
interface closely resembles those provided by the Apache Log4j implementation,
primarily as an aid for the large number of developers and administrators
familiar with log4j.

Whereas earlier versions of the Connector Manager relied strictly on
Java's native logging, the new enhanced diagnostic context logging
feature requires the configuration of a custom log [Formatter](http://java.sun.com/j2se/1.5.0/docs/api/java/util/logging/Formatter.html).
Two new log Formatters are provided:
[SimpleFormatter](http://code.google.com/p/google-enterprise-connector-manager/source/browse/trunk/projects/connector-manager/source/java/com/google/enterprise/connector/logging/SimpleFormatter.java)
and
[XmlFormatter](http://code.google.com/p/google-enterprise-connector-manager/source/browse/trunk/projects/connector-manager/source/java/com/google/enterprise/connector/logging/XmlFormatter.java).

By default, these Google Connector Log Formatters generate output similar
to the `java.util.logging` Formatters of the same name, with the discrete
addition of the diagnostic context.  However, the output of each may
be modified via a `format` property in the Connector Manager's
`logging.properties` file.

The default message layout for the `SimpleFormatter` includes the
diagnostic context in square brackets after the message timestamp.
The default message layout for the `XmlFormatter` adds an `<NDC>`
XML element that contains the diagnostic context.


## Changing the Format of the Generated Log Messages ##

The Google Connectors Installer sets up the `SimpleFormatter` with
its default log message layout.  This layout is similar to the
`java.util.logging.SimpleFormatter` produced by previous versions of the
Google Search Appliance Connector deployments.

A Connector administrator may select the desired log `Formatter` and modify
the layout of the generated messages by setting certain properties in the
`$CATALINA_HOME/webapps/connector-manager/WEB-INF/classes/logging.properties`
file.


## Configuring the SimpleFormatter ##

The Google Connector Logging SimpleFormatter is the log formatter set up
by the Google Connector Installer.

To enable the SimpleFormatter, set the `FileHandler` property in `logging.properties`
as follows:<br>
<code>java.util.logging.FileHandler.formatter=com.google.enterprise.connector.logging.SimpleFormatter</code>

By default, the <code>SimpleFormatter</code> generates output that resembles that<br>
of <code>java.util.logging.SimpleFormatter</code>.  The NDC context is inserted<br>
just after the timestamp on the first line of the log message.<br>
<br>
The output of the <code>SimpleFormatter</code> can be tailored using a layout pattern<br>
string specified as the <code>format</code> property value.  As an aid to administrators,<br>
the syntax of the layout pattern string is similar to the Log4j PatternLayout.<br>
However, since this logger is based upon <code>java.util.logging.LogRecord</code>, not all<br>
of Log4j's conversion patterns are supported.  The layout pattern syntax<br>
is described in the<br>
<a href='http://code.google.com/p/google-enterprise-connector-manager/source/browse/trunk/projects/connector-manager/source/java/com/google/enterprise/connector/logging/LayoutPattern.java'>com.google.enterprise.connector.logging.LayoutPattern</a>
javadoc.<br>
<br>
For example, the default <code>SimpleFormatter</code> output uses the layout pattern:<br>
<code>com.google.enterprise.connector.logging.SimpleFormatter.format=%d{MMM dd, yyyy h:mm:ss a} [%x] %C %M%n%p: %m%n</code>

For a condensed, the 1-line layout, suitable for grepping, try:<br>
<code>com.google.enterprise.connector.logging.SimpleFormatter.format=%d [%x] %p: %C{3}.%M(): %m%n</code>


<h2>Configuring the XmlFormatter</h2>

To enable the <code>XmlFormatter</code>, set the <code>FileHandler</code> property in <code>logging.properties</code>
as follows:<br>
<code>java.util.logging.FileHandler.formatter=com.google.enterprise.connector.logging.XmlFormatter</code>

The <code>XmlFormatter</code> has limited configuration options, supporting only two<br>
different output formats.  The default XmlFormatter output resembles the<br>
output of <code>java.util.logging.XMLFormatter</code>, with the addition of an <code>&lt;NDC&gt;</code>
element containing the NDC diagnostic context.<br>
<br>
Unfortunately, the additional <code>&lt;NDC&gt;</code> XML element in the default format is<br>
not recognized by the popular<br>
<a href='http://logging.apache.org/chainsaw/index.html'>Apache Chainsaw log viewer</a>.<br>
The Connector Logging XmlFormatter can be configured to produce output<br>
resembling that of the Apache Log4j logger, which is viewable in Chainsaw.<br>
<br>
To enable the default XML format, set the <code>format</code> property to <code>default</code>:<br>
<code>com.google.enterprise.connector.logging.XmlFormatter.format=default</code>

To enable Log4j-compatible XML format, set the <code>format</code> property to <code>log4j</code>
or <code>chainsaw</code>:<br>
<code>com.google.enterprise.connector.logging.XmlFormatter.format=log4j</code>

<b>Note:</b> A bug in the <code>XmlFormatter</code> that shipped with Connector Manager v2.0.0<br>
requires a <code>format</code> property to be specified, even if that property value<br>
is <code>default</code>.  The <code>format</code> property must be specified, and it must not be<br>
empty.  This issue was fixed in the Connector Manager v2.0.2 patch release.<br>
<br>
<br>
<h2>Configuring the Tomcat Server to Enable Connector Diagnostic Logging</h2>

The Google Connectors Installer will automatically configure use of<br>
the new Google Connector SimpleFormatter with its default layout.<br>
However, manual Connector installations will require small changes<br>
to the Tomcat configuration to enable this feature.<br>
<br>
You must add the new <code>connector-logging.jar</code> file to the system classpath that<br>
Tomcat uses at startup.  Tomcat ignores the <code>CLASSPATH</code> environment variable and<br>
builds a custom classpath using the <code>$CATALINA_HOME/bin/setclasspath.sh</code> or<br>
<code>%CATALINA_HOME%/bin/setclasspath.bat</code> scripts.  Modify these scripts, adding<br>
<code>connector-logging.jar</code> to the <code>CLASSPATH</code> constructed.<br>
<br>
For instance:<br>
<code>CLASSPATH="$CLASSPATH":"$BASEDIR"/webapps/connector-manager/WEB-INF/lib/connector-logging.jar</code><br>

If configuring the Connector as a Microsoft Windows service, you must also<br>
add <code>connector-logging.jar</code> to the <code>PR_CLASSPATH</code> constructed in <code>service.bat</code>
before installing the service.