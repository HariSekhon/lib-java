#
#  Author: Hari Sekhon
#  Date: 2015-10-06 14:02:35 +0100 (Tue, 06 Oct 2015)
#
#  vim:ts=4:sts=4:sw=4:noet
#
#  https://github.com/harisekhon
#
#  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help improve or steer this or other code I publish
#
#  http://www.linkedin.com/in/harisekhon
#

make:
	mvn clean package

clean:
	mvn clean

update:
	git pull
	make

p:
	mvn package
package:
	mvn package

tld:
	wget -O src/main/resources/tlds-alpha-by-domain.txt http://data.iana.org/TLD/tlds-alpha-by-domain.txt
