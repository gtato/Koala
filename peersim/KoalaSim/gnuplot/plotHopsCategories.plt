#!/usr/bin/gnuplot

if (!exists("filename")) filename='../out/results/results0.5.dat'


set title "Comparison of hops"
set xlabel "Time"
set ylabel 'Hops'
#set ytics "10"
set yrange [ 0 : * ]

#set key autotitle columnhead
datafile = filename


plot datafile  using 11 title "Koala local" smooth csplines with lines lc rgb "blue" , \
	 datafile  using ($11+$12) title "Koala total" smooth csplines with lines lw 2 lc rgb "blue" , \
	 datafile using 13 title "Flatala local" smooth csplines with lines lc rgb "forest-green", \
	 datafile using ($13+$14) title "Flatala total" smooth csplines with lines lw 2 lc rgb "forest-green", \
	 datafile using 15 title "HierKoala local" smooth csplines with lines lc rgb "red", \
	 datafile using ($15+$16) title "HierKoala total" smooth csplines with lines lw 2 lc rgb "red"

# no smoothing
#plot datafile using 1 title "Koala" with lines, \
#	 datafile using 2 title "Physical" with lines  lc rgb "forest-green", \
#	 datafile using 3 title "Chord" with lines  lc rgb "blue"
	 
pause mouse close 