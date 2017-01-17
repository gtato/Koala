#!/usr/bin/gnuplot


if (!exists("filename")) filename='../out/results/results.dat'

set title "Comparison of latencies"
set xlabel "Time"
set ylabel 'Latency'

#set ytics "10"
set yrange [ 0 : * ]

datafile = filename
#firstrows = system('head -2 '.datafile)
#set title substr(firstrows, 0, strstrt(firstrows, "\n"))
#set ylabel substr(firstrows, strstrt(firstrows, "\n")+1, strlen(firstrows))

plot "<(tail -n +2  ".datafile.")"  using 4 title "Koala" smooth csplines with lines lc rgb "blue" , \
	 "<(tail -n +2  ".datafile.")" using 2 title "Physical" smooth csplines with lines  lc rgb "forest-green"#, \
	 #"<(tail -n +2  ".datafile.")" using 3 title "Chord" smooth csplines with lines  lc rgb "red"

# no smoothing
#plot datafile using 1 title "Koala" with lines, \
#	 datafile using 2 title "Physical" with lines  lc rgb "forest-green", \
#	 datafile using 3 title "Chord" with lines  lc rgb "blue"
	 
pause mouse close