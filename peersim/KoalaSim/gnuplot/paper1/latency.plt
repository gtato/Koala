#!/usr/bin/gnuplot

if (!exists("outfile")) outfile='paper1/out/exp1latency.pdf'

set terminal pdf
set output outfile

set key horizontal 


#set title "Comparison of latencies"
set xlabel "Time (100 cycles)"
set ylabel 'Latency (ms)' offset 2

#set ytics "10"
set yrange [ 0 : * ]
set xrange [ 0 : 1000 ]

#plot filename1 using 2 title "Physical" smooth csplines with lines lc rgb "forest-green" ,\
#	filename1  using 4 title "C=1" smooth csplines with lines lc rgb "red" , \
#	filename2 using 4 title "C=2" smooth csplines with lines  lc rgb "brown", \
#	filename3 using 4 title "C=4" smooth csplines with lines  lc rgb "blue"

plot filename1 using 2 title "Physical" with lines lc rgb "forest-green" ,\
	filename1  using 4 title "C=1" with lines lc rgb "red" , \
	filename2 using 4 title "C=2" with lines  lc rgb "brown", \
	filename3 using 4 title "C=4" with lines  lc rgb "blue"



# no smoothing
#plot datafile using 1 title "Koala" with lines, \
#	 datafile using 2 title "Physical" with lines  lc rgb "forest-green", \
#	 datafile using 3 title "Chord" with lines  lc rgb "blue"
	 
pause mouse close
