This is a stubbed-out version of SLF4J logging used by mime-util.
In 2009, it was determined that we could not use SLF4J on-board
the GSA. Consequently, we developed connector-logging, which performs
context logging for the Connector Manager and the Connectors.

MimeUtil ( http://sourceforge.net/projects/mime-util/ ) utilizes
SLF4J logging.  However, it uses it in an extremely limited
fashion.  This slf4j-stub implements the small set of SLF4J
calls that mime-util makes, providing a very thin wrapper over
the java.util.logging equivalents.
