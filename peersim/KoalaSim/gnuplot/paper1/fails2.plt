#!/usr/bin/gnuplot

if (!exists("outfile")) outfile='out/exp1latency.pdf'

set terminal pdf
set output outfile

set key right 


#set title "Comparison of latencies"
set xlabel "Time (200 cycles)"
set ylabel 'Fails (%)' offset 2

#set ytics "10"
#set yrange [ 0 : * ]
#set xrange [ 0 : 100 ]


set xlabel "Time (1K cycles)"
plot filename7 using ($10*100) title "CHURN=9/10" with linespoints pt 7 ps 0.6 lc rgb "red" ,\
	filename6 using ($10*100) title "CHURN=3/4" with linespoints pt 7 ps 0.6 lc rgb "brown" ,\
	filename5 using ($10*100) title "CHURN=1/2" with linespoints pt 7 ps 0.6 lc rgb "violet" ,\
	filename4 using ($10*100) title "CHURN=1/4" with linespoints pt 7 ps 0.6 lc rgb "blue",\
	filename3 using ($10*100) title "CHURN=1/8" with linespoints pt 7 ps 0.6 lc rgb "orange"
	#,\
	#filename2 using ($10*100) title "CHURN=1/16" with linespoints pt 7 ps 0.6 lc rgb "forest-green"


	 
pause mouse close
