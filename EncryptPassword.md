## A Command Line Utility to Encrypt Passwords ##

Passwords stored in the Connector Manager's `applicationContext.properties` file and Connector's `connectorName.properties` file are stored in an encrypted fashion.  The Connector Manager decrypts these passwords when it loads the properties file.   The stored passwords are typically credentials needed by the Connector to gain access to Repositories, Databases, File shares, etc.

The passwords are typically encrypted automatically when supplied as input to the GSA Administrator Console.  However there may be instances during Connector development, Connector installation, or system integration when an encrypted password may need to be generated when the Connector Manager or GSA is not running.

A new EncryptPassword command line utility built into the Connector Manager allows an administrator to generate encrypted passwords.

## Details ##

```
usage: Manager EncryptPassword [-?] [-v] [-p password]
       -?, --help      Display this help.
       -v, --version   Display version.
       -p, --password  Encrypt the supplied password.
```


The EncryptPassword utility is built into the Connector Manager's `connector.jar` file and is run by executing the `Manager` script found in the Connector installation `Scripts` folder supplying the `EncryptPassword` command. _(Connector Manager 3.0 and greater.)_

When run, EncryptPassword prompts for a password to be entered, asks for it a second time to ensure it was correctly entered, then prints the encrypted password to the output.

Here is an example EncryptPassword execution:

```
% cd /path/to/connector/installation/Scripts
% ./Manager EncryptPassword
EncryptPassword v3.0.0 (build 2640MP  October 31 2010)

  Type Password: ******
Retype Password: ******

The encrypted password is:
ohFzzOHRJ/ByZSG0QMn04g==
```

Note that the entered passwords are not echoed to the console.  Given the encrypted password, the administrator may then manually add the password property to the appropriate properties file.  The property name **must** include the substring 'password', or the property value will not be correctly decrypted when read.

Excerpt from a `.properties` file:

```
jdbc.datasource.user=...
jdbc.datasource.password=ohFzzOHRJ/ByZSG0QMn04g==
```

## Running EncryptPassword under Connector Manager v2.6 ##

An early version of EncryptPassword was available in Connector Manager v2.6, before the `Manager` scripts were included.

In Connector Manger v2.6, the EncryptPassword utility is built into the Connector Manager's connector.jar file and is run by executing the `connector.jar` file using the `java -jar` command, supplying EncryptPassword as it's first argument.

The EncryptPassword utility needs access to the Connector Manager's keystore configuration.  For that reason, the command **must** be run from the installed Connector Manager's web application `WEB-INF` directory, where it can extract the keystore configuration from the the `web.xml` file located there.

Like the Connector Manager proper, this command requires Java 1.5 or Java 6 to execute.

```
% cd /path/to/connector/installation/Tomcat/webapps/connector-manager/WEB-INF
% java -jar lib/connector.jar EncryptPassword
```


---

New in Connector Manager v2.6.0, supported by `Manager` script in Connector Manger v3.0.0.