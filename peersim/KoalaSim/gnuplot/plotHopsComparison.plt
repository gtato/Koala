#!/usr/bin/gnuplot


if (!exists("filename1")) filename1='../out/results/resultsA0.0.dat'
if (!exists("filename2")) filename2='../out/results/resultsA0.5.dat'
if (!exists("filename3")) filename3='../out/results/resultsA1.0.dat'
if (!exists("filename4")) filename4='../out/results/resultsA-1.0.dat'

set title "Comparison of hops"
set xlabel "Time"
set ylabel 'hops'

#set ytics "10"
set yrange [ 0 : * ]



plot filename1 using 5 title "Renater" smooth csplines with lines lc rgb "forest-green" , \
	 filename1 using 6 title "Chord" smooth csplines with lines lc rgb "red" , \
	 filename1 using 7 title "Koala A=0" smooth csplines with lines lc rgb "web-blue" , \
	 filename2 using 7 title "Koala A=0.5" smooth csplines with lines lc rgb "blue" , \
	 filename3 using 7 title "Koala A=1" smooth csplines with lines lc rgb "violet" , \
	 filename4 using 7 title "Koala random" smooth csplines with lines lc rgb "green" 
	 
pause mouse close