#!/usr/bin/env python

import sys
from subprocess import Popen,PIPE,call
import re

INTF="wlan0"

def runCmd(c):
    retcode = call(cmd, shell=True)
    if retcode < 0:
        print >>sys.stderr, "Child was terminated by signal", -retcode
        sys.exit(1)
    elif retcode!=0:
        print >>sys.stderr, "Child returned", retcode
        sys.exit(1)

print "Detecting AR Drone"

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

