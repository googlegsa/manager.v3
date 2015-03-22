// Copyright 2006 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.spi;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Non-instantiable class that holds constants used by the SPI and
 * documents their meanings.
 * <p/>
 * All constants whose names begin with PROPNAME are reserved names for
 * properties that may be accessed from a Document returned as a query result.
 * The actual values of these property name constants all begin with "google:".
 * For future compatibility, all property names beginning with "google:" are
 * reserved.
 *
 * @since 1.0
 */
public class SpiConstants {
  private SpiConstants() {
    // prevents instantiation
  }

  /**
   * The prefix for the reserved property names.
   * <p>
   * Value: "google:"
   *
   * @since 2.6.6
   */
  public static final String RESERVED_PROPNAME_PREFIX = "google:";

  /**
   * Identifies a single-valued, string property that uniquely identifies a
   * document to this connector. The internal structure of this string is
   * opaque to the Search Appliance. Only printable, non-whitespace, ASCII
   * characters are permitted in a DOCID.
   * <p/>
   * This property is required on all Documents. The connector implementor is
   * encouraged to implement this by using the natural ID in the foreign
   * repository.
   * <p/>
   * This property is persistable (it is one of the keys in the
   * {@link #PERSISTABLE_ATTRIBUTES} map.
   * <p/>
   * Value: google:docid
   */
  public static final String PROPNAME_DOCID = "google:docid";

  /**
   * Identifies a single-valued String property. This value is used to 
   * embellish named resources constructed from {@link #PROPNAME_DOCID},
   * {@link #PROPNAME_FEEDTYPE}, and {@link #PROPNAME_FRAGMENT}.
   * <p/>
   * Value: google:fragment
   *
   * @since 3.2
   */
  public static final String PROPNAME_FRAGMENT = "google:fragment";

  /**
   * Identifies a single-valued, date property that gives the last modified
   * date of a document. This property is optional but strongly recommended in
   * order to associate a specific date to the document.
   * <p/>
   * Value: google:lastmodify
   */
  public static final String PROPNAME_LASTMODIFIED = "google:lastmodified";

  /**
   * Identifies an optional string property that is the title of the document.
   * This value is useful for providing a title for documents that supply no
   * content, or for which a title cannot be automatically extracted from the
   * supplied content.
   * <p/>
   * Value: google:title
   */
  public static final String PROPNAME_TITLE = "google:title";

  /**
   * Not used.
   * <p/>
   * Value: google:contenturl
   *
   * @deprecated This property is unused.
   */
  @Deprecated
  public static final String PROPNAME_CONTENTURL = "google:contenturl";

  /**
   * Identifies a single-valued FeedType property that, if present, will be
   * used to determine the feed type for this document. It is strongly
   * recommended that this property be set to explicitly determine the feed
   * type ('content' or 'web') for the document.
   * <p/>
   * If this property is not set, the feed type will be determined as follows:
   * <ol>
   * <li>If there is no {@link #PROPNAME_SEARCHURL} then the feed type will
   * default to 'content' feed using a fabricated URL derived from the
   * {@link #PROPNAME_DOCID}.
   * <li>If there is a {@link #PROPNAME_SEARCHURL} then the feed type will
   * default to 'web' feed and use the {@link #PROPNAME_SEARCHURL} as the
   * document URL.
   * </ol>
   * <p/>
   * Value: google:feedtype
   *
   * @since 2.4.2
   */
  public static final String PROPNAME_FEEDTYPE = "google:feedtype";

  /**
   * Identifies a single-valued Feed ID property that, if present, will be
   * used to identify the feed file that contains a fed document. All feed
   * records in a single feed file will share a common google:feedid value.
   * <p/>
   * Not used.
   * <p/>
   * Value: google:feedid
   *
   * @since 2.6
   * @deprecated This property is unused.
   */
  @Deprecated
  public static final String PROPNAME_FEEDID = "google:feedid";

  /**
   * Identifies an optional single-valued string property that, if present,
   * will be used by the Search Appliance as the primary URI for this document
   * - instead of the normal {@code googleconnector://} URI which the Connector
   * Manager fabricates based on the {@link #PROPNAME_DOCID} and the connector
   * name.
   * <p/>
   * Value: google:searchurl
   */
  public static final String PROPNAME_SEARCHURL = "google:searchurl";

