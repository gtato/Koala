#!/usr/bin/gnuplot

set termoption enhanced
save_encoding = GPVAL_ENCODING
set encoding utf8

#set title "Latencies"
# set xlabel "Time"
set ylabel 'Latency (ms)'
#set yrange [ 0 : 1100 ]
set yrange [ 0 : * ]
set xrange[0:4]


#plot koala0 using 4 title "Multi-ring" smooth bezier with lines lw 2 lc rgb "blue", \
#	koala0 using 5 title "Single-ring" smooth bezier with lines lw 2 lc rgb "black", \
#	 koala0 using 6 title "Hiearchical" smooth bezier with lines lw 2 lc rgb "red", \
#	 koala0 using 2 title "Physical" smooth bezier with lines lw 2 lc rgb "dark-grey"
	 

#"<(sed -n '1,10000p' ".koala0.")" using 5 title "Koala" with points pointtype 6 lw 0.5 lc rgb "grey", \
#	"<(sed -n '1,10000p' ".koala0.")" using 5 title "Average" smooth bezier with lines lw 2 lc rgb "black"
#	"<(sed -n '1,10000p' ".koala0.")" using 2 title "Physical" with points pointtype 1 lc rgb "dark-grey", \


set bars 6.0
set style fill empty
plot f1 using (column(0)+1):3:2:6:5:xticlabels(8) with candlesticks lc rgb "dark-grey" title 'Quartiles' whiskerbars, \
	 f1 using (column(0)+1):4:4:4:4 with candlesticks lt -1 notitle, \
	 f1 using (column(0)+1):7 with linespoints pointtype 7 lw 2 lc rgb "black" title 'Average'
	 
pause mouse close