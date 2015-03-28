# Introduction #

This document discusses the naming and archiving conventions that will be observed for the download packages prepared for the Google Connector Framework projects.

# Details #

This policy will be used for naming and labeling the files to be downloaded.  In most cases, the files contained in the Downloads Tab will be archives.  Adding a file to the Downloads Tab will require the declaration of the following:

  * Summary: This is a one-line summary of the file.  Something like "Google Connector Manager Binary Distribution - English Only".
  * File: For the Connectors, there will be three types of files in the Downloads area.  For each type, there will be both a ZIP archive and a TAR.GZ archive.  **Important:** Each file must have a distinct name and file contents cannot be changed, so be sure to include release numbers in each file name.  The three types of archives are as follows:
    1. Archive containing the binary files for all languages
    1. Archive containing the binary files for just English
    1. Archive containing the source files only
  * Labels: A series of labels can be attached to each file.

See discussion related to the Installer for information about what goes into the archive and how it should be structured.

The following policy will be used to name the file:

> ## `<name>-<version>[-src][_en].<extension>` ##

Where:

  * name = Connector Name (i.e. connector-otex, connector-dctm, ...)
  * version = major.minor.micro (i.e. 1.0.1, ...).  Each released version should have a representative tag directory in the **`tags/`** section of the repository.  'Major' will coincide with releases of the Google Search Appliance.  Right now **`major=1`** is schedule to work with Google Search Appliance **`major=5`**.  Each partner can control the 'minor' and 'micro' parts.  'Minor' should change when there are new features, new ECMS versions are supported, or compatibility with other modules has changed.  'Micro' should change when there are bug fixes. For example, patch releases that are available from code site's download tab but not included in the installer.
  * **`-src`** = added for source distributions
  * **`_en`** = added for English only distributions.  A similar convention can be used if we distribute for other languages separately.  If the **`_en`** is not present then it is assumed to support all languages.
  * extension = **`zip`** for zip files and **`tar.gz`** for gzip tar files

Note, in order to be able track versions and changes, please add the version information to the manifest file in connector-xxx.jar, and connector-manager-xxx.war.

Initially, each archive will be labeled with 3 of the following labels:

  1. Featured - This means the download should be listed on the project home page.
  1. Type-Archive - Use this with Binary distributions.  This means the user should download, unarchive, and follow directions.
  1. Type-Source - Use this with Source Code distributions.  This indicates it is a source code archive.
  1. OpSys-All - Since this is java, it should work for all operating systems.

## Examples: ##

  1. connector-manager-1.0.1\_en.tar.gz - Connector Manager 1.0.1 binary English only gzipped tar file.
  1. connector-manager-1.0.1-src.zip - Connector Manager 1.0.1 source files in zip archive.
  1. connector-otex-1.0.0.tar.gz - OpenText Connector 1.0.0 binary all language gzipped tar file.
  1. connector-otex-1.0.0-src.tar.gz - OpenText Connector 1.0.0 source only gzipped tar file.
  1. connector-dctm-1.0.1\_en.zip - Documentum Connector 1.0.1 binary English only zip file.
  1. connector-file-1.0.1.zip - FileNet Connector 1.0.1 binary all language zip file.

## Maintaining Releases ##

  * Case 1: The new release is a compatible bug fix release.  In this case, the new release should be tagged as _Featured_ and the previous release should be tagged as _Deprecated_.  See instructions below.
  * Case 2: The new release contains new functionality that is not backward compatible.  In this case, the new release should be tagged as _Featured_ and the previous release should not be tagged.  Simply remove the _Featured_ tag from the old release, however, do not tag it as _Deprecated_.

## Deleting Downloads ##

Any officially supported releases should not be deleted unless they have reach end of life and we need to make room for new releases.  Developer releases and early access releases can be deleted when they are replaced with a final release.

Before deleting a download it should be marked as _Deprecated_:

  1. Go to the Downloads Tab
  1. Search until you find the download you want to deprecate.
  1. Select the download Summary link
  1. In the lower left hand corner you should see a "Click to edit download" link.  Click on this.
  1. In the **Labels:** section look for the label edit box containing the current status and change it to _Deprecated_.
  1. Click the "Submit Changes" button

To delete a download:

  1. Go to the Downloads Tab
  1. Search the "Deprecated Downloads" for the download to be deleted.
  1. Select the download Summary link
  1. At the top next to the "Search" button you should see a "Delete" link - click that link
  1. Click the "Delete Download" button

# Signature #

There's no need to add a signature file to the Downloads Tab.  When the file is uploaded to the Code Site it should present a SHA1 signature for the file.  Users that download the file can click on the Summary to view the SHA1 checksum.