  /**
   * Identifies a single-valued property that may be either string or
   * binary and gives direct access to the primary content to be indexed.
   * <p/>
   * Value: google:content
   */
  public static final String PROPNAME_CONTENT = "google:content";

  /**
   * Not used.
   * <p/>
   * Value: google:securitytoken
   *
   * @deprecated This property is unused.
   */
  @Deprecated
  public static final String PROPNAME_SECURITYTOKEN = "google:securitytoken";

  /**
   * Identifies an single-valued String property that gives the mime type
   * for the content of this document. If this is not supplied, then the
   * system will use the value of {@link #DEFAULT_MIMETYPE}.
   * <p/>
   * Value: google:mimetype
   */
  public static final String PROPNAME_MIMETYPE = "google:mimetype";

  /**
   * The mime type that the connector manager uses as a default, if a
   * document does not specify.
   * <p/>
   * Value: text/html
   */
  public static final String DEFAULT_MIMETYPE = "text/html";

  /**
   * Identifies an optional, single-valued property that gives a URL that
   * should be used in a results page as the primary user reference for a
   * document. This may be different from the contenturl, if present:
   * contenturl should give direct access to the content file, whereas
   * displayurl may point into the CMS's web front-end application.
   * <p/>
   * Value: google:displayurl
   */
  public static final String PROPNAME_DISPLAYURL = "google:displayurl";

  /**
   * Unless this property is present and is {@code false}, then the document
   * will be marked as public.
   * <p/>
   * Value: google:ispublic
   */
  public static final String PROPNAME_ISPUBLIC = "google:ispublic";

  /**
   * Identifies a single-valued Boolean property. When this property is
   * {@code false}, ACLs will not be explicitly cleared if no ACL is provided.
   * This is to allow a Connector to send the ACLs for a Document as a separate
   * ACL Document. If this property is missing or {@code true}, the default
   * behavior of always specifying an empty ACL if no ACL is provided is used.
   * <p/>
   * Value: google:overwriteacls
   *
   * @since 3.0
   */
  public static final String PROPNAME_OVERWRITEACLS = "google:overwriteacls";

  /**
   * The prefix for the ACL property names.
   * <p/>
   * Value: "google:acl"
   *
   * @since 3.0
   */
  public static final String ACL_PROPNAME_PREFIX = "google:acl";

  /**
   * Identifies a multiple-valued String property that gives the list of group
   * ACL Scope IDs that are permitted
   * access to this document. If either of the {@link #PROPNAME_ACLGROUPS} or
   * {@link #PROPNAME_ACLUSERS} properties are non-{*code null}, then the
   * Search Appliance will grant or deny access to this document for a given
   * user on the basis of whether the user's name appears as one of the Scope
   * IDs in the {@link #PROPNAME_ACLUSERS} list or one of the user's groups
   * appears as one of the Scope IDs in the {@link #PROPNAME_ACLGROUPS} list.
   * <p/>
   * ACL Scope ID is a group or user name within the scope of the Connector.
   * <p/>
   * <strong>Note: Roles are ignored by the Google Search
   * Appliance.</strong> Support for roles should be removed from
   * connectors, as this feature will be removed from a future release
   * of Connector Manager. Without roles, users or groups with
   * {@link RoleType#PEEKER RoleType.PEEKER} should not appear in the
   * {@link #PROPNAME_ACLUSERS} or {@link #PROPNAME_ACLGROUPS} lists,
   * since {@link RoleType#READER RoleType.READER} access is implied.
   * <p/>
   * To specify more than just {@code RoleType.READER} access to the document,
   * the Connector must add additional multi-value role properties to the
   * document. These entries are of the form:
   *
   * <pre>
   *   Name = &lt;GROUP_ROLES_PROPNAME_PREFIX&gt; + &lt;scopeId&gt;
   *   Value = [RoleType[, ...]]
   * </pre>
   *
   * where &lt;GROUP_ROLES_PROPNAME_PREFIX&gt; is the
   * {@link #GROUP_ROLES_PROPNAME_PREFIX}, &lt;scopeId&gt; is the group ACL
   * Scope ID, and RoleType is one of the possible RoleType values. User ACL
   * Roles are of the form:
   *
   * <pre>
   *   Name = &lt;USER_ROLES_PROPNAME_PREFIX&gt; + &lt;scopeId&gt;
   *   Value = [RoleType[, ...]]
   * </pre>
   *
   * where the &lt;scopeId&gt; will be the user ACL Scope ID.
   * <p/>
   * If the {@link #PROPNAME_ISPUBLIC} is missing or is {@code true}, then this
   * property is ignored, since the document is public.
   * <p/>
   * If both the {@link #PROPNAME_ACLGROUPS} and {@link #PROPNAME_ACLUSERS}
   * properties are {@code null} or empty, then the GSA will use the
   * authorization SPI to grant or deny access to this document.
   * <p/>
   * The GSA may be configured to bypass on-board authorization, in which case
   * these properties will be ignored, and the GSA will use the authorization
   * SPI to grant or deny access to this document.
   * <p/>
   * Value: google:aclgroups
   */
  public static final String PROPNAME_ACLGROUPS = "google:aclgroups";

