// Copyright 2008 Google Inc.  All Rights Reserved.
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

package com.google.enterprise.saml.common;

import org.opensaml.saml2.metadata.EntityDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Metadata {

  private static final Map<String, Metadata> savedMetadata = new HashMap<String, Metadata>();
  public static final String MOCK_SP_KEY = "MockServiceProvider";
  public static final String SM_KEY = "SecurityManager";

  private final EntityDescriptor localEntity;
  private final List<EntityDescriptor> peerEntities;

  public Metadata(EntityDescriptor localEntity, String key) {
    this.localEntity = localEntity;
    peerEntities = new ArrayList<EntityDescriptor>();
    savedMetadata.put(key, this);
  }

  public static Metadata getMetadata(String key) {
    return savedMetadata.get(key);
  }

  public EntityDescriptor getLocalEntity() {
    return localEntity;
  }

  public void addPeerEntity(EntityDescriptor entity) {
    peerEntities.add(entity);
  }

  public EntityDescriptor getPeerEntity(String issuer) {
    for (EntityDescriptor entity: peerEntities) {
      if (entity.getEntityID().equals(issuer)) {
        return entity;
      }
    }
    throw new IllegalArgumentException("Unknown issuer: " + issuer);
  }

  public EntityDescriptor getPeerEntity() {
    return peerEntities.get(0);
  }
}