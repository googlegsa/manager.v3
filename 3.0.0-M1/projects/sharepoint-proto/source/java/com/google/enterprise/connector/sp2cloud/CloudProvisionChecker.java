// Copyright 2010 Google Inc. All Rights Reserved.
package com.google.enterprise.connector.sp2cloud;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.appsforyourdomain.AppsGroupsService;
import com.google.gdata.client.appsforyourdomain.UserService;
import com.google.gdata.data.appsforyourdomain.AppsForYourDomainErrorCode;
import com.google.gdata.data.appsforyourdomain.AppsForYourDomainException;
import com.google.gdata.data.appsforyourdomain.provisioning.UserEntry;
import com.google.gdata.util.AuthenticationException;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Simple {@link CloudProvisionChecker}.
 * TODO: cache provisioned and not provisioned users and groups.
 */
public class CloudProvisionChecker implements ProvisionChecker {
  private final UserService userService;
  private final AppsGroupsService groupsService;
  private final ProvisionedStateLRUCache userStates;
  private final ProvisionedStateLRUCache groupStates;
  private final String domainName;

  private static final String APPS_FEEDS_URL_BASE =
    "https://apps-apis.google.com/a/feeds/";

  private static final String SERVICE_VERSION = "2.0";

  static UserService newUserService(String adminEmail, String adminPassword,
      String domainName, String applicationName) throws AuthenticationException,
      ReportCaptchaException {
    try {

      UserService userService = new UserService(applicationName);
      userService.setUserCredentials(adminEmail, adminPassword);
      return userService;
    } catch (GoogleService.CaptchaRequiredException cre) {
      // See //http://code.google.com/googleapps/faq.html#handlingcaptcha
      throw  new ReportCaptchaException(domainName, cre);
    }
  }

  static AppsGroupsService newGroupsService(String adminEmail,
      String adminPassword, String domainName, String applicationName)
      throws AuthenticationException, ReportCaptchaException {
    try {
      return new AppsGroupsService(adminEmail, adminPassword, domainName,
          applicationName);
    } catch (GoogleService.CaptchaRequiredException cre) {
      // See //http://code.google.com/googleapps/faq.html#handlingcaptcha
      throw  new ReportCaptchaException(domainName, cre);
    }
  }

  public CloudProvisionChecker(UserService userService,
      AppsGroupsService groupService, String domainName,
      ProvisionedStateLRUCache userStates,
      ProvisionedStateLRUCache groupStates) {
    this.userService = userService;
    this.groupsService = groupService;
    this.domainName = domainName;
    this.userStates = userStates;
    this.groupStates = groupStates;
  }

  private URL getUserUrl(String userEmail) throws MalformedURLException {
     return new URL(APPS_FEEDS_URL_BASE + domainName + "/user/"
        + SERVICE_VERSION + "/" + userEmail);
  }

  /**
   * Fetches and returns if a user or group has been provisioned in the cloud
   * by querying the cloud.
   */
  private interface StateFetcher {
    public boolean fetch(String id) throws Exception;
  }

  private boolean getCachedValue(String id, StateFetcher fetcher,
      ProvisionedStateLRUCache cache) throws Exception {
    Boolean state = cache.getProvisionedState(id);
    if (state == null) {
     state = fetcher.fetch(id);
     cache.setProvisionedState(id, state);
     return state;
    } else {
      return state;
    }
  }

  @Override
  public boolean isGroupProvisioned(String groupId) throws Exception {
    StateFetcher groupStateFetcher = new StateFetcher() {
      public boolean fetch(final String groupId) throws Exception {
        return fetchGroupState(groupId);
      }
    };
    return getCachedValue(groupId, groupStateFetcher, groupStates);
  }

  private boolean fetchGroupState(String groupId) throws Exception {
    try {
      groupsService.retrieveGroup(groupId);
      return true;
    } catch (AppsForYourDomainException afyde) {
      if (afyde.getErrorCode().equals(
          AppsForYourDomainErrorCode.EntityDoesNotExist)) {
        return false;
      } else {
        throw afyde;
      }
    }
  }

  @Override
  public boolean isUserProvisioned(String userName) throws Exception {
    StateFetcher userStateFetcher = new StateFetcher() {
      public boolean fetch(final String userName) throws Exception {
        return fetchUserState(userName);
      }
    };
    return getCachedValue(userName, userStateFetcher, userStates);
  }

  private boolean fetchUserState(String userName) throws Exception {
    URL userUrl = getUserUrl(userName);
    try {
    UserEntry userEntry = userService.getEntry(userUrl, UserEntry.class);
    return true;
    } catch (AppsForYourDomainException afyde) {
      if (afyde.getErrorCode().equals(
          AppsForYourDomainErrorCode.EntityDoesNotExist)) {
        return false;
      } else {
        throw afyde;
      }
    }
  }

  public static class ReportCaptchaException extends Exception {
    ReportCaptchaException(String domainName,
        GoogleService.CaptchaRequiredException cause){
      super(getMessage(domainName), cause);
    }
    private static String getMessage(String domainName) {
      String url = "https://www.google.com/a/" + domainName + "UnlockCaptcha";
      return "Captcha required. From a browser please access " + url;
    }
  }
}
