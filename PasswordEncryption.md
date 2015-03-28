## Introduction ##

Among the data that the connector manager stores for each connector is a username/password pair that is used by the connector to crawl a CMS. The password is encrypted using a symmetric key that is stored in a keystore. Specifically,every connector is specified by a list of properties (key-value pairs) and the value of any property whose key contains the substring "password" (case-insensitive) is encrypted.

## Details ##

The [Java Cryptographic Extension (JCE)](http://download.oracle.com/javase/1.5.0/docs/guide/security/jce/JCERefGuide.html) is used for encryption and storing keys. This allows you to plug-in your own JCE provider. Our implementation uses the default provider in the Sun Java Runtime v 1.5.0.

The parameters used to do this have sane defaults that work out of the box, but they can be over-ridden. The parameters are specified in the web.xml for the connector manager webapp.

The parameters are:

  * **Keystore file** specifies the filename in which the keystore is kept. The default is `connector_manager.keystore`. The associated parameter in `web.xml` is called `keystore_file`. This file will be created if it does not exist. If the `StandardContext` is being used and the `unpackWAR` is defaulted to `true` then the webapp will search for this file under its `WEB-INF` directory.  If the Servlet container cannot translate this path to a real path, then it will be treated as a virtual path.

  * The **keystore password file** contains the password used to secure the keystore. If the file does not exist, an empty password is used. The default filename is `keystore_passwd`. The associated parameter in web.xml is called `keystore_passwd_file`.

  * The **keystore encryption algorithm** specifies what symmetric encryption algorithm to use for encrypting passwords. The default is "AES". Your JCE provider must support the algorithm. The associated parameter in `web.xml` is called `keystore_crypto_algo`.

  * The **type of keystore** used by JCE can also be specified. The default is "JCEKS". Your JCE provider must support this type of keystore. The associated parameter in `web.xml` is called `keystore_type`.

These parameters are specified in the `<context-params>` section of `web.xml`. Here is an example:

```

<context-param>
      <param-name>keystore_type</param-name>
      <param-value>JCEKS</param-value>
      <description>
        The type of keystore to use. Your JCE provider must support it.
      </description>
</context-param>

```

The keystore password is specified in a file (as opposed to interactively). This has two advantages:
  * It does not preclude automated startup of the application (say after a reboot).
  * You can use scripts that perform security checks before writing the password to the file.

## EncryptPassword Command Line Application ##

Connector Manager versions 2.6.0 and later provide a command line utility, EncryptPassword, that can be used to encrypt passwords using the mechanism described here.