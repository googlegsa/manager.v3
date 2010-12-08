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

/**
 * Non-instantiable class that holds constants used by the SPI and
 * documents their meanings.
 * <p>
 * All constants whose names begin with PROPNAME are reserved names for
 * properties that may be accessed from a Document returned as a query
 * result. The actual values of these property name constants all begin
 * with "google:". For future compatibility, all property names beginning
 * with "google:" are reserved.
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
   * <p>
   * This property is required on all Documents. The connector implementor is
   * encouraged to implement this by using the natural ID in the foreign
   * repository.
   * <p>
   * Value: google:docid
   */
  public static final String PROPNAME_DOCID = "google:docid";

  /**
   * Identifies a single-valued, date property that gives the last modified
   * date of a document.  This property is optional but strongly recommended in
   * order to associate a specific date to the document.
   * <p>
   * Value: google:lastmodify
   */
  public static final String PROPNAME_LASTMODIFIED = "google:lastmodified";

  /**
   * Identifies an optional string property that is the title of the document.
   * This value is useful for providing a title for documents that supply no
   * content, or for which a title cannot be automatically extracted from the
   * supplied content.
   * <p>
   * Value: google:title
   */
  public static final String PROPNAME_TITLE = "google:title";

  /**
   * Reserved for future use.
   * <p>
   * Value: google:contenturl
   */
  public static final String PROPNAME_CONTENTURL = "google:contenturl";

  /**
   * Identifies a single-valued FeedType property that, if present, will be
   * used to determine the feed type for this document.  It is strongly
   * recommended that this property be set to explicitly determine the feed
   * type ('content' or 'web') for the document.
   * <p>
   * If this property is not set, the feed type will be determined as follows:
   * <ol>
   * <li> If there is no {@link #PROPNAME_SEARCHURL} then the feed type will
   *      default to 'content' feed using a fabricated URL derived from the
   *      {@link #PROPNAME_DOCID}.
   * <li> If there is a {@link #PROPNAME_SEARCHURL} then the feed type will
   *      default to 'web' feed and use the {@link #PROPNAME_SEARCHURL} as the
   *      document URL.
   * </ol>
   * <p>
   * Value: google:feedtype
   *
   * @since 2.4.2
   */
  public static final String PROPNAME_FEEDTYPE = "google:feedtype";

  /**
   * Identifies a single-valued Feed ID property that, if present, will be
   * used to identify the feed file that contains a fed document.  All feed
   * records in a single feed file will share a common google:feedid value.
   * <p>
   * Reserved for internal use.
   * <p>
   * Value: google:feedid
   *
   * @since 2.6
   */
  public static final String PROPNAME_FEEDID = "google:feedid";

  /**
   * Identifies an optional single-valued string property that, if present,
   * will be used by the Search Appliance as the primary URI for this document
   * - instead of the normal googleconnector:// URI which the connector manager
   * fabricates based on the {@link #PROPNAME_DOCID} and the connector name.
   * <p>
   * Value: google:searchurl
   */
  public static final String PROPNAME_SEARCHURL = "google:searchurl";

  /**
   * Identifies a single-valued property that may be either string or
   * binary and gives direct access to the primary content to be indexed.
   * <p>
   * Value: google:content
   */
  public static final String PROPNAME_CONTENT = "google:content";

  /**
   * Identifies a single-valued string property that serves as a security
   * token. At serve time, the Search Appliance presents this token along
   * with the querying user's identity, and the connector tells us whether
   * this user has permission to view a document of this class. This may be
   * implemented by a textual pointer to an ACL.
   * <p>
   * Value: google:securitytoken
   */
  public static final String PROPNAME_SECURITYTOKEN = "google:securitytoken";

  /**
   * Identifies an single-valued String property that gives the mime type
   * for the content of this document. If this is not supplied, then the
   * system will use the value of DEFAULT_MIMETYPE.
   * <p>
   * Value: google:mimetype
   */
  public static final String PROPNAME_MIMETYPE = "google:mimetype";

  /**
   * The mime type that the connector manager uses as a default, if a
   * document does not specify.
   * <p>
   * Value: text/html
   */
  public static final String DEFAULT_MIMETYPE = "text/html";

  /**
   * Identifies an optional, single-valued property that gives a URL that
   * should be used in a results page as the primary user reference for a
   * document. This may be different from the contenturl, if present:
   * contenturl should give direct access to the content file, whereas
   * displayurl may point into the CMS's web front-end application.
   * <p>
   * Value: google:displayurl
   */
  public static final String PROPNAME_DISPLAYURL = "google:displayurl";

  /**
   * Unless this property is present and is false, then the document will
   * be marked as public.
   * <p>
   * Value: google:ispublic
   */
  public static final String PROPNAME_ISPUBLIC = "google:ispublic";

  /**
   * Identifies a multiple-valued String property that gives the list of
   * group ACL Scope IDs that are permitted RoleType.READER access to this
   * document. If either of the PROPNAME_ACLGROUPS or PROPNAME_ACLUSERS
   * properties are non-null, then the GSA will grant or deny access to this
   * document for a given user on the basis of whether the user's name appears
   * as one of the Scope IDs in the PROPNAME_ACLUSERS list or one of the user's
   * groups appears as one of the Scope IDs in the PROPNAME_ACLGROUPS list.
   * <p>
   * ACL Scope ID is a group or user name within the scope of the Connector.
   * <p>
   * To specify more than just RoleType.READER access to the document, the
   * Connector must add additional multi-value role properties to the document.
   * These entries are of the form:
   * <pre>
   *   Name = &lt;GROUP_ROLES_PROPNAME_PREFIX&gt; + &lt;scopeId&gt;
   *   Value = [RoleType[, ...]]
   * </pre>
   * where &lt;GROUP_ROLES_PROPNAME_PREFIX&gt; is the
   * {@link #GROUP_ROLES_PROPNAME_PREFIX}, &lt;scopeId&gt; is the group ACL
   * Scope ID, and RoleType is one of the possible RoleType values.  User ACL
   * Roles are of the form:
   * <pre>
   *   Name = &lt;USER_ROLES_PROPNAME_PREFIX&gt; + &lt;scopeId&gt;
   *   Value = [RoleType[, ...]]
   * </pre>
   * where the &lt;scopeId&gt; will be the user ACL Scope ID.
   * <p>
   * If the PROPNAME_ISPUBLIC is missing or is true, then this property
   * is ignored, since the document is public.
   * <p>
   * If both the PROPNAME_ACLGROUPS and PROPNAME_ACLUSERS properties are
   * null or empty, then the GSA will use the authorization SPI to grant
   * or deny access to this document.
   * <p>
   * The GSA may be configured to bypass on-board authorization, in which
   * case these properties will be ignored, and the GSA will use the
   * authorization SPI to grant or deny access to this document.
   * <p>
   * Value: google:aclgroups
   */
  public static final String PROPNAME_ACLGROUPS = "google:aclgroups";

  /**
   * Identifies a multiple-valued String property that gives the list of
   * users that are permitted access to this document. For details, see
   * the {@link #PROPNAME_ACLGROUPS}.
   * <p>
   * Value: google:aclusers
   */
  public static final String PROPNAME_ACLUSERS = "google:aclusers";

  /**
   * Prefix added to the front of the group ACL Scope ID when creating a group
   * roles property name.  If the Connector wants to define specific roles
   * associated with a group ACL Scope ID related to a document they should be
   * stored in a multi-valued property named:
   * <pre>
   *   GROUP_ROLES_PROPNAME_PREFIX + &lt;scopeId&gt;
   * </pre>
   * For example, given a group ACL Entry of "eng=reader,writer" the roles for
   * "eng" would be stored in a property as follows:
   * <pre>
   * Name = "google:group:roles:eng"
   * Value = [reader, writer]
   * </pre>
   */
  public static final String GROUP_ROLES_PROPNAME_PREFIX =
      "google:group:roles:";

  /**
   * Prefix added to the front of the user ACL Scope ID when creating a user
   * roles property name.  If the Connector wants to define specific roles
   * associated with a user ACL Scope ID related to a document they should be
   * stored in a multi-valued property named:
   * <pre>
   *   USER_ROLES_PROPNAME_PREFIX + &lt;scopeId&gt;
   * </pre>
   * For example, given a user ACL Entry of "joe=reader,writer" the roles for
   * "joe" would be stored in a property as follows:
   * <pre>
   * Name = "google:user:roles:joe"
   * Value = [reader, writer]
   * </pre>
   */
  public static final String USER_ROLES_PROPNAME_PREFIX = "google:user:roles:";

  /**
   * Identifies an optional, single-valued property that specifies the action
   * associated with the document.  If not specified, then the system will
   * not specify the action and the default behavior will be observed.
   * <p>
   * Value: google:action
   */
  public static final String PROPNAME_ACTION = "google:action";

  /**
   * Identifies an optional, multi-valued property that specifies the
   * folder path of the document. The document name should not be
   * included in the path. Multiple values are permitted to support
   * repositories that link documents to multiple parent folders.
   * <p>
   * Examples:
   * <pre>
   *     /ENGINEERING/techdoc/pdfs
   *
   *     Enterprise:Marketing:Press Releases
   *
   *     https://sp.example.com/sites/mylist
   * </pre>
   * <p>
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
   * @see <a href='http://code.google.com/apis/searchappliance/documentation/62/feedsguide.html#defining_the_xml'>Defining the XML Record for a Document</a>
   * @since 2.6.6
   */
  public static final String PROPNAME_LOCK = "google:lock";

  /**
   * Enum for the list of possible feed types.
   *
   * @since 2.4.2
   */
  public enum FeedType {
    CONTENT, WEB
  }

  /**
   * Ordinal-base typesafe enum for action types.
   */
  // TODO: Change this to an Enum.
  public static class ActionType implements Comparable<ActionType> {
    private static int nextOrdinal = 0;
    private final int ordinal = nextOrdinal++;

    public static final ActionType ADD = new ActionType("add");
    public static final ActionType DELETE = new ActionType("delete");
    public static final ActionType ERROR = new ActionType("error");

    private static final ActionType[] PRIVATE_VALUES = {ADD, DELETE};

    private final String tag;

    ActionType(String m) {
        tag = m;
    }

    @Override
    public String toString() {
      return tag;
    }

    /**
     * @return The enum matching the given <code>tag</code>.
     * <code>ActionType.ERROR</code> will be returned if the given
     * <code>tag</code> does not match a known <code>ActionType</code>.
     */
    public static ActionType findActionType(String tag) {
        if (tag == null) {
          return ERROR;
        }
        for (int i = 0; i < PRIVATE_VALUES.length; i++) {
          if (PRIVATE_VALUES[i].tag.equals(tag)) {
            return PRIVATE_VALUES[i];
          }
        }
        return ERROR;
      }

    public int compareTo(ActionType actionType) {
        return ordinal - actionType.ordinal;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ordinal;
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      ActionType other = (ActionType) obj;
      if (ordinal != other.ordinal) {
        return false;
      }
      return true;
    }
  }

  /**
   * Ordinal-base typesafe enum for known role types.
   */
  // TODO: Change this to an Enum.
  public static class RoleType implements Comparable<RoleType> {
    private static int nextOrdinal = 0;
    private final int ordinal = nextOrdinal++;

    public static final RoleType PEEKER = new RoleType("peeker");
    public static final RoleType READER = new RoleType("reader");
    public static final RoleType WRITER = new RoleType("writer");
    public static final RoleType OWNER = new RoleType("owner");
    public static final RoleType ERROR = new RoleType("error");

    private static final RoleType[] PRIVATE_VALUES =
        {PEEKER, READER, WRITER, OWNER};

    private final String tag;

    RoleType(String m) {
        tag = m;
    }

    @Override
    public String toString() {
      return tag;
    }

    /**
     * @return The enum matching the given <code>tag</code>.
     * <code>RoleType.ERROR</code> will be returned if the given
     * <code>tag</code> does not match a known <code>RoleType</code>.
     */
    public static RoleType findRoleType(String tag) {
        if (tag == null) {
          return ERROR;
        }
        for (int i = 0; i < PRIVATE_VALUES.length; i++) {
          if (PRIVATE_VALUES[i].tag.equals(tag)) {
            return PRIVATE_VALUES[i];
          }
        }
        return ERROR;
      }

    public int compareTo(RoleType roleType) {
        return ordinal - roleType.ordinal;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ordinal;
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      RoleType other = (RoleType) obj;
      if (ordinal != other.ordinal) {
        return false;
      }
      return true;
    }
  }
}
