#!/bin/bash

AWIKI=~/GitHub/automata_wiki/
runID=$(date +"%Y%m%d_%H%M%S_%N")
totRep=1
eqo="wphyp"
clos="CloseFirst"

cd ..
# create folder for the random OTs
mkdir -p ./results/
run_path=./results/run_$runID/
mkdir $run_path

# repeat the experiment "totRep" times
for (( repNum = 1; repNum <= totRep; repNum++ )); do
    echo "Running experiment: Iteration $repNum"
    # and save the results in the folder "repNum"
    rep_path=$run_path/rep_$repNum/
    mkdir $rep_path
    # run the experments for each subject
    for SBJCT_DIR in $AWIKI/benchmarks/[a-zA-Z]*/;do
        subject_name=$(basename -- "$SBJCT_DIR")
        mkdir $rep_path/$subject_name
        subj_run_path=$rep_path/$subject_name/
        echo "Running experiment @ '$subject_name'"
        # for each version:
        for sul_i in $SBJCT_DIR/*.dot; do
            # ...setup the SUL model   
            sul_basename=$(basename -- "$sul_i")
            sul_fname=$subj_run_path/$sul_basename
            cp "$sul_i" "$sul_fname"
            java -cp ./scripts/pdlstarm.jar br.usp.icmc.labes.mealyInference.utils.BuildOT -dot -sul "$sul_i" -out $subj_run_path/ -mkot
            # # and run traditional learning
            echo "Running experiment @ '$subject_name': Traditional learning on $sul_basename"
            java -jar ./scripts/pdlstarm.jar -cache -cexh RivestSchapire -learn lstar -clos $clos -eq $eqo -dot -sul "$sul_fname" -out $subj_run_path/SUL_"${sul_basename%.*}"/lstar/ -info "rep=$repNum"
            java -jar ./scripts/pdlstarm.jar -cache -cexh RivestSchapire -learn l1    -clos $clos -eq $eqo -dot -sul "$sul_fname" -out $subj_run_path/SUL_"${sul_basename%.*}"/l1/    -info "rep=$repNum"
        done
        # for each reused version "sul_j":
        for sul_j in $SBJCT_DIR/*.dot; do
            # ... create a random OT
            java -cp ./scripts/pdlstarm.jar br.usp.icmc.labes.mealyInference.utils.BuildOT -dot -sul "$sul_j" -out $subj_run_path/ -mkot -rnd
            # and get the OT filename
            ot_basename=$(basename -- "$sul_j")
            ot_fname=$subj_run_path/$ot_basename".ot"
            # for each SUL version "sul_i":
            for sul_i in $SBJCT_DIR/*.dot; do
                # get the SUL filename
                sul_basename=$(basename -- "$sul_i")
                sul_fname=$subj_run_path/$sul_basename
                echo "Running experiment @ '$subject_name': Adaptive learning on $sul_basename by reusing $ot_basename"
                # and then run the adaptive algorithms
                java -jar ./scripts/pdlstarm.jar -cache -cexh RivestSchapire -learn adaptive  -clos $clos -eq $eqo -dot -sul "$sul_fname" -ot "$ot_fname" -out $subj_run_path/SUL_"${sul_basename%.*}"/adaptive/OT_"${ot_basename%.*}"/   -info "rep=$repNum" &
                sleep 1s
                java -jar ./scripts/pdlstarm.jar -cache -cexh RivestSchapire -learn dlstar_v0 -clos $clos -eq $eqo -dot -sul "$sul_fname" -ot "$ot_fname" -out $subj_run_path/SUL_"${sul_basename%.*}"/dlstar_v0/OT_"${ot_basename%.*}"/  -info "rep=$repNum" &
                sleep 1s
                java -jar ./scripts/pdlstarm.jar -cache -cexh RivestSchapire -learn dlstar_v1 -clos $clos -eq $eqo -dot -sul "$sul_fname" -ot "$ot_fname" -out $subj_run_path/SUL_"${sul_basename%.*}"/dlstar_v1/OT_"${ot_basename%.*}"/  -info "rep=$repNum" &
                sleep 1s
                java -jar ./scripts/pdlstarm.jar -cache -cexh RivestSchapire -learn dlstar_v2 -clos $clos -eq $eqo -dot -sul "$sul_fname" -ot "$ot_fname" -out $subj_run_path/SUL_"${sul_basename%.*}"/dlstar_v2/OT_"${ot_basename%.*}"/  -info "rep=$repNum" &
                sleep 1s
                java -jar ./scripts/pdlstarm.jar -cache -cexh RivestSchapire -learn dlstar_v3 -clos $clos -eq $eqo -dot -sul "$sul_fname" -ot "$ot_fname" -out $subj_run_path/SUL_"${sul_basename%.*}"/dlstar_v3/OT_"${ot_basename%.*}"/  -info "rep=$repNum" &
                sleep 1s
                java -jar ./scripts/pdlstarm.jar -cache -cexh RivestSchapire -learn dlstar_v4 -clos $clos -eq $eqo -dot -sul "$sul_fname" -ot "$ot_fname" -out $subj_run_path/SUL_"${sul_basename%.*}"/dlstar_v4/OT_"${ot_basename%.*}"/  -info "rep=$repNum" &
                sleep 1s
                wait
            done;
        done;
    done
done