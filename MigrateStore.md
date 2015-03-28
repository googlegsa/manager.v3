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

A new MigrateStore command line utility built into the Connector
Manager allows an administrator to migrate data maintained in
one persistent store mechanism to another.

For users upgrading existing Connector deployments, the MigrateStore
utility allows them to transfer Connector information stored in
the file system to the new database Persistent Store (or back,
if necessary).  It will also allow you to migrate data from one
database store to another; for instance, when switching from the
embedded database to an external corporate database.

## Usage ##

```
usage: Manager MigrateStore [-?] [-v] [-l] [-c connector] [source_name] [dest_name]
       -?, --help            Display this help.
       -v, --version         Display version.
       -l, --list            List available PersistentStores.
       -f, --force           Overwrite existing data in destination PersistentStore.
       -c, --connector <connector_name>  Connector to migrate.
       source_name           Name of source PeristentStore (e.g. FilePersistentStore)
       dest_name             Name of destination PeristentStore (e.g. JdbcPersistentStore)
```
The `--list` option lists the available Persistent Stores as
identified by Bean name in the Connector Manager's
`applicationContext.xml`.

The `--force` option will allow data from the source store to
overwrite any existing data in the destination store.  By
default, existing data in the destination is preserved.

One or more connectors to migrate may be specified using `--connector`
options.  If unspecified, all connectors are migrated.

If you do not include the source and destination Persistent
Stores on the command line, you will be prompted to choose
from the available stores.

The MigrateStore utility is built into the Connector Manager's
`connector.jar` file and is run by executing the `Manager`
script found in the Connector installation `Scripts` folder:
```
% cd /path/to/connector/installation/Scripts
% ./Manager MigrateStore FilePersistentStore JdbcPersistentStore
```

## Upgrading Existing Connector Deployments ##

If you upgrade an existing file system Persistent Store-based
Connector deployment with a newer database Persistent Store-based
Connector Manager, at first it will appear as if your deployment
"lost" all it's Connector instances.  You will need to migrate
the persistently stored data from the old store to the new one.

If will be using an external database, such as Oracle or SQL Server,
you should first configure the Connector Manager's access to
that external database as indicated in the release notes.
If not using an external database, then Connector Manager will
already be configured to use an internal, embedded database.

## Migrating Connector Data using MigrateStore ##

On the Connector deployment machine, change directory to the
Connector installation `Scripts` directory and run MigrateStore:
```
% cd /path/to/connector/installation/Scripts
% ./Manager MigrateStore FilePersistentStore JdbcPersistentStore
```

This copies the data from the existing file system persistent store
to the new database persistent store.  The data in the file store
remains intact.

## Some Data is _Not_ Copied ##

At this time, MigrateStore copies the traversal checkpoints for
Connector instances, but **does not** migrate connector private data.
Specifically, the Sharepoint and FileSystem Connectors each maintain
a private file of data that is not copied.  Additional upgrade
instructions for these Connectors will be described in their Release
Notes or Wikis.

MigrateStore copies the configuration data for all the Connector
instances, but it does not migrate the Connector Manager's own
configuration (which includes the configuration of the Persistent
Stores itself).

## Modifying a Connector's `connectorInstance.xml` Configuration ##

MigrateStore is a useful tool to aid in making manual modifications
to a Connector's `connectorInstance.xml` configuration file when
that configuration is stored in a database rather than as plain
text files in the file system.

For details see the ModifyConnectorInstanceXml wiki page.

## Migrating Connector Data between Database Persistent Stores ##

**TODO** (Describe dual JdbcPersistentStore configurations.)


---

_Since Connector Manager v3.0_