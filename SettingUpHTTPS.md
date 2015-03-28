# Setting up HTTPS to an Offboard Connector Manager #

The Connector Manager supports using HTTPS requests from the Google Search Appliance. This is most important when using File System Connector 3.0 and later, as document contents are sent to the Google Search Appliance differently than connectors in the past. For the File System Connector 3.0, the Google Search Appliance issues standard HTTP/HTTPS requests to the Connector Manager and uses a client-certificate for authentication. Thus, for those contents to be encrypted as sent across the network:
  1. Your Tomcat instance must provide HTTPS
  1. The GSA’s certificate must be valid
  1. The GSA should trust Tomcat’s certificate if **Server Certificate Authentication** is enabled in the [GSA’s SSL Configuration](https://developers.google.com/search-appliance/documentation/614/help_gsa/admin_SSL#server_cert)
  1. Tomcat should trust the GSA’s certificate and ‘want’ the client certificate
  1. You must register the Connector Manager instance via HTTPS

Steps 1-3 are generic to most HTTPS configurations. Step 4 is generic to HTTPS configurations when client certificates are used. Step 5 is a Connector Manager-specific configuration step.

## Enabling HTTPS in Tomcat ##

Enabling HTTPS in Tomcat requires having a certificate with its corresponding private key loaded into a Java keystore. The certificate must be for the hostname of the Tomcat instance that will be used when connecting.

There are two possible types of certificates you can use: Certificate Authority-signed certificates and self-signed certificates. Each type of certificate will be covered here, but you may wish to see [Tomcat’s SSL documentation](http://tomcat.apache.org/tomcat-6.0-doc/ssl-howto.html) for more information. In addition, this documentation only covers Tomcat as configured by default by the connector installer.

### Certificate Authority-Signed Certificate ###

If you wish to use a Certificate Authority-signed certificate, you first obtain a signed certificate from the Certificate Authority of your choice. After communicating with the Certificate Authority you should eventually have a signed certificate, a private key, and the Certificate Authority’s chain.

You can use OpenSSL to create a new PKCS12 keystore (at `/path/to/keys.p12`) from the signed certificate `cmcert.crt` and the private key `cmcert.key`:
```
openssl pkcs12 -export -in cmcert.crt -inkey cmcert.key \
        -out /path/to/keys.p12 -password pass:changeit -name cm
```

You can then convert that PKCS12 keystore to Java’s KeyStore (KJS) using the `keytool` command provided by Java. This command will create a new JKS keystore (at `/path/to/keys.jks`) for use with Tomcat:
```
keytool -importkeystore -srckeystore /path/to/keys.p12 \
        -srcstoretype PKCS12 -srcstorepass changeit -alias cm \
        -destkeystore /path/to/keys.kjs \
        -deststorepass changeit -destkeypass changeit
```

You must also add the Certificate Authority chain to the trust store. Copy the default trusted certificates located in your JRE’s `lib/security/cacerts` to a new file of your choosing `/path/to/cacerts.jks`. You can then import the certificate chain `cachain.crt`:
```
keytool -importcert -trustcacerts -keystore /path/to/cacerts.jks \
        -storepass changeit -file cachain.crt -alias cachain
```

### Self-Signed Certificate ###

To generate a self-signed certificate, use the `keytool` command provided by Java. This command will create a new keystore (at `/path/to/keys.jks`) and create a self-signed certificate and private key that is valid for 365 days:
```
keytool -genkeypair -keystore /path/to/keys.jks -keyalg RSA -validity 365 \
        -storepass changeit -keypass changeit -alias cm
```

For “What is your first and last name?”, you should enter the hostname of the Tomcat instance. You are free to answer the other questions however you wish. When you are happy with yours answers, answer “yes” to “Is CN=cmhostname, OU=... correct?”

Since you are likely also using a self-signed certificate for the Google Search Appliance, in preparation for a future step copy the default trusted certificates located in your JRE’s lib/security/cacerts to a new file of your choosing `/path/to/cacerts.jks`.

### Enabling HTTPS ###

Now that the certificate and private keys are ready, Tomcat needs to be configured to use them. Add the following `<Connector>` configuration within the `<Service>` section of Tomcat’s `conf/server.xml`:
```
<Connector port="8443" protocol="HTTP/1.1" SSLEnabled="true"
        maxThreads="150" scheme="https" secure="true"
        sslProtocol="TLS" keystoreFile="/path/to/keys.jks"
        keystorePass="changeit" truststoreFile="/path/to/cacerts.jks"
        truststorePass="changeit"/>
```


## Configuring a Valid Certificate on the GSA ##

The certificate installed by default on the GSA is not valid for the hostname you use to access the GSA. To install a valid certificate for your GSA’s hostname, see [Uploading an Externally Generated Private Key and Certificate](https://developers.google.com/search-appliance/documentation/614/help_gsa/admin_SSL#External) or [Requesting and Installing a Certificate Using the Admin Console](https://developers.google.com/search-appliance/documentation/614/help_gsa/admin_SSL#UIprocess).

If you generated a self-signed certificate on the GSA, then you will need to download the certificate for later Tomcat configuration. To get the GSA’s certificate:
  * Using Firefox: Navigate to the GSA's secure search: `https://gsahostname/`. You should see a warning page that says, "This Connection is Untrusted." This message is because the certificate is self-signed and not signed by a trusted Certificate Authority. Click, "I Understand the Risks" and "Add Exception." Wait until the "View..." button is clickable, then click it. Change to the "Details" tab and click "Export...". Save the certificate using the save dialog. You can then hit "Close" and "Cancel" to close the certificate dialog windows.
  * Using Chrome: Navigate to the GSA's secure search: `https://gsahostname/`. You should see a warning page that says, "The site's security certificate is not trusted!" In the location bar, there should be a pad lock with a red 'x' on it. Click the pad lock and then click "Certificate Information." Change to the "Details" tab and click "Export...". Save the certificate using the save dialog. You can then hit "Close" and "Cancel" to close the certificate dialog windows.
  * Using OpenSSL in Linux: Execute with a command line:<br><code>openssl s_client -connect gsahostname:443 &lt; /dev/null</code><br>Copy the section that begins with -----BEGIN CERTIFICATE----- and ends with -----END CERTIFICATE----- (including the BEGIN and END CERTIFICATE portions) into a new file. That new file contains the certificate.</li></ul>


## Configuring the GSA to Trust Tomcat’s Certificate ##

This step is optional if **Server Certificate Authentication** is disabled in the [GSA’s SSL Configuration](https://developers.google.com/search-appliance/documentation/614/help_gsa/admin_SSL#server_cert).

This step is generally unnecessary if Tomcat is using a certificate signed by a Certificate Authority. Depending on the Certificate Authority used to sign the certificate, you may need to add the Certificate Authority to the GSA as seen at [Setting Server Certificates for Crawler Authentication](https://developers.google.com/search-appliance/documentation/614/help_gsa/admin_SSL#server_cert).

If you used a self-signed certificate for Tomcat (one not issued by a Certificate Authority), then you need to configure the GSA to trust that certificate. This can be performed by:
  1. Go to the **Administration > Certificate Authorities** page.
  1. Upload the public certificate for your Tomcat as a Certificate Authority.


## Configuring Tomcat to Request and Trust the GSA’s Certificate ##

Since client certificates are to be used, Tomcat needs to be configured to request them. This can be achieved by setting clientAuth to “want” in Tomcat’s `conf/server.xml` for the `<Connector>` with HTTPS enabled:
```
<Connector port="8443" protocol="HTTP/1.1" SSLEnabled="true"
        maxThreads="150" scheme="https" secure="true"
        sslProtocol="TLS" keystoreFile="/path/to/keys.jks"
        keystorePass="changeit" truststoreFile="/path/to/cacerts.jks"
        truststorePass="changeit" clientAuth="want"/>
```

If the GSA is using a certificate signed by a Certificate Authority, then this step is generally complete. Depending on the Certificate Authority used to sign the certificate, you may need to add the Certificate Authority’s root certificate to the trust store for Tomcat. If the GSA is using a self-signed certificate, then you must add the GSA’s certificate to the trust store for Tomcat.

Import the Google Search Appliance’s certificate or your Google Search Appliance’s Certificate Authority’s root certificate `gsa.crt` into the already-existing `/path/to/cacerts.jks` created earlier with:
```
keytool -importcert -trustcacerts -keystore /path/to/cacerts.jks \
        -storepass changeit -file gsa.crt -alias gsa
```

Tomcat should already be configured to use `/path/to/cacerts.jks` due to previous steps.


## Enabling HTTPS for Communicating to the Connector Manager ##

After you restart Tomcat due to the conf/server.xml changes, you can now [register your Connector Manager](https://developers.google.com/search-appliance/documentation/614/help_gsa/connector_manager#define) over HTTPS. For the **Service URL**, you should use the HTTPS protocol and port 8443 (as seen in the `conf/server.xml config`). It should be in the form: “`https://cmhostname:8443/connector-manager`”.