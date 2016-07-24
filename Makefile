#  vim:ts=4:sts=4:sw=4:noet
#
#  Author: Hari Sekhon
#  Date: 2015-10-06 14:02:35 +0100 (Tue, 06 Oct 2015)
#
#  vim:ts=4:sts=4:sw=4:noet
#
#  https://github.com/harisekhon/lib-java
#
#  License: see accompanying Hari Sekhon LICENSE file
#
#  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help improve or steer this or other code I publish
#
#  https://www.linkedin.com/in/harisekhon
#

.PHONY: build
build:
	make mvn

.PHONY: common
common:
	git submodule init
	git submodule update --recursive

.PHONY: mvn
mvn:
	make common
	./mvnw clean package
	cp -av target/harisekhon-utils-*.jar harisekhon-utils.jar

# don't use SBT - it will bundle Scala ballooning the jar size
.PHONY: sbt
sbt:
	make common
	@#sbt clean package
	sbt clean assembly
	cp -av target/scala-*/harisekhon-utils-assembly-*.jar harisekhon-utils.jar

.PHONY: gradle
gradle:
	make common
	./gradlew clean build
	cp -av build/libs/harisekhon-utils-*.jar harisekhon-utils.jar

.PHONY: clean
clean:
	./mvnw clean || :
	sbt clean || :
	gradle clean || :
	rm -f harisekhon-utils.jar

.PHONY: update
update:
	git pull
	git submodule update --init --recursive
	make

.PHONY: update-submodules
update-submodules:
	git submodule update --init --remote
.PHONY: updatem
updatem:
	make update-submodules

.PHONY: p
p:
	make package
.PHONY: package
package:
	./mvnw package

.PHONY: sonar
sonar:
	#mvn clean install
	./mvnw sonar:sonar
	# or
	# sonar-scanner

.PHONY: test
test:
	mvn test
	bash-tools/all.sh

.PHONY: tld
tld:
	wget -O src/main/resources/tlds-alpha-by-domain.txt http://data.iana.org/TLD/tlds-alpha-by-domain.txt
