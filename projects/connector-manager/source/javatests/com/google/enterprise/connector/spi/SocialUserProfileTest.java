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

import com.google.enterprise.connector.spi.SocialUserProfileDocument.ColleagueData;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class SocialUserProfileTest extends TestCase {
  private SocialUserProfileDocument userProfile;
  private ArrayList<String> data;

  @Override
  protected void setUp() throws Exception {
    userProfile = new SocialUserProfileDocument(
        SpiConstants.DEFAULT_USERPROFILE_COLLECTION);
    data = new ArrayList<String>();
    data.add("abc");
    data.add("def");
    userProfile.setAskmeAbout(data);
    userProfile.setOrgUrl("http://foo/bar");
    userProfile.setPastProjects(data);
    userProfile.setPictureUrl("http://foo/picture");
    userProfile.setProperty("prop1", data);
    userProfile.setProperty("prop2", data);
    ArrayList<ColleagueData> colleagues = new ArrayList<ColleagueData>();
    ColleagueData colleague = new ColleagueData();
    colleague.setIdentity("google\\foo");
    colleague.setEmail("foo@google.com");
    colleague.setGroup("mygroup");
    colleague.setInWorkGroup(true);
    colleague.setTitle("big guy");
    colleague.setUrl("http://foo/itshim");
    colleagues.add(colleague);
    userProfile.setColleagues(colleagues);
    userProfile.setPublic(true);
    userProfile.setSkills(data);
    userProfile.setUserContent("Hey hey");
    userProfile.setUserKey("google\\foo");
  }

  private void validateLists(List<String> expected, List<Value> actual) {
    assertEquals(expected.size(), actual.size());
    for (int v = 0; v < actual.size(); v++) {
      assertEquals(expected.get(v), actual.get(v).toString());
    }
  }

  public void testDocid() throws RepositoryException {
    String docid =
        Value.getSingleValueString(userProfile, SpiConstants.PROPNAME_DOCID);
    assertNotNull(docid);
    assertTrue(docid.startsWith("social:"));
  }

  public void testGetUserContent() {
    assertEquals("Hey hey", userProfile.getUserContent().toString());
  }

  public void testGetSkills() {
    validateLists(data, userProfile.getSkills());
  }

  public void testGetAskmeAbout() {
    validateLists(data, userProfile.getAskmeAbout());
  }

  public void testGetPastProjects() {
    validateLists(data, userProfile.getPastProjects());
  }

  public void testGetPublic() {
    assertTrue(userProfile.getPublic());
  }

  public void testGetUserKey() {
    assertEquals("google\\foo", userProfile.getUserKey().toString());
  }

  public void testGetProperty() {
    validateLists(data, userProfile.getProperty("prop1"));
  }

  public void testGetPictureUrl() {
    assertEquals("http://foo/picture", userProfile.getPictureUrl().toString());
  }

  public void testGetOrgUrl() {
    assertEquals("http://foo/bar", userProfile.getOrgUrl().toString());
  }

  public void testGetColleagues() {
    assertEquals("big guy", userProfile.getColleagues().get(0).getTitle());
  }

  public void testGetColleagueXml() {
    String xml = userProfile.getColleagueXml().toString();
    assertTrue(xml.indexOf("gsa:accountname=\"google%5Cfoo\"") != -1);
    assertTrue(xml.indexOf("gsa:email=\"foo%40google.com\"") != -1);
  }

}
