#!/usr/bin/gnuplot




set title "Hops"
set xlabel "Time"
set ylabel 'Hops'
set yrange [ 0 : * ]


#plot koala0  using 5 title "Koala" smooth csplines with lines lw 1.3 lc rgb "forest-green" , \
#     koala0  using 2 title "Physical" smooth csplines with lines lw 1.3 dt "-" lc rgb "grey"

set xtics 0,5000,10000


#f(x) = c
#fit f(x) "<(sed -n '1,10000p' ".koala0.")" using 1:14 every 5 via c
#plot "<(sed -n '1,10000p' ".koala0.")" using 1:14, \
#f(x) with lines



plot koala0 using 14 title "Koala" with points pointtype 6 lw 0.5 lc rgb "grey", \
	koala0 using 14 title "Average" smooth bezier with lines lw 2 lc rgb "black", \
	koala1 using 1:14 title "Average2" smooth bezier with lines lw 2 lc rgb "red"
#	"<(sed -n '1,10000p' ".koala0.")" using 8 title "Physical" with points pointtype 1 lc rgb "dark-grey", \
  
#plot koala0  using 5 title "Koala" with points pointtype 6 lc rgb "dark-grey" , \
#     koala0  using 5 title "Koala average" smooth bezier with lines lw 2 lc rgb "red" , \
#     koala0  using 2 title "Physical" with points pointtype 1 lc rgb "dark-grey"
      
     

	 
pause mouse close