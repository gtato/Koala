#!/usr/bin/gnuplot


if (!exists("filename")) filename='../out/results/results.dat'

set title "Comparison of latencies"
set xlabel "Time"
set ylabel 'Latency'

#set ytics "10"
set yrange [ 0 : * ]

datafile = filename

# plot "<(tail -n +2  ".datafile.")"  using 4 title "Koala" smooth csplines with lines lc rgb "blue" , \
#  	 "<(tail -n +2  ".datafile.")" using 2 title "Physical" smooth csplines with lines  lc rgb "forest-green", \
# 	 "<(tail -n +2  ".datafile.")" using 3 title "Chord" smooth csplines with lines  lc rgb "red"

plot "<(tail -n +2  ".datafile.")"  using 4 title "Koala" smooth csplines with lines lc rgb "blue" , \
	"<(tail -n +2  ".datafile.")"  using 5 title "Flatoala" smooth csplines with lines lc rgb "orange" , \
	"<(tail -n +2  ".datafile.")"  using 6 title "Hierarkoala" smooth csplines with lines lc rgb "violet" , \
	"<(tail -n +2  ".datafile.")"  using 2 title "Physical" smooth csplines with lines  lc rgb "forest-green"

# no smoothing
#plot datafile using 4 title "Koala" with lines lc rgb "blue", \
# 	 datafile using 2 title "Physical" with lines  lc rgb "forest-green", \
# 	 datafile using 3 title "Chord" with lines  lc rgb "red"
	 
pause mouse close