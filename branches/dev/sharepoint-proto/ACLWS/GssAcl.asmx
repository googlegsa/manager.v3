//Copyright 2010 Google Inc.

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

<%@ WebService Language="C#" Class="GssAclMonitor" %>
using System;
using System.Net;
using System.Text;
using System.Xml;
using System.Collections;
using System.Collections.Generic;
using System.Web;
using System.Web.Services;
using Microsoft.SharePoint;
using Microsoft.SharePoint.Administration;
using Microsoft.SharePoint.Utilities;

/// <summary>
/// Represents a user/group which is used in the <see cref="GssAce"/>
/// </summary>
[WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
[Serializable]
public class GssPrincipal
{
    // Site Collection specific ID. This is very useful to track such users/groups which have been deleted
    // A -1 value is used for the hypoyhetical site collection group and anything smaller than -1 is considered to be unknown
    private int id;

    // Name of the prinicpal
    private string name;

    public enum PrincipalType
    {
        USER, DOMAINGROUP, SPGROUP, NA
    }

    private PrincipalType type;

    /// <summary>
    /// Represents the member users If the current principal is a group
    /// </summary>
    private List<GssPrincipal> members;
    private StringBuilder logMessage = new StringBuilder();

    public int ID
    {
        get { return id; }
        set { id = value; }
    }
    public string Name
    {
        get { return name; }
        set { name = value; }
    }
    public PrincipalType Type
    {
        get { return type; }
        set { type = value; }
    }
    public List<GssPrincipal> Members
    {
        get { return members; }
        set { members = value; }
    }
    public String LogMessage
    {
        get { return logMessage.ToString(); }
        set { logMessage = new StringBuilder(value); }
    }

    // A web service always require a default constructor. But, we do not want to use it intentionally
    private GssPrincipal() { }

    public GssPrincipal(string name, int id)
    {
        ID = id;
        Name = name;
        Members = new List<GssPrincipal>();
    }

    public override bool Equals(object obj)
    {
        if (obj is GssPrincipal)
        {
            GssPrincipal principal = (GssPrincipal)obj;
            if (null == this.name || null == principal || null == principal.name)
            {
                return false;
            }

            if (principal.ID == this.ID && principal.Name.Equals(this.Name) && principal.Type == this.Type)
            {
                return true;
            }
        }
        return false;
    }

    public override int GetHashCode()
    {
        return 13 * (this.Name.GetHashCode() + this.Type.GetHashCode() + ID);
    }

    public void AddLogMessage(string logMsg)
    {
        logMessage.AppendLine(logMsg);
    }
}

/// <summary>
/// An object of this class represents the actual SharePoint permission
/// </summary>
[WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
[Serializable]
public class GssSharepointPermission
{
  // The type being used for permissions are a list of string. SPBasePermission which is an enumeration provided by the SharePoint can also be used over here and would make better sense to have. But, it has a problem when SOAP serialization occurs.
  // The bitmask representing a set of permissions may not be serialized properly during SOAP serialization. One such case is when "Deny Write" is used in the security policy. The bitmask used in that case is 4611685812065333150.

    // List of allowed permissions
    private List<string> allowedPermissions = new List<string>();

    // List denied permission
    private List<string> deniedPermission = new List<string>();

    public List<string> AllowedPermissions
    {
        get { return allowedPermissions; }
        set { allowedPermissions = value; }
    }
    public List<string> DeniedPermission
    {
        get { return deniedPermission; }
        set { deniedPermission = value; }
    }

    /// <summary>
    /// Converts a SPBasePermission object into a set of string representing the actual permission being used
    /// </summary>
    /// <param name="spPerms"></param>
    /// <returns></returns>
    private List<string> GetPermissions(SPBasePermissions spPerms)
    {
        List<string> perms = new List<string>();
        foreach (SPBasePermissions value in Enum.GetValues(typeof(SPBasePermissions)))
        {
            if (value == (value & spPerms))
            {
                perms.Add(value.ToString());
            }
        }
        return perms;
    }

    /// <summary>
    /// Adds new grant and deny permission(s) to the current object
    /// </summary>
    /// <param name="allowedPermissions"></param>
    /// <param name="deniedPermission"></param>
    public void UpdatePermission(SPBasePermissions allowedPermissions, SPBasePermissions deniedPermission)
    {
        this.allowedPermissions.AddRange(GetPermissions(allowedPermissions));
        this.deniedPermission.AddRange(GetPermissions(deniedPermission));
    }
}

/// <summary>
/// An object of ths class represents an Access Control Entry in an <see cref="ACL"/>
/// </summary>
[WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
[Serializable]
public class GssAce
{
    private GssPrincipal principal;
    private GssSharepointPermission permission;

    public GssPrincipal Principal
    {
        get { return principal; }
        set { principal = value; }
    }
    public GssSharepointPermission Permission
    {
        get { return permission; }
        set { permission = value; }
    }

    // A web servcei always require a default constructor. But, we do not want to use it intentionally
    private GssAce() { }

    public GssAce(GssPrincipal principal, GssSharepointPermission permission)
    {
        Principal = principal;
        Permission = permission;
    }

    public override bool Equals(object obj)
    {
        if (obj is GssAce)
        {
            GssAce ace = (GssAce)obj;
            if(null != this.Principal && null != ace && null != ace.Principal
                && this.Principal.Equals(ace.Principal) && this.Permission.Equals(ace.Permission))
            {
                return true;
            }
        }
        return false;
    }

    public override int GetHashCode()
    {
        return 17 * this.Principal.GetHashCode();
    }
}

/// <summary>
/// Represents ACL of a SharePoint entity. The represented ACL is a collection of all the effective <see cref="ACE"/>
/// </summary>
[WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
[Serializable]
public class GssAcl
{
    // URL of the entity whose ACLs are being represented
    private string entityUrl;

    // Author/Owner of the document. This is an added info returned along with the ACL. This could be relevant to the clients because the owner's value as returned by the available SharePoint web services are not LDAP format.
    private string owner;

    // List of all the ACEs
    private List<GssAce> allAce;

    private StringBuilder logMessage = new StringBuilder();

    public string EntityUrl
    {
        get { return entityUrl; }
        set { entityUrl = value; }
    }
    public string Owner
    {
        get { return owner; }
        set { owner = value; }
    }
    public List<GssAce> AllAce
    {
        get { return allAce; }
        set { allAce = value; }
    }
    public String LogMessage
    {
        get { return logMessage.ToString(); }
        set { logMessage = new StringBuilder(value); }
    }

    // A web service always require a default constructor. But, we do not want to use it intentionally
    private GssAcl() { }

    public GssAcl(string entityUrl, int count)
    {
        this.entityUrl = entityUrl;
        this.allAce = new List<GssAce>(count);
    }

    public void AddAce(GssAce ace)
    {
        allAce.Add(ace);
    }

    public void AddLogMessage(string logMsg)
    {
        logMessage.AppendLine(logMsg);
    }
}

/// <summary>
/// Type of possible Object/Entity which the web service deals with
/// </summary>
public enum ObjectType
{
    NA,
    SECURITY_POLICY,
    ADMINISTRATORS,
    GROUP,
    USER,
    WEB,
    LIST,
    ITEM
}

/// <summary>
/// Represents a single ACL specific change in SharePoint
/// </summary>
[WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
[Serializable]
public class GssAclChange
{
    // The object type that has changed
    private ObjectType changedObject;

    // Type of change
    private SPChangeType changeType;

    // An additional hint to identify the exact object/entity that has changed. Most of the time, this would be the ID, GUID or URL.
    private string hint;

    // A way to identify if the current change has its implication under the web site to which the request has been sent. This is useful because the web service processing is done at site collection level and all changes might not be relevant to all the web sites in the site collection.
    private bool isEffectiveIncurrentWeb;

    public ObjectType ChangedObject
    {
        get { return changedObject; }
        set { changedObject = value; }
    }

    public SPChangeType ChangeType
    {
        get {return changeType; }
        set { changeType = value; }
    }

    public string Hint
    {
        get { return hint; }
        set { hint = value; }
    }

    public bool IsEffectiveInCurrentWeb
    {
        get { return isEffectiveIncurrentWeb; }
        set { isEffectiveIncurrentWeb = value; }
    }

    // A web servcei always require a default constructor. But, we do not want to use it intentionally
    private GssAclChange() { }

    public GssAclChange(ObjectType inChangedObject, SPChangeType inChangeType, string inHint)
    {
        ChangedObject = inChangedObject;
        ChangeType = inChangeType;
        Hint = inHint;
    }
}

/// <summary>
/// Represents a list of <see cref="GssAclChnage"/> that have happened on SharePoint and provides a Change Token for synchronization purpose
/// </summary>
[WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
[Serializable]
public class GssAclChangeCollection
{
    // Next change token that should be used for synchronization
    private string changeToken;
    private List<GssAclChange> changes;
    private StringBuilder logMessage = new StringBuilder();

    public string ChangeToken
    {
        get { return changeToken; }
        set { changeToken = value; }
    }

    public List<GssAclChange> Changes
    {
        get { return changes; }
        set { changes = value; }
    }

    public String LogMessage
    {
        get { return logMessage.ToString(); }
        set { logMessage = new StringBuilder(value); }
    }

    // A web servcei always require a default constructor. But, we do not want to use it intentionally
    private GssAclChangeCollection() { }

    public GssAclChangeCollection(SPChangeToken changeToken)
    {
        if (null != changeToken)
        {
            ChangeToken = changeToken.ToString();
        }
        else
        {
            AddLogMessage("Invalid Token");
        }
        Changes = new List<GssAclChange>();
    }

    /// <summary>
    /// Construct an appropriate <see cref="GssAclChnage"/> object from a SharePoint's SPChange object and adds it to the list of changes
    /// </summary>
    /// <param name="change"> SharePoint's change object. This may not necessarily be a ACL related change. </param>
    /// <param name="site"> The site collection from which the change has been found. </param>
    /// <param name="web"> The web site from which the change has been found. </param>
    public void AddChange(SPChange change, SPSite site, SPWeb web)
    {
        if (change is SPChangeWeb)
        {
            switch (change.ChangeType)
            {
                case SPChangeType.AssignmentAdd:
                case SPChangeType.AssignmentDelete:
                case SPChangeType.RoleAdd:
                case SPChangeType.RoleDelete:
                case SPChangeType.RoleUpdate:
                    SPChangeWeb changeWeb = (SPChangeWeb)change;
                    // Instead of sending the ID of the web that has changed, one may consider sending the actual List IDs that should be re-crawled. This is because,
                    // connector does the actual document discovery per list level. Sending the changed webId will force the connector to make an extra call to get the Lists that should be re-crawled.
                    // But, such implementation will become confusing when the connector will evolve in future to support site collection and web application level crawling.
                    // It's better to send the change web ID as hint and let the connector decide how to work on this.
                    GssAclChange gssChange = new GssAclChange(ObjectType.WEB, changeWeb.ChangeType, changeWeb.Id.ToString());
                    gssChange.IsEffectiveInCurrentWeb = IsEffectiveForWeb(site, web, changeWeb.Id);
                    changes.Add(gssChange);
                    break;
            }
        }
        else if (change is SPChangeList)
        {
            switch (change.ChangeType)
            {
                case SPChangeType.AssignmentAdd:
                case SPChangeType.AssignmentDelete:
                case SPChangeType.RoleAdd:
                case SPChangeType.RoleDelete:
                case SPChangeType.RoleUpdate:
                    SPChangeList changeList = (SPChangeList)change;
                    GssAclChange gssChange = new GssAclChange(ObjectType.LIST, changeList.ChangeType, changeList.Id.ToString());
                    gssChange.IsEffectiveInCurrentWeb = IsEffectiveForWeb(site, web, changeList.WebId);
                    changes.Add(gssChange);
                    break;
            }
        }
        else if (change is SPChangeUser)
        {
            SPChangeUser changeUser = (SPChangeUser)change;
            GssAclChange gssChange = null;
            if (changeUser.IsSiteAdminChange)
            {
                gssChange = new GssAclChange(ObjectType.ADMINISTRATORS, changeUser.ChangeType, changeUser.Id.ToString());
            }
            else if (changeUser.ChangeType == SPChangeType.Delete)
            {
                gssChange = new GssAclChange(ObjectType.USER, changeUser.ChangeType, changeUser.Id.ToString());
            }
            if (null != gssChange)
            {
                gssChange.IsEffectiveInCurrentWeb = true;
                changes.Add(gssChange);
            }
        }
        else if (change is SPChangeGroup)
        {
            switch (change.ChangeType)
            {
                case SPChangeType.MemberAdd:
                case SPChangeType.MemberDelete:
                case SPChangeType.Delete:
                    SPChangeGroup changeGroup = (SPChangeGroup)change;
                    GssAclChange gssChange = new GssAclChange(ObjectType.GROUP, changeGroup.ChangeType, changeGroup.Id.ToString());
                    gssChange.IsEffectiveInCurrentWeb = true;
                    changes.Add(gssChange);
                    break;
            }
        }
        else if (change is SPChangeSecurityPolicy)
        {
            SPChangeSecurityPolicy changeSecurityPolicy = (SPChangeSecurityPolicy)change;
            GssAclChange gssChange = new GssAclChange(ObjectType.SECURITY_POLICY, changeSecurityPolicy.ChangeType, "");
            gssChange.IsEffectiveInCurrentWeb = true;
            changes.Add(gssChange);
        }
    }

  /// <summary>
    /// Determines if any change in SPWeb identified by changeWebId can affect the ACLs under SPWeb web
    /// </summary>
    /// <param name="site"> The site collection from which the change has been found. </param>
    /// <param name="web"> The web site from which the change has been found. </param>
    /// <param name="changeWebId"> Guid of the web site where the change has occured. </param>
    private bool IsEffectiveForWeb(SPSite site, SPWeb web, Guid changeWebId)
    {
        SPWeb thisWeb = site.OpenWeb(changeWebId);
        if (null == thisWeb)
        {
            return false;
        }

        if (web.ID.Equals(thisWeb.ID))
        {
           return true;
        }
        else
        {
            GssAclUtility gssUtil = new GssAclUtility();
            return gssUtil.isSame(web.FirstUniqueAncestor, thisWeb.FirstUniqueAncestor);
        }
        return false;
    }

    public void UpdateChangeToken(SPChangeToken inToken)
    {
        if (null != inToken)
        {
            ChangeToken = inToken.ToString();
        }
    }

    public void AddLogMessage(string logMsg)
    {
        logMessage.AppendLine(logMsg);
    }
}

/// <summary>
/// Represents a basic response object containing minimal information that can be used by all other web methods.
/// For now, site collection information has been identified as one such information. The reason being, Java connector
/// uses SharePoint site's URL to access this web service. However, the operation GetAclForURLs, GetAclChangeSinceToken,
/// ResolveSPGroup etc works at site collection level. Returning this site collection info in the web service response will
/// tell the client (Java connector) about the actual site collection which was used by the web service for serving the request.
/// This info can be used for various purposes like maintaining users/groups membership as they are defined at site collection level.
/// </summary>
[WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
[Serializable]
public abstract class GssAclBaseResult
{
    private string siteCollectionUrl;
    private Guid siteCollectionGuid;
    private StringBuilder logMessage = new StringBuilder();

    public string SiteCollectionUrl
    {
        get { return siteCollectionUrl; }
        set { siteCollectionUrl = value; }
    }
    public Guid SiteCollectionGuid
    {
        get { return siteCollectionGuid; }
        set { siteCollectionGuid = value; }
    }

    public String LogMessage
    {
        get { return logMessage.ToString(); }
        set { logMessage = new StringBuilder(value); }
    }

    public void AddLogMessage(string logMsg)
    {
        logMessage.AppendLine(logMsg);
    }
}

/// <summary>
/// Response Object for GetAclForUrls web method
/// </summary>
[WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
[Serializable]
public class GssGetAclForUrlsResult : GssAclBaseResult
{
    // Ideally, a map of <url, Acl> should be returned. But, C# Dictionary is not SOAP serializable. Hence, using List.
    private List<GssAcl> allAcls;

    public List<GssAcl> AllAcls
    {
        get { return allAcls; }
        set { allAcls = value; }
    }
}

/// <summary>
/// Response Object for GetAclChangesSinceToken web method
/// </summary>
[WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
[Serializable]
public class GssGetAclChangesSinceTokenResult : GssAclBaseResult
{
    private GssAclChangeCollection allChanges;

    public GssAclChangeCollection AllChanges
    {
        get { return allChanges; }
        set { allChanges = value; }
    }
}

/// <summary>
/// Response Object for ResolveSPGroup web method
/// </summary>
[WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
[Serializable]
public class GssResolveSPGroupResult : GssAclBaseResult
{
    private List<GssPrincipal> prinicpals;

    public List<GssPrincipal> Prinicpals
    {
        get { return prinicpals; }
        set { prinicpals = value; }
    }
}

/// <summary>
///
/// </summary>
[WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
[Serializable]
public class GssGetListItemsWithInheritingRoleAssignments : GssAclBaseResult
{
  // XML representation of all the documents/items to be returned
    private string docXml;

  // Are there more documents to be crawled?
    private bool moreDocs;

    // From where the next set of documents should be requested. This info has to be sent explicitly in the response because, it may happen that WS crawl some documents but does not return any as none of the documents inherits the permission. In such case, this explicit info about the lastDocCraed will save the client from visiting the same set of documents again and again
    private int lastIdVisited;

    public string DocXml
    {
        get { return docXml; }
        set { docXml = value; }
    }
    public bool MoreDocs
    {
        get { return moreDocs; }
        set { moreDocs = value; }
    }

    public int LastIdVisited
    {
        get { return lastIdVisited; }
        set { lastIdVisited = value; }
    }
}

/// <summary>
/// Provides alll the necessary web methods exposed by the Web Service
/// </summary>
[WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
public class GssAclMonitor
{
    GssAclUtility gssUtil = new GssAclUtility();
    // Current site collection in which request is to be served
    SPSite site;

    // SharePoint site used for constructing the web service endpoint
    SPWeb web;

    // A random guess about how many items should be query at a time. Such threshold is required to save the web service from being unresponsive for a long time
    public const int ROWLIMIT = 500;

    // A hypothetical name given to the site collection administrator group. This is required because the web service treats
    // site collection administrators as one of the SharePoitn groups. This is in benefit of avoiding re-crawling all the documents
    // when there is any change in the administrators list. Java connector sends ACL as document's metadata and any change in the
    // administrator requires re-crawling all the documents in the site collection. Having a common group for the administrator will
    // just require updating the group membership info and no re-crawl will be required.
    public const string GSSITEADMINGROUP = "[GSSiteCollectionAdministrator]";

    public GssAclMonitor()
    {
        SPContext spContext = SPContext.Current;
        if (null == spContext)
        {
            throw new Exception("Unable to get SharePoint context. The web service endpoint might not be referring to a valid SharePoitn site. ");
        }
        site = spContext.Site;
        if (null == site)
        {
            throw new Exception("SharePoint site collection not found");
        }
        web = site.OpenWeb();
        if (null == web)
        {
            throw new Exception("SharePoint site not found");
        }
    }

    ~GssAclMonitor()
    {
        if (null != web)
        {
          web.Dispose();
        }
        if (null != site)
        {
          site.Dispose();
        }
    }

    /// <summary>
    /// A dummy method used mainly to test the availability and connectivity of the web service.
    /// </summary>
    [WebMethod]
    public string CheckConnectivity()
    {
        // Ensure that all the required APIs are accessible
        SPUserCollection admins = web.SiteAdministrators;
        SPPolicyCollection policies = site.WebApplication.Policies;
        policies = site.WebApplication.ZonePolicies(site.Zone);
        return "success";
    }

    /// <summary>
    /// Returns ACLs of a set of entities which belongs to a single SharePoint web site. The SharePoint site is identified by the SPContext in which the request is being served.
    /// </summary>
    /// <param name="urls"> Entity URLs whose ACLs are to be returned </param>
    /// <returns> List Of ACLs corresponding to the entity URLs </returns>
    [WebMethod]
    public GssGetAclForUrlsResult GetAclForUrls(string[] urls)
    {
        GssGetAclForUrlsResult result = new GssGetAclForUrlsResult();

        List<GssAcl> allAcls = new List<GssAcl>();

        Dictionary<GssPrincipal, GssSharepointPermission> commonAceMap = new Dictionary<GssPrincipal, GssSharepointPermission>();
        try
        {
            gssUtil.FetchSecurityPolicyForAcl(site, commonAceMap);
        }
        catch (Exception e)
        {
            result.AddLogMessage("Problem while processing security policies. Exception [" + e.Message + " ] ");
        }

        try
        {
            gssUtil.FetchSiteAdminsForAcl(web, commonAceMap);
        }
        catch (Exception e)
        {
            result.AddLogMessage("Problem while processing site collection admins. Exception [" + e.Message + " ] ");
        }

        foreach (string url in urls)
        {
            GssAcl acl = null;
            try
            {
                Dictionary<GssPrincipal, GssSharepointPermission> aceMap = new Dictionary<GssPrincipal, GssSharepointPermission>(commonAceMap);
                ISecurableObject secobj = gssUtil.IdentifyObject(url, web);
                gssUtil.FetchRoleAssignmentsForAcl(secobj.RoleAssignments, aceMap);
                acl = new GssAcl(url, aceMap.Count);
                foreach (KeyValuePair<GssPrincipal, GssSharepointPermission> keyVal in aceMap)
                {
                    acl.AddAce(new GssAce(keyVal.Key, keyVal.Value));
                }
                allAcls.Add(acl);

                SPUser owner = gssUtil.GetOwner(secobj);
                if (null != owner)
                {
                    acl.Owner = owner.LoginName;
                }
            }
            catch (Exception e)
            {
                acl = new GssAcl(url, 0);
                acl.AddLogMessage("Problem while processing role assignments. Exception [" + e.Message + " ] ");
            }
        }

        result.AllAcls = allAcls;
        result.SiteCollectionUrl = site.Url;
        result.SiteCollectionGuid = site.ID;
        result.AddLogMessage(gssUtil.LogMessage);
        return result;
    }

    /// <summary>
    /// Returns a list of ACL specific changes that have happened over a period of time, determined by the change token.
    /// These changes purely reflects the actions performed on the SharePoint but does not talk about their implications. The caller should not assume that the ACL of any entity has changed just because it receives a set of changes from this API.
    /// A typical example could be when the permission hierarchy of a list/web is reset and immediately brought to its original state. In that case this API will return two changes but there is no ACL change on the SharePoint.
    /// Deletion of an empty group is another such case.
    /// </summary>
    /// <param name="fromChangeToken"> The change token from where the changes are to be scanned in the SharePoint's change log. It defines the starting point in the change log </param>
    /// <param name="toChangeToken"> Only those changes which have been registered in the change log with a token that appears before this token will be retrieved. It defines the ending pooint in the change log </param>
    /// <returns> a list of ACL specific changes </returns>
    [WebMethod]
    public GssGetAclChangesSinceTokenResult GetAclChangesSinceToken(string fromChangeToken, string toChangeToken)
    {
        GssGetAclChangesSinceTokenResult result = new GssGetAclChangesSinceTokenResult();
        GssAclChangeCollection allChanges = null;
        SPChangeToken changeTokenEnd = null;
        if (null != fromChangeToken && fromChangeToken.Length != 0)
        {
            if (null != toChangeToken && toChangeToken.Length != 0)
            {
                changeTokenEnd = new SPChangeToken(toChangeToken);
            }

            SPChangeToken changeTokenStart = new SPChangeToken(fromChangeToken);
            allChanges = new GssAclChangeCollection(changeTokenStart);
            try
            {
                SPChangeCollection spChanges = gssUtil.FetchAclChanges(site, changeTokenStart, changeTokenEnd);
                foreach (SPChange change in spChanges)
                {
                    allChanges.AddChange(change, site, web);
                }

                // There are two ways to get the next Change Token value that should be used for synchronization. 1) Get the last change token available for the site
                // 2) Get the last change token corresponding to which changes have been tracked.
                // The problem with the second approach is that if no ACL specific changes will occur, the change token will never gets updated and will become invalid after some time.
                // Another performance issue is is that, the scan will always start form the same token unless there is a ACL specific change.
                // Since, the change tracking logic ensures that all changes will be tracked (i.e there is no rowlimit kind of thing associated), it is safe to use the first approach.
                if (null == changeTokenEnd)
                {
                    // Since all the canges have been detected till the time, use the current chage token of the site collection as the next token for synchronization
                    allChanges.UpdateChangeToken(site.CurrentChangeToken);
                }
                else
                {
                    // Since cange detection was done only till changeTokenToEnd, we have to use the same token as next token for synchronization
                    allChanges.UpdateChangeToken(changeTokenEnd);
                }
            }
            catch (Exception e)
            {
                // All the changes should be processed as one atomic operation. If any one fails, all should be ignored. This is in lieu of maintaining a single change token which will be used for executing change queries.
                // Since, we are not progressing, use the same change token that was received
                allChanges = new GssAclChangeCollection(changeTokenStart);
                result.AddLogMessage("Exception occurred while change detection, Exception [ " + e.Message + " ] ");
            }
        }
        else
        {
            // It's the first request. Return the current chage token of the site collection as the next token for synchronization
            allChanges = new GssAclChangeCollection(site.CurrentChangeToken);
        }

        result.AllChanges = allChanges;
        result.SiteCollectionUrl = site.Url;
        result.SiteCollectionGuid = site.ID;
        result.AddLogMessage(gssUtil.LogMessage);
        return result;
    }

    /// <summary>
    /// Expands a SharePoint group to find all the member users and domain groups. Creates a <see cref="GssPrincipal"/> object for each of them and returns the same.
    /// The group must exist in the site collection for which the request has been sent.
    /// </summary>
    /// <param name="groupId"> the SharePoint group ID/Name that is to be resolved </param>
    /// <returns> list of GssPrincipal object with the Members attribute set as the list of member users/domain-groups</returns>
    [WebMethod]
    public GssResolveSPGroupResult ResolveSPGroup(string[] groupId)
    {
        GssResolveSPGroupResult result = new GssResolveSPGroupResult();
        List<GssPrincipal> prinicpals = new List<GssPrincipal>();
        if (null != groupId)
        {
            foreach (string id in groupId)
            {
                GssPrincipal principal = null;
                try
                {
                    if (GSSITEADMINGROUP.Equals(id))
                    {
                        principal = new GssPrincipal(GSSITEADMINGROUP, -1);
                        // Get all the administrator users as member of the GSSITEADMINGROUP.
                        List<GssPrincipal> admins = new List<GssPrincipal>();
                        foreach (SPPrincipal spPrincipal in web.SiteAdministrators)
                        {
                            GssPrincipal admin = gssUtil.GetGssPrincipalFromSPPrincipal(spPrincipal);
                            if (null == admin)
                            {
                                continue;
                            }
                            admins.Add(admin);
                        }
                        principal.Members = admins;
                    }
                    else
                    {
                        SPGroup spGroup = web.SiteGroups.GetByID(int.Parse(id));
                        if(null != spGroup)
                        {
                            principal = new GssPrincipal(spGroup.Name, spGroup.ID);
                            principal.Members = gssUtil.ResolveSPGroup(spGroup);
                        }
                        else
                        {
                            principal = new GssPrincipal(id, -2);
                            principal.AddLogMessage("Could not resolve Group Id [ " + id + " ] ");
                        }
                    }
                    principal.Type = GssPrincipal.PrincipalType.SPGROUP;
                }
                catch (Exception e)
                {
                    principal = new GssPrincipal(id, -2);
                    principal.AddLogMessage("Could not resolve Group Id [ " + id + " ]. Exception: " + e.Message);
                    principal.Type = GssPrincipal.PrincipalType.NA;
                }
                prinicpals.Add(principal);
            }
        }

        result.Prinicpals = prinicpals;
        result.SiteCollectionUrl = site.Url;
        result.SiteCollectionGuid = site.ID;
        result.AddLogMessage(gssUtil.LogMessage);
        return result;
    }

    /// <summary>
    /// Returns the GUIDs of all those Lists which are inheriting their permissions from the SharePoint web site to which the request has been sent
    /// </summary>
    /// <returns> list of GUIDs of the lists </returns>
    [WebMethod]
    public List<string> GetListsWithInheritingRoleAssignments()
    {
        List<string> listIDs = new List<string>();
        SPListCollection lists = web.Lists;
        foreach (SPList list in lists)
        {
            if (!list.HasUniqueRoleAssignments && gssUtil.isSame(web.FirstUniqueAncestor, list.FirstUniqueAncestor))
            {
                listIDs.Add(list.ID.ToString());
            }
        }
        return listIDs;
    }

    /// <summary>
    /// Returns the List Items (sorted in ascending order of their IDs) which are inheriting the role assignments from the passed in SharePoint List.
    /// </summary>
    /// <param name="listGuId"> GUID of the SharePoint List from which the Items are to be returned. The list must belong to the the site in which the request has been sent </param>
    /// <param name="rowLimit"> Threshold value for the document count to be returned </param>
    /// <param name="lastItemId"> Only document ahead of this ID should be returned </param>
    /// <returns> list of IDs of the items </returns>
    [WebMethod]
    public GssGetListItemsWithInheritingRoleAssignments GetListItemsWithInheritingRoleAssignments(string listGuId, int rowLimit, int lastItemId)
    {
        SPList changeList = null;
        try
        {
            changeList = web.Lists[new Guid(listGuId)];
            if (null == changeList)
            {
                throw new Exception("Passed in listId [ " + listGuId + " ] does not exist in the current web site context");
            }
        }
        catch (Exception e)
        {
            throw new Exception("Passed in listId [ " + listGuId + " ] does not exist in the current web site context");
        }

        List<string> itemIDs = new List<string>();
        SPQuery query = new SPQuery();
        query.RowLimit = GssAclMonitor.ROWLIMIT;
        // CAML query to do a progressive crawl of items in ascending order of their IDs. The prgression is controlled by lastItemId
        query.Query =    "<Where>"
                       +   "<Gt>"
                       +       "<FieldRef Name=\"ID\"/>"
                       +       "<Value Type=\"Counter\">" + lastItemId + "</Value>"
                       +   "</Gt>"
                       + "</Where>"
                       + "<OrderBy>"
                       +   "<FieldRef Name=\"ID\" Ascending=\"TRUE\" />"
                       + "</OrderBy>";
        if (changeList.BaseType == SPBaseType.DocumentLibrary
            || changeList.BaseType == SPBaseType.GenericList
            || changeList.BaseType == SPBaseType.Issue)
        {
            query.ViewAttributes = "Scope = 'Recursive'";
        }

        SPListItemCollection items = changeList.GetItems(query);

        GssGetListItemsWithInheritingRoleAssignments result = new GssGetListItemsWithInheritingRoleAssignments();
        XmlDocument xmlDoc = new XmlDocument();
        XmlNode rootNode = xmlDoc.CreateNode(XmlNodeType.Element, "GssListItems", "");

        int i = 0;
        foreach (SPListItem item in items)
        {
            if (i >= rowLimit)
            {
                result.MoreDocs = true;
                break;
            }
            if (!item.HasUniqueRoleAssignments && gssUtil.isSame(changeList.FirstUniqueAncestor, item.FirstUniqueAncestor))
            {
                XmlNode node = handleOwsMetaInfo(item);
                node = xmlDoc.ImportNode(node, true);
                rootNode.AppendChild(node);
                ++i;
            }
            result.LastIdVisited = item.ID;
        }
        if (null != items.ListItemCollectionPosition)
        {
            result.MoreDocs = true;
        }
        XmlAttributeCollection allAttrs = rootNode.Attributes;
        XmlAttribute attr = xmlDoc.CreateAttribute("Count");
        attr.Value = i.ToString();
        allAttrs.Append(attr);

        result.DocXml = rootNode.OuterXml;
        result.SiteCollectionUrl = site.Url;
        result.SiteCollectionGuid = site.ID;
        return result;
    }

    /// <summary>
    /// Return the XML representation of a ListItem. Handles the ows_MetaInfo attribute by taking these value explicitly from the item's property bag. This ensures that the (key, value) pairs stored inside the property bag will be returned as separate attributes.
    /// </summary>
    /// <param name="listItem"></param>
    /// <returns></returns>
    private XmlNode handleOwsMetaInfo(SPListItem listItem)
    {
        Hashtable props = listItem.Properties;
        XmlDocument xmlDoc = new XmlDocument();
        xmlDoc.LoadXml(listItem.Xml);
        XmlNodeList nodeList = xmlDoc.GetElementsByTagName("z:row");
        XmlNode node = nodeList[0];
        if (null == node)
        {
            return null;
        }
        XmlAttributeCollection allAttrs = node.Attributes;
        XmlAttribute ows_MetaInfo = node.Attributes["ows_MetaInfo"];
        if (null == allAttrs || null == ows_MetaInfo)
        {
            return null;
        }
        allAttrs.Remove(ows_MetaInfo);
        foreach (DictionaryEntry propEntry in props)
        {
            XmlAttribute attr = xmlDoc.CreateAttribute("ows_MetaInfo_" + propEntry.Key.ToString());
            attr.Value = propEntry.Value.ToString();
            allAttrs.Append(attr);
        }
        return node;
    }
}

/// <summary>
/// Provides general purpose utility methods
/// </summary>
public class GssAclUtility
{
    StringBuilder logMessage = new StringBuilder();
    public String LogMessage
    {
        get { return logMessage.ToString(); }
        set { logMessage = new StringBuilder(value); }
    }

    private void AddLogMessage(string logMsg)
    {
        logMessage.AppendLine(logMsg);
    }

    /// <summary>
    /// Update the incoming ACE Map with the users,permissions identified from the web application security policies
    /// </summary>
    /// <param name="site"> Site Collection for which the security policies are to be tracked </param>
    /// <param name="userAceMap"> ACE map to be updated </param>
    public void FetchSecurityPolicyForAcl(SPSite site, Dictionary<GssPrincipal, GssSharepointPermission> aceMap)
    {
        // policies apllied at web application level. This is applicable to all the zones
        SPPolicyCollection policies = site.WebApplication.Policies;
        foreach (SPPolicy policy in policies)
        {
            GssPrincipal principal = GetGssPrincipalForSecPolicyUser(site, policy.UserName);
            if (null == principal)
            {
                continue;
            }

            GssSharepointPermission permission = null;

            if (aceMap.ContainsKey(principal))
            {
                permission = aceMap[principal];
            }
            else
            {
                permission = new GssSharepointPermission();
                aceMap.Add(principal, permission);
            }

            foreach (SPPolicyRole policyRole in policy.PolicyRoleBindings)
            {
                permission.UpdatePermission(policyRole.GrantRightsMask, policyRole.DenyRightsMask);
            }
        }

        // policies applied on the current URL zone
        policies = site.WebApplication.ZonePolicies(site.Zone);
        foreach (SPPolicy policy in policies)
        {
            GssPrincipal principal = GetGssPrincipalForSecPolicyUser(site, policy.UserName);
            if (null == principal)
            {
                continue;
            }

            GssSharepointPermission permission = null;

            if (aceMap.ContainsKey(principal))
            {
                permission = aceMap[principal];
            }
            else
            {
                permission = new GssSharepointPermission();
                aceMap.Add(principal, permission);
            }

            foreach (SPPolicyRole policyRole in policy.PolicyRoleBindings)
            {
                permission.UpdatePermission(policyRole.GrantRightsMask, policyRole.DenyRightsMask);
            }
        }
    }

    /// <summary>
    /// Update the incoming ACE Map with the users,permissions identified from the site collection administrators list.
    /// Site Collection Administrator is treated as another site collection group. All the users/groups are sent as members of this group
    /// </summary>
    /// <param name="web"> SharePoint web site whose administrators are to be tracked </param>
    /// <param name="userAceMap"> ACE Map to be updated </param>
    public void FetchSiteAdminsForAcl(SPWeb web, Dictionary<GssPrincipal, GssSharepointPermission> aceMap)
    {
        GssPrincipal principal = new GssPrincipal(GssAclMonitor.GSSITEADMINGROUP, -1);
        principal.Type = GssPrincipal.PrincipalType.SPGROUP;
        GssSharepointPermission permission = new GssSharepointPermission();
        // Administrators have Full Rights in the site collection.
        permission.UpdatePermission(SPBasePermissions.FullMask, SPBasePermissions.EmptyMask);
        aceMap.Add(principal, permission);

        // Get all the administrator user as member of the GSSITEADMINGROUP.
        List<GssPrincipal> admins = new List<GssPrincipal>();
        foreach (SPPrincipal spPrincipal in web.SiteAdministrators)
        {
            GssPrincipal admin = GetGssPrincipalFromSPPrincipal(spPrincipal);
            if (null == admin)
            {
                continue;
            }
            admins.Add(admin);
        }

        principal.Members = admins;
    }

    /// <summary>
    /// Update the incoming ACE Map with the users,permissions identified from a list of role assignments
    /// </summary>
    /// <param name="roles"> list of role assignments </param>
    /// <param name="userAceMap"> ACE Map to be updated </param>
    public void FetchRoleAssignmentsForAcl(SPRoleAssignmentCollection roles, Dictionary<GssPrincipal, GssSharepointPermission> aceMap)
    {
        foreach (SPRoleAssignment roleAssg in roles)
        {
            GssPrincipal principal = GetGssPrincipalFromSPPrincipal(roleAssg.Member);
            GssSharepointPermission permission = null;

            if (null == principal)
            {
                continue;
            }

            if (aceMap.ContainsKey(principal))
            {
                permission = aceMap[principal];
            }
            else
            {
                permission = new GssSharepointPermission();
                aceMap.Add(principal, permission);
            }

            foreach (SPRoleDefinition roledef in roleAssg.RoleDefinitionBindings)
            {
                permission.UpdatePermission(roledef.BasePermissions, SPBasePermissions.EmptyMask);
            }
        }
    }

    /// <summary>
    /// Identifies the SharePoint object represented by the incoming URL and returns a corresponding ISecurable object for same
    /// </summary>
    /// <param name="url"> Entity URL</param>
    /// <param name="web"> Parent Web to which the entity URL belongs </param>
    /// <returns></returns>
    public ISecurableObject IdentifyObject(string url, SPWeb web)
    {
        SPListItem listItem = web.GetListItem(url);
        if (null != listItem)
        {
            return listItem;
        }

        SPList list = web.GetList(url);
        if (null != list)
        {
            try
            {
                Uri uri = new Uri(url);
                string query = uri.Query;
                string id = HttpUtility.ParseQueryString(query).Get("ID");
                listItem = list.GetItemById(int.Parse(id));
                return listItem;
            }
            catch (Exception e)
            {
                return list;
            }
        }

        return web.Site.OpenWeb(url);
    }

    /// <summary>
    /// Retrieves the Owner's information about a given ISecurable entity
    /// </summary>
    /// <param name="secobj"></param>
    /// <returns></returns>
    public SPUser GetOwner(ISecurableObject secobj)
    {
        SPUser owner = null;
        if (secobj is SPList)
        {
            owner = ((SPList)secobj).Author;
        }
        else if (secobj is SPListItem)
        {
            SPListItem item = (SPListItem)secobj;
            SPFile file = item.File;
            if (null != file)
            {
                // Case of Document Library
                owner = file.Author;
            }
            else
            {
                // Case of other generic lists
                String key = "Created By";
                try
                {
                    SPFieldUser field = item.Fields[key] as SPFieldUser;
                    if (field != null)
                    {
                        SPFieldUserValue fieldValue = field.GetFieldValue(item[key].ToString()) as SPFieldUserValue;
                        if (fieldValue != null)
                        {
                            owner = fieldValue.User;
                        }
                    }
                }
                catch (Exception e)
                {
                    AddLogMessage("Failed to detect Owner for the list item [ " + item.Url + " ] ");
                }
            }
        }
        else if (secobj is SPWeb)
        {
            owner = ((SPWeb)secobj).Author;
        }
        else
        {
            AddLogMessage("Failed to detect Owner becasue the entity is neither a listitem, list or a web. ");
        }
        return owner;
    }

    /// <summary>
    /// Tracks the list of ACL related changes that have happened under the current site collection (to which the request has been sent) from a given point of time, determined by the change token value.
    /// These list of changes are not guaranteed to affect the ACL of every or a even a single entity in the SharePoint. Rather, it purely reflects what action has been performed.
    /// Caller should analyse the implications of these changes and work accordingly.
    ///
    /// This method does not currently supports Item level tracking. This is not required becasue getListItemChangesSinceToken which connector already usees already does the same.
    /// This makes the web service implementation tightly coupled with the connector though, we can live with this limitation for now as the web service, for now, is to be used by the connector only.
    /// </summary>
    /// <param name="site"> The Site collection in which the changes are to be tracked </param>
    /// <param name="changeTokenStart"> The starting change token value from where the changes are to be scanned in the SharePoint's change log </param>
    /// <param name="changeTokenEnd"> The ending change token value until where the changes are to be scanned in the SharePoint's change log </param>
    /// <returns> list of changes that most likely expected to change the ACLs of the entities </returns>
    public SPChangeCollection FetchAclChanges(SPSite site, SPChangeToken changeTokenStart, SPChangeToken changeTokenEnd)
    {
        SPChangeQuery query = new SPChangeQuery(false, false);
        query.ChangeTokenStart = changeTokenStart;
        query.ChangeTokenEnd = changeTokenEnd;

        // Define objects on which changes are to be tracked
        query.SecurityPolicy = true;
        query.User = true;
        query.Group = true;
        query.Web = true;
        query.List = true;

        // Define the type of changes that are to be tracked
        query.RoleAssignmentAdd = true;
        query.RoleAssignmentDelete = true;
        query.RoleDefinitionAdd = true;
        query.RoleDefinitionDelete = true;
        query.RoleDefinitionUpdate = true;
        query.Update = true;
        query.Delete = true;
        query.GroupMembershipAdd = true;
        query.GroupMembershipDelete = true;

        return site.GetChanges(query);
    }

    /// <summary>
    /// Resolves a SharePoint group and create GssPrincipal object containg the expanded list memners users
    /// </summary>
    /// <param name="group"> SharePoint group ID/Name to be resolved </param>
    /// <returns> list of GssPricnicpal object corresponding to the group that was resolved </returns>
    public List<GssPrincipal> ResolveSPGroup(SPGroup group)
    {
        if (null == group)
        {
            return null;
        }
        List<GssPrincipal> members = new List<GssPrincipal>();
        foreach (SPUser user in group.Users)
        {
            GssPrincipal principal = GetGssPrincipalFromSPPrincipal(user);
            if (null != principal)
            {
                members.Add(principal);
            }
        }
        return members;
    }

    /// <summary>
    /// create a GssPrincipal object from the SharePoint's SPPrincipal object and returns the same
    /// </summary>
    /// <param name="spPrincipal"></param>
    /// <returns></returns>
    public GssPrincipal GetGssPrincipalFromSPPrincipal(SPPrincipal spPrincipal)
    {
        if (null == spPrincipal)
        {
            return null;
        }
        GssPrincipal gssPrincipal = null;
        if (spPrincipal is SPUser)
        {
            SPUser user = (SPUser)spPrincipal;
            gssPrincipal = new GssPrincipal(user.LoginName, user.ID);
            if (user.IsDomainGroup)
            {
                gssPrincipal.Type = GssPrincipal.PrincipalType.DOMAINGROUP;
            }
        }
        else if (spPrincipal is SPGroup)
        {
            SPGroup group = (SPGroup)spPrincipal;
            gssPrincipal = new GssPrincipal(group.Name, group.ID);
            gssPrincipal.Type = GssPrincipal.PrincipalType.SPGROUP;
            gssPrincipal.Members = ResolveSPGroup(group);
        }
        else
        {
            gssPrincipal = new GssPrincipal(spPrincipal.Name, -2);
            gssPrincipal.AddLogMessage("could not create GssPrincipal for SPSprincipal [ " + spPrincipal.Name + " ] since it's neither a SPGroup nor a SPUser. ");
        }

        return gssPrincipal;
    }

    /// <summary>
    /// Creates a GssPrincipal object from the user's login name. The login name must e identifiable in the web application and UrlZone to which the specified site belongs
    /// </summary>
    /// <param name="site">SharePoint Site Collection whose context is to be used for constructing the prinicpal</param>
    /// <param name="login">user login name for which the prinicipal is to be created</param>
    /// <returns></returns>
    public GssPrincipal GetGssPrincipalForSecPolicyUser(SPSite site, string login)
    {
        if (null == site || null == login)
        {
            return null;
        }
        GssPrincipal gssPrincipal = null;
        SPPrincipalInfo userInfo = SPUtility.ResolvePrincipal(site.WebApplication, site.Zone, login, SPPrincipalType.All, SPPrincipalSource.All, false);
        if (null == userInfo)
        {
            gssPrincipal = new GssPrincipal(login, -2);
            gssPrincipal.AddLogMessage("[ " + login + " ] could not be resolved a valid windows principal. ");
            gssPrincipal.Type = GssPrincipal.PrincipalType.NA;
            return gssPrincipal;
        }

        // There is no concept of ID for security policy users. IDs are an offset defined in context of a site collection and policies are defined at web application level
        gssPrincipal = new GssPrincipal(userInfo.LoginName, -2);

        if (userInfo.PrincipalType.Equals(SPPrincipalType.DistributionList) || userInfo.PrincipalType.Equals(SPPrincipalType.SecurityGroup))
        {
            gssPrincipal.Type = GssPrincipal.PrincipalType.DOMAINGROUP;
        }
        return gssPrincipal;
    }

    /// <summary>
    /// Check if the two ISecurable objects are same
    /// </summary>
    /// <param name="secObj1"> First Object </param>
    /// <param name="secObj2"> Second Object </param>
    /// <returns></returns>
    public bool isSame(ISecurableObject secObj1, ISecurableObject secObj2)
    {
        if (secObj1 is SPWeb && secObj2 is SPWeb)
        {
            SPWeb web1 = (SPWeb)secObj1;
            SPWeb web2 = (SPWeb)secObj2;
            if (null != web1 && null != web2 && web1.ID.Equals(web2.ID))
            {
                return true;
            }
        }
        else if (secObj1 is SPList && secObj2 is SPList)
        {
            SPList list1 = (SPList)secObj1;
            SPList list2 = (SPList)secObj2;
            if (null != list1 && null != list2 && list1.ID.Equals(list2.ID))
            {
                return true;
            }
        }
        else if (secObj1 is SPListItem && secObj2 is SPListItem)
        {
            SPListItem listItem1 = (SPListItem)secObj1;
            SPListItem listItem2 = (SPListItem)secObj2;
            if (null != listItem1 && null != listItem2 && listItem1.ID.Equals(listItem2.ID))
            {
                return true;
            }
        }
        return false;
    }
}


