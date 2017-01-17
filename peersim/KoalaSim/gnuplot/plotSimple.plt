#!/usr/bin/gnuplot


if (!exists("filename")) filename='../out/diffA0.5.dat'
#if (!exists("filename")) filename='../out/nndiffA0.5.dat'
#if (!exists("filename")) filename='../out/ndiffA0.5.dat'

set title "simple"
set xlabel "x"
set ylabel 'y'

#set ytics "10"
set yrange [ 0 : * ]

datafile = filename

# plot datafile title "distribution" smooth csplines with points lc rgb "blue" 
plot datafile title "distribution" with points lc rgb "blue"	 

# no smoothing
#plot datafile using 1 title "Koala" with lines, \
#	 datafile using 2 title "Physical" with lines  lc rgb "forest-green", \
#	 datafile using 3 title "Chord" with lines  lc rgb "blue"
	 
pause mouse close