// Copyright 2009 Google Inc.
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

package com.google.enterprise.connector.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.enterprise.connector.spi.TraversalContext;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil2;
import eu.medsea.mimeutil.detector.ExtensionMimeDetector;
import eu.medsea.mimeutil.detector.MagicMimeMimeDetector;
import eu.medsea.util.EncodingGuesser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Detector for MIME type based on file name and content.
 *
 * @since 3.0
 */
public class MimeTypeDetector {
  private static final Logger LOGGER =
      Logger.getLogger(MimeTypeDetector.class.getName());

  /**
   * MIME type for documents whose MIME type cannot be determined.
   */
  public static final String UNKNOWN_MIME_TYPE =
      mimeTypeStringValue(MimeUtil2.UNKNOWN_MIME_TYPE);

  private static MimeUtil2 extensionDetector;
  private static MimeUtil2 magicDetector;

  /**
   * The mime-util library leaks memory like a sieve on each new instance,
   * and is not thread-safe. So we want to share instances of MimeUtil2.
   * To avoid problems with mime-util trying to open a file with the given
   * name, we use two separate instances, one using only the extension
   * detector which we give the file name to, and the other using only the
   * magic detector which we give the byte[] to.
   */
  private static synchronized void init() {
    if (magicDetector == null) {
      LOGGER.info("Initializing MimeTypeDetector");
      setSupportedEncodings(
          Sets.newHashSet("UTF-8", "ISO-8859-1", "windows-1252"));

      extensionDetector = new MimeUtil2();
      extensionDetector.registerMimeDetector(
          ExtensionMimeDetector.class.getName());
      // TODO: Should we add the WindowsRegistryMimeDetector?  This might
      // yield different results when run on Windows vs. Unix.

      // TODO: If "/usr/share/mime/mime.cache exists use
      // OpendesktopMimeDetector instead of MagicMimeMimeDetector. It seems
      // more accurate but was logging NullPointerExceptions so I temporarily
      // removed it pending further testing/fixing.
      magicDetector = new MimeUtil2();
      magicDetector.registerMimeDetector(MagicMimeMimeDetector.class.getName());
    }
  }

  /** TraversalContext used to rank differented mime types. */
  private static TraversalContext traversalContext;

  /** TraversalContext injected by Spring from the manager configuration. */
  public static void setTraversalContext(TraversalContext traversalContext) {
    Preconditions.checkNotNull(traversalContext,
                               "traversalContext must not be null.");
    MimeTypeDetector.traversalContext = traversalContext;
  }

  public MimeTypeDetector() {
    init();
  }

  /**
   * Sets the supported
   * <a href="http://docs.oracle.com/javase/6/docs/technotes/guides/intl/encoding.doc.html">
   * character encodings</a> for the {@code MimeTypeDetector}. When determining
   * Mime type based upon content, MimeTypeDetector will interpret the content
   * using the various encodings until it has found a match.  For performance
   * reasons, the Set of expected encodings should remain as small as possible.
   * The JVM default encoding is automatically supported.
   * <p>
   * The default set of supported encodings is "UTF-8", "ISO-8859-1",
   * "windows-1252", and the current JVM default encoding.
   * <p>
   *
   * @param encodings a Set of canonical encoding names.
   * @see <a href="http://docs.oracle.com/javase/6/docs/technotes/guides/intl/encoding.doc.html">Java Supported Encodings</a>
   */
  public static synchronized void setSupportedEncodings(Set<String> encodings) {
    Set<String> enc = Sets.newHashSet(encodings);
    enc.add(EncodingGuesser.getDefaultEncoding());
    EncodingGuesser.setSupportedEncodings(enc);
  }

