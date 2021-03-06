#!/bin/bash      

rm "time.txt"

echo "#(threads, time)" >> "time.txt"

for i in `seq 1 10`
do
	T="$(date +%s%N)"

	./matrix_mult input3.txt input4.txt $i > silence.txt

	# Time interval in nanoseconds
	T="$(($(date +%s%N)-T))"
	echo "${i},${T}" >> "time.txt"

done

echo "Saved to time.txt"

rm silence.txt