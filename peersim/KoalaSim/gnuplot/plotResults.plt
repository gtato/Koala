#!/usr/bin/gnuplot

set title "Some line"
set xlabel "Time"
set ylabel "Msg latency"
#set ytics "10"
plot "out/results00000000.dat" using 1 title "Koala" with lines, \
	 "out/results00000000.dat" using 2 title "Physical" with lines  lc rgb "forest-green"
pause -1 "Hit any key to continue"