#!/usr/bin/gnuplot

if (!exists("filename")) filename=''

#set title "Some line"
set xlabel "50 cycles"
set ylabel "nr elements in rt"
set ytics "10"
datafile = '../out/koala/rt'.filename.'.dat'
plot datafile  using 1 title "" with lines  lc rgb "red" ,\
	 datafile  using 0:4:3:7:6 with candlesticks title 'Quartiles' whiskerbars
pause mouse close