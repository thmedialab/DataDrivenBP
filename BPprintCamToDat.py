#!/usr/bin/python2
# -*- coding: utf-8 -*-
# camera PPG signal is collected at a variable frequency.
# This script prints the array of camera signal from path sys.argv[1] to filename sys.argv[2]
# and the timestamp to filename sys.argv[3]
# eg. ./BPprintCamToDat.py records cam.dat camTime.dat

import ConfigParser
import glob
import os.path
import sys


def printCam(path, outfile, timeOutfile):
    owd = os.getcwd()
    os.chdir(str(path).decode("UTF-8"))
    # files_in_dir=sorted(glob.glob('*.rcd'))
    files_in_dir = glob.glob('*.rcd')
    outStr = ''
    outTimeStr = ''
    for rcd_file in files_in_dir:
        print rcd_file
        rcd = ConfigParser.ConfigParser()
        rcd.readfp(open(rcd_file.decode("UTF-8")))
        camAmpArray = rcd.get("cam_PPG", "AmpArray")
        camAmpArrayTokened = camAmpArray.split(',')
        camAmpArrayTokened.pop()
        camAmpArrayTokened = map(float, camAmpArrayTokened)
        camAmpArrayTokened = map(int, camAmpArrayTokened)

        camTimeStampArray = rcd.get("cam_PPG", "TimeStampArray")
        camTimeStampArrayTokened = camTimeStampArray.split(',')
        camTimeStampArrayTokened.pop()
        camTimeStampArrayTokened = map(int, camTimeStampArrayTokened)

        for item in camAmpArrayTokened:
            outStr = outStr + str(item) + ' '
        outStr += '\n'

        for item in camTimeStampArrayTokened:
            outTimeStr = outTimeStr + str(item) + ' '
        outTimeStr += '\n'

    os.chdir(owd)
    with open(str(outfile), "w") as text_file:
        text_file.write("%s" % outStr)

    with open(str(timeOutfile), "w") as text_file:
        text_file.write("%s" % outTimeStr)


if __name__ == "__main__":
    printCam(sys.argv[1], sys.argv[2], sys.argv[3])
