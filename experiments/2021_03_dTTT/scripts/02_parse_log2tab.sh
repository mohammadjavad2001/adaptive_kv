#!/bin/sh

cd ..

# merge log files of each SUL
grep "ModelRef|ModelUpdt|Precision|Recall|F-measure" ./subjects/*/comparison.log | cut -d: -f3 > ./data/comparisons.tmp

# create header of the comparison file  
echo "ModelRef|ModelUpdt|Precision|Recall|F-measure" > ./data/comparisons.tab

# append lines of the log file with measures (and discard their headers)
grep -v "ModelRef|ModelUpdt|Precision|Recall|F-measure" ./data/comparisons.tmp >> ./data/comparisons.tab

# delete tmp file
rm ./data/comparisons.tmp