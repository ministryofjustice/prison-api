#!/usr/bin/env bash

mkdir -p tmp

version_number=$1
newSeq=1
fileSeq=($(ls -1 *.sql | sed  s/[vV]//g | sed s/__.*$//g | sort --version-sort --field-separator=_))
for version in "${fileSeq[@]}"; do
   oldFileName=$(ls V${version}__*)
   endFileName=$(ls V${version}__* | sed s/^.*__//g | sed s/.sql//g | tr '[:lower:]' '[:upper:]')


   echo $oldFileName '-->' ${version_number}_${newSeq}__${endFileName}.sql
   cp $oldFileName tmp/${version_number}_${newSeq}__${endFileName}.sql
   let newSeq=newSeq+1
done

rm *.sql
mv tmp/*.sql .
rmdir tmp