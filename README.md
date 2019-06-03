Usage: `nomad:deploy`. Plugin is *not* bind to any lifecycle phase by default (rather it is expected to be used as a part of release process), but it could be bind as usual, using `<execution>` block 

Nomad needs artifact to be downloaded from some repository, it could not be deployed right from the target folder. 
Therefore, artifact is expected to be deployed to the repository first, and project `distributionManagement.downloadUrl` have to be set.
Plugin constructs download URL using `distributionManagement.downloadUrl`, `groupId`, `artifactId`, `version` and `classifier` from the project POM. 
For SNAPSHOT artifacts, it downloads and parses maven-metadata.xml to get the latest uploaded version 

Task name and task group name will be set to artifactId. These Nomad environment variables will be used:

Variable         | Default value         | Description
-----------------|-----------------------|-----------------------------------------------
NOMAD_ADDR       | http://127.0.0.1:4646 | URL of the Nomad HTTP API
NOMAD_REGION     |                       | Nomad region  
NOMAD_NAMESPACE  | default               | Job namespace
NOMAD_CA_CERT    |                       | Path to TLS Authority certificate file (see https://www.nomadproject.io/guides/security/securing-nomad.html) 
NOMAD_CLIENT_CERT|                       | Path to client certificate file
NOMAD_CLIENT_KEY |                       | Path to client key file
NOMAD_TOKEN      |                       | HTTP API token (see https://www.nomadproject.io/guides/security/acl.html)
  
Additionally, you can set these parameters in the `configuration` block:

Variable         | Default value         | Description
-----------------|-----------------------|-----------------------------------------------
addr             | from NOMAD_ADDR       | Override of API address
options          |                       | Java options, passed to nomad java driver to start the job
datacenters      | dc1                   | List of data centers, divided by comma