# Introduction #

In preparation for the upcoming 1.3.0 release of the Connector
Manager and Connectors, we want to make sure that all of the
Connectors are compatible with changes to the Connector Manager
since the last release.  There have been several small changes
to the SPI, some additional functionality made available for the
Connectors, and some clarification of flow of control.


# Details #

## ConnectorFactory for use by ConnectorType.validateConfig() ##

The Connector Factory is provided to `ConnectorType.validateConfig()`,
which may use it to construct Connector instances for the purpose
of validation.  The `ConnectorFactory` uses the same mechanism to
create the Connector instance that the ConnectorManager uses to
create the "Normal" running instances.  However, the instances
created by the ConnectorFactory are considered transient - they
are not scheduled for traversal or used to authorize search
results.

For additional information see:
  * [Connector Manager Issue 80](http://code.google.com/p/google-enterprise-connector-manager/issues/detail?id=80)
  * [ConnectorFactory.java](http://code.google.com/p/google-enterprise-connector-manager/source/browse/trunk/projects/connector-manager/source/java/com/google/enterprise/connector/spi/ConnectorFactory.java)
For an example of ConnectorFactory use:
  * [LivelinkConnectorType.java](http://code.google.com/p/google-enterprise-connector-otex/source/browse/trunk/projects/otex-core/source/java/com/google/enterprise/connector/otex/LivelinkConnectorType.java)



## ConnectorType.validateConfig() May Return a Modified Configuration ##

`ConnectorType.validateConfig()` may now return a modified configuration
in the `ConfigureResponse` if desired.  That modified configuration
will be saved and used to created the running connector instance.

For additional information see:
  * [ConnectorType.java](http://code.google.com/p/google-enterprise-connector-manager/source/browse/trunk/projects/connector-manager/source/java/com/google/enterprise/connector/spi/ConnectorType.java)
  * [ConfigureResponse.java](http://code.google.com/p/google-enterprise-connector-manager/source/browse/trunk/projects/connector-manager/source/java/com/google/enterprise/connector/spi/ConfigureResponse.java)


## Exception Handling in TraversalManager, DocumentList, Document, Property, Value ##

The handling of Exceptions thrown during document traversal and feeding
has been greatly improved.  In the previous releases, exceptions thrown
during traversal would often result in loops or hangs, usually halting
traversal progress.  Connectors should only ever throw `RepositoryExceptions`
out of these interfaces, however we now provide a new subclass of
`RepositoryException`, called `RepositoryDocumentException`, that is handled
differently.  In short, throwing a `RepositoryDocumentException` will
force the Connector Manager to skip the document currently being processed,
proceeding to the next one.  Throwing a `RepositoryException` will instruct
the Connector Manager to abandon the current batch of documents and
retry later.  The Connector must also properly handle a call to
`DocumentList.checkpoint()` after an exception is thrown.

For more information, see:
  * [Connector Manager Issue 72](http://code.google.com/p/google-enterprise-connector-manager/issues/detail?id=72)
  * [Connector Manager Issue 108](http://code.google.com/p/google-enterprise-connector-manager/issues/detail?id=108)
  * [DocumentList.java](http://code.google.com/p/google-enterprise-connector-manager/source/browse/trunk/projects/connector-manager/source/java/com/google/enterprise/connector/spi/DocumentList.java)
  * [Document.java](http://code.google.com/p/google-enterprise-connector-manager/source/browse/trunk/projects/connector-manager/source/java/com/google/enterprise/connector/spi/Document.java)


## Returning Null DocumentList vs Empty DocumentList from TraversalManager ##

Previous versions of the Connector Manager handled a `null` return value
and an empty `DocumentList` [non-null, but zero items] returned from
`TraversalManager.startTraversal()` and `TraversalManager.resumeTraversal()`
identically.  This version of the Connector Manager makes a subtle
differentiation between the two.  A `null` return value is interpreted
as before: no new content is available for indexing, sleep for a few
minutes and try again.  An returned empty `DocumentList` is interpreted
differently: although no suitable documents were found yet, the
Connector is performing a rather time-consuming search looking for
appropriate content.  The Connector Manager will call `checkpoint()`
and reschedule the Connector for an immediate call to `resumeTraversal()`.
This allows the Connector to time-slice or monitor a time-consuming
search for content without running afoul of the Connector Manager
time-out of work threads.  Connectors that return an empty `DocumentList`
when they should be returning null, will effectively run in a busy loop.

For more information, see:
  * [Connector Manager Issue 72](http://code.google.com/p/google-enterprise-connector-manager/issues/detail?id=72)
For an example of a Connector that uses this model, see the
`LivelinkTraversalManager.listNodes()` method at:
  * [LivelinkTraversalManager.java](http://code.google.com/p/google-enterprise-connector-otex/source/browse/trunk/projects/otex-core/source/java/com/google/enterprise/connector/otex/LivelinkTraversalManager.java)


## New "google:title" Property ##

The named link that the GSA presents in search results is usually
a title or headline that the GSA extracts from the document content.
At this time, the GSA does not make use of other meta-data supplied
by the Connector to display this title, so if the feed has no content
or the GSA cannot extract a meaningful title from the supplied
content, it instead displays the URL to the document in the search
result.  Unfortunately, the URLs of documents from Connector Feeds
are usually uninformative to the viewer.

The Connector Manager has created a new canonical metadata field,
"google:title", defined as `SpiConstants.PROPNAME_TITLE`.  At this
point, the GSA makes no special use of this field.  However, if
the Connector Manager receives a meta-data and content feed with
no actual "google:content" field, it will create stub content
consisting of an html title fragment.  This fools the current
GSA versions into displaying that title in the search results.

In the future the GSA may make more direct use of the _google:title_
field, so even if your Connector does provide content, it should
still present the document name/title/headline/subject as _google:title_.

For more information see:
  * [Connector Manager Issue 59](http://code.google.com/p/google-enterprise-connector-manager/issues/detail?id=59)


## TraversalContext and TraversalContextAware ##

The Connector Manager now provides a TraversalContext implementation
to Connectors so that they may better determine what types of
document content to provide during a traversal.  Connectors may use
the information provided by the TraversalContext to limit content
provided for indexing, based upon document size or mime-type.

For instance, the Connector might use TraversalContext information to:
  * Provide a Document with meta-data and full content.
  * Provide a Document with meta-data but supply content in an alternate format (such as HTML or PDF).
  * Provide a Document with meta-data and summarized content.
  * Provide a Document with meta-data but no content.
  * Skip a Document entirely.

If a Connector's TraversalManager implementation adds the
com.google.enterprise.connector.spi.TraversalContextAware interface,
the Connector Manager will then call the setTraversalContext()
method, supplying a TraversalContext for the Connector to use,
before calling any methods in the TraversalManager interface.

If a TraversalContext is provided, the Connector's TraversalManager
may then use it to tailor its Document feed.  For instance, the
TraversalContext could be used to determine whether or not to
supply a "google:content" property for a Document, based upon
the document size or mime-type.  Note that the TraversalContext
interface has changed slightly from its previous (unimplemented)
version.

For additional information, see:
  * [Connector Manager Issue 78](http://code.google.com/p/google-enterprise-connector-manager/issues/detail?id=78)
  * [The New TraversalContext Interface wiki page](TraversalContext.md)


## Connector Configuration Storage ##

This version of the Connector Manager moves the stored Connector
schedule and traversal state (checkpoint) from the Java Preferences
to files stored in the Connector instance directory (found under
`$TOMCAT_HOME/webapps/connector-manager/WEB-INF/connectors`).  This
is the same directory that the Connector's configuration properties
file and optional `connectorInstance.xml` file is stored.

The presence of these two additional files is unlikely to affect the
Connectors.  The files are named $CONNECTOR\_NAME\_schedule.txt and
$CONNECTOR\_NAME\_state.txt, where $CONNECTOR\_NAME is the name of the
Connector instance.

For more information, see:
  * [Connector Manager Issue 78](http://code.google.com/p/google-enterprise-connector-manager/issues/detail?id=78)


## Password Encryption with EncryptedPropertyPlaceholderConfigurer ##

All properties in the Connector's configuration properties file,
whose property key contains the substring "password" (case-insensitive
match) are now encrypted by default.  In the past, only properties
with the key "Password" were encrypted.  Connectors using the
`EncryptedPropertyPlaceholderConfigurer` are unlikely to notice the
change.

The names of future new configuration properties should be chosen
accordingly.  For instance, this now allows a Connector to maintain
separate passwords for different repository services.  However,
the Livelink Connector configuration now has an encrypted boolean
property, because it happens to contain the substring "password"
in its name.

For more information, see:
  * [Connector Manager Issue 80](http://code.google.com/p/google-enterprise-connector-manager/issues/detail?id=81)


## SMB Search URLs ##

Previous versions of the Connector Manager would reject
google:searchurl metadata that used the "smb:" scheme for
the URL.  This has been fixed.

For additional information, see:
  * [Connector Manager Issue 100](http://code.google.com/p/google-enterprise-connector-manager/issues/detail?id=100)


## AuthorizationResponse.equals() and AuthorizationResponse.hash() ##

The `AuthorizationResponse.equals()` and `AuthorizationResponse.hash()`
methods have been changed to include the `AuthorizationResponse.valid`
member in the computations.  In previous versions of the Connector
Manager, only the `AuthorizationResponse.docid` member was used in
`AuthorizationResponse.equals()` and `AuthorizationResponse.hash()`.

The change is subtle, but AuthorizationResponse instances
{ "1234", true } and { "1234", false } are now considered inequal,
where they would have been considered equal in the past.

For more information, see:
  * [Connector Manager Issue 57](http://code.google.com/p/google-enterprise-connector-manager/issues/detail?id=57)