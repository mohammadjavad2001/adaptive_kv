#!/bin/sh

sed -i -r "s/<[^<>]+>//g" ./*.dot
sed -i -r "s/>\]/\"\]/g" ./*.dot
sed -i -r "s/=</=\"/g" ./*.dot
sed -i -r "s/[,+()]//g" ./*.dot
sed -i -r "s/[,+()]//g" ./*.dot

for i in ./*.dot; do 
    java -cp ../mylearn.jar br.usp.icmc.labes.mealyInference.utils.Dot2Kiss -sul $i; 
done