  /**
   * Identifies a multiple-valued String property that gives the list of
   * users that are permitted access to this document. For details, see
   * the {@link #PROPNAME_ACLGROUPS}.
   * <p/>
   * Value: google:aclusers
   */
  public static final String PROPNAME_ACLUSERS = "google:aclusers";

  /**
   * Identifies a multiple-valued String property that gives the list of
   * groups that are denied access to this document. For details, see
   * the {@link #PROPNAME_ACLGROUPS}.
   * <p/>
   * Value: google:acldenygroups
   *
   * @since 3.0
   */
  public static final String PROPNAME_ACLDENYGROUPS = "google:acldenygroups";

  /**
   * Identifies a multiple-valued String property that gives the list of
   * users that are denied access to this document. For details, see
   * the {@link #PROPNAME_ACLGROUPS}.
   * <p/>
   * Value: google:acldenyusers
   *
   * @since 3.0
   */
  public static final String PROPNAME_ACLDENYUSERS = "google:acldenyusers";

  /**
   * Identifies a single-valued InheritanceType property. This value is
   * used to identify the ACL inheritance when no specific ACL principal is
   * specified.
   * <p/>
   * Value: google:aclinheritancetype
   *
   * @since 3.0
   */
  public static final String PROPNAME_ACLINHERITANCETYPE =
      "google:aclinheritancetype";

  /**
   * Identifies a single-valued String property. This value is
   * used to identify the document URL from which the ACL is inherited from.
   * <p/>
   * This property takes precedence over values specified by
   * {@link #PROPNAME_ACLINHERITFROM_DOCID},
   * {@link #PROPNAME_ACLINHERITFROM_FEEDTYPE}, and
   * {@link #PROPNAME_ACLINHERITFROM_FRAGMENT}.
   * <p/>
   * Value: google:aclinheritfrom
   *
   * @since 3.0
   */
  public static final String PROPNAME_ACLINHERITFROM = "google:aclinheritfrom";

  /**
   * Identifies a single-valued String property. This value is used to identify
   * the document ID from which the ACL is inherited from.
   * <p/>
   * If the document from which the ACL is inherited from was fed using a
   * different {@link FeedType} than this document, then
   * {@link #PROPNAME_ACLINHERITFROM_FEEDTYPE} should also be specified.
   * <p/>
   * This property is ignored if {@link #PROPNAME_ACLINHERITFROM} is also
   * specified.
   * <p/>
   * Value: google:aclinheritfrom:docid
   *
   * @since 3.0
   */
  public static final String PROPNAME_ACLINHERITFROM_DOCID =
      "google:aclinheritfrom:docid";

  /**
   * Identifies a single-valued String property. This value is used to identify
   * the {@link FeedType} of document from which the ACL is inherited from.
   * If unspecified, the {@code FeedType} of the inheriting ACL is used.
   * <p/>
   * This property should be specified in conjunction with
   * {@link #PROPNAME_ACLINHERITFROM_DOCID}, and will be ignored if
   * {@link #PROPNAME_ACLINHERITFROM} is also specified.
   * <p/>
   * Value: google:aclinheritfrom:feedtype
   *
   * @since 3.0
   */
  public static final String PROPNAME_ACLINHERITFROM_FEEDTYPE =
      "google:aclinheritfrom:feedtype";

