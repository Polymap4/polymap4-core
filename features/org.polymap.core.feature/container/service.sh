#!/bin/sh
#
# Start POLYMAP3 as a service.
# This script depends on the PolymapServiceController to actually
# start/stop/control the service. The controller support log file
# rotation, status check, ...
#
# Copy this script to /etc/init.d/<SERVICENAME>
#
### BEGIN INIT INFO
# Provides: polymap3_work
# Required-Start: $network
# Required-Stop: $network
# Default-Start: 3 5
# Default-Stop: 0 1 2 6
# Description: Start a POLYMAP3 service instance
### END INIT INFO

# Change this to be the install dir of POLYMAP3 
POLYMAPDIR=`dirname $0`
# The user to start this service for
USER=`whoami`

EXE=$POLYMAPDIR/start.sh
SERVICENAME=Polymap3
LOG=$POLYMAPDIR/logs/$SERVICENAME.log

java -jar $POLYMAPDIR/PolymapServiceController.jar -exe $EXE -serviceName $SERVICENAME -log $LOG -user $USER $1 