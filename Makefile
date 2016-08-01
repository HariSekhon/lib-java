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

SHELL=/bin/bash

.PHONY: build
build:
	make gradle

# used by CI
.PHONY: random-build
random-build:
	@a[0]=build; a[1]=mvn; a[2]=gradle; a[3]=sbt; rand=$$[ $$RANDOM % 4 ]; r=$${a[$$rand]}; echo make $$r; make $$r

.PHONY: common
common:
	git submodule init
	git submodule update --recursive

.PHONY: mvn
mvn:
	make common
	./mvnw clean install
	@#ln -sfv target/harisekhon-utils-*.jar harisekhon-utils.jar

# don't use SBT - it will bundle Scala ballooning the jar size
.PHONY: sbt
sbt:
	make common
	@#                 .m2     .ivy
	sbt clean assembly publish publishLocal
	@#ln -sfv target/scala-*/harisekhon-utils-assembly-*.jar harisekhon-utils.jar

.PHONY: gradle
gradle:
	make common
	@#              .m2     .ivy
	./gradlew clean install uploadArchives
	@#ln -sfv build/libs/harisekhon-utils-*.jar harisekhon-utils.jar

.PHONY: clean
clean:
	./mvnw clean || :
	sbt clean || :
	./gradlew clean || :
	@#rm -vf harisekhon-utils.jar

.PHONY: deep-clean
deep-clean:
	make clean
	rm -rf .gradle ~/.gradle/{caches,native,wrapper} ~/.m2/{repository,wrapper} ~/.ivy2 ~/.sbt/boot

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
	make gradle-sonar

.PHONY: gradle-sonar
gradle-sonar:
	@# calls compileJava
	./gradlew sonarqube

.PHONY: mvn-sonar
mvn-sonar:
	./mvnw sonar:sonar

.PHONY: sonar-scanner
sonar-scanner:
	sonar-scanner

.PHONY: test
test:
	./mvnw test
	bash-tools/all.sh

.PHONY: tld
tld:
	wget -O src/main/resources/tlds-alpha-by-domain.txt http://data.iana.org/TLD/tlds-alpha-by-domain.txt

.PHONY: findbugs
findbugs:
	./mvnw compile
	./mvnw findbugs:findbugs
	./mvnw findbugs:gui
