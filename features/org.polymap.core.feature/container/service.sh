#!/bin/sh
#
# Start POLYMAP3 as a service.
# This script depends on the PolymapServiceController to actually
# start/stop/control the service. The controller support log file
# rotation, status check, ...
#
# Copy this script to /etc/init.d/<SERVICENAME>

# Change this to be the install dir of POLYMAP3 
POLYMAPDIR=`dirname $0`

EXE=$POLYMAPDIR/start.sh
SERVICENAME=Polymap3
LOG=$POLYMAPDIR/logs/$SERVICENAME.log

java -jar $POLYMAPDIR/PolymapServiceController.jar -exe $EXE -serviceName $SERVICENAME -log $LOG $1 