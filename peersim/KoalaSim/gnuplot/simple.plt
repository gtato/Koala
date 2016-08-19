#!/usr/bin/gnuplot

#set title "Some line"
set xlabel "200 cycles"
set ylabel "nr elements in rt"
set ytics "10"
plot "../out/test.dat" title "" with lines 
pause -1 "Hit any key to continue"