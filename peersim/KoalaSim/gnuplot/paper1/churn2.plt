#!/usr/bin/gnuplot

if (!exists("outfile")) outfile='out/exp1latency.pdf'

set terminal pdf
set output outfile

set key horizontal 


#set title "Comparison of latencies"
set xlabel "Time (1K cycles)"
set ylabel 'Latency (ms)' offset 2


set yrange [ 0 : * ]
#set xrange [ 0 : 100 ]

plot filename1 using 4 title "No churn" smooth csplines with lines lc rgb "forest-green",\
	 filename3 using 4 title "CHURN=1/8" smooth csplines with lines lc rgb "orange",\
	 filename4 using 4 title "CHURN=1/4" smooth csplines with lines  lc rgb "blue",\
	 filename5 using 4 title "CHURN=1/2" smooth csplines with lines  lc rgb "violet",\
	 filename6 using 4 title "CHURN=3/4" smooth csplines with lines  lc rgb "brown",\
	 filename7 using 4 title "CHURN=9/10" smooth csplines with lines  lc rgb "red"
	 
pause mouse close
