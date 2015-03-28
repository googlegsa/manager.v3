# Introduction #

Newer releases of the Google Search Appliance require that
Connector names contain only lower case alphabetic letters.
Numerical digits, dashes, and underscores are still allowed,
with the previously documented limitations, however upper case
letters should no longer be used.


# Details #


Newer releases of the Google Search Appliance (GSA) require that
Connector names contain only lower case alphabetic characters.
Numerical digits, dashes, and underscores are still allowed,
with the previously documented limitations, however upper case
alphabetics should no longer be used.  Problems using upper
case characters in Connector names first appeared in GSA
version 5.0.4.G22, with showing only minor inconsistencies
in crawl diagnostics.  In GSA version 5.2, however, search
authorizations fail.

At this time, the new lower case connector name limitation
is not enforced when creating Connectors on the GSA
_Connector Administration_ page.

Connector Manager version 1.3.0 makes small concessions to
this issue.  New Connectors have their name
lower cased at the time of creation.  This may be somewhat
visually confusing, as the Connector's name may appear in
mixed case or upper case while filling out the _Connector
Configuration_ forms on the GSA, but once the form is submitted
the resulting connector name will be converted to lower case.

The Connector Manager does not change the case
of existing Connectors or migrate existing Connectors to
the new lower case form.  Doing so would lead to inconsistent
search results and make previously indexed content inaccessible.

If you have existing Connector instances with mixed case or
upper case names, you should change them before upgrading to
GSA version 5.2, or all existing non-public content fed from
that Connector will become unsearchable.

Correcting the problem requires creating a new Connector
instance and re-indexing all the documents.  The old Connector
should be removed.  It can be removed before re-indexing to
conserve resources, or it can be removed after re-indexing
so that previously indexed content may continue to be served
while re-indexing takes place.  This document describes the steps
involved for each strategy.


## Re-Indexing on a GSA running version 5.0.4 ##

Verify that your GSA v5.0.4 has VM Patch #1 installed.
This contains a fix that improves feed and indexing performance.
Login to the GSA _Version Manager_ (http://appliance_hostname:9941/).
**Current Software Version** should show **5.0.4.G22 (or G24) VM patch #1**.
If your GSA has not been patched, you should follow the instructions at the
[VM Patch #1 update page](https://support.google.com/enterprise/doc/gsa/update_inst/patch_update_for_504G22_24.html).

GSA version 5.2 does not require this patch.


## If Deleting the Connector First ##

This is the appropriate choice if re-indexing the content
does not take very much time, or if you have already updated
your GSA to software version 5.2.  This approach is simpler
to execute and is especially convenient if resetting (deleting)
the index is an option.

1) Make a note of the Connector Configuration as shown in the
_Google Search Appliance > Connector Administration > Connectors > Add/Edit_
page for the Connector instance.

2) If you have a customized `connectorInstance.xml` file, found in
`$TOMCAT_HOME/webapps/connector-manager/WEB_INF/connectors/$CONNECTOR_TYPE/$CONNECTOR_NAME/connectorInstance.xml`,
make a backup copy of that file outside of the Connector instance directory.
  * $TOMCAT\_HOME is the Tomcat installation location for the connector.
  * $CONNECTOR\_TYPE is the type of the Connector (Livelink\_Enterprise\_Server, etc.)
  * $CONNECTOR\_NAME is the name of the Connector instance.

3) Delete the Connector instance using the form
_Google Search Appliance > Connector Administration > Connectors > Delete_.

4) Manually delete the Connector instance directory, if it still exists:
`$TOMCAT_HOME/webapps/connector-manager/WEB_INF/connectors/$CONNECTOR_TYPE/$CONNECTOR_NAME`.
Deleting the connector directory is especially important in Microsoft Windows
and Mac OS X, which have case-insensitive file systems.

5) Remove the Feeds from the old Connector.
Go to _Google Search Appliance > Crawl and Index > Feeds_.
Delete all **Feeds** originating from the old Connector.

6) Go to _Google Search Appliance > Crawl and Index > Collections_.
If any **Collections** contain the Connector name in the
**Include Content Matching the Following Patterns:** or
**Do Not Include Content Matching the Following Patterns:**
sections, replace the old (mixed case or upper case) name with the
new (lower case) name.

7) Go to _Google Search Appliance > Crawl and Index > Crawl URLs_.
If any **Crawl URLs**  (or **Do Not Crawl URLs**) contain the Connector
name, replace the old (mixed case or upper case) name with the
new (lower case) name.  **Note:** It is not usually the case that
the Crawl URLs or Do Not Crawl URLs have embedded connector
names, but it is possible.

