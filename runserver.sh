#!/bin/sh

java -classpath lib/*:bin/. edu.upenn.cis.cis455.webserver.HttpServer 8080 /home/cis555/git/555-hw1 conf/web.xml
