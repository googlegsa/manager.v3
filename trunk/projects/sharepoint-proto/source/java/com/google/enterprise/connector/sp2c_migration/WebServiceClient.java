//Copyright 2009 Google Inc.
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.sp2c_migration;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.Collator;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;

import org.apache.axis.AxisFault;
import org.apache.axis.holders.UnsignedIntHolder;
import org.apache.axis.message.MessageElement;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

import com.google.enterprise.connector.sharepoint.generated.gssAcl.ACL;
import com.google.enterprise.connector.sharepoint.generated.gssAcl.GSSAclMonitor;
import com.google.enterprise.connector.sharepoint.generated.gssAcl.GSSAclMonitorLocator;
import com.google.enterprise.connector.sharepoint.generated.gssAcl.GSSAclMonitorSoap_BindingStub;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenContains;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenQuery;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenQueryOptions;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenResponseGetListItemChangesSinceTokenResult;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenViewFields;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsQuery;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsQueryOptions;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsResponseGetListItemsResult;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsViewFields;
import com.google.enterprise.connector.sharepoint.generated.lists.Lists;
import com.google.enterprise.connector.sharepoint.generated.lists.ListsLocator;
import com.google.enterprise.connector.sharepoint.generated.lists.ListsSoap_BindingStub;
import com.google.enterprise.connector.sharepoint.generated.sitedata.SiteData;
import com.google.enterprise.connector.sharepoint.generated.sitedata.SiteDataLocator;
import com.google.enterprise.connector.sharepoint.generated.sitedata.SiteDataSoap_BindingStub;
import com.google.enterprise.connector.sharepoint.generated.sitedata._sList;
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders.ArrayOf_sListHolder;
import com.google.enterprise.connector.sharepoint.generated.webs.GetWebCollectionResponseGetWebCollectionResult;
import com.google.enterprise.connector.sharepoint.generated.webs.Webs;
import com.google.enterprise.connector.sharepoint.generated.webs.WebsLocator;
import com.google.enterprise.connector.sharepoint.generated.webs.WebsSoap_BindingStub;

/**
 * This class is used to talk to SharePoitn web services
 *
 * @author nitendra_thakur
 */
public class WebServiceClient {
    private String webUrl;
    private String username;
	private String password;
    private String domain;

	enum AuthScheme {
		NTLM, BASIC
	}

    AuthScheme auth = AuthScheme.NTLM;

    private ListsSoap_BindingStub listStub;
	private SiteDataSoap_BindingStub siteDataStub;
	private WebsSoap_BindingStub webStub;
	private GSSAclMonitorSoap_BindingStub gssAclStub;

    public final String ROWLIMIT = "500";
    public final String UNAUTHORIZED = "(401)Unauthorized";
    public final String DATA = "data";
    public final String LIST_ITEM_COLLECTION_POSITION_NEXT = "ListItemCollectionPositionNext";
    public final String FILEREF = "ows_FileRef";
    public final String FILDIREREF = "ows_FileDirRef";
    public final String FILELEAFREF = "ows_FileLeafRef";
    public final String ID = "ows_ID";
    public final String EDITOR = "ows_Editor";
    public final String AUTHOR = "ows_Author";
    public final String DOC_LIB = "DocumentLibrary";
	public final String LIST_URL_SUFFIX = "/Forms/AllItems.aspx";

    final Collator collator = Collator.getInstance();

    public WebServiceClient(String siteUrl, String username, String password,
            String domain) throws Exception {
		collator.setStrength(Collator.PRIMARY);
        this.username = username;
        this.domain = domain;
		this.password = password;

        URI uri = null;
        URL url = new URL(siteUrl);
        final int port = url.getPort() == -1 ? url.getDefaultPort()
                : url.getPort();
        String endpoint = url.getProtocol() + "://" + url.getHost() + ":"
                + port;

        // Initializing Webs stub
        WebsLocator websLocator = new WebsLocator();
        websLocator.setWebsSoapEndpointAddress(endpoint + "/_vti_bin/Webs.asmx");
        Webs service = websLocator;
        webStub = (WebsSoap_BindingStub) service.getWebsSoap();
		webStub.setUsername(domain + "\\" + username);
        webStub.setPassword(password);

        // Get the Url which can be used for making further web service call
        webUrl = getWebURLFromPageURL(siteUrl);
        url = new URL(webUrl);
        uri = new URI(url.getProtocol(), null, url.getHost(),
                url.getPort(), url.getPath(), url.getQuery(), url.getRef());
        endpoint = uri.toASCIIString();
        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }
        
