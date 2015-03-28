# Introduction #


In order to debug a Tomcat war file, there are two things you need to setup:  1) tomcat, and 2) your debugging client.


# Details #

BTW, this description is based on linux.  Developers using other platforms: please ContactUs if you find any platform-specific gotchas.

1) Start up tomcat with appropriate debug flags.

export JPDA\_ADDRESS=8000
export JPDA\_TRANSPORT=dt\_socket
$CATALINA\_HOME/bin/catalina.sh jpda start

2) Connect to tomcat with your debugging client.

Click on Run -> Debug...  and then go to "Remote Java Application".
If you right click on "Remote Java Application" and pick "New" configuration, you set up your new launch configuration.
Specify the tomcat host and port (this will be your JPDA\_ADDRESS or 8000 in my example).

Enjoy!

