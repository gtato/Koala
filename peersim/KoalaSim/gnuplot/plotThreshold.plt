#!/usr/bin/gnuplot


if (!exists("filename")) filename='../out/diffA0.5.dat'
#if (!exists("filename")) filename='../out/nndiffA0.5.dat'
#if (!exists("filename")) filename='../out/ndiffA0.5.dat'

# set title "simple"
set key above maxrows 4
set xlabel "Time (1K cycles)"
set ylabel 'R (%)'

#set ytics "10"
set yrange [ 0 : * ]

datafile = filename
#	

plot koala10 title "T=0.1" smooth csplines with lines lw 1.3 dt 1 lc rgb "forest-green", \
	koala20 title "T=0.2" smooth csplines with lines lw 1.3 dt 2 lc rgb "forest-green", \
	koala30 title "T=0.3" smooth csplines with lines lw 1.3 dt 3 lc rgb "forest-green", \
	koala40 title "T=0.4" smooth csplines with lines lw 1.3 dt 4 lc rgb "orange", \
	koala50 title "T=0.5" smooth csplines with lines lw 1.3 dt 5 lc rgb "orange", \
	koala60 title "T=0.6" smooth csplines with lines lw 1.3 dt 1 lc rgb "orange", \
	koala70 title "T=0.7" smooth csplines with lines lw 1.3 dt 2 lc rgb "red", \
	koala80 title "T=0.8" smooth csplines with lines lw 1.3 dt 3 lc rgb "red", \
	koala90 title "T=0.9" smooth csplines with lines lw 1.3 dt 4 lc rgb "red"

	
	
	 
#plot datafile title "distribution" with points lc rgb "blue"	 

# no smoothing
#plot datafile using 1 title "Koala" with lines, \
#	 datafile using 2 title "Physical" with lines  lc rgb "forest-green", \
#	 datafile using 3 title "Chord" with lines  lc rgb "blue"
	 
pause mouse close