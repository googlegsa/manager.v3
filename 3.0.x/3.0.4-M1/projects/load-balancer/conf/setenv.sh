# JVM memory config that mirrors the GCI installation
JAVA_OPTS="$JAVA_OPTS -Xms256m -Xmx1024m"

# Google Enterprise Connector Logging must be on the system CLASSPATH
# if using java.util.logging.FileHandler
CONNECTOR_LOGGING_JAR="lib/connector-logging.jar"
if [ -n "$CATALINA_BASE" ] && [ -f "$CATALINA_BASE/$CONNECTOR_LOGGING_JAR" ]; then
  CLASSPATH="$CLASSPATH":"$CATALINA_BASE/$CONNECTOR_LOGGING_JAR"
elif [ -n "$CATALINA_HOME" ] && [ -f "$CATALINA_HOME/$CONNECTOR_LOGGING_JAR" ]; then
  CLASSPATH="$CLASSPATH":"$CATALINA_HOME/$CONNECTOR_LOGGING_JAR"
fi

# Google Enterprise Connector for Documentum Additions to CLASSPATH
if [ -n "$DOCUMENTUM_SHARED" ] ; then
  CLASSPATH="$CLASSPATH":"$DOCUMENTUM_SHARED"/dfc/dfc.jar:"$DOCUMENTUM_SHARED"/config
fi

