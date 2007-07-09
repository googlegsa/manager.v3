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

package com.google.enterprise.connector.spi;

/**
 * Interface that represents a list of documents to be traversed. This type is
 * the return type of <code>{@link TraversalManager}.startTraversal()</code>
 * and <code>resumeTraversal()</code>.
 * <p>
 * This single interface can be thought of as representing a list of documents,
 * each of which is a list of properties, each of which is a list of values.
 * Iteration is provided through each object via <code>nextXXX()</code>,
 * similar to the "cursor" concept of <code>{@link java.sql.ResultSet}</code>.
 * By analogy, we can say that an object implementing this interface contains
 * three "cursors": a "document cursor", a "property cursor" and a "value
 * cursor". Thus, a consumer might traverse as follows:
 * 
 * <pre>
 * DocumentList docList = ...
 * while (docList.nextDocument()) {
 *   while (docList.nextProperty()) {
 *     while (docList.nextValue()) {
 *       Value v = docList.getValue();
 *     }
 *   }
 * }
 * </pre>
 * 
 * There are two additions to this pattern:
 * <ol>
 * <li> A property has a name, accessed by <code>getPropertyName()</code>
 * <li> Properties can be accessed by name, using
 * <code>findProperty(String name)</code>
 * </ol>
 * Thus, to look at a particular named property specially, the consumer can call
 * <code>findProperty(String name)</code> for that name. Subsequent calls to
 * <code>nextValue()</code> and <code>getValue</code> will then apply to the
 * Values of that property. However, a call to
 * <code>findProperty(String name)</code> does not advance the property
 * cursor. Subsequent calls to <code>nextProperty()</code> advance the
 * property cursor exactly as though <code>findProperty(String name)</code>
 * had not been called.
 * <p>
 * In practice the consumer is expected to call
 * <code>findProperty(String name)</code> for certain named properties of
 * interest and then to iterate through all the properties by calls to
 * <code>nextProperty()</code>, as follows:
 * 
 * <pre>
 * DocumentList docList = ...
 * while (docList.nextDocument()) {
 *   if (findProperty(special1)) {
 *      // do something special with this special property's values
 *   }
 *   if (findProperty(special2)) {
 *      // similar...
 *   }
 *   while (docList.nextProperty()) {
 *     String propertyName = docList.getPropertyName();
 *     // in this loop it is expected to re-encounter  
 *     // the special properties dealt with earlier
 *     while (docList.nextValue()) {
 *       Value v = docList.getValue();
 *       // do something...
 *     }
 *   }
 * }
 * </pre>
 * 
 * The data of a property is carried by the <code>{@link Value}</code> type.
 * <p>
 * All of the methods of this interface may throw a
 * <code>{@link RepositoryException}</code>, since any of them may be
 * implemented by a call to a foreign repository that may itself throw an
 * exception.
 */
public interface DocumentList extends Document, Property {

  /**
   * Moves the document cursor down one row from its current position. The
   * document cursor is initially positioned before the first document; the
   * first call to the method <code>nextDocument</code> makes the first
   * document the current document; the second call makes the second document
   * the current document, and so on. In addition, after each call to
   * <code>nextDocument</code>, the property cursor is positioned before the
   * first property for the current document.
   * 
   * @return <code>true</code> if the new current document is valid;
   *         <code>false</code> if there are no more documents
   */
  public boolean nextDocument();

  /**
   * Moves the property cursor down one row from its current position. The
   * property cursor is initially positioned before the first property; the
   * first call to the method <code>nextProperty</code> makes the first
   * property the current property; the second call makes the second property
   * the current property, and so on. In addition, after each call to
   * <code>nextProperty</code>, the value cursor is positioned before the
   * first value for the current property.
   * 
   * @return <code>true</code> if the new current property is valid;
   *         <code>false</code> if there are no more properties
   */
  public boolean nextProperty();

  /**
   * Finds a property by name. If the current document has a property with that
   * name, then it becomes the current property: that is, subsequent calls to
   * <code>nextValue</code> and <code>getValue</code> will be with respect
   * to the named property. However, the property cursor is not advanced. A
   * subsequent call to
   * <code>nextProperty<code> will advance the property cursor in exactly the 
   * same way as if <code>findProperty</code> had not been called.
   * 
   * @param name The name of the property to find
   * @return <code>true</code> if the new current document has a valid property 
   *         with the requested name; <code>false</code> if not
   */
  public boolean findProperty(String name);

  /**
   * Get the name of the current property.
   * 
   * @return The property name
   * @throws RepositoryException if a repository access error occurs
   */
  public String getPropertyName() throws RepositoryException;

  /**
   * Moves the value cursor down one row from its current position. The property
   * cursor is initially positioned before the first value; the first call to
   * the method <code>nextValue</code> makes the first value the current
   * value; the second call makes the second value the current value, and so on.
   * 
   * @return <code>true</code> if the new current value is valid;
   *         <code>false</code> if there are no more values
   */
  public boolean nextValue();

  /**
   * Get the current value, as a <code>{@link Value}</code> object
   * 
   * @return the current value
   * @throws RepositoryException if a repository access error occurs
   */
  public Value getValue() throws RepositoryException;

  /**
   * Checkpoints the traversal process. The caller may call this at any time
   * during consumption of the document list. By calling this method, the caller
   * indicates that it has successfully processed all the documents up to and
   * including the document cursor. The caller need not consume the entire
   * document list - the caller may consume as much or as little as it chooses.
   * If the implementation wants the caller to persist the traversal state, then
   * it should write a string representation of that state and return it. If the
   * implementation prefers to maintain state itself, it should use this call as
   * a signal to commit its state, up to the document before the document at the
   * document cursor.
   * 
   * @return A string that can be used by a subsequent call to the
   *         {@link TraversalManager#resumeTraversal(String)} method.
   * @throws RepositoryException 
   */
  public String checkpoint() throws RepositoryException;

}
