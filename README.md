Usage: `nomad:deploy`. Plugin is *not* bind to any lifecycle phase by default (rather it is expected to be used as a part of release process), but it could be bind as usual, using `<execution>` block 

Nomad needs artifact to be downloaded from some repository, it could not be deployed right from the target folder. 
Therefore, artifact is expected to be deployed to the repository first, and project `distributionManagement.downloadUrl` have to be set.
Plugin constructs download URL using `distributionManagement.downloadUrl`, `groupId`, `artifactId`, `version` and `classifier` from the project POM. 
For SNAPSHOT artifacts, it downloads and parses maven-metadata.xml to get the latest uploaded version 

Task name and task group name will be set to artifactId. These Nomad environment variables will be used:

Variable         | Default value         | Description
-----------------|-----------------------|-----------------------------------------------
NOMAD_ADDR       | http://127.0.0.1:4646 | URL of the HTTP API
NOMAD_REGION     |                       | Region  
NOMAD_NAMESPACE  | default               | Job namespace
NOMAD_CA_CERT    |                       | Path to [TLS](https://www.nomadproject.io/guides/security/securing-nomad.html)  Authority certificate file  
NOMAD_CLIENT_CERT|                       | Path to client certificate file
NOMAD_CLIENT_KEY |                       | Path to client key file
NOMAD_TOKEN      |                       | [ACL token](https://www.nomadproject.io/guides/security/acl.html)
  
Additionally, you can set these parameters in the `configuration` block:

Variable         | Default value         | Description
-----------------|-----------------------|-----------------------------------------------
addr             | from NOMAD_ADDR       | Overrides API address
options          |                       | Java options, passed to nomad java driver to start the job
datacenters      | dc1                   | List of data centers, divided by comma
port             | http                  | Label for the [dynamic port allocation](https://www.nomadproject.io/docs/job-specification/network.html#dynamic-ports)
env              |                       | `<env><key1>value1</key1>...</env>` passed as a set of [environment variables](https://www.nomadproject.io/docs/job-specification/env.html)
meta             |                       | `<meta><key1>value1</key1>...</meta>` passed as [meta information](https://www.nomadproject.io/docs/job-specification/meta.html)  

Configuration example:

```xml
            <plugin>
                <groupId>com.github.vantonov1</groupId>
                <artifactId>nomad-maven-plugin</artifactId>
                <version>1.0-SNAPSHOT</version>
                <configuration>
                    <addr>http://nomad.local:4646</addr>
                    <datacenters>west,east</datacenters>
                    <options>-Xmx1G</options>
                    <port>web</port>
                    <env>
                        <DB.ADDRESS>localhost:3389</DB.ADDRESS>
                    </env>
                    <meta>
                        <k1>v1</k1>
                    </meta>
                </configuration>
            </plugin>
```