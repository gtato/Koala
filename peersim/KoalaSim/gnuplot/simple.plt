#!/usr/bin/gnuplot

#set title "Some line"
set xlabel "200 cycles"
set ylabel "nr elements in rt"
set ytics "10"
datafile = "../out/koalaToplogy00000000.dat"
plot datafile  using 1 title "" with lines  lc rgb "red" ,\
	 datafile  using 0:4:3:7:6 with candlesticks title 'Quartiles' whiskerbars
pause -1 "Hit any key to continue"