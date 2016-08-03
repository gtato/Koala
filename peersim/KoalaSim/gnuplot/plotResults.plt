#!/usr/bin/gnuplot

set title "Some line"
set xlabel "X"
set ylabel "Y"
#set ytics "10"
plot "out/results00000000.dat" using 1 title "lesh" with lines, \
	 "out/results00000000.dat" using 2 title "presh" with lines  
pause -1 "Hit any key to continue"