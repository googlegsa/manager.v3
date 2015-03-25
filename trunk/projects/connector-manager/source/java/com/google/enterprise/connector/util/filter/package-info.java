// Copyright 2011 Google Inc.
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

/**
 * {@link com.google.enterprise.connector.spi.Document Document}
 * filters act to transform their source Document's
 * {@link com.google.enterprise.connector.spi.Property Properties}.
 * Document filters can add,
 * remove, or modify a document's properties, including the document
 * content.  Properties in which the filter has no interest are
 * passed through unmodified. A document filter might even throw a
 * {@link com.google.enterprise.connector.spi.SkippedDocumentException
 * SkippedDocumentException}
 * to prevent a document from being fed to the Google Search Appliance.
 * <p>
 * Multiple document filters may be chained together, forming
 * a transformational document processing pipeline.  Similar to a
 * Unix command pipeline, the filters are linked together, each using
 * the previous one as its source Document.
 * <p>
 * <img src="doc-files/pipeline.png" alt="Pipeline diagram">
 * <p>
 * Documents are extracted from the Repository,
 * and then supplied to the filter chain. {@code FilterA} gets first
 * crack at the document, then {@code FilterB}, then finally {@code FilterC},
 * before the Document is added to the Feed and sent to the GSA.
 * <p>
 * Filter instances are manufactured anew for each Document by a
 * {@link com.google.enterprise.connector.util.filter.DocumentFilterFactory
 * DocumentFilterFactory}, which wraps a new Filter around the supplied
 * source Document.
 * <p>
 * Several filters are included in this package, providing the ability to
 * modify property values, add, copy, rename, or remove properties.
 * <p>
 * You can also implement custom document filters. By extending
 * {@link com.google.enterprise.connector.util.filter.AbstractDocumentFilter
 * AbstractDocumentFilter}, you need only override one or
 * two methods to implement a new filter.
 * <p>
 * Document filters are configured in the Connector Manager's
 * {@code documentFilters.xml} file, located in the web application's
 * {@code WEB-INF} directory.  Document filters defined here will be applied
 * to all documents across all connector instances hosted by the Connector
 * Manager.
 * <p>
 * Document filters may also be configured for individual connector instances
 * in a connector's {@code connectorInstance.xml} (Advanced Configuration)
 * or {@code connectorDefaults.xml} file.  Connector-specific document filters
 * will be applied before the Connector Manager's global document filters.
 * <p>
 * For example, a filter chain might be configured as follows: <pre><code>
   &lt;bean id="DocumentFilters"
         class="com.google.enterprise.connector.util.filter.DocumentFilterChain"&gt;
     &lt;constructor-arg&gt;
       &lt;list&gt;
         &lt;!-- Don't reveal the secret recipe! --&gt;
         &lt;bean id="FilterA"
               class="com.google.enterprise.connector.util.filter.DeletePropertyFilter"&gt;
           &lt;property name="propertyName" value="SecretRecipe"/&gt;
         &lt;/bean&gt;
         &lt;!-- Make news articles appear in title and author searches. --&gt;
         &lt;bean id="FilterB"
               class="com.google.enterprise.connector.util.filter.CopyPropertyFilter"&gt;
           &lt;property name="propertyNameMap"&gt;
             &lt;map&gt;
               &lt;entry key="HeadLine" value="Title"/&gt;
               &lt;entry key="ByLine" value="Author"/&gt;
             &lt;/map&gt;
           &lt;/property&gt;
         &lt;/bean&gt;
         &lt;!-- Reveal authors behind noms de plume. --&gt;
         &lt;bean id="FilterC"
               class="com.google.enterprise.connector.util.filter.ModifyPropertyFilter"&gt;
           &lt;property name="propertyName" value="Author"/&gt;
           &lt;property name="pattern" value="Mark Twain"/&gt;
           &lt;property name="replacement" value="Samuel Clemens"/&gt;
           &lt;property name="overwrite" value="false"/&gt;
         &lt;/bean&gt;
       &lt;/list&gt;
     &lt;/constructor-arg&gt;
   &lt;/bean&gt;
   </code></pre>
 *
 * @since 2.8
 */
package com.google.enterprise.connector.util.filter;
