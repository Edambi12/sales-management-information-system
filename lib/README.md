# `lib/` — third-party libraries

The application depends on a single library: **MySQL Connector/J** (JDBC driver).
JAR files are not committed to Git (`lib/*.jar` is ignored).

## Option A — Eclipse (no Maven)

1. Download **MySQL Connector/J 9.6.0** (platform independent ZIP) from
   <https://dev.mysql.com/downloads/connector/j/> or from Maven Central:
   <https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.6.0/>.
2. Copy `mysql-connector-j-9.6.0.jar` into this `lib/` folder.
3. Refresh the project in Eclipse (`F5`). The `.classpath` already references
   `lib/mysql-connector-j-9.6.0.jar`.

Using another 8.x/9.x version? Either rename the JAR or update the path in
`.classpath` (*Project → Properties → Java Build Path → Libraries*).

## Option B — Maven

Nothing to download manually: `mvn package` fetches the driver declared in
`pom.xml` and copies it to `target/lib/`.

> History: in the original Eclipse project the driver was referenced through an
> absolute Windows path (`C:/Soktrapa ETUDE DE CAS/mysql-connector-j-9.6.0/...`).
> It was replaced with this project-relative path so the project builds on any machine.
