# The New TraversalContext Interface in v1.3 (Updated for v2.4.4) #

The Connector Manager provides a TraversalContext implementation
to Connectors so that they may better determine what types of
document content to provide during a traversal.  Connectors may use
the information provided by the TraversalContext to limit content
provided for indexing, based upon document size or mime-type.


# Details #

Recently, a customer experienced traversal difficulties (failure)
when a Connector attempted to feed a 600 megabyte ZIP archive to
the GSA for indexing.  The Connector had no way of knowing that
the GSA does not index content larger than 30 megabytes, nor
does the GSA index compressed archives, so even if the attempted
feed had succeeded (rather than failing dramatically with an
out of memory error), the GSA would have simply discarded the
content anyway.  Attempting to feed inappropriate content could
be considered a waste of computing and network resources.

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


## Making your Connector TraversalContextAware ##

If a Connector's `TraversalManager` implementation adds the
`com.google.enterprise.connector.spi.TraversalContextAware` interface,
the Connector Manager will then call the `setTraversalContext()`
method, supplying a `TraversalContext` for the Connector to use,
before calling any methods in the `TraversalManager` interface.
```
#import com.google.enterprise.connector.spi.TraversalContext;
#import com.google.enterprise.connector.spi.TraversalContextAware;

class MyTraversalManager
    implements TraversalManager, TraversalContextAware {

  /** The TraversalContext from TraversalContextAware Interface */
  private TraversalContext traversalContext = null;

  public void setTraversalContext(TraversalContext traversalContext) {
    this.traversalContext = traversalContext;
  }

  ...
}
```


## Using the TraversalContext to more Intelligently Feed Content ##

If a `TraversalContext` is provided, the Connector's TraversalManager
may then use it to tailor its Document feed.  For instance, in
the following code snippet, the `TraversalContext` is used to determine
whether or not to supply a `google:content` property for a Document.
```
  private void addContentProperty() throws RepositoryException {
    File contentFile;
    String mimeType;
    ...

    long fileSize = contentFile.length();

    // Empty/NonExistent file has no content.
    if (fileSize <= 0L)
      return;

    // The TraversalContext Interface provides additional
    // screening based upon content size and mimetype.
    if (traversalContext != null) {
      // Is the content too large?
      if (fileSize > traversalContext.maxDocumentSize())
        return;

      // Is this MimeType supported?
      if (traversalContext.mimeTypeSupportLevel(mimeType) <= 0)
        return;
    }

    // If we pass the gauntlet, create a content stream property
    // and add it to the property map.
    InputStream contentStream = new FileInputStream(contentFile);
    Value contentValue = Value.getBinaryValue(contentStream);
    props.addProperty(SpiConstants.PROPNAME_CONTENT, contentValue);
    return;
  }
```


## The Updated TraversalContext Interface ##

The new `TraversalContext` Interface has been enhanced slightly.
Whereas the previous (unimplemented) interface methods remain
largely unchanged, a few new convenience methods have been
added.
```
public interface TraversalContext {
  /**
   * Gets a size limit for contents passed through the connector framework. 
   * If a developer has a way of asking the repository for the size of
   * a content file before fetching it, then a comparison with this size
   * would save the developer the cost of fetching a content that is too 
   * big to be used.
   *
   * @return The size limit in bytes
   */
  long maxDocumentSize();
  
  /**
   * Gets information about whether a mime type is supported.  Positive     
   * values indicate possible support for this mime type, with larger       
   * values indicating better support or preference.                        
   *                                                                     
   * Non-positive numbers mean that there is no support for this mime type.
   * A zero value indicates the content encoding is not supported.          
   * The connector may choose to supply meta-data for the document, but the
   * content should not be provided.                                        
   *                                                                     
   * A negative value indicates the document should be skipped entirely.    
   * Neither the content, nor the meta-data should be provided.             
   *                                                                        
   * @return The support level - non-positive means no support              
   */  
  int mimeTypeSupportLevel(String mimeType);

  /**
   * Returns the most preferred mime type from the supplied set.
   * This returns the mime type from the set with the highest support level.
   * Mime types with "/vnd.*" subtypes are preferred over others, and
   * mime types registered with IANA are preferred over those with "/x-*" 
   * experimental subtypes.
   *
   * If a repository contains multiple renditions of a particular item,
   * it may use this to select the best rendition to supply for indexing.
   *
   * The support level values for mime types are defined in the MimeTypeMap
   * bean definition in applicationContext.xml.
   *
   * @param mimeTypes a set of mime types.
   * @returns the most preferred mime type from the Set.
   */
  String preferredMimeType(Set mimeTypes);

  /**                                                                           
   * Returns the time in seconds alloted for traversals to complete.
   * TraversalManager.startTraversal() and TraversalManager.resumeTraversal(String)
   * can avoid interrupts due to timeouts by returning within this amount of time.
   *                                                                            
   * @since 2.4                                                                 
   */
  long traversalTimeLimitSeconds();
}
```


