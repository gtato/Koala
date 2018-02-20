#!/usr/bin/gnuplot

set title "Latencies"
set xlabel "Time"
set ylabel 'Latency'
set yrange [ 0 : * ]


plot koala0 using 5 title "Average" smooth bezier with lines lw 1 lc rgb "black", \
	 koala1 using 5 title "Average" smooth bezier with lines lw 1 lc rgb "red", \
	 koala0 using 2 title "Physical" smooth bezier with lines lw 1 lc rgb "dark-grey"
      
     

	 
pause mouse close