## Introduction ##

The Connector Manager maintains a selection of important Connector
related data in a Persistent Store.  The persistently stored data
includes the Connector's configuration, traversal schedule, and
portions of its traversal state.

In early versions of Connector Manager, this information was kept
in system preferences.  In recent versions, this information is
kept in files in the file system of the computer hosting the
Connector Manager.  In future versions, this information will be
maintained in a database.

Once this data resides in a database, it becomes less directly
available to the developer or administrator for examination and
troubleshooting.

A new DumpConnectors command line utility built into the Connector
Manager allows the connector developer or administrator to dump
the information stored in the database to a more easily readable
XML file.


## Usage ##

```
usage: Manager DumpConnectors [-?] [-v] <output_filename>
       -?, --help                        Display this help.
       -v, --version                     Display version.
       -c, --connector <connector_name>  Connector to export.
       output_filename                   Name of output XML file.
```

One or more connectors to export may be specified using `--connector`
options.  If unspecified, the configurations for all connectors are written
to the output file.

The DumpConnectors utility is built into the Connector Manager's
`connector.jar` file and is run by executing the `Manager`
script found in the Connector installation's `Scripts` folder:
```
% cd /path/to/connector/installation/Scripts
% ./Manager DumpConnectors /tmp/connectors.xml
```

This creates a XML file, `connectors.xml`, which contains the stored
persistent state for all Connector instances, including configuration,
traversal schedule, and traversal checkpoint.


---

_Since Connector Manager v3.0_