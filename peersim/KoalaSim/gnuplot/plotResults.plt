#!/usr/bin/gnuplot

set title "Comparison asdf asdfasdf"
set xlabel "Time"
#set ytics "10"
set yrange [ 0 : * ]

#set key autotitle columnhead
datafile = 'out/results00000000.dat'
firstrows = system('head -2 '.datafile)
set title substr(firstrows, 0, strstrt(firstrows, "\n"))
set ylabel substr(firstrows, strstrt(firstrows, "\n")+1, strlen(firstrows))

plot "<(tail -n +3  ".datafile.")"  using 1 title "Koala" smooth csplines with lines , \
	 "<(tail -n +3  ".datafile.")" using 2 title "Physical" smooth csplines with lines  lc rgb "forest-green", \
	 "<(tail -n +3  ".datafile.")" using 3 title "Chord" smooth csplines with lines  lc rgb "blue"

# no smoothing
#plot datafile using 1 title "Koala" with lines, \
#	 datafile using 2 title "Physical" with lines  lc rgb "forest-green", \
#	 datafile using 3 title "Chord" with lines  lc rgb "blue"
	 
pause -1 "Hit any key to continue"