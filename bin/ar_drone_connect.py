#!/usr/bin/env python

from subprocess import Popen,PIPE,call
import re

INTF="wlan0"

def runCmd(c):
    retcode = call(cmd, shell=True)
    if retcode < 0:
        print >>sys.stderr, "Child was terminated by signal", -retcode
    elif retcode!=0:
        print >>sys.stderr, "Child returned", retcode

print "Detecting AR Drone"

scanres = Popen(["/sbin/iwlist", INTF, "scan"], stdout=PIPE).communicate()[0]
SSID=None
for l in scanres.split("\n"):
    m = re.search(r'ESSID\:"(ardrone_[0-9]+)',l)
    if m:
        SSID=m.group(1)
        break

if SSID:
    print "Found Ar.Drone with SSID %s" % SSID
else:
    print >>sys.stderr, "Ar.Drone not found"
    sys.exit(1)

