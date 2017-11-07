#!/usr/bin/gnuplot

set termoption enhanced
save_encoding = GPVAL_ENCODING
set encoding utf8

#set title "Latencies"
# set xlabel "Time"
set ylabel 'Hops'
set yrange [ 0 : *]

set xrange[0:3]


#plot koala0 using 4 title "Multi-ring" smooth bezier with lines lw 2 lc rgb "blue", \
#	koala0 using 5 title "Single-ring" smooth bezier with lines lw 2 lc rgb "black", \
#	 koala0 using 6 title "Hiearchical" smooth bezier with lines lw 2 lc rgb "red", \
#	 koala0 using 2 title "Physical" smooth bezier with lines lw 2 lc rgb "dark-grey"
	 

#"<(sed -n '1,10000p' ".koala0.")" using 5 title "Koala" with points pointtype 6 lw 0.5 lc rgb "grey", \
#	"<(sed -n '1,10000p' ".koala0.")" using 5 title "Average" smooth bezier with lines lw 2 lc rgb "black"
#	"<(sed -n '1,10000p' ".koala0.")" using 2 title "Physical" with points pointtype 1 lc rgb "dark-grey", \

i=0.5
is = i+0.4
set bars 4.0
set style fill empty
plot f1 using (column(0)+i):3:2:6:5:xticlabels("inter") with candlesticks lc rgb "dark-grey" title 'Quartiles' whiskerbars, \
	 f1 using (column(0)+i):4:4:4:4 with candlesticks lt -1 notitle, \
	 f1 using (column(0)+i):7 with linespoints pointtype 7 lw 2 lc rgb "black" title 'Average', \
	 f2 using (column(0)+is):3:2:6:5:xticlabels("intra") with candlesticks lc rgb "dark-grey" notitle whiskerbars, \
	 f2 using (column(0)+is):4:4:4:4 with candlesticks lt -1 notitle, \
	 f2 using (column(0)+is):7 with linespoints pointtype 7 lw 2 lc rgb "black" notitle 
 

	 
pause mouse close