  /**
   * Returns the MIME type for the document with the provided filename and/or
   * content.
   * <p>
   * If {@code filename} is provided, the file will not be accessed; however,
   * the filename extension will be used for MIME type determination.  For
   * this reason, filenames that are extracted from ECMs, remote filesytems,
   * even URLs (using
   * <a href="http://docs.oracle.com/javase/6/docs/api/java/net/URL.html#getPath()">
   * URL.getPath()</a>) should work.  If {@code filename} is {@code null},
   * only the supplied {@code content} will be used to determine the MIME type.
   * <p>
   * If {@code content} is provided, {@link MimeTypeDetector} will examine
   * the first few thousand bytes of the content, looking for a match against
   * a set of known character sequences found in common file formats.  The
   * caller need not supply the entire document content - only the
   * beginning of the content is examined to determine MIME type, so the
   * first 4 kilobytes of content is sufficient at this time.
   * If {@code content} is {@code null}, only the filename extension will be
   * used to determine the MIME type.
   *
   * @param filename used for filename extension MIME type detection
   *        (may be {@code null})
   * @param content a byte array of document content used for MIME type
   *        detection (may be {@code null})
   * @return the most preferred MIME type for the document
   * @throws IllegalArgumentException if both {@code filename} and
   *        {@code content} are {@code null}.
   */
  public String getMimeType(String filename, byte[] content) {
    Preconditions.checkArgument((filename != null || content != null),
                                "filename and content may not both be null");
    // We munge the file name we pass to getMimeTypes so that it will
    // not find the file exists, open it and perform content based
    // detection here.
    String bestMimeType =
        pickBestMimeType(getMimeTypes(filename), getMimeTypes(content));
    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.finest("MimeType " + bestMimeType + " determined for "
                    + ((filename == null) ? "content." : filename));
    }
    return bestMimeType;
  }

  /**
   * Returns the MIME type for the document with the provided filename or
   * content read from an {@code InputStream}.
   * <p>
   * If {@code filename} is provided, the file will not be accessed; however,
   * the filename extension will be used for MIME type determination.  For
   * this reason, filenames that are extracted from ECMs, remote filesytems,
   * even URLs (using
   * <a href="http://docs.oracle.com/javase/6/docs/api/java/net/URL.html#getPath()">
   * URL.getPath()</a>) should work.  If the MIME type can be determined
   * solely by the filename extension, it will be returned.
   * <p>
   * If the MIME type cannot be determined solely from the filename extension
   * and {@code inputStreamFactory} is provided, {@link MimeTypeDetector} will
   * get an {@code InputStream} from the factory and read the
   * first few thousand bytes of the content, looking for a match against
   * a set of known character sequences found in common file formats.
   * If {@code inputStreamFactory} is {@code null}, only the filename extension
   * will be used to determine the MIME type.
   *
   * @param filename used for filename extension MIME type detection
   *        (may be {@code null})
   * @param inputStreamFactory an {@link InputStreamFactory} used to fetch
   *        and {@code InputStream} from which the document content may be read
   *        (may be {@code null})
   * @return the most preferred MIME type for the document
   * @throws IllegalArgumentException if both {@code filename} and
   *        {@code InputStreamFactory} are {@code null}
   * @throws IOException if there is an error reading from the InputStream
   */
  public String getMimeType(String filename,
        InputStreamFactory inputStreamFactory) throws IOException {
    Preconditions.checkArgument((filename != null || inputStreamFactory != null),
        "filename and inputStreamFactory may not both be null");
    Collection<MimeType> mimeTypes = getMimeTypes(filename);
    String bestMimeType = pickBestMimeType(mimeTypes);
    if (UNKNOWN_MIME_TYPE.equals(bestMimeType) && inputStreamFactory != null) {
      InputStream is = inputStreamFactory.getInputStream();
      try {
        byte[] bytes = getBytes(is);
        mimeTypes = getMimeTypes(bytes);
      } finally {
        is.close();
      }
      bestMimeType = pickBestMimeType(mimeTypes);
    }
    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.finest("MimeType " + bestMimeType + " determined for "
                    + ((filename == null) ? "content." : filename));
    }
    return bestMimeType;
  }

  @SuppressWarnings("unchecked")
  private Collection<MimeType> getMimeTypes(String filename) {
    if (filename == null) {
      return null;
    }
    synchronized (extensionDetector) {
      return extensionDetector.getMimeTypes(filename);
    }
  }

  @SuppressWarnings("unchecked")
  private Collection<MimeType> getMimeTypes(byte[] content) {
    if (content == null) {
      return null;
    }
    synchronized (magicDetector) {
      return magicDetector.getMimeTypes(content);
    }
  }

  /**
   * This method returns the most suitable MIME type of the document
   * from the MIME types collected by the filename extension MIME type
   * detector and/or the document content MIME type detector.
   *
   * @param extensionMimeTypes a Collection of MimeTypes as determined by
   *        the filename extension (may be {@code null})
   * @param contentMimeTypes a Collection of MimeTypes as determined by
   *        the document content (may be {@code null})
   * @return most suitable MIME type for the document
   */
  private String pickBestMimeType(Collection<MimeType> extensionMimeTypes,
                                  Collection<MimeType> contentMimeTypes) {
    // Use a LinkedHashSet so we preserve the order of the mimetypes
    // as they are returned by MimeUtil.
    Set<String> mimeTypeNames = new LinkedHashSet<String>();
    if (extensionMimeTypes != null) {
      for (MimeType mimeType : extensionMimeTypes) {
        if (!MimeUtil2.UNKNOWN_MIME_TYPE.equals(mimeType)) {
          mimeTypeNames.add(mimeTypeStringValue(mimeType));
        }
      }
    }
    if (contentMimeTypes != null) {
      for (MimeType mimeType : contentMimeTypes) {
        if (!MimeUtil2.UNKNOWN_MIME_TYPE.equals(mimeType)) {
          mimeTypeNames.add(mimeTypeStringValue(mimeType));
        }
      } 
    }
    if (mimeTypeNames.isEmpty()) {
      return UNKNOWN_MIME_TYPE;
    }
    // get the most suitable MIME type for this document
    Preconditions.checkState(traversalContext != null,
                             "traversalContext must be set.");
    return traversalContext.preferredMimeType(mimeTypeNames);
  }

  private String pickBestMimeType(Collection<MimeType> mimeTypes) {
    return pickBestMimeType(mimeTypes, null);
  }

  private static String mimeTypeStringValue(MimeType mimeType) {
    return mimeType.getMediaType() + "/" + mimeType.getSubType();
  }

  /** Read up to 4KB of content from the InputStream. */
  private static byte[] getBytes(InputStream is) throws IOException {
    // As of mime-utils v2.1.3, buffer needs to be at least 2120 bytes.
    byte[] result = new byte[4096];
    int bytesRead = 0;
    while (bytesRead < result.length) {
      int bytesThisTime = is.read(result, bytesRead, result.length - bytesRead);
      if (bytesThisTime == -1) {
        break;
      }
      bytesRead += bytesThisTime;
    }
    return trim(result, bytesRead);
  }

  /**
   * Trims the passed in array to the desired length and
   * returns the result. If the passed in array is already
   * the desired length this simply returns the passed in
   * array.
   */
  /* TODO: When we move to Java 6, replace this with Arrays.copyOf() */
  private static byte[] trim(byte[] input, int desiredLength) {
    if (input.length == desiredLength) {
      return input;
    } else {
      byte[] result = new byte[desiredLength];
      System.arraycopy(input, 0, result, 0, desiredLength);
      return result;
    }
  }
}
