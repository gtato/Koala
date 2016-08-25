#!/usr/bin/gnuplot

#set title "Some line"
set xlabel "200 cycles"
set ylabel "nr elements in rt"
set ytics "10"
plot "../out/test.dat"  using 1 title "" with lines  lc rgb "red" ,\
	 "../out/test.dat"  using 0:4:3:7:6 with candlesticks title 'Quartiles' whiskerbars
pause -1 "Hit any key to continue"