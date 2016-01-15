#
#  Author: Hari Sekhon
#  Date: 2015-10-06 14:02:35 +0100 (Tue, 06 Oct 2015)
#
#  vim:ts=4:sts=4:sw=4:noet
#
#  https://github.com/harisekhon/lib-java
#
#  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help improve or steer this or other code I publish
#
#  http://www.linkedin.com/in/harisekhon
#

.PHONY: make
make:
	mvn clean package

.PHONY: clean
clean:
	mvn clean

.PHONY: update
update:
	git pull
	make

.PHONY: p
p:
	mvn package
.PHONY: package
package:
	mvn package

.PHONY: test
test:
	mvn test
	tests/travis.sh

.PHONY: tld
tld:
	wget -O src/main/resources/tlds-alpha-by-domain.txt http://data.iana.org/TLD/tlds-alpha-by-domain.txt