        // Initializing Webs stub with the new updated endpoint
        websLocator = new WebsLocator();
        websLocator.setWebsSoapEndpointAddress(endpoint + "/_vti_bin/Webs.asmx");
        service = websLocator;
        webStub = (WebsSoap_BindingStub) service.getWebsSoap();
        webStub.setPassword(password);

        // Initializing Lists stub
        final ListsLocator listLocator = new ListsLocator();
        listLocator.setListsSoapEndpointAddress(endpoint
                + "/_vti_bin/Lists.asmx");
        final Lists listsService = listLocator;
        listStub = (ListsSoap_BindingStub) listsService.getListsSoap();
        listStub.setPassword(password);

        // Initializing SiteData stub
        final SiteDataLocator siteDataLocator = new SiteDataLocator();
        siteDataLocator.setSiteDataSoapEndpointAddress(endpoint
                + "/_vti_bin/SiteData.asmx");
        final SiteData servInterface = siteDataLocator;
        siteDataStub = (SiteDataSoap_BindingStub) servInterface.getSiteDataSoap();
        siteDataStub.setPassword(password);

		// Initializing GssAcl stub
		final GSSAclMonitorLocator gssAclLocator = new GSSAclMonitorLocator();
		gssAclLocator.setGSSAclMonitorSoapEndpointAddress(endpoint
				+ "/_vti_bin/GssAcl.asmx");
		GSSAclMonitor gssAclService = gssAclLocator;
		gssAclStub = (GSSAclMonitorSoap_BindingStub) gssAclService.getGSSAclMonitorSoap();
		gssAclStub.setPassword(password);