8) If this GSA only indexed content from Connectors that are
being replaced (no web crawling, no correctly named connectors, etc),
as configured in
_Google Search Appliance > Crawl and Index > Crawl URLs_
then consider deleting the existing index using
_Google Search Appliance > Administration > System Settings > Reset Index_.

9) Create a new connector with the configuration copied down
in step 1).  If you saved a custom `connectorInstance.xml` in step 2),
create the connector with an inactive schedule (1am - 1am), otherwise
create the connector with the desired (or default) schedule and you
are done.

10) Copy the saved `connectorInstance.xml` into the new Connector
instance directory:
`$TOMCAT_HOME/webapps/connector-manager/WEB_INF/connectors/$CONNECTOR_TYPE/$CONNECTOR_NAME`

11) Go to
_Google Search Appliance > Connector Administration > Connectors > Add/Edit_
and change the schedule to the desired (or default 12am - 12am) schedule.



## If Deleting the Old Connector After Re-indexing ##

This option allows you to keep serving up previously indexed
content while re-indexing that content with the new connector.
This is appropriate if you are still running GSA v5.0.4 and
indexing the full content takes considerable time.  One downside
to this approach is that while both connectors are running,
duplicate search results may be seen (one hit for each connector).

1) Choose a new name for the Connector that is distinctly
different from the old name.  The new name should not simply
be a lowercase version of the old name, especially on operating
systems with case-insensitive file systems (like Windows and Mac OS X).

2) Go to
_Google Search Appliance > Crawl and Index > Collections_.
If any Collections contain the old Connector name in the
**Include Content Matching the Following Patterns:** or
**Do Not Include Content Matching the Following Patterns:**
sections, create an equivalent entry for the new Connector.

3) Go to
_Google Search Appliance > Crawl and Index > Crawl URLs_.
If any **Crawl URLs** (or **Do Not Crawl URLs**) contain the old Connector
name, create an equivalent entry for the new Connector.
**Note:** It is not usually the case that the Crawl URLs or
Do Not Crawl URLs have embedded connector names,
but it is possible.

4) Make a note of the Connector Configuration as shown in the
_Google Search Appliance > Connector Administration > Connectors > Add/Edit_
page for the existing Connector instance.

5) Create a new connector with the configuration copied down
in step 4).  If the old connector has a custom `connectorInstance.xml`,
create the new connector with an inactive schedule (1am - 1am), otherwise
create the new connector with the desired (or default) schedule and go to
step 8).

6) If the old Connector has a customized `connectorInstance.xml` file, found in
`$TOMCAT_HOME/webapps/connector-manager/WEB_INF/connectors/$CONNECTOR_TYPE/$CONNECTOR_NAME/connectorInstance.xml`,
copy that `connectorInstance.xml` file from the old Connector instance
directory to the new Connector instance directory.
  * $TOMCAT\_HOME is the Tomcat installation location for the connector.
  * $CONNECTOR\_TYPE is the type of the Connector (Documentum, Livelink, etc.)
  * $CONNECTOR\_NAME is the name of the Connector instance.

7) Enable the Schedule for the new Connector.  Go to
_Google Search Appliance > Connector Administration > Connectors > Add/Edit_
and change the schedule to the desired (or default 12am - 12am) schedule.

8) Delete the old Connector instance using the form
_Google Search Appliance > Connector Administration > Connectors > Delete_.

9) Manually delete the old Connector instance directory, if it still exists:
`$TOMCAT_HOME/webapps/connector-manager/WEB_INF/connectors/$CONNECTOR_TYPE/$CONNECTOR_NAME`

10) Go to
_Google Search Appliance > Crawl and Index > Collections_.
If any **Collections** contain the old Connector name in the
**Include Content Matching the Following Patterns:** section,
remove the old Connector from the Collection.

11) Go to
_Google Search Appliance > Crawl and Index > Crawl URLs_.
If any **Crawl URLs** contain the old Connector name, remove
that URL.

12) Remove content fed by the old Connector from the index.
Go to _Google Search Appliance > Crawl and Index > Crawl URLs_
and add a new **Do Not Crawl** pattern for the old Connector.
For instance, if the old Connector was named "OldConnector",
then you would add a Do Not Crawl pattern "^googleconnector://OldConnector"

13) Remove the Feeds from the old Connector.
Go to _Google Search Appliance > Crawl and Index > Feeds_ and
delete all **Feeds** originating from the old Connector.