  /**
   * Identifies a single-valued String property. This value is used to 
   * embellish the ACL named resource from which the ACL is inherited from.
   * <p/>
   * {@link #PROPNAME_ACLINHERITFROM_DOCID},
   * {@link #PROPNAME_ACLINHERITFROM_FEEDTYPE}, and
   * {@link #PROPNAME_ACLINHERITFROM_FRAGMENT} are used to construct an
   * {@link #PROPNAME_ACLINHERITFROM} named resource.
   * <p/>
   * This property is ignored if {@link #PROPNAME_ACLINHERITFROM} is also
   * specified.
   * <p/>
   * Value: google:aclinheritfrom:fragment
   *
   * @since 3.2
   */
  public static final String PROPNAME_ACLINHERITFROM_FRAGMENT =
      "google:aclinheritfrom:fragment";

  /**
   * Prefix added to the front of the group ACL Scope ID when creating a group
   * roles property name. If the Connector wants to define specific roles
   * associated with a group ACL Scope ID related to a document they should be
   * stored in a multi-valued property named:
   *
   * <pre>
   *   GROUP_ROLES_PROPNAME_PREFIX + &lt;scopeId&gt;
   * </pre>
   *
   * For example, given a group ACL Entry of "eng=reader,writer" the roles for
   * "eng" would be stored in a property as follows:
   *
   * <pre>
   * Name = "google:group:roles:eng"
   * Value = [reader, writer]
   * </pre>
   *
   * @deprecated Roles are ignored by the Google Search Appliance.
   * Support for roles should be removed from connectors, as this
   * feature will be removed from a future release of Connector
   * Manager.
   */
  @Deprecated
  public static final String GROUP_ROLES_PROPNAME_PREFIX =
      "google:group:roles:";

  /**
   * Prefix added to the front of the user ACL Scope ID when creating a user
   * roles property name. If the Connector wants to define specific roles
   * associated with a user ACL Scope ID related to a document they should be
   * stored in a multi-valued property named:
   *
   * <pre>
   *   USER_ROLES_PROPNAME_PREFIX + &lt;scopeId&gt;
   * </pre>
   *
   * For example, given a user ACL Entry of "joe=reader,writer" the roles for
   * "joe" would be stored in a property as follows:
   *
   * <pre>
   * Name = "google:user:roles:joe"
   * Value = [reader, writer]
   * </pre>
   *
   * @deprecated Roles are ignored by the Google Search Appliance.
   * Support for roles should be removed from connectors, as this
   * feature will be removed from a future release of Connector
   * Manager.
   */
  @Deprecated
  public static final String USER_ROLES_PROPNAME_PREFIX = "google:user:roles:";

  /**
   * Identifies an optional, single-valued property that specifies the action
   * associated with the document. If not specified, then the system will
   * not specify the action and the default behavior will be observed.
   * <p/>
   * Value: google:action
   */
  public static final String PROPNAME_ACTION = "google:action";

  /**
   * Identifies an optional, multi-valued property that specifies the
   * folder path of the document. The document name should not be
   * included in the path. Multiple values are permitted to support
   * repositories that link documents to multiple parent folders.
   * <p/>
   * Examples:
   *
   * <pre>
   *     /ENGINEERING/techdoc/pdfs
   *     Enterprise:Marketing:Press Releases
   *     https://sp.example.com/sites/mylist
   * </pre>
   * <p/>
   * Value: google:folder
   *
   * @see "RFC 3986: Uniform Resource Identifier (URI): Generic Syntax"
   * @since 2.6.6
   */
  public static final String PROPNAME_FOLDER = "google:folder";

  /**
   * Identifies an optional, single-valued boolean property that specifies
   * whether the document should be locked, to prevent it from being evicted
   * if the GSA reaches its license limit. Default: {@code false}.
   * <p/>
   * Note: this property will not be indexed, it only controls whether the GSA
   * will lock the document.
   * <p/>
   * Value: google:lock
   *
   * @see "<a href='http://www.google.com/support/enterprise/static/gsa/docs/admin/72/gsa_doc_set/feedsguide/feedsguide.html#1073054'>Defining the XML Record for a Document</a>"
   * @since 2.6.4
   */
  public static final String PROPNAME_LOCK = "google:lock";

