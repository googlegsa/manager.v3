// Copyright (C) 2006 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.spi;

/**
 * Non-instantiable class that holds constants used by the SPI and documents
 * their meanings.
 * <p>
 * All constants whose names begin with PROPNAME are reserved names for
 * properties that may be accessed from a PropertyMap returned as a query
 * result. The actual values of these property name constants all begin with
 * "google:". For future compatibility, all property names beginning with
 * "google:" are reserved.
 */
public class SpiConstants {
  private SpiConstants() {
    // prevents instantiation
  }

  /**
   * Identifies a single-valued, string property that uniquely identifies a
   * document to this connector. The internal structure of this string is opaque
   * to the GSA. Only printable, non-whitespace, ASCII characters are permitted
   * in a DOCID. The connector implementor is encouraged to implement this by
   * using the natural ID in the foreign repository.
   * <p>
   * Value: google:docid
   * 
   */
  public static final String PROPNAME_DOCID = "google:docid";

  /**
   * Identifies a single-valued, date property that gives the last modified date
   * of a document.
   * <p>
   * Value: google:lastmodify
   */
  public static final String PROPNAME_LASTMODIFY = "google:lastmodify";

  /**
   * Identifies a single-valued string property that gives a legal http url that
   * the GSA can use to pull the primary content file for a document.
   * <p>
   * Value: google:contenturl
   */
  public static final String PROPNAME_CONTENTURL = "google:contenturl";

  /**
   * Identifies an optional single-valued string property that, if present, will
   * be used by the GSA as the primary URI for this documeny - instead of the
   * normal googleconnector:// URI which the connector manager fabricates.
   * Connector developers should provide this if they the GSA to do web-style
   * authentication and authorization for this document.  If this is specified, 
   * the GSA will not call back to the connector manager as serve-time
   * <p>
   * Value: google:contenturl
   */
  public static final String PROPNAME_SEARCHURL = "google:searchurl";

  /**
   * Identifies a single-valued property that may be either string or binary and
   * gives direct access to the primary content to be indexed.
   * <p>
   * Value: google:content
   */
  public static final String PROPNAME_CONTENT = "google:content";

  /**
   * Identifies a single-valued string property that serves as a security token.
   * At serve time, the GSA presents this token along with the querying user's
   * identity, and the connector tells us whether this user has permission to
   * view a document of this class. This may be implemented by a textual pointer
   * to an ACL.
   * <p>
   * Value: google:securitytoken
   */
  public static final String PROPNAME_SECURITYTOKEN = "google:securitytoken";

  /**
   * Identifies an single-valued String property that gives the mime type for
   * the content of this document. If this is not supplied, then the system will
   * use the value of DEFAULT_MIMETYPE.
   * <p>
   * Value: google:mimetype
   */
  public static final String PROPNAME_MIMETYPE = "google:mimetype";

  /**
   * The mime type that the connector manager uses as a default, if a document
   * does not specify.
   * <p>
   * Value: text/html
   */
  public static final String DEFAULT_MIMETYPE = "text/html";

  /**
   * Identifies an optional, single-valued property that gives a URL that should
   * be used in a results page as the primary user reference for a document.
   * This may be different from the contenturl, if present: contenturl should
   * give direct access to the content file, whereas displayurl may point into
   * the CMS's web front-end application.
   * <p>
   * Value: google:displayurl
   */
  public static final String PROPNAME_DISPLAYURL = "google:displayurl";

  /**
   * If this boolean property is present and is true, then the document will
   * be marked as public.
   */
  public static final String PROPNAME_ISPUBLIC = "google:ispublic";

  
  /**
   * Identifies a property used in authorization results that indicates whether
   * this user can see this document.
   * <p>
   * Value: google:authviewpermit
   */
  public static final String PROPNAME_AUTH_VIEWPERMIT = "google:authviewpermit";
}
