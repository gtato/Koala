#!/usr/bin/gnuplot

if (!exists("outfile")) outfile='out/exp1latency.pdf'

set terminal pdf
set output outfile

set key left 


#set title "Comparison of latencies"
set xlabel "Time (1K cycles)"
set ylabel 'Fails (%)' offset 2


plot filename5 using ($10*100) title "CHURN=8" with linespoints pt 7 ps 0.6 lc rgb "red"  ,\
	filename4 using ($10*100) title "CHURN=4" with linespoints pt 7  ps 0.6 lc rgb "violet",\
	filename3 using ($10*100) title "CHURN=2" with linespoints pt 7 ps 0.6 lc rgb "blue",\
	filename2 using ($10*100) title "CHURN=1" with linespoints pt 7 ps 0.6 lc rgb "orange" 
	#filename1 using ($10*100) title "No churn" with linespoints pt 7 ps 0.6 lc rgb "forest-green"
	 
pause mouse close
