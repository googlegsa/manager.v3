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

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * A user profile document object.
 *
 * @author tapasnay
 * @since 3.0
 */
public class SocialUserProfileDocument implements Document {
  private static List<Value> getValueList(String value) {
    return Collections.singletonList(Value.getStringValue(value));
  }

  private static List<Value> getValueList(List<String> valueList) {
    List<Value> values = new ArrayList<Value>();
    for (String val : valueList) {
      values.add(Value.getStringValue(val));
    }
    return values;
  }

  private static Value getSingleValue(List<Value> valueList) {
    if (valueList == null) {
      return null;
    }
    return valueList.get(0);
  }

  /**
   * A colleague object. A colleague of a user is a person who the user normally
   * works with, may or may not be in the same group.
   *
   * @since 3.0
   */
  public static class ColleagueData {
    private String identity;
    private String name;
    private boolean isInWorkGroup;
    private String group;
    private String email;
    private String title;
    private String url;

    /**
     * Sets identity of colleague.
     *
     * @param identity identity (account name) of colleague
     */
    public void setIdentity(String identity) {
      this.identity = identity;
    }

    /**
     * Gets identity of colleague.
     *
     * @return identity of colleague
     */
    public String getIdentity() {
      return identity;
    }

    /**
     * Sets name of colleague.
     *
     * @param name commonly used name of colleague
     */
    public void setName(String name) {
      this.name = name;
    }

    /**
     * Gets name of colleague.
     *
     * @return commonly used name of colleague
     */
    public String getName() {
      return name;
    }

    /**
     * Sets if colleague is in same work group as user.
     *
     * @param isInWorkGroup {@code true} if colleague is in same work group as
     *          user
     */
    public void setInWorkGroup(boolean isInWorkGroup) {
      this.isInWorkGroup = isInWorkGroup;
    }

    /**
     * Gets whether colleague is in same work group as user.
     *
     * @return {@code true} if colleague is in same work group as user
     */
    public boolean isInWorkGroup() {
      return isInWorkGroup;
    }

    /**
     * Sets work group of colleague.
     *
     * @param group work group of colleague
     */
    public void setGroup(String group) {
      this.group = group;
    }

    /**
     * Gets work group of colleague.
     *
     * @return work group of colleague
     */
    public String getGroup() {
      return group;
    }

    /**
     * Sets emailId of colleague.
     *
     * @param email emailId of colleague
     */
    public void setEmail(String email) {
      this.email = email;
    }

    /**
     * Gets emailId of colleague.
     *
     * @return emailId of colleague
     */
    public String getEmail() {
      return email;
    }

    /**
     * Sets title of colleague.
     *
     * @param title title of colleague
     */
    public void setTitle(String title) {
      this.title = title;
    }

    /**
     * Gets title of colleague.
     *
     * @return title of colleague
     */
    public String getTitle() {
      return title;
    }

    /**
     * Sets URL of colleague.
     *
     * @param url URL of the colleague
     */
    public void setUrl(String url) {
      this.url = url;
    }

    /**
     * Gets URL of colleague.
     *
     * @return URL of the colleague
     */
    public String getUrl() {
      return url;
    }
  }

  /**
   * Set of properties, besides the special ones.
   */
  private final Map<String, List<Value>> properties =
      new HashMap<String, List<Value>>();
  /**
   * Colleagues of this user.
   */
  private final List<ColleagueData> colleagues = new ArrayList<ColleagueData>();
  /**
   * Collection name where the userprofile needs to go.
   */
  private String collectionName;

  /**
   * Constructor takes collectionName as parameter for the user profile
   * document.
   *
   * @param collectionName name of collection where the profile document needs
   *          to go
   */
  public SocialUserProfileDocument(String collectionName) {
    this.collectionName = collectionName;
  }

  /**
   * Gets user content which is typically a description of the user in his/her
   * own words.
   *
   * @return user content
   */
  public Value getUserContent() {
    List<Value> values = properties
        .get(SocialUserProfileProperties.PROPNAME_USERCONTENT);
    if ((values == null) || (values.size() == 0)) {
      return null;
    }
    return values.get(0);
  }

  /**
   * Sets user content: typically a description of user in own words.
   *
   * @param userContent user content
   */
  public void setUserContent(String userContent) {
    properties.put(SocialUserProfileProperties.PROPNAME_USERCONTENT,
        getValueList(userContent));
  }

  /**
   * Sets skill set of user.
   *
   * @param skillText skills
   */
  public void setSkills(List<String> skillText) {
    properties.put(SocialUserProfileProperties.PROPNAME_SKILLS,
        getValueList(skillText));
  }

  /**
   * Gets skills of user.
   *
   * @return skills
   */
  public List<Value> getSkills() {
    return properties.get(SocialUserProfileProperties.PROPNAME_SKILLS);
  }

