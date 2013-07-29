RM - Riksmedia monitor
Small Java application for monitoring Riksmedia ads on Aftenposten no.
------

Project creation:
$ mvn archetype:create -DgroupId=org.qnot.example -DartifactId=hello-world

$ mkdir -p src/main/resources/META-INF/
$ cd src/main/resources/META-INF/
$ echo 'Main-Class: org.qnot.example.App' > MANIFEST.MF

$ mkdir src/assemble
create file, exe.xml in above folder

Command to create jar file with associated jar files: mvn assembly:single

$ java -jar target/app-exe.jar

-----
Resources:
package executable                http://left.subtree.org/2008/01/24/creating-executable-jars-with-maven/
