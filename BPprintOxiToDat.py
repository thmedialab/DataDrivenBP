#!/usr/bin/python2
# -*- coding: utf-8 -*-
# oximeter PPG signal is collected at a fixed frequency of 20 Hz.
# This script prints the array of oximeter signal from path sys.argv[1] to filename sys.argv[2]
# eg. ./BPprintOxiToDat.py records oxi.dat

import ConfigParser
import glob
import os.path
import sys


def printOxi(path, outfile):
    owd = os.getcwd()
    os.chdir(str(path).decode("UTF-8"))
    # files_in_dir=sorted(glob.glob('*.rcd'))
    files_in_dir = glob.glob('*.rcd')
    outStr = ''
    for rcd_file in files_in_dir:
        print rcd_file
        rcd = ConfigParser.ConfigParser()
        rcd.readfp(open(rcd_file.decode("UTF-8")))
        oxiAmpArray = rcd.get("oxi_PPG", "AmpArray")
        oxiAmpArrayTokened = oxiAmpArray.split(',')
        oxiAmpArrayTokened.pop()
        oxiAmpArrayTokened = map(float, oxiAmpArrayTokened)
        oxiAmpArrayTokened = map(int, oxiAmpArrayTokened)
        for item in oxiAmpArrayTokened:
            outStr = outStr + str(item) + ' '
        outStr += '\n'
    os.chdir(owd)
    with open(str(outfile), "w") as text_file:
        text_file.write("%s" % outStr)


if __name__ == "__main__":
    printOxi(sys.argv[1], sys.argv[2])
