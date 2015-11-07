Hari Sekhon Java Library [![Build Status](https://travis-ci.org/harisekhon/lib-java.svg?branch=master)](https://travis-ci.org/harisekhon/lib-java) [![Coverage Status](https://coveralls.io/repos/harisekhon/lib-java/badge.svg?branch=master&service=github)](https://coveralls.io/github/harisekhon/lib-java?branch=master)
========================

My personal Java library, full of lots of validation code and utility functions, ported from [my previous code and libraries in other languages](https://github.com/harisekhon/lib).

#### Build ####

Uses Maven as standard, just call
```
mvn clean package
```

Continuous Integration is run on this repo to build and unit test it (JUnit).

#### Configuration ####

Strict validations include host/domain/FQDNs using TLDs which are populated from the official IANA list, a snapshot of which is shipped as part of this project.

To update the bundled official IANA TLD list with the latest valid TLDs do
```
make tld
```
##### Custom TLDs #####

If using bespoke internal domains such as ```.local``` or ```.intranet``` that aren't part of the official IANA TLD list then this is additionally supported via a custom configuration file in ```src/main/resources``` called ```custom_tlds.txt``` containing one TLD per line, with support for # comment prefixes. Just add your bespoke internal TLD to the file and it will then pass the host/domain/fqdn validations.

#### See Also ####

* [Python version of this library](https://github.com/harisekhon/pylib)
* [Perl version of this library](https://github.com/harisekhon/lib)

* [Advanced Nagios Plugins Collection](https://github.com/harisekhon/nagios-plugins) - largest repo of monitoring code for Hadoop & NoSQL technologies, every Hadoop vendor's management API and every major NoSQL technology (HBase, Cassandra, MongoDB, Elasticsearch, Solr, Riak, Redis etc.) as well as traditional Linux and infrastructure
* [Tools](https://github.com/harisekhon/tools) - Hadoop, NoSQL, Hive, Solr, Ambari, Web, Linux
* [PyTools](https://github.com/harisekhon/pytools) - Hadoop, PySpark, IPython, Pig => Solr / Elasticsearch, Pig Jython UDFs, Ambari, Linux
* [Spark => Elasticsearch](https://github.com/harisekhon/spark-to-elasticsearch) - Spark Apps including ready built indexers from Spark => Elasticsearch
