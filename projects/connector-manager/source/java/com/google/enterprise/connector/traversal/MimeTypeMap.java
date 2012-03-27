// Copyright 2007-2009 Google Inc.
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

package com.google.enterprise.connector.traversal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides context to the traversal process on what mime types are acceptable
 * to the GSA. We might think about getting this info dynamically from the GSA
 * instead of getting from config (as here).
 */
public class MimeTypeMap {

  private static final Logger LOGGER =
      Logger.getLogger(MimeTypeMap.class.getName());

  private final Map<String, Integer> typeMap;
  private int unknownMimeTypeSupportLevel;

  public MimeTypeMap() {
    // if no setters are called, then all mime types are supported
    typeMap = new HashMap<String, Integer>();
    unknownMimeTypeSupportLevel = 1;
  }

  /**
   * Set the support level for any mime type not explicitly
   * included in the map.  There are hundreds of mime types
   * and more a being added all the time.  Setting this value
   * to 0 means that unknown mime types will not be indexed.
   * Setting it to 1 means that the content will be sent to
   * the GSA, where it may or may not be successfully indexed.
   *
   * @param unknownMimeTypeSupportLevel an int greater than or equal to 0.
   */
  public void setUnknownMimeTypeSupportLevel(int unknownMimeTypeSupportLevel) {
    LOGGER.config("Setting unknownMimeTypeSupportLevel to "
                  + unknownMimeTypeSupportLevel);
    this.unknownMimeTypeSupportLevel = unknownMimeTypeSupportLevel;
  }

  /**
   * Sets the preferred mime types to index.  If a repository
   * supplies a choice of document representations, the connector
   * should try to provide one of these preferred types.
   * These mime types require little or no preprocessing
   * or file format conversion to extract text and metadata.
   *
   * @param mimeTypes Set of mime types that are preferred.
   */
  public void setPreferredMimeTypes(Set<String> mimeTypes) {
    LOGGER.config("Setting preferred mime types to " + mimeTypes.toString());
    initMimeTypes(mimeTypes, 8);
  }

  /**
   * Sets the supported mime types to index.
   * These mime types may require some preprocessing or
   * file format conversion to extract text and metadata.
   * Some information may be lost or discarded.
   *
   * @param mimeTypes Set of mime types that are preferred.
   */
  public void setSupportedMimeTypes(Set<String> mimeTypes) {
    LOGGER.config("Setting supported mime types to " + mimeTypes.toString());
    initMimeTypes(mimeTypes, 4);
  }


  /**
   * Set the unsupported mime types whose content cannot be indexed.
   * These mime types provide little or no textual content, or are
   * data formats that are either unknown or do not have a format converter.
   * The connector may still provide meta-data describing the content,
   * but the content itself should not be pushed.
   *
   * @param mimeTypes Set of mime types that are not indexable.
   */
  public void setUnsupportedMimeTypes(Set<String> mimeTypes) {
    LOGGER.config("Setting unsupported mime types to " + mimeTypes.toString());
    initMimeTypes(mimeTypes, -1);
  }

  /**
   * Set the excluded mime types whose content should not be indexed.
   * The connector should not feed these documents at all, supplying
   * neither meta-data nor content.
   *
   * @param mimeTypes Set of mime types that should not be fed.
   */
  public void setExcludedMimeTypes(Set<String> mimeTypes) {
    LOGGER.config("Setting excluded mime types to " + mimeTypes.toString());
    // -5 is for historical reasons, as Excluded was added after Unsupported.
    initMimeTypes(mimeTypes, -5);
  }

