#!/bin/bash

#PBS -l ncpus=80
#PBS -l walltime=96:00:00
#PBS -m ae
#PBS -M damascenodiego@alumni.usp.br

# for i in `seq 1 10`; do qsub -N "fase_r$i" -v "var=10" ./fase19.sh ; done
# qsub -N "nrd_wwph" ./nordsec16.sh 
module load gcc/4.9.2 R/3.3.3 java-oracle/jdk1.8.0_65

my_dir=/home/damascdn/remote_euler/
cd $my_dir


if [[ $totRep -eq 0 ]] ; 
then
	totRep=2
fi 

runID=$(date +"%Y%m%d_%H%M%S_%N")
# for rep in `seq -f "%03g" 1 $var`; do for eqo in "rndWalk" "wrndhyp"; do
# for eqo in "wphyp" "whyp"; do
for eqo in "wphyp"; do
	# for side in "client" "server"; do
	for side in "server"; do
	run_path=$my_dir/Nordsec16"_"$side/run_$runID/
	for sul_j in $my_dir/Benchmark/Nordsec16/$side"_"*.dot; do
		java -cp $my_dir/mylearn.jar br.usp.icmc.labes.mealyInference.utils.BuildOT -sul $sul_j -out $run_path/ -dot 
		sul_basename=$(basename -- "$sul_j")
		sul_fname=$run_path/$sul_basename".kiss"
		java -jar $my_dir/mylearn.jar -cache -cexh RivestSchapire -learn lstar -clos CloseFirst -eq $eqo -sul $sul_fname -out $run_path/SUL_"${sul_basename%.*}"/lstar/ &
		# sleep 0.5s
		java -jar $my_dir/mylearn.jar -cache -cexh RivestSchapire -learn l1    -clos CloseFirst -eq $eqo -sul $sul_fname -out $run_path/SUL_"${sul_basename%.*}"/l1/ &
		# sleep 0.5s
		java -jar $my_dir/mylearn.jar -cache 					  -learn ttt 					-eq $eqo -sul $sul_fname -out $run_path/SUL_"${sul_basename%.*}"/ttt/ &
		# sleep 0.5s
	done;
	wait
	done
	# for side in "client" "server"; do
	for side in "server"; do
		run_path=$my_dir/Nordsec16"_"$side/run_$runID/
		for (( repNum = 0; repNum < totRep; repNum++ )); do
			rep_path=$run_path/rep_$repNum/
			for sul_i in $my_dir/Benchmark/Nordsec16/$side"_"*.dot; do
				java -cp $my_dir/mylearn.jar br.usp.icmc.labes.mealyInference.utils.BuildOT -sul $sul_i -out $rep_path/ -dot
			done
			for sul_j in $my_dir/Benchmark/Nordsec16/$side"_"*.dot; do
				java -cp $my_dir/mylearn.jar br.usp.icmc.labes.mealyInference.utils.BuildOT -sul $sul_j -out $rep_path/ -dot -mkot -rnd
				for sul_i in $my_dir/Benchmark/Nordsec16/$side"_"*.dot; do
					sul_basename=$(basename -- "$sul_i")
					ot_basename=$(basename -- "$sul_j")
					sul_fname=$rep_path/$sul_basename".kiss"
					ot_fname=$rep_path/$ot_basename".ot"
					if [[ $sul_i != $sul_j ]] ; 
					then
						java -jar $my_dir/mylearn.jar -cache -cexh RivestSchapire -learn adaptive  -clos CloseFirst -eq $eqo -sul $sul_fname -ot $ot_fname -out $rep_path/SUL_"${sul_basename%.*}"/adaptive/OT_"${ot_basename%.*}"/   -info "rep=$repNum" &
						# sleep 0.5s
						java -jar $my_dir/mylearn.jar -cache -cexh RivestSchapire -learn dlstar_v0 -clos CloseFirst -eq $eqo -sul $sul_fname -ot $ot_fname -out $rep_path/SUL_"${sul_basename%.*}"/dlstar_v0/OT_"${ot_basename%.*}"/  -info "rep=$repNum" &
						# sleep 0.5s
						java -jar $my_dir/mylearn.jar -cache -cexh RivestSchapire -learn dlstar_v1 -clos CloseFirst -eq $eqo -sul $sul_fname -ot $ot_fname -out $rep_path/SUL_"${sul_basename%.*}"/dlstar_v1/OT_"${ot_basename%.*}"/  -info "rep=$repNum" &
						# sleep 0.5s
						java -jar $my_dir/mylearn.jar -cache -cexh RivestSchapire -learn dlstar_v2 -clos CloseFirst -eq $eqo -sul $sul_fname -ot $ot_fname -out $rep_path/SUL_"${sul_basename%.*}"/dlstar_v2/OT_"${ot_basename%.*}"/  -info "rep=$repNum" &
						# sleep 0.5s
					fi
				done;
				wait
			done;
		done
	done
done 
#done