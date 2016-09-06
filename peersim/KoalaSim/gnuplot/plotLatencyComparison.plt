#!/usr/bin/gnuplot


if (!exists("filename1")) filename1='../out/results/resultsA0.0.dat'
if (!exists("filename2")) filename2='../out/results/resultsA0.5.dat'
if (!exists("filename3")) filename3='../out/results/resultsA1.0.dat'

if (!exists("filename4")) filename4='../out/results/resultsA0.25.dat'
if (!exists("filename5")) filename5='../out/results/resultsA0.75.dat'

set title "Comparison of latencies"
set xlabel "Time"
set ylabel 'Latency'

#set ytics "10"
set yrange [ 0 : * ]

#firstrows = system('head -2 '.datafile)
#set title substr(firstrows, 0, strstrt(firstrows, "\n"))
#set ylabel substr(firstrows, strstrt(firstrows, "\n")+1, strlen(firstrows))

plot filename1 using 2 title "Renater" smooth csplines with lines lc rgb "forest-green" , \
	 filename1 using 3 title "Chord" smooth csplines with lines lc rgb "red" , \
	 filename1 using 4 title "Koala A=0" smooth csplines with lines lc rgb "web-blue" , \
	 filename2 using 4 title "Koala A=0.5" smooth csplines with lines lc rgb "blue" , \
	 filename3 using 4 title "Koala A=1" smooth csplines with lines lc rgb "violet" ,\
	 #filename4 using 4 title "Koala A=0.25" smooth csplines with lines lc rgb "green" , \
	 #filename5 using 4 title "Koala A=0.75" smooth csplines with lines lc rgb "yellow"
	 
pause mouse close