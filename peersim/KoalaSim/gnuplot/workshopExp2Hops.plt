#!/usr/bin/gnuplot

set termoption enhanced
save_encoding = GPVAL_ENCODING
set encoding utf8

#set title "Latencies"
# set xlabel "Time"
set ylabel 'Hops'
set yrange [ 0 : 8 ]

set xrange[0:3]




set bars 6.0
set style fill empty
plot f1 using 1:3:2:6:5:xticlabels("α=".strcol(8)) with candlesticks lc rgb "dark-grey" title 'Quartiles' whiskerbars, \
	 f1 using 1:4:4:4:4 with candlesticks lt -1 notitle, \
	 f1 using 1:7 with linespoints pointtype 7 lw 2 lc rgb "black" title 'Average' 

	 
pause mouse close