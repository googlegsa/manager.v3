// Copyright (C) 2008 Google Inc.
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

package com.google.enterprise.sessionmanager;

/**
 * Interface to a Authn/Authz session.
 */
public interface SessionInterface {

  /**
   * checks if a key exists associated with a given session
   *
   * @return            returns true if the key exists and false if
   *                    it does not.
   */
  public abstract boolean keyExists(String key);

  /**
   * sets a given key to a given data value, within a particular session store.
   * Any previous value for that key is replaced.  New keys are accepted
   * silently.  Callers need to ensure that key names do not conflict between
   * different sections of the overall system;  prefixing your key name with
   * your package name is advised.
   *
   * @param key         arbitrary string the caller wishes to assign, which
   *                    is used for later retrieval of this data using the
   *                    getValue method.
   * @param newValue    the new value to assign to this key within this session.
   *                    if null, a blank newValue is recorded
   */
  public abstract void setValue(String key, String newValue);

  /**
   * retrieves the value set for a given key in a given session
   *
   * @param key         the key value used in setValue() for the session data
   *                    being retrieved
   * @return            the value set by setValue() for this key in this session
   *                    or null if no such key has been set thus far.  Returns
   *                    null if key is passed null.
   */
  public abstract String getValue(String key);

  /**
   * sets a given key to a given binary value, within a particular session.
   * Any previous value for that key is replaced.  New keys are accepted
   * silently.  Callers need to ensure that key names do not conflict between
   * different sections of the overall system;  prefixing your key name with
   * your package name is advised.
   *
   * @param key         arbitrary string the caller wishes to assign, which
   *                    is used for later retrieval of this data using the
   *                    getValue method.
   * @param newValue    the new value to assign to this key within this session.
   *                    if null, a blank newValue is recorded
   */
  public abstract void setValueBin(String key, byte[] newValue);

  /**
   * retrieves a binary value set for a given key in a given session
   *
   * @param key         the key value used in setValue() for the session data
   *                    being retrieved
   * @return            the value set by setValue() for this key in this session
   *                    or null if no such key has been set thus far.  Returns
   *                    null if key is passed null.
   */
  public abstract byte[] getValueBin(String key);

  /**
   * pass an SPNEGO/Kerberos token to the Session Manager so that it may extract
   * the delegated user identity for use in subsequent Head Requests 
   * @param spnegoBlob  SPNEGO/Kerberos token fetched from the client
   * @return            the Kerberos identity if the operation completed
   *                    successfully, null otherwise
   */
  public abstract String storeKrb5Identity(String spnegoBlob);

  /**
   * request an Kerberos KeyMaterial object based on the currently Kerberos 
   * identity associated with the session
   * @param server      Target server name
   * @return            Base64-encoded value of the SPNEGO/Kerberos token
   */
  public abstract KeyMaterial getKrb5TokenForServer(String server);

  /**
   * Returns the Kerberos identity if it has been initialized.
   * @return            A non-null string with the Kerberos identity if the
   *                    credentials have been properly initialized for
   *                    delegation. Null otherwise.
   */
  public abstract String getKrb5Identity();

  /**
   * Returns the path to the Kerberos Credentials Cache where the user
   * credentials are stored.
   * @return            Credentials Cache filename.
   */
  public abstract String getKrb5CcacheFilename();

}
