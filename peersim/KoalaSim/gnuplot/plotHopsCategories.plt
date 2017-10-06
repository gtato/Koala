#!/usr/bin/gnuplot


set title "Comparison of hops"
set xlabel "Time"
set ylabel 'Hops'
#set ytics "10"
set yrange [ 0 : * ]

#set key autotitle columnhead



#plot datafile  using 11 title "Koala local" smooth csplines with lines lc rgb "blue" , \
#	 datafile  using ($11+$12) title "Koala total" smooth csplines with lines lw 2 lc rgb "blue" , \
#	 datafile using 13 title "Flatala local" smooth csplines with lines lc rgb "forest-green", \
#	 datafile using ($13+$14) title "Flatala total" smooth csplines with lines lw 2 lc rgb "forest-green", \
#	 datafile using 15 title "HierKoala local" smooth csplines with lines lc rgb "red", \
#	 datafile using ($15+$16) title "HierKoala total" smooth csplines with lines lw 2 lc rgb "red"

# no smoothing	 
plot datafile  using 11 title "Koala local" with lines lc rgb "blue" , \
	 datafile using 13 title "Flatala local" with lines lc rgb "forest-green", \
	 datafile using 15 title "HierKoala local" with lines lc rgb "red"
	 
#plot datafile  using 12 title "Koala global" with lines lc rgb "blue" , \
#	 datafile using 14 title "Flatala global" with lines lc rgb "forest-green", \
#	 datafile using 16 title "HierKoala global" with lines lc rgb "red"
	 
	 
#plot datafile  using ($11+$12) title "Koala total" with lines lw 2 lc rgb "blue" , \
#	 datafile using ($13+$14) title "Flatala total" with lines lw 2 lc rgb "forest-green", \
#	 datafile using ($15+$16) title "HierKoala total" with lines lw 2 lc rgb "red"	 
	 
pause mouse close 