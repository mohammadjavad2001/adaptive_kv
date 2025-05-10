#!/bin/sh

cd ..
# for each subject
for SBJCT_DIR in ./subjects/eidentifier/;do
    # parse .dot files to .kiss format
    for sul_fname in $SBJCT_DIR/*.dot; do
        java -cp ./scripts/mylearn.jar br.usp.icmc.labes.mealyInference.utils.Dot2Kiss -sul $sul_fname;
        echo "Parsed $sul_fname to .kiss format!"
    done

    # create header of the comparison file  
    echo "ModelRef|ModelUpdt|Precision|Recall|F-measure:ModelRef|ModelUpdt|Precision|Recall|F-measure" > "$SBJCT_DIR/comparison.log"
    # compare all pairs of SULs
    for i in $SBJCT_DIR/*.kiss; do
        for j in $SBJCT_DIR/*.kiss; do
            java -cp ./scripts/learnFFSM.jar uk.le.ac.compare.CompareFSMs $i $j >> "$SBJCT_DIR/comparison.log"
        done
        echo "Comparison of $i against all other versions has finished!"
    done
    exit
done