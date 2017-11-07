#!/usr/bin/gnuplot




set title "Latencies"
set xlabel "Time"
set ylabel 'Latency'
set yrange [ 0 : * ]


#plot koala0  using 5 title "Koala" smooth csplines with lines lw 1.3 lc rgb "forest-green" , \
#     koala0  using 2 title "Physical" smooth csplines with lines lw 1.3 dt "-" lc rgb "grey"

set xtics 0,5000,10000


plot "<(sed -n '1,10000p' ".koala0.")" using 5 title "Koala" with points pointtype 6 lw 0.5 lc rgb "grey", \
	"<(sed -n '1,10000p' ".koala0.")" using 5 title "Average" smooth bezier with lines lw 2 lc rgb "black"
#	"<(sed -n '1,10000p' ".koala0.")" using 2 title "Physical" with points pointtype 1 lc rgb "dark-grey", \
  
#plot koala0  using 5 title "Koala" with points pointtype 6 lc rgb "dark-grey" , \
#     koala0  using 5 title "Koala average" smooth bezier with lines lw 2 lc rgb "red" , \
#     koala0  using 2 title "Physical" with points pointtype 1 lc rgb "dark-grey"
      
     

	 
pause mouse close