  /**
   * Optional, single-valued property to specify the crawl immediately property
   * of the record element.
   *
   * @since 3.2.4
   */
  public static final String PROPNAME_CRAWL_IMMEDIATELY =
      "google:crawl-immediately";

  /**
   * Optional, single-valued property to specify the crawl once property
   * of the record element.
   *
   * @since 3.2.4
   */
  public static final String PROPNAME_CRAWL_ONCE = "google:crawl-once";

  /**
   * Identifies an optional, single-valued integer property that specifies
   * the initial PageRank of the document. Default: {@code 96}.
   * <p/>
   * A higher value specifies a higher PageRank. To boost the PageRank
   * of the URL or group of URLs, increase the value to a number
   * between 97 and 100. To lower the PageRank, decrease the value.
   * <p/>
   * Note: Content feeds only with GSA 7.2 or earlier. This property
   * will not be indexed, it only controls the PageRank for the
   * document.
   * <p/>
   * Value: google:pagerank
   *
   * @see "<a href='http://www.google.com/support/enterprise/static/gsa/docs/admin/72/gsa_doc_set/feedsguide/feedsguide.html#1073054'>Defining the XML Record for a Document</a>"
   * @since 2.8
   */
  public static final String PROPNAME_PAGERANK = "google:pagerank";

  /**
   * Identifies an optional, single-valued string property that specifies
   * the authmethod of the document. Default: {@code httpbasic}.
   * <p/>
   * AddPropertyFilter can be used to override the default value.
   * <p/>
   *
   * @since 3.0.8
   */
  public static final String PROPNAME_AUTHMETHOD = "google:authmethod";

  /**
   * Identifies a single-valued {@link DocumentType} property that,
   * if present, will be used to determine the object type for this
   * document.
   * <p/>
   * If this property is not set, the document type will be {@code RECORD}.
   * <p/>
   * Value: google:documenttype
   *
   * @since 3.0
   */
  public static final String PROPNAME_DOCUMENTTYPE = "google:documenttype";

  /**
   * Enum for the list of possible feed types.
   *
   * @since 2.4.2
   */
  public enum FeedType {
    CONTENT, WEB, CONTENTURL;

    /**
     * @return The enum matching the given {@code tag}, or
     *         {@code null} if a match is not found.
     */
    public static FeedType findFeedType(String tag) {
      try {
        return Enum.valueOf(FeedType.class, tag.toUpperCase());
      } catch (IllegalArgumentException e) {
        // Not found, return null.
        return null;
      }
    }

    /**
     * @param other a FeedType
     * @return {@code true} if the other FeedType may exist in the same
     *         feed file as this FeedType
     * @since 3.0
     */
    public boolean isCompatible(FeedType other) {
      return (this == other || (this != CONTENT && other != CONTENT));
    }

    /**
     * @return a legacy feed type string
     * @since 3.0
     */
    public String toLegacyString() {
      return (this == CONTENT) ? "incremental" : "metadata-and-url";
    }
  }

  /**
   * Enum for the list of possible document types. A {@code RECORD} is
   * an ordinary document, which may also have ACL properties. An
   * {@code ACL} is a stand-alone ACL with no document properties.
   *
   * @since 3.0
   */
  public enum DocumentType {
    RECORD, ACL;

    /**
     * @return The enum matching the given {@code tag}, or
     *         {@code null} if a match is not found.
     */
    public static DocumentType findDocumentType(String tag) {
      try {
        return Enum.valueOf(DocumentType.class, tag.toUpperCase());
      } catch (IllegalArgumentException e) {
        // Not found, return null.
        return null;
      }
    }
  }

  /**
   * Enum for action types.
   */
  public enum ActionType {
    ADD("add"), DELETE("delete"), ERROR("error"), SKIPPED("skipped");

    private final String tag;

    ActionType(String m) {
      tag = m;
    }

    /**
     * @return The enum matching the given {@code tag}.
     *         {@code ActionType.ERROR} will be returned if the given
     *         {@code tag} does not match a known {@code ActionType}.
     */
    public static ActionType findActionType(String tag) {
      try {
        return Enum.valueOf(ActionType.class, tag.toUpperCase());
      } catch (IllegalArgumentException e) {
        // Not found, return ERROR.
        return ERROR;
      }
    }

    @Override
    public String toString() {
      return tag;
    }
  }

