#!/bin/sh

cd ..

# merge log files of each SUL
grep "ModelRef|ModelUpdt|Precision|Recall|F-measure" ./results/*/comparison.log | cut -d: -f3 > ./results/comparisons.tmp

# create header of the comparison file  
echo "ModelRef|ModelUpdt|Precision|Recall|F-measure" > ./results/comparisons.tab

# append lines of the log file with measures (and discard their headers)
grep -v "ModelRef|ModelUpdt|Precision|Recall|F-measure" ./results/comparisons.tmp >> ./results/comparisons.tab

# delete tmp file
rm ./results/comparisons.tmp
rm ./results/*/comparison.log
rm -rf ./results/*/