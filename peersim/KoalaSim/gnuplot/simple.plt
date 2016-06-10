#!/usr/bin/gnuplot

set title "Some line"
set xlabel "X"
set ylabel "Y"
set ytics "10"
plot "out/test.dat" title "" with lines 
pause -1 "Hit any key to continue"