  /**
   * Enum for known role types.
   *
   * @deprecated Roles are ignored by the Google Search Appliance.
   * Support for roles should be removed from connectors, as this
   * feature will be removed from a future release of Connector
   * Manager.
   */
  @Deprecated
  public enum RoleType {
    PEEKER("peeker"), READER("reader"), WRITER("writer"), OWNER("owner"),
    ERROR("error");

    private final String tag;

    RoleType(String m) {
      tag = m;
    }

    /**
     * @return The enum matching the given {@code tag}.
     *         {@code RoleType.ERROR} will be returned if the given
     *         {@code tag} does not match a known {@code RoleType}.
     */
    public static RoleType findRoleType(String tag) {
      try {
        return Enum.valueOf(RoleType.class, tag.toUpperCase());
      } catch (IllegalArgumentException e) {
        // Not found, return ERROR.
        return ERROR;
      }
    }

    @Override
    public String toString() {
      return tag;
    }
  }

  /**
   * This enumeration identifies databases.
   */
  public enum DatabaseType {
    OTHER("other"),
    ORACLE("oracle"),
    SQLSERVER("sqlserver"),
    H2("h2"),
    MYSQL("mysql"), ;

    private final String tag;

    private DatabaseType(String tag) {
      this.tag = tag;
    }

    /**
     * @return The enum matching the given {@code tag}, or
     *         {@code OTHER} if a match is not found.
     */
    public static DatabaseType findDatabaseType(String tag) {
      try {
        return Enum.valueOf(DatabaseType.class, tag.toUpperCase());
      } catch (IllegalArgumentException e) {
        // Not found, return OTHER.
        return OTHER;
      }
    }

    @Override
    public String toString() {
      return tag;
    }
  }

  /**
   * A map keyed by property names, giving corresponding column names. If a
   * property name is a key in this map, then it can be persisted by the
   * Connector Manager in its per-document store. The associated value gives
   * the name that a connector implementor should use to read records from the
   * per-document store using JDBC. However, implementors are encouraged to use
   * {@link LocalDocumentStore} methods rather than JDBC if possible.
   * <p/>
   * At present, the persistable attributes are:
   * <ul>
   * <li>{@link #PROPNAME_CONNECTOR_INSTANCE}</li>
   * <li>{@link #PROPNAME_CONNECTOR_TYPE}</li>
   * <li>{@link #PROPNAME_DOCID}</li>
   * <li>{@link #PROPNAME_FEEDID}</li>
   * <li>{@link #PROPNAME_PRIMARY_FOLDER}</li>
   * <li>{@link #PROPNAME_ACTION}</li>
   * <li>{@link #PROPNAME_TIMESTAMP}</li>
   * <li>{@link #PROPNAME_MESSAGE}</li>
   * <li>{@link #PROPNAME_SNAPSHOT}</li>
   * <li>{@link #PROPNAME_CONTAINER}</li>
   * <li>{@link #PROPNAME_PERSISTED_CUSTOMDATA_1}</li>
   * <li>{@link #PROPNAME_PERSISTED_CUSTOMDATA_2}</li>
   * </ul>
   *
   * @deprecated The per-document store has never been publicly implemented
   */
  @Deprecated
  public static final Map<String, String> PERSISTABLE_ATTRIBUTES;

  /**
   * Optional single-valued, boolean property that marks this document as one
   * the Connector Manager should persist locally in its per-document store.
   * If not present, this is assumed to be {@code false}. If {@code true},
   * then the Connector Manager will persist all attributes that are keys in
   * the {@link #PERSISTABLE_ATTRIBUTES} map.
   *
   * @deprecated The per-document store has never been publicly implemented
   */
  @Deprecated
  public static final String PROPNAME_MANAGER_SHOULD_PERSIST = "google:persist";

  /**
   * Reserved by the Connector Manager to indicate the connector instance
   * that submitted this document. Should not be supplied by the
   * connector developer, and if supplied, it will be ignored.
   * <p/>
   * This property is persistable (it is one of the keys in the
   * {@link #PERSISTABLE_ATTRIBUTES} map.
   *
   * @deprecated The per-document store has never been publicly implemented
   */
  @Deprecated
  public static final String PROPNAME_CONNECTOR_INSTANCE = "google:connector_instance";

