Hari Sekhon Java Library
========================
[![Build Status](https://travis-ci.org/HariSekhon/lib-java.svg?branch=master)](https://travis-ci.org/HariSekhon/lib-java)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/b929aef71cb34ede9decf79459aa936d)](https://www.codacy.com/app/harisekhon/lib-java)
[![Coverage Status](https://coveralls.io/repos/HariSekhon/lib-java/badge.svg?branch=master&service=github)](https://coveralls.io/github/HariSekhon/lib-java?branch=master)
[![Dependency Status](https://www.versioneye.com/user/projects/57616cdb0a82b20053182c74/badge.svg)](https://www.versioneye.com/user/projects/57616cdb0a82b20053182c74)
[![Platform](https://img.shields.io/badge/platform-Linux%20%7C%20OS%20X-blue.svg)](https://github.com/harisekhon/lib-java#hari-sekhon-java-library)
[![DockerHub](https://img.shields.io/badge/docker-available-blue.svg)](https://hub.docker.com/r/harisekhon/centos-github/)

My personal Java library, full of lots of validation code and utility functions.

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

* [Advanced Nagios Plugins Collection](https://github.com/harisekhon/nagios-plugins) - largest repo of monitoring code for Hadoop & NoSQL technologies, every Hadoop vendor's management API and every major NoSQL technology (HBase, Cassandra, MongoDB, Elasticsearch, Solr, Riak, Redis etc.) as well as traditional Linux and infrastructure
* [DevOps Perl Tools](https://github.com/harisekhon/devops-perl-tools) - 30+ tools for Hadoop, NoSQL, Solr, Elasticsearch, Pig, Hive, Web URL + Nginx stats watchers, SQL and NoSQL syntax recasers, various Linux CLI tools
* [DevOps Python Tools](https://github.com/harisekhon/devops-python-tools) - Hadoop, Spark (PySpark), Pig => Solr / Elasticsearch indexers, Pig Jython UDFs, Ambari Blueprints, AWS CloudFormation templates, HBase, Linux, IPython Notebook, Data converters between different data formats and syntactic validators for Avro, Parquet, CSV, JSON, YAML...
* [Spark => Elasticsearch](https://github.com/harisekhon/spark-to-elasticsearch) - Spark Apps including ready built indexers from Spark => Elasticsearch
