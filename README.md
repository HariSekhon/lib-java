Hari Sekhon Java Library
========================
[![Build Status](https://travis-ci.org/HariSekhon/lib-java.svg?branch=master)](https://travis-ci.org/HariSekhon/lib-java)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/b929aef71cb34ede9decf79459aa936d)](https://www.codacy.com/app/harisekhon/lib-java)
[![Coverage Status](https://coveralls.io/repos/HariSekhon/lib-java/badge.svg?branch=master&service=github)](https://coveralls.io/github/HariSekhon/lib-java?branch=master)
[![Dependency Status](https://www.versioneye.com/user/projects/57616cdb0a82b20053182c74/badge.svg)](https://www.versioneye.com/user/projects/57616cdb0a82b20053182c74)
[![Platform](https://img.shields.io/badge/platform-Linux%20%7C%20OS%20X-blue.svg)](https://github.com/harisekhon/lib-java#hari-sekhon-java-library)
[![DockerHub](https://img.shields.io/badge/docker-available-blue.svg)](https://hub.docker.com/r/harisekhon/centos-github/)

My personal Java library, full of lots of validation code and utility functions.

Hari Sekhon

Cloud & Big Data Contractor, United Kingdom

(ex-Cloudera, former Hortonworks Consultant)

https://www.linkedin.com/in/harisekhon
###### (you're welcome to connect with me on LinkedIn)

#### Build ####

Builds with any one of Maven, Gradle or SBT. The Maven and Gradle builds are best as they will auto-download their own build systems of the correct compatible version for you without you having to pre-install them.

The default build will trigger a Gradle build which requires no pre-installed dependencies other than Java. This is preferred because of the self-bootstrap but the Gradle mechanism additionally has an embedded checksum for security:
```
make
```

You can call any one of the 3 major build systems explicitly instead:

Maven:
```
make mvn
```

Gradle:
```
make gradle
```

SBT:
```
make sbt
```

#### Testing

[Continuous Integration] is run on this repo to build and unit test it (around 400 JUnit tests).

You can launch tests manually by running this command at the top level of the repo:

```
make test
```

#### Configuration ####

Strict validations include host/domain/FQDNs using TLDs which are populated from the official IANA list, a snapshot of which is shipped as part of this project.

To update the bundled official IANA TLD list with the latest valid TLDs do
```
make tld
```
##### Custom TLDs #####

If using bespoke internal domains such as `.local`, `.intranet`, `.vm`, `.cloud` etc. that aren't part of the official IANA TLD list then this is additionally supported via a custom configuration file in [src/main/resources](https://github.com/HariSekhon/lib-java/tree/master/src/main/resources) called [custom_tlds.txt](https://github.com/HariSekhon/lib-java/blob/master/src/main/resources/custom_tlds.txt) containing one TLD per line, with support for # comment prefixes. Just add your bespoke internal TLD to the file and it will then pass the host/domain/fqdn validations.

#### See Also ####

* [Python version of this library](https://github.com/harisekhon/pylib)
* [Perl version of this library](https://github.com/harisekhon/lib)

* [DevOps Python Tools](https://github.com/harisekhon/devops-python-tools) - 80+ DevOps CLI tools for AWS, Hadoop, HBase, Spark, Log Anonymizer, Ambari Blueprints, AWS CloudFormation, Linux, Docker, Spark Data Converters & Validators (Avro / Parquet / JSON / CSV / INI / XML / YAML), Elasticsearch, Solr, Travis CI, Pig, IPython

* [The Advanced Nagios Plugins Collection](https://github.com/harisekhon/nagios-plugins) - 450+ programs for Nagios monitoring your Hadoop & NoSQL clusters. Covers every Hadoop vendor's management API and every major NoSQL technology (HBase, Cassandra, MongoDB, Elasticsearch, Solr, Riak, Redis etc.) as well as message queues (Kafka, RabbitMQ), continuous integration (Jenkins, Travis CI) and traditional infrastructure (SSL, Whois, DNS, Linux)

* [DevOps Bash Tools](https://github.com/harisekhon/devops-bash-tools) - 100+ DevOps Bash scripts, advanced `.bashrc`, `.vimrc`, `.screenrc`, `.tmux.conf`, `.toprc`, Utility Code Library used by CI and all my GitHub repos - includes code for AWS, Kubernetes, Kafka, Docker, Git, Code & build linting, package management for Linux / Mac / Perl / Python / Ruby / Golang, and lots more random goodies

* [DevOps Perl Tools](https://github.com/harisekhon/perl-tools) - 25+ DevOps CLI tools for Hadoop, HDFS, Hive, Solr/SolrCloud CLI, Log Anonymizer, Nginx stats & HTTP(S) URL watchers for load balanced web farms, Dockerfiles & SQL ReCaser (MySQL, PostgreSQL, AWS Redshift, Snowflake, Apache Drill, Hive, Impala, Cassandra CQL, Microsoft SQL Server, Oracle, Couchbase N1QL, Dockerfiles, Pig Latin, Neo4j, InfluxDB), Ambari FreeIPA Kerberos, Datameer, Linux...

* [HAProxy-configs](https://github.com/harisekhon/haproxy-configs) - 80+ HAProxy Configs for Hadoop, Big Data, NoSQL, Docker, Elasticsearch, SolrCloud, HBase, Cloudera, Hortonworks, MapR, MySQL, PostgreSQL, Apache Drill, Hive, Presto, Impala, ZooKeeper, OpenTSDB, InfluxDB, Prometheus, Kibana, Graphite, SSH, RabbitMQ, Redis, Riak, Rancher etc.

* [Dockerfiles](https://github.com/HariSekhon/Dockerfiles) - 50+ DockerHub public images for Docker & Kubernetes - Hadoop, Kafka, ZooKeeper, HBase, Cassandra, Solr, SolrCloud, Presto, Apache Drill, Nifi, Spark, Mesos, Consul, Riak, OpenTSDB, Jython, Advanced Nagios Plugins & DevOps Tools repos on Alpine, CentOS, Debian, Fedora, Ubuntu, Superset, H2O, Serf, Alluxio / Tachyon, FakeS3