  /**
   * Reserved by the Connector Manager to indicate type of the connector
   * that submitted this document. Should not be supplied by the
   * connector developer, and if supplied, it will be ignored.
   * <p/>
   * This property is persistable (it is one of the keys in the
   * {@link #PERSISTABLE_ATTRIBUTES} map.
   *
   * @deprecated The per-document store has never been publicly implemented
   */
  @Deprecated
  public static final String PROPNAME_CONNECTOR_TYPE = "google:connector_type";

  /**
   * Optional, single-valued property that gives the name primary folder in
   * which this document lives. If not supplied, but the
   * {@link #PROPNAME_FOLDER} property is supplied, then the first value of
   * that multi-valued property will be used here. The primary use-case of this
   * attribute is to be stored, so that a connector can later query to find
   * all documents in a folder.
   * <p/>
   * This property is persistable (it is one of the keys in the
   * {@link #PERSISTABLE_ATTRIBUTES} map.
   *
   * @deprecated The per-document store has never been publicly implemented
   */
  @Deprecated
  public static final String PROPNAME_PRIMARY_FOLDER = "google:primary_folder";

  /**
   * Reserved by the Connector Manager to indicate the time at which the
   * Connector Manager handled this document. Should not be supplied by the
   * connector developer, and if supplied, it will be ignored.
   * <p/>
   * This property is persistable (it is one of the keys in the
   * {@link #PERSISTABLE_ATTRIBUTES} map.
   *
   * @deprecated The per-document store has never been publicly implemented
   */
  @Deprecated
  public static final String PROPNAME_TIMESTAMP = "google:timestamp";

  /**
   * Optional, single-valued property that gives a message from the connector
   * instance about the state of this document.
   * <p/>
   * This property is persistable (it is one of the keys in the
   * {@link #PERSISTABLE_ATTRIBUTES} map.
   *
   * @deprecated The per-document store has never been publicly implemented
   */
  @Deprecated
  public static final String PROPNAME_MESSAGE = "google:message";

  /**
   * Optional, single-valued property that gives a compact representation of a
   * document's content and attributes, to enable a quick comparison with a
   * foreign repository to see if the document has changed. For example, this
   * attribute might contain a content hash. The primary use-case of this
   * attribute is to be stored in the Connector Manager's per-document store. It
   * will not be supplied when sending a document to the GSA for indexing.
   * <p/>
   * This property is persistable (it is one of the keys in the
   * {@link #PERSISTABLE_ATTRIBUTES} map.
   *
   * @deprecated The per-document store has never been publicly implemented
   */
  @Deprecated
  public static final String PROPNAME_SNAPSHOT = "google:snapshot";

  /**
   * Optional, single-valued property that gives the name of the high-level
   * container object in which the document lives. This may be an object such
   * as a cabinet or list. The primary use-case of this attribute is to be
   * stored, so that a connector can later query to find all documents in a
   * container.
   * <p/>
   * This property is persistable (it is one of the keys in the
   * {@link #PERSISTABLE_ATTRIBUTES} map.
   *
   * @deprecated The per-document store has never been publicly implemented
   */
  @Deprecated
  public static final String PROPNAME_CONTAINER = "google:container";

  /**
   * Optional, single-valued property to specify the length, in bytes, of the
   * supplied content.
   * 
   * @since 3.0.8
   */
  public static final String PROPNAME_CONTENT_LENGTH =
      "google:contentlength";

  /**
   * Optional, single-valued property to specify the existing encoding of the 
   * supplied content, rather than letting the Connector Manager choose an 
   * encoding to apply to the supplied content.
   * 
   * If a value of google:contentencoding is null, Connector Manager will encode 
   * the content. If the value is "base64binary", it denotes that the supplied 
   * content is already Base64 encoded. If the value is "base64compressed" 
   * it denotes the supplied content is already compressed then Base64 encoded.
   * 
   * @since 3.0.8
   */
  public static final String PROPNAME_CONTENT_ENCODING =
      "google:contentencoding";

  /**
   * Enum for feed content encoding.
   * 
   * @see "Feeds Protocol Developer's Guide"
   */
  public enum ContentEncoding {
    BASE64BINARY("base64binary"), 
    BASE64COMPRESSED("base64compressed"),
    ERROR("error");

    private final String tag;

    ContentEncoding(String tag) {
      this.tag = tag;
    }

