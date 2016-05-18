# DataDrivenBP

This is the repository sharing research code of paper "Data-Driven Estimation of Blood Pressure Using Photoplethysmographic Signals"

## BP PPG Collector

Building the app under directory `kiwi_bpppg_collector_studio` with Android Studio and running this Android app would record the user index file `users.idx` and record csv files with pattern `DATE-NAME.rcd` under the directory `BPrecord` inside the storage.

## Python scripts
The Python scripts with name pattern  `BP*.py` are used to extract information from BP record `.rcd` files for futre Mathematica processing:

`BPstats.py`  prints some statistics of the ages, heart rate and blood pressure. usage:

    ./BPstats.py records

`BPrecordparser.py` plots both signals of one record to visualize and inspect the signals. `argv[1]`` should be the rcd file name. usage:

    ./BPrecordparser.py records/2016-03-31-09-02-01-XXX.rcd

`BPprintOxiBP.py` prints the array of systolic blood pressure, diastolic blood pressure, heart rate, age, gender from path `sys.argv[1]` to filename `sys.argv[2]`. usage:

    ./BPprintOxiBP.py records bp.dat

This script prints the array of camera signal from path `sys.argv[1]` to filename `sys.argv[2]` and the timestamp to filename `sys.argv[3]`. usage:

    ./BPprintCamToDat.py records cam.dat camTime.dat

This script prints the array of oximeter signal from path `sys.argv[1]` to filename `sys.argv[2]`. usage:

     ./BPprintOxiToDat.py records oxi.dat