  /**
   * Sets topics user can be asked about.
   *
   * @param askmeAbout topics user can be asked about
   */
  public void setAskmeAbout(List<String> askmeAbout) {
    properties.put(SocialUserProfileProperties.PROPNAME_ASKMEABOUT,
        getValueList(askmeAbout));
  }

  /**
   * Gets topics user can be asked about.
   *
   * @return topics user can be asked about
   */
  public List<Value> getAskmeAbout() {
    return properties.get(SocialUserProfileProperties.PROPNAME_ASKMEABOUT);
  }

  /**
   * Sets user's past projects.
   *
   * @param values projects user worked in the past
   */
  public void setPastProjects(List<String> values) {
    properties.put(SocialUserProfileProperties.PROPNAME_PASTPROJECTS,
        getValueList(values));
  }

  /**
   * Gets user's past projects.
   *
   * @return projects user worked in the past
   */
  public List<Value> getPastProjects() {
    return properties.get(SocialUserProfileProperties.PROPNAME_PASTPROJECTS);
  }

  /**
   * Sets whether the profile is public or not.
   *
   * @param isPublic true if the profile is public
   */
  public void setPublic(boolean isPublic) {
    properties.put(SpiConstants.PROPNAME_ISPUBLIC,
        getValueList(isPublic ? "true" : "false"));
  }

  /**
   * Gets whether profile is public.
   *
   * @return true if the profile is public
   */
  public boolean getPublic() {
    Value value = getSingleValue(properties.get(SpiConstants.PROPNAME_ISPUBLIC));
    if (value == null) {
      return false;
    }
    if (value.toString().equals("true")) {
      return true;
    }
    return false;
  }

  /**
   * Gets user's unique identity.
   *
   * @return unique key identifying a user
   */
  public Value getUserKey() {
    return getSingleValue(properties
        .get(SocialUserProfileProperties.PROPNAME_ACCOUNTNAME));
  }

  /**
   * Sets user's unique identity.
   *
   * @param userKey unique key identifying a user
   */
  public void setUserKey(String userKey) {
    properties.put(SocialUserProfileProperties.PROPNAME_ACCOUNTNAME,
        getValueList(userKey));
  }

  /**
   * Sets a list of values to a property (maybe beyond the distinguished
   * properties).
   *
   * @param name name of user profile property
   * @param values list of values of property
   */
  public void setProperty(String name, List<String> values) {
    properties.put(name, getValueList(values));
  }

  /**
   * Sets a single value to a property.
   *
   * @param name name of property
   * @param value value of property
   */
  public void setProperty(String name, String value) {
    properties.put(name, getValueList(value));
  }

  /**
   * Gets a list of values for a property, the list may be null.
   *
   * @param name name of property
   * @return list of values: may be null
   */
  public List<Value> getProperty(String name) {
    return properties.get(name);
  }

  /**
   * Sets URL of the profile picture of user.
   *
   * @param pictureUrl URL with the profile picture of user
   */
  public void setPictureUrl(String pictureUrl) {
    properties.put(SocialUserProfileProperties.PROPNAME_PICTUREURL,
        getValueList(pictureUrl));
  }

  /**
   * Gets URL of the profile picture.
   *
   * @return : profile picture URL
   */
  public Value getPictureUrl() {
    return getSingleValue(properties
        .get(SocialUserProfileProperties.PROPNAME_PICTUREURL));
  }

  /**
   * Sets organization URL of the user.
   *
   * @param orgUrl URL of user's organization
   */
  public void setOrgUrl(String orgUrl) {
    properties.put(SocialUserProfileProperties.PROPNAME_ORGURL,
        getValueList(orgUrl));
  }

  /**
   * Gets organization URL of the user.
   *
   * @return URL of user's organization
   */
  public Value getOrgUrl() {
    return getSingleValue(properties
        .get(SocialUserProfileProperties.PROPNAME_ORGURL));
  }

  /**
   * Sets list of colleagues of user. This method serializes the list into XML
   * according to <code>Contacts.xsd</code>.
   *
   * @param colleagues list of colleagues of user
   * @throws RepositoryException
   */
  public void setColleagues(List<ColleagueData> colleagues)
      throws RepositoryException {
    this.colleagues.clear();
    this.colleagues.addAll(colleagues);
    try {
      properties.put(SocialUserProfileProperties.PROPNAME_COLLEAGUES,
          getValueList(serializeColleagues()));
    } catch (IOException e) {
      throw new RepositoryException(e);
    }
  }

  /**
   * Gets list of colleagues.
   *
   * @return list of colleagues of user
   */
  public List<ColleagueData> getColleagues() {
    return this.colleagues;
  }