    /**
     * @return The enum matching the given {@code tag}.
     *         {@code ContentEncoding.ERROR} will be returned if the given
     *         {@code tag} does not match a known {@code ContentEncoding}.
     */
    public static ContentEncoding findContentEncoding(String tag) {
      try {
        return Enum.valueOf(ContentEncoding.class, tag.toUpperCase());
      } catch (IllegalArgumentException e) {
        // Not found, return ERROR.
        return ERROR;
      }
    }

    @Override
    public String toString() {
      return tag;
    }
  }

  /**
   * Optional, single-valued property the Connector Manager will persist in its
   * per-document store. This property will not be supplied when sending a
   * document to the GSA for indexing.
   *
   * @deprecated The per-document store has never been publicly implemented
   */
  @Deprecated
  public static final String PROPNAME_PERSISTED_CUSTOMDATA_1 = "google:custom1";

  /**
   * Optional, single-valued property the Connector Manager will persist in its
   * per-document store. This property will not be supplied when sending a
   * document to the GSA for indexing.
   *
   * @deprecated The per-document store has never been publicly implemented
   */
  @Deprecated
  public static final String PROPNAME_PERSISTED_CUSTOMDATA_2 = "google:custom2";

  static {
    PERSISTABLE_ATTRIBUTES = ImmutableMap.<String, String> builder().
        put(PROPNAME_DOCID, "docid").
        put(PROPNAME_FEEDID, "feedid").
        put(PROPNAME_PRIMARY_FOLDER, "folderparent").
        put(PROPNAME_ACTION, "action").
        put(PROPNAME_TIMESTAMP, "timestamp").
        put(PROPNAME_MESSAGE, "message").
        put(PROPNAME_SNAPSHOT, "snapshot").
        put(PROPNAME_CONTAINER, "container").
        put(PROPNAME_PERSISTED_CUSTOMDATA_1, "custom1").
        put(PROPNAME_PERSISTED_CUSTOMDATA_2, "custom2").
        build();
  }

  /**
   * Name of the default user profile collection.
   *
   * @since 3.0
   */
  public static final String DEFAULT_USERPROFILE_COLLECTION =
      "_google_social_userprofile_collection";

  /**
   * Enum for the list of possible inheritance types.
   *
   * @since 3.0
   */
  public enum AclInheritanceType {
    PARENT_OVERRIDES("parent-overrides"), CHILD_OVERRIDES("child-overrides"),
    AND_BOTH_PERMIT("and-both-permit");

    private final String tag;

    private AclInheritanceType(String tag) {
      this.tag = tag;
    }

    @Override
    public String toString() {
      return tag;
    }
  }

  /**
   * Enum for the list of possible ACL scope values.
   *
   * @since 3.0
   */
  public enum AclScope {
    USER("user"), GROUP("group");

    private final String tag;

    private AclScope(String tag) {
      this.tag = tag;
    }

    @Override
    public String toString() {
      return tag;
    }
  }

  /**
   * Enum for the list of possible ACL access values.
   *
   * @since 3.0
   */
  public enum AclAccess {
    PERMIT("permit"), DENY("deny");

    private final String tag;

    private AclAccess(String tag) {
      this.tag = tag;
    }

    @Override
    public String toString() {
      return tag;
    }
  }

  /**
   * Enum for the types of Principals.
   *
   * @since 3.0
   */
  public enum PrincipalType {
    /** The type is not known. The GSA will guess what the domain is. */
    UNKNOWN("unknown"),
    /** There is no domain. The GSA will not guess the domain. */
    UNQUALIFIED("unqualified"),
    ;

    private final String tag;

    private PrincipalType(String tag) {
      this.tag = tag;
    }

    @Override
    public String toString() {
      return tag;
    }
  }

  /**
   * Enum for the possible case sensitivity rules.
   *
   * @since 3.0
   */
  public enum CaseSensitivityType {
    /** All strings are case sensitive. */
    EVERYTHING_CASE_SENSITIVE("everything-case-sensitive"),
    /** All strings are case insensitive. */
    EVERYTHING_CASE_INSENSITIVE("everything-case-insensitive"),
    ;

    private final String tag;

    private CaseSensitivityType(String tag) {
      this.tag = tag;
    }

    @Override
    public String toString() {
      return tag;
    }
  }
}
