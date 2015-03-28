# Introduction #

When a release candidate is signed off for release, the release candidate downloads are repackaged. The ZIP file names and the directory prefixes in the ZIP files are renamed. Repackaging is non-destructive; the original files are not modified.

# Details #

Repackaging replaces "`connector-foo-${svn.branch}`" with "`connector-foo-${version}`",
for example, "`connector-sharepoint-3.2.2-RC2`" with "`connector-sharepoint-3.2.2`". The `svn.branch` property is the name of the last directory in the Subversion branch for the working copy, for example, the value for "/svn/trunk" is "trunk", and the value for "/svn/tags/3.2.0" is "3.2.0". The `version` property is the value from the version.properties file. This version string may have an X or M build number, for example, "3.2.4-M1", but it should not have an RC build number, e.g., "~~3.2.4-RC1~~".

# Steps #

To repackage the downloads for a release candidate:
  1. Go to a checkout of the release candidate. **Do not use a checkout of the tag**
  1. Make sure the original release candidate downloads are in the downloads directory. **Do not recreate the downloads**
  1. Run "`ant repackage`"
  1. Upload the release builds