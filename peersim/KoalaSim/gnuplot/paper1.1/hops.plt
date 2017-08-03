#!/usr/bin/gnuplot

if (!exists("outfile")) outfile='out/exp1hops.pdf'

set terminal pdf
set output outfile


#set title "Comparison of hops"
set key horizontal
set xlabel "Time (100 cycles)"
set ylabel 'Hops' offset 2


set yrange [ 0 : * ]
set xrange [ 0 : 1000 ]

plot "<(tail -n +2  ".filename1.")"  using 7 title "C=1" smooth csplines with lines lc rgb "red" , \
	 "<(tail -n +2  ".filename2.")" using 7 title "C=2" smooth csplines with lines  lc rgb "brown", \
	 "<(tail -n +2  ".filename3.")" using 7 title "C=4" smooth csplines with lines  lc rgb "blue"

# no smoothing
#plot datafile using 1 title "Koala" with lines, \
#	 datafile using 2 title "Physical" with lines  lc rgb "forest-green", \
#	 datafile using 3 title "Chord" with lines  lc rgb "blue"
	 
pause mouse close