## Tailoring the TraversalContext Settings to your Enterprise ##

The default values for the TraversalContext settings reflect
either common usage or Google Search Appliance limitations.
However, a knowledgeable user or administrator may tailor some
of the GSA and TraversalContext configuration to better suit
their enterprise content.

For instance, the Google Search Appliance will not index a
document whose content exceeds 30 megabytes in size and the
default `TraversalContext.maxDocumentSize()` value reflects
that limit.  However, perhaps you wish to impose a lower
limit, only indexing documents that are 5 megabytes or less.
Or perhaps you prefer to index Postscript or PDF pre-press
versions of documents, rather than the original Office suite
formats.

You may tune TraversalContext settings by editing one or
more of the Connector Manager web application configuration
files.  In some cases it may also be necessary to modify
the Search Appliance configuration (using the GSA Administration
pages).

## Setting the maxDocumentSize ##

### For Connector Manager v 2.4.2 and earlier ###
The maximum document size limit may be configured by adjusting
the `maxDocumentSize` property of the `FileSizeLimitInfo` bean
definition in `applicationContext.xml`.

### For Connector Manager v 2.4.4 and later ###
The maximum document size limit may be configured by setting the
`feed.document.size.limit` property in `applicationContext.properties`.
For example:
```
feed.document.size.limit=10485760
```
Sets the maximum document size to 10 megabytes.
See also [Connector Manager Advanced Configuration](AdvancedConfiguration.md).

### Note ###
Configuring the maximum content size to be larger than 30 MB
is not recommended,  as the GSA typically discards content
larger than that.

## Setting the traversalTimeLimit ##

As of Connector Manager v 2.0, the default traversalTimeLimit
changed from 5 minutes to 30 minutes.  This dramatically reduced
the occasions where traversal batches that ran a bit slowly were
timed-out and discarded on the mistaken assumption that the
the traversal was hung.

At this time, calls to `TraversalManager.startTraversal()` or
`TraversalManager.resumeTraversal()` that take more than 30
minutes are indicative of poor Repository Database tuning (such
as lack of proper indexes for deleted document queries) or
problems with Repository connectivity (such as pulling document
content over a slow WAN).   It is recommended that the administrator
first try to address the fundamental problem behind the slow
traversal before simply lengthening the time out.

### For Connector Manager v 2.4.4 and later ###
Setting the `traversal.time.limit` property in `applicationContext.properties`
defines the number of seconds a traversal batch should run before
gracefully exiting.  Traversals that exceed this time period risk cancelation.
For example:
```
traversal.time.limit=3600
```
Raises the traversal time limit to 1 hour (3600 seconds).
See also [Connector Manager Advanced Configuration](AdvancedConfiguration.md).

## Mime Type Support Levels ##

The support level values for mime types (used by the
`mimeTypeSupportLevel` and `preferredMimeType` methods) are defined
in the `MimeTypeMap` bean definition in `applicationContext.xml`.
Support levels are integer values, with larger positive
integers being more preferred over lower values.  Support
levels less than or equal to zero indicate that mime type
is not supported.

`TraversalContext.maxDocumentSize(String mimeType)` returns
the support level of the specified mime type.  Mime type
support levels are used to rank items supplied to
`TraversalContext.preferredMimeType(Set mimeTypes)`.

