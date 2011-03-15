#!/bin/sh

DIRNAME=`dirname $0`

#export JAVA_HOME=/usr/local/jrockit-R27.2.0-jdk1.6.0/
#export JAVA_HOME=/usr/local/jdk1.6.0_17/

# JAI
#export JAIHOME=/home/falko/packages/jai-1_1_2_01/lib
#export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$JAIHOME

# defaults to english, translations are used as they are are requested by the browser
# and they are installed as Eclipse Babel packages
export LANG=en_US.UTF-8

VMARGS="-Xverify:none -XX:+UseParallelGC -Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog -Dorg.eclipse.rwt.compression=true"
WORKSPACE=~/servers/workspace

cd $DIRNAME/bin
./eclipse -vm $JAVA_HOME/bin/java -console -consolelog -registryMultiLanguage -data $WORKSPACE -vmargs $VMARGS
