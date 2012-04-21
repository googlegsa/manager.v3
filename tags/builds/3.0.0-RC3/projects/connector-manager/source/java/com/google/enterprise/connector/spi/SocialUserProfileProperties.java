// Copyright 2012 Google Inc.
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
 * Distinguished Properties for user profiles
 * 
 * @author tapasnay
 * @since 3.0
 */
public class SocialUserProfileProperties {
  // for name of properties
  // I actually wanted to use '.' or ':' separated namespaces, but
  // unfortunately meta queries dont seem to work with names with . in it.
  // TBD: fix this.

  /**
   * Optional, multi-valued property that provides the subjects the user can be
   * asked about.
   */
  public static final String PROPNAME_ASKMEABOUT = "google_social_user_askmeabout";

  /**
   * Optional, multi-valued property that provides the topics in which the user
   * has specific skills.
   */
  public static final String PROPNAME_SKILLS = "google_social_user_skills";

  /**
   * Optional, multi-valued property that provides the names of projects user
   * has been involved in the past.
   */
  public static final String PROPNAME_PASTPROJECTS = 
      "google_social_user_pastprojects";

  /**
   * Optional, single valued property that provides the accountname of the user.
   * It is typically a unique value per user.
   */
  public static final String PROPNAME_ACCOUNTNAME = 
      "google_social_user_accountname";

  /**
   * Optional, single valued property that provides a name user prefers to be
   * called by
   */
  public static final String PROPNAME_PREFERREDNAME = 
      "google_social_user_preferredname";

  /**
   * Optional, single valued property that provides a name user prefers to be
   * called by
   */
  public static final String PROPNAME_USERCONTENT = 
      "google_social_user_content";

  /**
   * Optional, single valued property that provides URL to a profile picture of
   * user
   */
  public static final String PROPNAME_PICTUREURL = 
      "google_social_user_pictureurl";

  /**
   * Optional, single valued property that provides URL to a profile picture of
   * user
   */
  public static final String PROPNAME_ORGURL = "google_social_user_orgurl";

  /**
   * Optional, property that describes contact information for user's colleagues
   * in xml. Schema of this xml is as follows
   * 
   * <pre>
   * {@code <?xml version="1.0" encoding="UTF-8"?>
   *       <schema xmlns="http://www.w3.org/2001/XMLSchema" 
   *       targetNamespace="http://www.google.com/schemas/gsa"
   *         xmlns:gsa="http://www.google.com/schemas/gsa" 
   *         elementFormDefault="qualified">
   *         <element name="Contacts">
   *           <complexType>
   *             <sequence>
   *               <element name="contact" maxOccurs="unbounded" minOccurs="1">
   *                 <complexType>
   *                   <attribute name="name" type="string" form="qualified"
   *                     use="optional" />
   *                   <attribute name="email" type="string" form="qualified" />
   *                   <attribute name="accountname" type="string" 
   *                   form="qualified" use="required" />
   *                   <attribute name="url" type="string" form="qualified"
   *                     use="optional" />
   *                   <attribute name="title" type="string" form="qualified"
   *                     use="optional" />
   *                   <attribute name="group" type="string" form="qualified"
   *                     use="optional" />
   *                   <attribute name="isinworkinggroup" type="boolean" 
   *                   form="qualified" use="optional" />
   *                 </complexType>
   *               </element>
   *             </sequence>
   *           </complexType>
   *         </element>
   *       </schema> }
   * </pre>
   */
  public static final String PROPNAME_COLLEAGUES = 
      "google_social_user_colleagues";

  /**
   * Optional, single-valued property that provides the url to point to for
   * opening the profile of user
   */
  public static final String PROPNAME_USERPROFILEURL = 
      "google_social_userprofile_url";
}