The `MimeTypeMap` bean definition in `applicationContext.xml`
groups mime types into 4 broad categories: _preferred_,
_supported_, _unsupported_, _excluded_, and _unknown_.  The `MimeTypeMap`
bean definition contains sets itemizing mime types for the _preferred_,
_supported_, _unsupported_, and _excluded_ categories.  Any mime type
not contained in one of those sets is considered _unknown_.

Setting `unknownMimeTypeSupportLevel` to a positive value will allow
the GSA to attempt to index the document contents of documents with
unrecognized content types.  Setting `unknownMimeTypeSupportLevel`
to zero (or any other non-positive value) will disable sending
document content for unknown content types to the GSA.  The default
value ranks unknown mime types below _supported_ mime types and
_preferred_ mime types, but above _unsupported_ mimetypes.

_Preferred_ mime types have higher support levels than merely
_supported_ mime types, which in turn, have higher support
levels than _unsupported_ or _excluded_ mime types.

The entries or each of the _preferred, supported, unsupported, and excluded_
sets are a list of content types that may or may not include
subtypes.  Exact (case-insensitive) matches are attempted first.
If an exact match is not found, a match is attempted using just
the base type without the subtype.

For instance, suppose these properties were as follows:
`preferredMimeTypes={}` (empty), `supportedMimeTypes={"foo/bar"}`,
`unsupportedMimeTypes={"foo", "cat"}`.  "Foo/Bar" matches (case-
insensitively) "foo/bar", so it would be considered supported.
"Foo/baz" does not have an exact match, but its content type
(sans subtype) "foo" does have a match in the unsupported table,
so it should be considered unsupported.  Similarly, "cat/persian"
would be considered unsupported.  "Xyzzy/bar" lacks an exact
match, and its content type (sans subtype), "xyzzy", is also not
present, so it would be assigned the `unknownMimeTypeSupportLevel`.

Nearly all of the IANA recognized content type classes are well
represented in the _preferred/supported/unsupported/excluded_ mime types sets,
so very few mime types should end up as _unknown_.  Removing content
type (sans subtype) entries from the _preferred/supported/unsupported/excluded_
mime types sets would force more mime types to become _unknown_.

Moving mime types between _preferred_, _supported_, _unsupported_, and _excluded_
adjusts the support level for that mime type.
Moving the mime type (sans subtype) entries, like "application",
can have sweeping effects.

The mime types in each set are classified as follows:
  * **preferred** - The set of preferred mime types to index.  These mime types require little or no preprocessing or file format conversion to extract text and metadata.
  * **supported** - The set of supported mime types to index.  These mime types may require some preprocessing or file format conversion to extract text and metadata.  Some information may be lost or discarded.
  * **unsupported** - The set of mime types whose content should not be indexed. These mime types provide little or no textual content, or are data formats that are either unknown or do not have a format converter.  The connector may still provide meta-data describing the content, but the content itself should not be pushed.
  * **excluded** - The set of mime types whose document should not be indexed at all.  The connector should skip the document, providing neither meta-data, nor the content.

You could add mime types for new, experimental, or custom content.
Deleting entries with explicit subtypes will force an attempt
to match an entry sans subtype.  For instance, deleting the
"application/postscript" entry from the _supported_ set would
cause the support level for such content to drop to the
"application" support level, currently _unsupported_.

Deleting an entry that is sans subtype will force all subtypes
not explicitly listed to be considered _unknown_.  For instance,
the "application" content type class is currently listed in
the _unsupported_ set.  As such, the mime type
"application/vnd.informix-visionary", not explicitly mentioned
elsewhere, would have the support level of _unsupported_.
Deleting the "application" entry from the _unsupported_ set
would force "application/vnd.informix-visionary", lacking both
an explicit entry and an "application" (sans subtype) entry,
to be assigned the `unknownMimeType` support level.


## Keep the Connector Manager and the GSA Configured Similarly ##

The default configuration for the `TraversalContext` tries to
mirror similar configuration parameters for the Google Search
Appliance.  However, changes to one are not automatically
reflected in changes to the other.

Note that modifying entries in the `MimeTypeMap` bean configuration
may require corresponding modifications
to the Google Search Appliance _Crawl and Index_ administration page.
Similarly, modifications to the _Crawl and Index_ page may also require
changes to the Connector Manager configuration.