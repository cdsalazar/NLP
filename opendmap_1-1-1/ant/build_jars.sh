#!/bin/bash

extract_jar () {
	TARGZFILE=$1
	JARPATH=$2
	
	HERE=`pwd`
	cd ../lib
	cp ../download/$TARGZFILE .
	gunzip -f $TARGZFILE
	echo $FILE
	TARFILE=`echo $TARGZFILE | sed -e "s/.gz//"`
	tar xvf $TARFILE $JARPATH
	rm $TARFILE

	DIR=`dirname $JARPATH`
        JAR=`basename $JARPATH`
	JARNAME=`find $DIR -name $JAR`
	mv $JARNAME .
	rm -rf $DIR
	cd $HERE
}

mkdir ../lib

extract_jar apache-ant-1.7.1-bin.tar.gz apache-ant-1.7.1/lib/ant.jar

extract_jar apache-log4j-1.2.15.tar.gz apache-log4j-1.2.15/log4j-1.2.15.jar

extract_jar colt-1.2.0.tar.gz colt/lib/colt.jar

extract_jar commons-collections-3.2.1-bin.tar.gz commons-collections-3.2.1/commons-collections-3.2.1.jar

extract_jar commons-logging-1.1.1-bin.tar.gz commons-logging-1.1.1/commons-logging-1.1.1.jar

cp ../download/jung-1.7.6.jar ../lib

cp ../download/junit-4.8.1.jar ../lib

