#!/bin/bash
OutFileName="/tmp/virtual-dispatcher/data/"$1"/upload/output.csv"
i=0
for filename in /tmp/virtual-dispatcher/data/"$1"/cleaned/*.csv; do
 if [ "$filename"  != "$OutFileName" ] ;
 then
   if [[ $i -eq 0 ]] ; then
      head -1  $filename >   $OutFileName
   fi
   tail -n +2  $filename >>  $OutFileName
   i=$(( $i + 1 ))
 fi
done
echo 'completed'

