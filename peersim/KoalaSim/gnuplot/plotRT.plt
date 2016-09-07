#!/usr/bin/gnuplot

if (!exists("filename")) filename='../out/koala/rtA0.5.dat'

#set title "Some line"
set xlabel "50 cycles"
set ylabel "nr elements in rt"


plot filename  using 1 title "" with lines  lc rgb "red" ,\
	 filename  using 0:4:3:7:6 with candlesticks title 'Quartiles' whiskerbars ,\
	 filename  using 0:($1 + $2):($1 + $2):($1 + $2):($1 + $2) with candlesticks lt -1 notitle ,\
	 filename  using 0:($1 - $2):($1 - $2):($1 - $2):($1 - $2) with candlesticks lt -1 notitle
pause mouse close