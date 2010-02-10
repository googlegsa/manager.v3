package com.google.enterprise.connector.sp2c_migration;

import java.io.InputStream;
import java.util.List;

/**
 * Represents a SharePoint site and can be used for traversing the same
 *
 * @author strellis
 * @author nitendra_thakur
 */
public interface SharepointSite {
	/**
	 * Returns a primary key to identify the SharePoint site. It will be same as
	 * the site's Url.
	 *
	 * @return
	 */
    String getId();

    /**
     * Returns the site URL
     *
     * @return
     */
    String getUrl();

	/**
	 * Returns all the top level {@link Folder} objects from this SharePoint
	 * site. For example, every document library will map to a top level folder.
	 * There can be multiple document libraries in SharePoint site
	 *
	 * @throws Exception
	 */
    List<Folder> getRootFolders() throws Exception;

	/**
	 * Does a recursive scan of the the folders down the hierarchy under the
	 * given root folder {@link Folder}and returns all the folders as
	 * {@link Folder} objects. Typically, this should be called once for every
	 * {@link Folder} object returned as per the call to
	 * {@link SharepointSite#getRootFolders()}
	 *
	 * @throws Exception if the passed in folder is not a root folder or the
	 *             folder can not be traversed for any other reason
	 */
    List<Folder> getFolders(Folder rootfolder) throws Exception;

	/**
	 * Does a recursive scan of the the documents down the hierarchy under the
	 * given root folder {@link Folder}and returns all the documents as
	 * {@link Document} objects. Typically, this should be called once for every
	 * root {@link Folder} object returned as per the call to
	 * {@link SharepointSite#getRootFolders()} Documents will have a reference
	 * to their parent folder.
	 *
	 * @throws Exception if the passed in folder is not a root folder or the
	 *             folder can not be traversed for any other reason
	 */
    List<Document> getDocuments(Folder rootfolder) throws Exception;

    /**
     * Returns an {@link InputStream} for reading the file's content.
     * <p>
     * Please let us know if returning a stream is a problem. For the POC
     * we need not handle large files so a ByteArrayInputStream would
     * suffice.
     * @throws Exception
     */
    InputStream getDocumentContent(Document document) throws Exception;
}
