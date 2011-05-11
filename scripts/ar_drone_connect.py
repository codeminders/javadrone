#!/usr/bin/env python

import sys,os
from subprocess import Popen,PIPE,call
import re,string

INTF="wlan0"

def runCmd(cmd):
    retcode = call(string.join(cmd, " "), shell=True)
    if retcode < 0:
        print >>sys.stderr, "Child was terminated by signal", -retcode
        sys.exit(1)
    elif retcode!=0:
        print >>sys.stderr, "Child returned", retcode
        sys.exit(1)

if os.getuid()!=0:
    print >>sys.stderr, "Not enough privilidges"
    sys.exit(1)

print "Detecting AR Drone"

runCmd(["/sbin/ifconfig", INTF ,"up"])
scanres = Popen(["/sbin/iwlist", INTF, "scan"], stdout=PIPE).communicate()[0]
SSID=None
cells = re.split(r'\s+Cell [0-9]+',scanres)
for c in cells:
    m = re.search(r'\s+ESSID\:"(ardrone_[0-9]+)',c)
    if m:
        SSID=m.group(1)
        m = re.search(r'\s+Channel\:([0-9]+)',c)
        if not m:
            print >>sys.stderr, "Could not detect channel!"
            sys.exit(1)
        CHANNEL = m.group(1)
        break

if SSID:
    print "Found Ar.Drone with SSID %s on channel %s" % (SSID,CHANNEL)
else:
    print >>sys.stderr, "Ar.Drone not found"
    sys.exit(1)

print "Configuring WiFi interface"

runCmd(["/sbin/ifconfig", INTF ,"up"])
runCmd(["/sbin/iwconfig", INTF ,"mode","ad-hoc"])
runCmd(["/sbin/iwconfig", INTF ,"channel",str(CHANNEL)])
runCmd(["/sbin/iwconfig", INTF ,"Bit","54Mb/s"])
runCmd(["/sbin/iwconfig", INTF ,"essid",SSID])

print "Connecting to Ar.Drone"

runCmd(["/sbin/dhclient", INTF])
runCmd(["/sbin/route", "delete", "default", "gw", "192.168.1.1"])

print "Checking connectivity"
runCmd(["/bin/ping", "-c3", "192.168.1.1"])

print "Ar.Drone is ready with IP 192.168.1.1" 
