// Copyright 2007 Google Inc.
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import org.springframework.core.io.Resource;

/**
 * Provides context to the traversal process on what mime types are acceptable
 * to the GSA. We might think about getting this info dynamically from the GSA
 * instead of getting from config (as here).
 */
public class MimeTypeMap {

  private static final Logger LOGGER =
      Logger.getLogger(MimeTypeMap.class.getName());

  private Map typeMap;
  private int unknownMimeTypeSupportLevel;
  private static HashMap extToMimeTypesMap;

  public MimeTypeMap() {
    // if no setters are called, then all mime types are supported
    typeMap = new HashMap();
    extToMimeTypesMap = new HashMap();
    unknownMimeTypeSupportLevel = 2;
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
  public void setPreferredMimeTypes(Set mimeTypes) {
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
  public void setSupportedMimeTypes(Set mimeTypes) {
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
  public void setUnsupportedMimeTypes(Set mimeTypes) {
    LOGGER.config("Setting unsupported mime types to " + mimeTypes.toString());
    initMimeTypes(mimeTypes, -1);
  }


  /*
   * Add the set of mimetypes to the typeMap at the desired support level.
   * Mimetypes with "/vnd.*" subtypes are preferred over others, and
   * mimetypes registered with IANA are preferred over those with "/x-*" 
   * experimental subtypes.  This ranking is done by adjusting the support
   * level +/- 1, accordingly.
   */
  private void initMimeTypes(Set mimeTypes, int supportLevel) {
    if (mimeTypes == null || mimeTypes.size() == 0)
      return;
    
    // Adjust the support level so that "/vdn." and "/x-" subtype
    // sorting does not accidentally cross above or below 0.
    if (supportLevel == 0) {
      supportLevel = -1;
    } else if (supportLevel == 1) {
      supportLevel = 2;
    }

    Integer level1 = new Integer(supportLevel - 1);
    Integer level2 = new Integer(supportLevel);
    Integer level3 = new Integer(supportLevel + 1);

    // Add the mimetypes to the map.  We adjust the support levels
    // slightly to prefer "vnd." subtypes over others, and prefer
    // any other subtype over "x-" subtypes.
    for (Iterator i = mimeTypes.iterator(); i.hasNext(); ) {
      String mimeType = ((String) i.next()).trim().toLowerCase();
      if (mimeType.startsWith("x-") || (mimeType.indexOf("/x-") > 0)) {
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
   * @return zero (or negative) means that this mimetype is not supported.
   *         Positive integers may be compared to choose which mime types are
   *         preferred.
   */
  public int mimeTypeSupportLevel(String mimeType) {
    Integer result = null;
    if (mimeType != null) {
      result = (Integer) typeMap.get(mimeType.trim().toLowerCase());
      if (result == null) {
        // If exact match not found, look for a match on just the
        // primary mimetype (sans the subtype).
        int i = mimeType.indexOf('/');
        if (i > 0) {
          result = (Integer) typeMap.get(mimeType.substring(0, i));
        }
      } 
    }
    int sl = (result == null) ? unknownMimeTypeSupportLevel : result.intValue();
    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.finest("Mime type support level for " + mimeType + " is " + sl);
    }
    return sl;
  }


  /**
   * Return the most preferred mime type from a Set of candidates.
   * Mime types with higher support levels are preferred over those
   * with lower support levels.  For those with equal support levels,
   * non-'x-*' subtypes are preferred over 'x-*' subtypes, and mimetyps
   * with subtypes are preferred over those without.  If all the mime
   * types are unsupported, null is returned.
   *
   * @param mimeTypes a Set of mime type Strings.
   * @return the most preferred mime type from the set, or null if
   * none are supported.
   */
  public String preferredMimeType(Set mimeTypes) {
    if (mimeTypes == null || mimeTypes.size() == 0)
      return null;

    // Look for an exact match on one of the mimeTypes.
    int bestLevel = Integer.MIN_VALUE;
    String bestMimeType = null;
    for (Iterator iter = mimeTypes.iterator(); iter.hasNext(); ) {
      String mimeType = (String) iter.next();
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

    return (bestLevel > 0) ? bestMimeType : null;
  }


  /**
   * Return the preferred mime type for a file, given its filename extension.
   * Mime types with higher support levels are preferred over those
   * with lower support levels.  For those with equal support levels,
   * non-'x-*' subtypes are preferred over 'x-*' subtypes, and mimetyps
   * with subtypes are preferred over those without.
   *
   * @param extension a filename extension including the leading '.'
   * for instance ".doc" or ".tar.gz".
   * @returns the preferred mimetype for this filename extension; or null
   * if no mimetype is known for this extension or if none of the appropriate
   * mimetypes are supported.
   */
  public String preferredMimeTypeForExtension(String extension) {
    if (extension != null && (extension = extension.trim()).length() > 0) {
      // Normalize the file extension (lowercase with leading '.')
      String ext;
      if (extension.startsWith(".")) {
        ext = extension.toLowerCase();
      } else {
        ext = '.' + extension.toLowerCase();
      }
      
      // If we have an exact match for this extension, return it.
      Set mimetypes = (Set) extToMimeTypesMap.get(ext);
      if (mimetypes != null) {
        String mimetype = preferredMimeType(mimetypes);
        if (LOGGER.isLoggable(Level.FINEST)) {
          LOGGER.finest("Preferred mime type for " + ext + " is " + mimetype);
        }
        return mimetype;
      }
      
      // If we don't have an exact match, but do have a compound extension
      // (like ".tar.gz"), then look for a match on the less complex suffixes.
      int i = ext.indexOf('.', 1);
      if (i != -1) {
        return (preferredMimeTypeForExtension(ext.substring(i)));
      }
      LOGGER.finest("Preferred mime type for " + ext + " is unknown.");
    }
    return null;
  }


  /**
   * Set the Resource location for the extensionToMimetype table.
   *
   * @param resource Resource location of the ext2mimetype file.
   */
  public void setExtensionToMimeType(Resource resource) {
    BufferedReader reader = null;
    LOGGER.config("Loading filename extension to mime type map from " +
                  resource.toString());
    try {
      reader =
          new BufferedReader(new InputStreamReader(resource.getInputStream()));
      String line;
      while ((line = reader.readLine()) != null) {
        // Ignore comments.
        if (line.startsWith("#")) {
          continue;
        }
        // Parse file extension, followed by one or more mimeTypes.
        String[] tokens = line.split("[ \t,]+");
        if (tokens.length > 0) {
          String ext;
          if (tokens[0].startsWith(".")) {
            ext = tokens[0].toLowerCase();
          } else {
            ext = '.' + tokens[0].toLowerCase();
          }
          if (tokens.length > 1) {
            HashSet mimeTypes = (HashSet) extToMimeTypesMap.get(ext);
            if (mimeTypes == null) {
              mimeTypes = new HashSet();
            }
            for (int i = 1; i < tokens.length; i++) {
              mimeTypes.add(tokens[i].toLowerCase());
            }
            extToMimeTypesMap.put(ext, mimeTypes);
          }
        }
      }
    } catch (IOException e) {
      LOGGER.log(Level.WARNING,
          "Error initializing extToMimeTypes Map from Resource "
          + resource.getDescription() , e);
    } finally {
      if (reader != null) {
        try { reader.close(); } catch (IOException e) {}
      }
    }
  }
}