  /*
   * Add the set of mimetypes to the typeMap at the desired support level.
   * Mimetypes with "/vnd.*" subtypes are preferred over others, and
   * mimetypes registered with IANA are preferred over those with "/x-*"
   * experimental subtypes.  This ranking is done by adjusting the support
   * level +/- 1, accordingly.  Content types sans subtypes are preferred
   * least of all, so their support level is adjusted by -2.
   */
  private void initMimeTypes(Set<String> mimeTypes, int supportLevel) {
    if (mimeTypes == null || mimeTypes.size() == 0)
      return;

    // Adjust the support level so that "/vdn." and "/x-" subtype
    // sorting does not accidentally cross above or below 0.
    if (supportLevel == 0) {
      supportLevel = -1;
    } else if (supportLevel > 0 && supportLevel < 3) {
      supportLevel = 3;
    }

    Integer level0 = Integer.valueOf(supportLevel - 2);
    Integer level1 = Integer.valueOf(supportLevel - 1);
    Integer level2 = Integer.valueOf(supportLevel);
    Integer level3 = Integer.valueOf(supportLevel + 1);

    // Add the mimetypes to the map.  We adjust the support levels
    // slightly to prefer "vnd." subtypes over others, and prefer
    // any other subtype over "x-" subtypes.  Content types sans
    // subtypes are ranked below all others.
    for (Iterator<String> i = mimeTypes.iterator(); i.hasNext(); ) {
      String mimeType = i.next().trim().toLowerCase();
      if (mimeType.indexOf('/') < 0) {
        typeMap.put(mimeType, level0);
      } else if (mimeType.startsWith("x-") || (mimeType.indexOf("/x-") > 0)) {
        typeMap.put(mimeType, level1);
      } else if (mimeType.indexOf("/vnd.") > 0) {
        typeMap.put(mimeType, level3);
      } else if (mimeType.length() > 0) {
        typeMap.put(mimeType, level2);
      }
    }
  }

  /**
   * Return the support level for a given mime type. No validation is
   * performed.
   *
   * @param mimeType
   * @return zero (or negative) means that this mimetype is not supported, with
   *         negative values indicating the document should be skipped entirely.
   *         Positive integers may be compared to choose which mime types are
   *         preferred.
   */
  public int mimeTypeSupportLevel(String mimeType) {
    Integer result = null;
    if (mimeType != null) {
      result = typeMap.get(mimeType.trim().toLowerCase());
      if (result == null) {
        // If exact match not found, look for a match on just the
        // primary mimetype (sans the subtype).
        int i = mimeType.indexOf('/');
        if (i > 0) {
          result = typeMap.get(mimeType.substring(0, i));
        }
      }
    }
    int sl;
    if (result == null) {
      sl = unknownMimeTypeSupportLevel;
    } else {
      // Map all Unsupported to 0, and all Excluded to -1.
      sl = (result > 0) ? result : ((result < -3) ? -1 : 0);
    }

    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.finest("Mime type support level for " + mimeType + " is " + sl);
    }
    return sl;
  }


  /**
   * Return the most preferred mime type from a Set of candidates.
   * Mime types with higher support levels are preferred over those
   * with lower support levels.  For those with equal support levels,
   * non-'x-*' subtypes are preferred over 'x-*' subtypes, and mimetypes
   * with subtypes are preferred over those without.
   *
   * @param mimeTypes a Set of mime type Strings.
   * @return the most preferred mime type from the Set.
   */
  public String preferredMimeType(Set<String> mimeTypes) {
    if (mimeTypes == null || mimeTypes.size() == 0)
      return null;

    // Look for an exact match on one of the mimeTypes.
    int bestLevel = Integer.MIN_VALUE;
    String bestMimeType = null;
    for (String mimeType : mimeTypes) {
      int thisLevel = mimeTypeSupportLevel(mimeType);
      if (thisLevel > bestLevel) {
        bestLevel = thisLevel;
        bestMimeType = mimeType;
      } else if (thisLevel == bestLevel) {
        if (mimeType.trim().length() < bestMimeType.trim().length()) {
          bestMimeType = mimeType;
        }
      }
    }

    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.finest("Preferred mime type from " + mimeTypes.toString()
                    + " is " + bestMimeType);
    }

    return bestMimeType;
  }
}
