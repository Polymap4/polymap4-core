#!/bin/sh

DIRNAME=`dirname $0`
WORKSPACE=$DIRNAME/workspace
PORT=8080

#export JAVA_HOME=/usr/local/jrockit-R27.2.0-jdk1.6.0/
#export JAVA_HOME=/usr/local/jdk1.6.0_17/

# JAI
#export JAIHOME=/home/falko/packages/jai-1_1_2_01/lib
#export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$JAIHOME

# defaults to english, translations are used as they are are requested by the browser
# and they are installed as Eclipse Babel packages
export LANG=en_US.UTF-8

cd $DIRNAME/bin
export SUN_VM='-server -XX:MaxPermSize=128M -XX:NewRatio=2 -XX:+UseConcMarkSweepGC -XX:SoftRefLRUPolicyMSPerMB=50'
export VMARGS='-Xverify:none $SUN_VM -Xmx512M -Dorg.eclipse.rwt.compression=true'
export ARGS='-console -consolelog -registryMultiLanguage'
#export LOGARGS='-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog'
#export PROXY='-Dhttp.proxyHost=someHost -Dhttp.proxyPort=somePort -Dhttp.proxyUser=someUserName -Dhttp.proxyPassword=somePassword'

./eclipse $ARGS -data $WORKSPACE -vmargs $VMARGS $PROXY -Dorg.osgi.service.http.port=$PORT $LOGARGS