        setUsername();
	}

    private void changeAuthScheme() {
		if (auth == AuthScheme.NTLM) {
			auth = AuthScheme.BASIC;
		} else if (auth == AuthScheme.BASIC) {
			auth = AuthScheme.NTLM;
		}
	}

    private void setUsername() {
		if (auth == AuthScheme.NTLM) {
			webStub.setUsername(domain + "\\" + username);
			siteDataStub.setUsername(domain + "\\" + username);
			listStub.setUsername(domain + "\\" + username);
			gssAclStub.setUsername(domain + "\\" + username);
		} else if (auth == AuthScheme.BASIC) {
			webStub.setUsername(username + "@" + domain);
			siteDataStub.setUsername(username + "@" + domain);
			listStub.setUsername(username + "@" + domain);
			gssAclStub.setUsername(username + "@" + domain);
		}
    }

	/**
	 * Returns a Url which is safe to use for making with web service calls
	 * 
	 * @return
	 */
    public String getWebUrl() {
        return webUrl;
    }

    /**
     * Creates View Fields required for making web service call
     *
     * @return the view fields being used for WS call
     */
    private MessageElement[] createViewFields() throws SOAPException {
        final MessageElement me = new MessageElement(new QName("ViewFields"));
        me.addChildElement("FieldRef").addAttribute(SOAPFactory.newInstance().createName("Name"), "Author");
        me.addChildElement("FieldRef").addAttribute(SOAPFactory.newInstance().createName("Name"), "Editor");
        me.addChildElement("FieldRef").addAttribute(SOAPFactory.newInstance().createName("Name"), "FileRef");
        me.addChildElement("FieldRef").addAttribute(SOAPFactory.newInstance().createName("Name"), "FileDirRef");
        me.addChildElement("FieldRef").addAttribute(SOAPFactory.newInstance().createName("Name"), "FileLeafRef");
        final MessageElement[] meArray = { me };
        return meArray;
    }

    /**
     * Creates QueryOptions required for making WS calls
     *
     * @param nextPage to handle pagination. The value used here must be the one
     *            returned by the WS in last web service call.
     * @return
     * @throws SOAPException
     */
    private MessageElement[] createQueryOptions(final String nextPage)
            throws SOAPException {
        final MessageElement me = new MessageElement(new QName("QueryOptions"));
        me.addChildElement(new MessageElement(new QName("ViewAttributes"))).addAttribute(SOAPFactory.newInstance().createName("Scope"), "Recursive");
        me.addChildElement(new MessageElement(new QName("OptimizeFor"))).addTextNode("FolderUrls");
        if (nextPage != null) {
            me.addChildElement(new MessageElement(new QName("Paging"))).addAttribute(SOAPFactory.newInstance().createName("ListItemCollectionPositionNext"), nextPage);
        }
        final MessageElement[] meArray = { me };
        return meArray;
    }

	/**
	 * Creates query for making WS call
	 * 
	 * @param folders True, when folders are to be retrieved. False, if only
	 *            documents are to be retrieved.
	 * @return
	 * @throws ParseException
	 * @throws SOAPException
	 */
    private MessageElement[] createQuery(boolean folders)
			throws ParseException, SOAPException {
        MessageElement elem = new MessageElement(new QName("Query"));
        if (folders) {
			SOAPElement elem1 = elem.addChildElement(new MessageElement(
					new QName("Where")));
			SOAPElement elem2 = elem1.addChildElement(new MessageElement(
                    new QName("Eq")));
			elem2.addChildElement(new MessageElement(new QName("FieldRef"))).addAttribute(SOAPFactory.newInstance().createName("Name"), "ContentType");
			elem2.addChildElement(new MessageElement(new QName("Value"))).addAttribute(SOAPFactory.newInstance().createName("Type"), "Text").addTextNode("Folder");
        }
        final MessageElement[] meArray = { elem };
        return meArray;
    }

	/**
	 * Makes WS call to get all the folders from a document library's root
	 * folder
	 * 
	 * @param rootfolder Root Folder of a document library
	 * @param nextPage to handle pagination. if null, first page is retrieved.
	 *            Value of this is determined by the WS itself; the same value
	 *            should be passed in the next subsequent call without any
	 *            manipulation.
	 * @return
	 * @throws Exception
	 */
	public Map<String, Folder> getFolders(Folder rootfolder, String nextPage)
            throws Exception {
		Map<String, Folder> folders = new HashMap<String, Folder>();
        if (!rootfolder.isRootFolder()) {
            throw new Exception("Folder is not a root folder");
        }
        final String viewName = "";
        final GetListItemChangesSinceTokenQuery query = new GetListItemChangesSinceTokenQuery();
        final GetListItemChangesSinceTokenViewFields viewFields = new GetListItemChangesSinceTokenViewFields();
        final GetListItemChangesSinceTokenQueryOptions queryOptions = new GetListItemChangesSinceTokenQueryOptions();
        final String token = null;
        final GetListItemChangesSinceTokenContains contains = null;
        GetListItemChangesSinceTokenResponseGetListItemChangesSinceTokenResult res = null;

		query.set_any(createQuery(true));
		viewFields.set_any(createViewFields());
		queryOptions.set_any(createQueryOptions(nextPage));

        try {
			res = listStub.getListItemChangesSinceToken(rootfolder.getName(), viewName, query, viewFields, ROWLIMIT, queryOptions, token, contains);
		} catch (final AxisFault af) {
			if ((UNAUTHORIZED.indexOf(af.getFaultString()) != -1)
					&& (null != domain)) {
				changeAuthScheme();
				setUsername();
				try {
					res = listStub.getListItemChangesSinceToken(rootfolder.getName(), viewName, query, viewFields, ROWLIMIT, queryOptions, token, contains);
				} catch (final Throwable e) {
					throw new Exception(
							"Problem while getting Folders under [ "
									+ rootfolder.getId() + " ].", e);
				}
			} else {
				throw new Exception("Problem while getting Folders under [ "
						+ rootfolder.getId() + " ].", af);
			}
		} catch (final Exception e) {
			throw new Exception("Problem while getting Folders under [ "
					+ rootfolder.getId() + " ].", e);
		}

        if (res != null) {
            final MessageElement[] me = res.get_any();
            if ((me != null) && (me.length > 0)) {
                for (final Iterator itChilds = me[0].getChildElements(); itChilds.hasNext();) {
                    final MessageElement child = (MessageElement) itChilds.next();
                    if (DATA.equalsIgnoreCase(child.getLocalName())) {
                        final String tmpNextPage = child.getAttribute(LIST_ITEM_COLLECTION_POSITION_NEXT);
                        for (final Iterator itrchild = child.getChildElements(); itrchild.hasNext();) {
                            try {
                                final MessageElement row = (MessageElement) itrchild.next();
                                Folder folder = createFolder(row);
                                if (null != folder) {
									folders.put(folder.getRelativeUrl(), folder);
                                }
                            } catch (Exception e) {
                                // Problem while processing some folders.
                                continue;
                            }
                        }
                        if (tmpNextPage != null) {
							folders.putAll(getFolders(rootfolder, nextPage));
                        }
                    }
                }
            }
        }

        return folders;
    }

	/**
	 * creates a {@link Folder} object from the Web Service response
	 * 
	 * @param row
	 * @return
	 */
    private Folder createFolder(MessageElement row) {
        String name = row.getAttribute(FILELEAFREF);
        name = name.substring(name.indexOf("#") + 1);
        String relativeURL = row.getAttribute(FILEREF);
        relativeURL = relativeURL.substring(relativeURL.indexOf("#") + 1);
        String parentDir = row.getAttribute(FILDIREREF);
        parentDir = parentDir.substring(parentDir.indexOf("#") + 1);
        String author = row.getAttribute(EDITOR);
        if (author == null) {
            author = row.getAttribute(AUTHOR);
        }
        author = author.substring(author.indexOf("#") + 1);

		Folder folder = new Folder(name, relativeURL, relativeURL, parentDir,
				null, author, false);
        return folder;
    }

	/**
	 * Makes WS call to get all the documents from a document library's root
	 * folder
	 * 
	 * @param rootfolder Root folder of a document library
	 * @param nextPage to handle pagination
	 * @return
	 * @throws Exception
	 */
	public Map<String, Document> getDocuments(Folder rootfolder, String nextPage)
            throws Exception {
        Map<String, Document> documents = new HashMap<String, Document>();
        if (!rootfolder.isRootFolder()) {
            throw new Exception("Folder is not a root folder");
        }
        final String viewName = "";
        final GetListItemsQuery query = new GetListItemsQuery();
        final GetListItemsViewFields viewFields = new GetListItemsViewFields();
        final GetListItemsQueryOptions queryOptions = new GetListItemsQueryOptions();
        GetListItemsResponseGetListItemsResult res = null;

        try {
            query.set_any(createQuery(false));
            viewFields.set_any(createViewFields());
            queryOptions.set_any(createQueryOptions(nextPage));
            res = listStub.getListItems(rootfolder.getName(), viewName, query, viewFields, ROWLIMIT, queryOptions, null);
        } catch (final AxisFault af) {
            if ((UNAUTHORIZED.indexOf(af.getFaultString()) != -1)
                    && (null != domain)) {
                changeAuthScheme();
                setUsername();
                try {
                    res = listStub.getListItems(rootfolder.getName(), viewName, query, viewFields, ROWLIMIT, queryOptions, null);
                } catch (final Throwable e) {
                    throw new Exception(
                            "Problem while getting Documents under [ "
                                    + rootfolder.getId() + " ].", e);
                }
            } else {
                throw new Exception("Problem while getting Documents under [ "
                        + rootfolder.getId() + " ].", af);
            }
        } catch (final Exception e) {
            throw new Exception("Problem while getting Documents under [ "
                    + rootfolder.getId() + " ].", e);
        }

        if (res != null) {
            final MessageElement[] me = res.get_any();
            if ((me != null) && (me.length > 0)) {
                for (final Iterator itChilds = me[0].getChildElements(); itChilds.hasNext();) {
                    final MessageElement child = (MessageElement) itChilds.next();
                    if (DATA.equalsIgnoreCase(child.getLocalName())) {
                        final String tmpNextPage = child.getAttribute(LIST_ITEM_COLLECTION_POSITION_NEXT);
                        for (final Iterator itrchild = child.getChildElements(); itrchild.hasNext();) {
                            try {
                                final MessageElement row = (MessageElement) itrchild.next();
                                Document document = createDocument(row);
                                if (null != document) {
									documents.put(document.getDocUrl(), document);
                                }
                            } catch (Exception e) {
                                // Problem while processing some documents.
                                continue;
                            }
                        }
                        if (tmpNextPage != null) {
							documents.putAll(getDocuments(rootfolder, nextPage));
                        }
                    }
                }
            }
        }

        return documents;
    }

	/**
	 * creates a {@link Document} object from the Web Service response
	 * 
	 * @param row a Messageelemnt node returned in the WS response
	 * @return
	 * @throws MalformedURLException
	 */
    private Document createDocument(MessageElement row)
            throws MalformedURLException {
        String name = row.getAttribute(FILELEAFREF);
        name = name.substring(name.indexOf("#") + 1);
        String relativeURL = row.getAttribute(FILEREF);
        relativeURL = relativeURL.substring(relativeURL.indexOf("#") + 1);
        String parentDir = row.getAttribute(FILDIREREF);
        parentDir = parentDir.substring(parentDir.indexOf("#") + 1);
        String author = row.getAttribute(EDITOR);
        if (author == null) {
            author = row.getAttribute(AUTHOR);
        }
        author = author.substring(author.indexOf("#") + 1);

        final StringBuffer docUrl = new StringBuffer();
        URL url = new URL(webUrl);
        final int port = url.getPort() == -1 ? url.getDefaultPort()
                : url.getPort();
        docUrl.append(url.getProtocol() + "://" + url.getHost() + ":" + port);
        if (!relativeURL.startsWith("/")) {
            docUrl.append("/");
        }
        docUrl.append(relativeURL);

        Document document = new Document(name, relativeURL, parentDir, null,
                author, null, docUrl.toString());

        return document;
    }

	/**
	 * Makes getListCollection WS call to retrieve all the root folders
	 * corresponding to all the document libraries in the site Returns a map of
	 * FolderURLs<->Folder
	 * 
	 * @param parentSiteId primary identifier of the site from which root
	 *            folders are to be retrieved.
	 * @return
	 * @throws Exception
	 */
	public Map<String, Folder> getListsAsFolders(String parentSiteId)
            throws Exception {
        final Map<String, Folder> rootFolders = new HashMap<String, Folder>();

        final ArrayOf_sListHolder vLists = new ArrayOf_sListHolder();
        final UnsignedIntHolder getListCollectionResult = new UnsignedIntHolder();

        try {
            siteDataStub.getListCollection(getListCollectionResult, vLists);
        } catch (final AxisFault af) {
            if ((UNAUTHORIZED.indexOf(af.getFaultString()) != -1)
                    && (null != domain)) {
                changeAuthScheme();
                setUsername();
                try {
                    siteDataStub.getListCollection(getListCollectionResult, vLists);
                } catch (final Throwable e) {
                    throw new Exception(
                            "Problem while getting top level root folders [ "
                                    + parentSiteId + " ].", e);
                }
            } else {
                throw new Exception(
                        "Problem while getting top level root folders [ "
                                + parentSiteId + " ].", af);
            }
        } catch (final Exception e) {
            throw new Exception(
                    "Problem while getting top level root folders [ "
                            + parentSiteId + " ].", e);
        }

        if (vLists == null) {
            return rootFolders;
        }
        final _sList[] sl = vLists.value;
        if (null == sl) {
            return rootFolders;
        }

        for (_sList element : sl) {
            if (element == null) {
                continue;
            }

            try {
                final String baseType = element.getBaseType();
                if (!collator.equals(baseType, DOC_LIB)) {
                    continue;
                }

                // TODO: Author is not supported currently for the root
				// folder. From the raw XML response, i observed that
                // Author is returned. But, probably it's not in the schema
                // defined for the WSDL response and hence Axis is not
				// returning the same. Also, Author value as returned in the
                // XML response is not the actual login name of the user.
				// Instead, it returns the ID of the user which is in
                // numerical form. Extra web service call will be required
                // for resolving the userID to the loginName
                String relativeUrl = element.getDefaultViewUrl();
				int pos = relativeUrl.lastIndexOf(LIST_URL_SUFFIX);
				if (pos != -1) {
					relativeUrl = relativeUrl.substring(0, pos);
				}
				
				String id = relativeUrl;
				// Remove the leading slash if one exists
				if (id.startsWith("/")) {
				  id = id.substring(1);
				}
				
				Folder folder = new Folder(element.getTitle(), id,
						relativeUrl, parentSiteId, null, null, true);
				rootFolders.put(relativeUrl, folder);
            } catch (Exception e) {
                // Problem while processing some lists.
                continue;
            }
        }

        return rootFolders;
    }

	/**
	 * Makes getWebURLFromPageURL WS call to get the webUrl which is safe to use
	 * with further WS calls. Also set the username format as per the current
	 * authentication scheme applied on the site
	 * 
	 * @param pageURL any page Url under the current SharePoint site
	 * @return
	 * @throws Exception
	 */
    private String getWebURLFromPageURL(final String pageURL) throws Exception {
        String strWebURL = null;
        try {
            strWebURL = webStub.webUrlFromPageUrl(pageURL);
        } catch (final AxisFault af) {
            if ((UNAUTHORIZED.indexOf(af.getFaultString()) != -1)
                    && (null != domain)) {
                changeAuthScheme();
				if (auth == AuthScheme.BASIC) {
                    webStub.setUsername(username + "@" + domain);
                } else {
					webStub.setUsername(domain + "\\" + username);
                }
                try {
                    strWebURL = webStub.webUrlFromPageUrl(pageURL);
                } catch (final Exception e) {
                    changeAuthScheme();
                    strWebURL = getWebURLForWSCall(pageURL);
                    return strWebURL;
                }
            } else {
                strWebURL = getWebURLForWSCall(pageURL);
                return strWebURL;
            }
        } catch (final Throwable e) {
            strWebURL = getWebURLForWSCall(pageURL);
            return strWebURL;
        }
        return strWebURL;
    }

	/**
	 * Since, getWebURLFromPageURL is not fully reliable, this method does a
	 * custom manipulation in the Url to make it safe for WS calls.
	 * 
	 * @param strUrl a SharePoint site Url
	 * @return
	 * @throws Exception
	 */

    private String getWebURLForWSCall(final String strUrl) throws Exception {
        final URL url = new URL(strUrl);
        final String hostTmp = url.getHost();
        final String protocolTmp = url.getProtocol();
        int portTmp = -1;
        if (-1 != url.getPort()) {
            if (url.getPort() != url.getDefaultPort()) {
                portTmp = url.getPort();
            }
        }
        String siteNameTmp = url.getPath();
        String strSPURL = protocolTmp + "://" + hostTmp;
        if (portTmp != -1) {
            strSPURL += ":" + portTmp;
        }
        if (siteNameTmp.endsWith("/")) {
            siteNameTmp = siteNameTmp.substring(0, siteNameTmp.length() - 1);
        }
        strSPURL += siteNameTmp;
        return strSPURL;
    }

	/**
	 * Makes a HTTP GET call to to a URL to access document's content and
	 * metadata
	 * 
	 * @param strURL The URL to connect to
	 * @return
	 * @throws Exception if the HTTP response was not 200 or the call failed for
	 *             any other reason
	 */
	public HttpMethodBase downloadContent(final String strURL) throws Exception {
		URL url = new URL(strURL);
		final URI uri = new URI(url.getProtocol(), null, url.getHost(),
				url.getPort(), url.getPath(), url.getQuery(), url.getRef());

        HttpMethodBase method = new GetMethod(uri.toASCIIString());
		Credentials credentials = null;
		if (auth == AuthScheme.NTLM) {
			credentials = new NTCredentials(username, password, url.getHost(),
					domain);
		} else {
			credentials = new UsernamePasswordCredentials(domain + "\\"
					+ username, password);
		}
		final HttpClient httpClient = new HttpClient();
		httpClient.getState().setCredentials(AuthScope.ANY, credentials);

        int responseCode = httpClient.executeMethod(method);

        if (responseCode == 200) {
			return method;
		} else {
			throw new Exception("Can not access document's content");
		}
	}

    public ACL[] getAclForUrls(String[] urls, String webUrl) {
        try {
			ACL[] acls = gssAclStub.getAclForUrls(urls, webUrl);
			return acls;
        } catch (Exception e) {
            System.out.println(e);
        }
		return  new ACL[0];
	}

    public Set<String> getDirectChildsites() throws Exception {
		final Set<String> allSites = new TreeSet<String>();
		// to store all the sub-webs state
		GetWebCollectionResponseGetWebCollectionResult webcollnResult = null;

        try {
			webcollnResult = webStub.getWebCollection();
		} catch (final AxisFault af) {
			if ((UNAUTHORIZED.indexOf(af.getFaultString()) != -1)
					&& (null != domain)) {
				changeAuthScheme();
				setUsername();
				try {
					webcollnResult = webStub.getWebCollection();
				} catch (final Throwable e) {
					throw new Exception("Problem while getting child sites.", e);
				}
			} else {
				throw new Exception("Problem while getting child sites.", af);
			}
		} catch (final RemoteException e) {
			throw new Exception("Problem while getting child sites.", e);
		}

        if (webcollnResult != null) {
			final MessageElement[] meWebs = webcollnResult.get_any();
			if ((meWebs != null) && (meWebs[0] != null)) {

                final Iterator itWebs = meWebs[0].getChildElements();
				if (itWebs != null) {
					while (itWebs.hasNext()) {
						final MessageElement meWeb = (MessageElement) itWebs.next();
						if (null == meWeb) {
							continue;
						}
						final String url = meWeb.getAttribute("Url");
						allSites.add(url);
					}
				}
			}
		}

        return allSites;
    }
}
