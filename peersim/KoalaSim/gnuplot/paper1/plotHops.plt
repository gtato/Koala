#!/usr/bin/gnuplot

if (!exists("filename")) filename='../out/results/results0.5.dat'


#set title "Comparison of hops"
set xlabel "Time"
set ylabel 'Hops'
set hey horizontal
#set ytics "10"
set yrange [ 0 : * ]

#set key autotitle columnhead
datafile = filename
#firstrows = system('head -2 '.datafile)
#set title substr(firstrows, 0, strstrt(firstrows, "\n"))
#set ylabel substr(firstrows, strstrt(firstrows, "\n")+1, strlen(firstrows))


plot datafile  using 7 title "Koala" smooth csplines with lines lc rgb "blue" , \
	 datafile using 5 title "Physical" smooth csplines with lines lc rgb "forest-green", \
	 datafile using 6 title "Chord" smooth csplines with lines lc rgb "red"

# no smoothing
#plot datafile using 1 title "Koala" with lines, \
#	 datafile using 2 title "Physical" with lines  lc rgb "forest-green", \
#	 datafile using 3 title "Chord" with lines  lc rgb "blue"
	 
pause mouse close 