  private String serializeColleagues() throws IOException, RepositoryException {
    // Get an instance of factory
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    org.w3c.dom.Document dom;
    DOMImplementation domImpl;
    try {
      // Get an instance of builder
      DocumentBuilder db = dbf.newDocumentBuilder();
      domImpl = db.getDOMImplementation();
      // Create an instance of DOM
      dom = domImpl.createDocument(GSA_NAMESPACE, CONTACTS_ROOT_ELEMENT, null);
    } catch (ParserConfigurationException pce) {
      throw new RepositoryException(pce);
    }

    // Create the root element
    for (ColleagueData oneColleague : this.colleagues) {
      // For each Colleague object create element and attach it to root
      Element colleagueElem = createColleagueElement(dom, oneColleague);
      dom.getDocumentElement().appendChild(colleagueElem);
    }

    DOMImplementationLS ls = (DOMImplementationLS) domImpl;
    LSSerializer lss = ls.createLSSerializer();
    LSOutput lso = ls.createLSOutput();
    StringWriter writer = new StringWriter();
    lso.setCharacterStream(writer);
    lss.write(dom, lso);

    String result = writer.toString();
    return result;
  }

  private Element createColleagueElement(org.w3c.dom.Document dom,
      ColleagueData oneColleague) {
    try {
      Element colleagueEle = dom
          .createElementNS(GSA_NAMESPACE, CONTACT_ELEMENT);
      colleagueEle
          .setAttributeNS(GSA_NAMESPACE, "gsa:accountname", URLEncoder.encode(
              spaceForNullString(oneColleague.identity), "UTF-8"));

      colleagueEle.setAttributeNS(GSA_NAMESPACE, "gsa:name",
          URLEncoder.encode(spaceForNullString(oneColleague.name), "UTF-8"));
      colleagueEle.setAttributeNS(GSA_NAMESPACE, "gsa:email",
          URLEncoder.encode(spaceForNullString(oneColleague.email), "UTF-8"));
      colleagueEle.setAttributeNS(GSA_NAMESPACE, "gsa:url",
          URLEncoder.encode(spaceForNullString(oneColleague.url), "UTF-8"));
      colleagueEle.setAttributeNS(GSA_NAMESPACE, "gsa:title",
          URLEncoder.encode(spaceForNullString(oneColleague.title), "UTF-8"));
      colleagueEle.setAttributeNS(GSA_NAMESPACE, "gsa:group",
          URLEncoder.encode(spaceForNullString(oneColleague.group), "UTF-8"));
      colleagueEle.setAttributeNS(GSA_NAMESPACE, "gsa:isinworkinggroup",
          oneColleague.isInWorkGroup ? "true" : "false");
      return colleagueEle;
    } catch (UnsupportedEncodingException ex) {
      // ignore: cant happen
      throw new AssertionError();
    }
  }

  private String spaceForNullString(String s) {
    if (s == null) {
      return "";
    } else {
      return s;
    }
  }

  private final String CONTACT_ELEMENT = "gsa:contact";
  private final String CONTACTS_ROOT_ELEMENT = "gsa:Contacts";
  private final String GSA_NAMESPACE = "http://www.google.com/schemas/gsa";

  /**
   * Gets colleague list as XML of the form below:
   *
   * <pre>
   * {@code <?xml version="1.0" encoding="UTF-8"?>
   *       <gsa:Contacts xmlns:gsa="http://www.google.com/schemas/gsa"
   *       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   *         <gsa:contact gsa:accountname="accountname" gsa:email="email"
   *         gsa:group="group" gsa:isinworkinggroup="true" gsa:name="name"
   *         gsa:title="mytitle" gsa:url="myurl"/>
   *       </gsa:Contacts> }
   * </pre>
   *
   * XSD for the XML is
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
   *                   <attribute name="accountname" type="string" form="qualified"
   *                     use="required" />
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
   *
   * @return colleague list as XML
   */
  public Value getColleagueXml() {
    return getSingleValue(properties
        .get(SocialUserProfileProperties.PROPNAME_COLLEAGUES));
  }

  /**
   * Creates a docId from a user profile.
   *
   * @return docId for the user profile document
   */
  @SuppressWarnings("deprecation") // SocialCollectionHandler
  private String makeDocId() {
    try {
      return SocialCollectionHandler.getDocIdPrefix(collectionName)
          + URLEncoder.encode(getUserKey().toString(), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      // Can not happen.
      throw new AssertionError();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Property findProperty(String name) {
    if (name.equals(SpiConstants.PROPNAME_DOCID)) {
      return new SimpleProperty(getValueList(makeDocId()));
    }
    List<Value> list = properties.get(name);
    return (list == null) ? null : new SimpleProperty(list);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<String> getPropertyNames() throws RepositoryException {
    return properties.keySet();
  }
}
