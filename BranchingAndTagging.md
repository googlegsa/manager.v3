# Introduction #

This document will outline the branching and tagging strategy used in the Connector Manager Code Site SVN repository to manage releases of the project.  It is recommended that all Connector Code Sites follow the same procedure.

# Overview #

In general, we try to keep the _trunk_ of the repository open for continued and new development.  As we near a release point, we will branch the _trunk_ at a well known revision.  Branch names will be of the form 1.0.x.  The branch is not tied to a specific release of the project, but it will be the source for future 1.0._something_ releases such as 1.0.0 or 1.0-stable.

After the branch is created, bug fixes and new development will continue to be done in the _trunk_.  Any changes that need to be integrated into the released version will first be merged into the release candidate branch.  No development or bug fixing should be done on the branch - only merges from the _trunk_.

When it has been determined that a new release is needed, the accumulated merges in the release candidate branch will be tagged.  The tags will be named for the specific release of the project such as 1.0.0, 1.0.1.  These tags represent specific releases of the project and will be used to build the downloads.

# Details #

The first step is to select a revision of the _trunk_ that is stable and a good canidate for release.  This revision will basically be copied over into the release candidate branch during the branching.

For example, for the Connector Manager project, [Revision 641](https://code.google.com/p/google-enterprise-connector-manager/source/detail?r=641) of the _trunk_ was selected as the initial release point for 1.0.x.  The easiest method to branch is to do it directly in the repository.  Using this technique, a branch was created in the Connector Manager repository using the following command:

```
svn copy -r641 -m "Creating 1.0.x release candidate branch" https://google-enterprise-connector-manager.googlecode.com/svn/trunk https://google-enterprise-connector-manager.googlecode.com/svn/branches/1.0.x
```

After this command completes, in your repository, you should see `branches/1.0.x`, and it should contain a copy of the contents of the _trunk_.

NOTE: If your repository contains more than one project under _trunk_ you might want to maintain separate branch folders as well.

The overall work flow is as follows:

  1. All changes and bug fixes will continue to be done on the _trunk_
  1. Any change that has been approved as being needed for the 1.0.x release will be merged from the _trunk_ into the _1.0.x_ branch.
  1. At some point, after merges have been submitted to the branch and a new release is needed, a tag will be created associated with the specific release (e.g. 1.0.1)
  1. Any downloads such as the WAR and JAR files will be created from the tag and put up on the Downloads page.

The rest of this section will cover working with the Connector Manager release candidate branch.

## Checkout the Branch ##

The first thing you will need to is get a working copy of the branch.  This works like checking out other parts of the repository.

```
$ svn checkout https://google-enterprise-connector-manager.googlecode.com/svn/branches/1.0.x google-enterprise-connector-manager-branch-1.0.x
A    google-enterprise-connector-manager-branch-1.0.x/COPYING
A    google-enterprise-connector-manager-branch-1.0.x/projects
[...]

Checked out revision 642.
```

Now you are ready to merge changes from the _trunk_ into the branch.

## Merging Changes Into the Branch ##

Let's say we initially branched the _trunk_ from [Revision 641](https://code.google.com/p/google-enterprise-connector-manager/source/detail?r=641) and there's now a change on the _trunk_ at [Revision 655](https://code.google.com/p/google-enterprise-connector-manager/source/detail?r=655) that we need to merge.  We do this using the `svn merge` command.  Assuming you checked out the branch from the root directory as shown above, you must first set the current working directory to be the root of your branched working copy.  Therefore, if you have checked out your branch to `~/svnbench/google-enterprise-connector-manager-branch-1.0.x`:

```
$ cd ~/svnbench/google-enterprise-connector-manager-branch-1.0.x
/home/user/svnbench/google-enterprise-connector-manager-branch-1.0.x
```

It's best to preview the merge before actually preforming the command.  It's possible that your local working copy might already have local modifications.  The changes applied by a merge will be mixed with your pre-existing ones and can't be undone with `svn revert`.

One simple way to do this is to run `svn diff` with the same arguments you plan to pass to `svn merge`.  Another method of previewing is to pass the `--dry-run` option to the merge command:

```
$ svn diff -c 655 https://google-enterprise-connector-manager.googlecode.com/svn/trunk
Index: projects/connector-manager/source/java/com/google/enterprise/connector/servlet/GetConnectorInstanceList.java
===================================================================
[...]
Index: projects/connector-manager/source/java/com/google/enterprise/connector/servlet/ConnectorManagerServlet.java
===================================================================
[...]

# or

$ svn merge --dry-run -r 641:655 https://google-enterprise-connector-manager.googlecode.com/svn/trunk
U   projects/connector-manager/source/java/com/google/enterprise/connector/servlet/GetConnectorInstanceList.java
U   projects/connector-manager/source/java/com/google/enterprise/connector/servlet/ConnectorManagerServlet.java
```

Once you are satisfied that the merge will affect the proper files and won't cause any unexpected conflicts you can execute the `svn merge` command with the same arguments as above:

```
$ svn merge -r 641:655 https://google-enterprise-connector-manager.googlecode.com/svn/trunk
U    projects/connector-manager/source/java/com/google/enterprise/connector/servlet/GetConnectorInstanceList.java
U    projects/connector-manager/source/java/com/google/enterprise/connector/servlet/ConnectorManagerServlet.java

$ svn status
M      projects/connector-manager/source/java/com/google/enterprise/connector/servlet/GetConnectorInstanceList.java
M      projects/connector-manager/source/java/com/google/enterprise/connector/servlet/ConnectorManagerServlet.java
```

## Submitting Changes to the Branch ##

At this point you use your normal code review process to review the changes to your working copy of the branch before submitting.  Once this is done you commit your branch changes to the repository:

```
$ pwd
/home/user/svnbench/google-enterprise-connector-manager-branch-1.0.x

$ svn commit 
Sending        projects/connector-manager/source/java/com/google/enterprise/connector/servlet/ConnectorManagerServlet.java
Sending        projects/connector-manager/source/java/com/google/enterprise/connector/servlet/GetConnectorInstanceList.java

Submitted 656.
```


## Creating a Tag and Building the Download Files ##

Once you have a set of merges that have been submitted on the branch that you want to release, a tag needs to be created.  A tag is a "snapshot" of a project in time.  It allows you to give a specific name to the tag and associate it with a release.  Otherwise, you would have to remember that release 1.0.1 is [Revision 656](https://code.google.com/p/google-enterprise-connector-manager/source/detail?r=656) of branch 1.0.x.  This snapshot of the branch will be used to preserve and build the release to be copied over to the Downloads tab.

Once again, this can be done in the repository using `svn copy`.

```
$ svn copy -r656 -m "Tagging the 1.0.1 Release" https://google-enterprise-connector-manager.googlecode.com/svn/branches/1.0.x https://google-enterprise-connector-manager.googlecode.com/svn/tags/1.0.1

Committed revision 657.
```


Once this is done, the new `1.0.1/` directory is forever a snapshot of how the project looked in the branch at the time you made the copy.  It's always a good idea to specify the revision of the branch in case someone else has committed changes.

Now the tag should be checked out to build the install files that are to be copied over to the Downloads tab.  This is done just like the branch.

```
$ svn checkout https://google-enterprise-connector-manager.googlecode.com/svn/tags/1.0.1 google-enterprise-connector-manager-1.0.1
A    google-enterprise-connector-manager-1.0.1/COPYING
A    google-enterprise-connector-manager-1.0.1/projects
[...]
Checked out revision 657.
```

Once this is done, you can build the install files that need to be copied over to the Downloads tab.  See the DeveloperEnvironmentSetup page for details.

## Guidelines related to Branching and Tagging for Connector Releases ##
  * All the new feature development MUST take place on trunk<sup>1</sup>
  * Whenever we are about to make any release e.g. 2.8.0 ; create a     branch off the trunk called 2.8.x
  * The SVN will look like
    * trunk
    * branches
      * 2.8.x
  * This 2.8.x branch will always have the latest code for any 2.8 release
  * When we want to make an RC for 2.8.0 we tag the 2.8.x branch  e.g. 2.8.0-RC1.
  * This tag location will be tags/builds/2.8.0-RC1.
  * "builds" will be a folder under tags which will maintain all the internal tags (RC, milestones, custom etc) for the project.
  * So the new SVN structure will become
    * trunk
    * branches
      * 2.8.x
    * tags
      * builds
        * 2.8.0-RC1
        * 2.8.0-RC2 (if there is an RC2)
  * Dev will share the URL of the 2.8.0-RC1 tag with QA for the RC
  * Once we feel that 2.8.0-RC2 is the final build, we tag the 2.8.x branch with new tag called 2.8.0
  * This 2.8.0 tag will go directly in tags. Final release tags will feature directly under 'tags' and will NOT go under 'tags/builds'
  * So the SVN structure will become
    * trunk
    * branches
      * 2.8.x
    * tags
      * builds
        * 2.8.0-RC1
        * 2.8.0-RC2
      * 2.8.0
  * Once we release 2.8 and want to move on with 2.10 release a new branch will be created off the trunk called 2.10.x
  * After we release 2.8, if there are any patches, then the bug will be fixed on Trunk first and then down integrated into 2.8.x branch
  * The major releases will move from 2.8 to 2.10 to 2.12 and so on...  any 2.8.x release after 2.8.0 will only be a patch on top of the 2.8.0 build and will not be a major release.

  1. The only time we will directly check in code in the release branches is when the code on trunk has become very different from the branch code and we want to patch a particular release. This should be an exceptional scenario and not a general practice.


## For More Information ##

See:

  * [Version Control with Subversion](http://svnbook.red-bean.com/)