#!/bin/bash

# get_jars.sh 
#
# assumes its being run from the ant directory
# depends on wget
#
# Chris Roeder 5/2009 original for BioNLP-UIMA
# Chris Roeder 1/2010 adapted to OpenDMAP


mkdir ../download 2> /dev/null
cd ../download
DOWNLOAD=`pwd`

get_file () {
	URL=$1
    	FILE=$2

	if  [[ ! -e $DOWNLOAD/$FILE ]]
	then
		HERE=`pwd`
		cd $DOWNLOAD
		wget $URL/$FILE
		RETVAL=$?
		cd $HERE
	
		return $RETVAL 
	else
		echo ""
		echo "not downloading $FILE, we already have it."
		echo ""
		return 0
	fi
	
}

check_its_type () {
	# basic goal to distinguish a real file from an error message
	FILE=$1
	TYPE=$2
	FILETYPE=`file $DOWNLOAD/$FILE`
	echo $FILETYPE | grep $TYPE
	RETVAL=$?
	if (( $RETVAL != 0 )) 
	then
		echo "$FILE is not a $TYPE file, removing and exiting. Run again and hope for a good download. $RETVAL"
		file $DOWNLOAD/$FILE
		echo "return is $?"
		rm $DOWNLOAD/$FILE
		exit 1
	fi
}

# ant
get_file http://archive.apache.org/dist/ant/binaries/ apache-ant-1.7.1-bin.tar.gz 
if (( $? != 0 )) ; then echo "Failed on ant"; exit 3; fi
check_its_type apache-ant-1.7.1-bin.tar.gz gzip

# log4j
get_file http://archive.apache.org/dist/logging/log4j/1.2.15/ apache-log4j-1.2.15.tar.gz 
if (( $? != 0 )) ; then echo "Failed on log4j"; exit 4; fi
check_its_type apache-log4j-1.2.15.tar.gz  gzip


# junit.jar www.junit.org
get_file  http://downloads.sourceforge.net/project/junit/junit/4.8.1/ junit-4.8.1.jar
if (( $? != 0 )) ; then echo "Failed on junit"; exit 17; fi
check_its_type junit-4.8.1.jar Zip

# commons-logging
get_file http://apache.securedservers.com/commons/logging/binaries/ commons-logging-1.1.1-bin.tar.gz
if (( $? != 0 )) ; then echo "Failed on commons logging"; exit 19; fi
check_its_type commons-logging-1.1.1-bin.tar.gz gzip

# commons-collections
get_file http://apache.securedservers.com/commons/collections/binaries/ commons-collections-3.2.1-bin.tar.gz
if (( $? != 0 )) ; then echo "Failed on commons collections"; exit 20; fi
check_its_type commons-collections-3.2.1-bin.tar.gz gzip

# colt
get_file http://acs.lbl.gov/~hoschek/colt-download/releases/ colt-1.2.0.tar.gz
if (( $? != 0 )) ; then echo "Failed on colt.jar"; exit 21; fi
check_its_type colt-1.2.0.tar.gz gzip

# jung
get_file http://downloads.sourceforge.net/project/jung/jung/jung-1.7.6/ jung-1.7.6.jar
if (( $? != 0 )) ; then echo "Failed on jung.jar"; exit 22;  fi
check_its_type jung-1.7.6.jar Zip

