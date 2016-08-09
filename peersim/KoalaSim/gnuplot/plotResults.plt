#!/usr/bin/gnuplot

set title "Comparison"
set xlabel "Time"
#set ytics "10"
set yrange [ 0 : * ]

set key autotitle columnhead
datafile = 'out/results00000000.dat'
firstrow = system('head -1 '.datafile)
set ylabel word(firstrow, 1)

plot datafile using 1 title "Koala" smooth csplines with lines, \
	 datafile using 2 title "Physical" smooth csplines with lines  lc rgb "forest-green", \
	 datafile using 3 title "Chord" smooth csplines with lines  lc rgb "blue"

# no smoothing
#plot datafile using 1 title "Koala" with lines, \
#	 datafile using 2 title "Physical" with lines  lc rgb "forest-green", \
#	 datafile using 3 title "Chord" with lines  lc rgb "blue"
	 
pause -1 "Hit any key to continue"