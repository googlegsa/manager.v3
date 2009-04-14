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

import com.google.enterprise.common.FileUtil;

import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;

import javax.servlet.ServletException;

public class Metadata {

  private final String filename;
  private final String smEntityId;
  private final String spEntityId;
  private MetadataProvider provider;

  public Metadata(String filename, String smEntityId, String spEntityId) {
    this.filename = filename;
    this.smEntityId = smEntityId;
    this.spEntityId = spEntityId;
    this.provider = null;
  }

  public EntityDescriptor getEntity(String id) throws ServletException {
    EntityDescriptor entity;
    try {
      if (provider == null) {
        provider = OpenSamlUtil.getMetadataFromFile(FileUtil.getContextFile(filename));
      }
      entity = provider.getEntityDescriptor(id);
    } catch (MetadataProviderException e) {
      throw new ServletException(e);
    }
    if (entity == null) {
      throw new IllegalArgumentException("Unknown issuer: " + id);
    }
    return entity;
  }

  public EntityDescriptor getSmEntity() throws ServletException {
    return getEntity(smEntityId);
  }

  public EntityDescriptor getSpEntity() throws ServletException {
    return getEntity(spEntityId);
  }
}
