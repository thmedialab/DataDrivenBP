#!/usr/bin/python2
# -*- coding: utf-8 -*-
# This script plots both signals of one record to visualize and inspect the signals. argv[1] should be the rcd file name
# eg. ./BPrecordparser.py records/2016-03-31-09-02-01-XXX.rcd
import ConfigParser
import matplotlib.pyplot as plt
import sys


def main(argv):
    rcd = ConfigParser.ConfigParser()
    rcd.readfp(open(str(argv)))

    camTimeArray = rcd.get("cam_PPG", "TimeStampArray")
    camTimeArrayTokened = camTimeArray.split(',')
    camTimeArrayTokened.pop()
    camTimeArrayTokened = map(int, camTimeArrayTokened)

    print camTimeArrayTokened
    print len(camTimeArrayTokened)

    camAmpArray = rcd.get("cam_PPG", "AmpArray")
    camAmpArrayTokened = camAmpArray.split(',')
    camAmpArrayTokened.pop()
    camAmpArrayTokened = map(float, camAmpArrayTokened)
    print camAmpArrayTokened
    print len(camAmpArrayTokened)
    # print length
    # outputfile=''
    oxiTimeArray = rcd.get("oxi_PPG", "TimeStampArray")
    oxiTimeArrayTokened = oxiTimeArray.split(',')
    oxiTimeArrayTokened.pop()
    oxiTimeArrayTokened = map(int, oxiTimeArrayTokened)

    print oxiTimeArrayTokened
    print len(oxiTimeArrayTokened)

    oxiAmpArray = rcd.get("oxi_PPG", "AmpArray")
    oxiAmpArrayTokened = oxiAmpArray.split(',')
    oxiAmpArrayTokened.pop()
    oxiAmpArrayTokened = map(float, oxiAmpArrayTokened)
    oxiAmpArrayTokened = map(int, oxiAmpArrayTokened)
    print oxiAmpArrayTokened
    print len(oxiAmpArrayTokened)
    Fig = plt.figure()
    ax = Fig.add_subplot(211)
    ax.plot(camTimeArrayTokened, camAmpArrayTokened, marker='o')
    bx = Fig.add_subplot(212)
    bx.plot(oxiTimeArrayTokened, oxiAmpArrayTokened, marker='o')

    plt.show()


if __name__ == "__main__":
    main(sys.argv[1])
