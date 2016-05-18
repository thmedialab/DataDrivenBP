#!/usr/bin/python2
# -*- coding: utf-8 -*-
# This script prints the array of systolic blood pressure, diastolic blood pressure, heart rate, age, gender
# from path sys.argv[1] to filename sys.argv[2]
# eg. ./BPprintOxiBP.py records bp.dat
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
    index = 1
    for rcd_file in files_in_dir:
        print rcd_file
        rcd = ConfigParser.ConfigParser()
        rcd.readfp(open(rcd_file.decode("UTF-8")))
        systolic = rcd.get("bp", "Systolic")
        diastolic = rcd.get("bp", "Diastolic")
        hr = rcd.get("bp", "HeartRate")
        age = rcd.get("profile", "Age")
        gender = rcd.get("profile", "Gender")
        outStr += str(systolic) + ' ' + str(diastolic) + ' ' + str(hr) + ' ' + str(age) + ' ' + str(gender)
        outStr += '\n'
        index += 1
    os.chdir(owd)
    with open(str(outfile), "w") as text_file:
        text_file.write("%s" % outStr)


if __name__ == "__main__":
    printOxi(sys.argv[1], sys.argv[2])
