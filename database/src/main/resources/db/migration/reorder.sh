#!/usr/bin/env bash

mkdir -p tmp

newSeq=1
fileSeq=($(ls -1 *.sql | sed  s/[vV]//g | sed s/__.*$//g | sed s/_/./g | sort -n))
for oldSeq in "${fileSeq[@]}"; do

  new=$(printf "%04d.sql" "$a") #04 pad to length of 4

  pattern="."
  version=${oldSeq//$pattern/_}

   oldFileName=$(ls V${version}__*)
   endFileName=$(ls V${version}__* | sed s/^.*__//g | sed s/.sql//g | tr '[:lower:]' '[:upper:]')

   echo $oldFileName '-->' V1_${newSeq}__${endFileName}.sql
   cp $oldFileName tmp/V1_${newSeq}__${endFileName}.sql

   let newSeq=newSeq+1
done