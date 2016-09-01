#!/usr/bin/gnuplot

if (!exists("filename")) filename=''

unset key
set size square
set title "Physical topology" 
#r = 0.01
#types = 6
#keyx = -137.0
#keyy = -15.0
#keyr = 25.0
#i = 5

set xrange [ 0 : * ]
set yrange [ 0 : * ]

datafile = '../out/renater/topology'.filename.'.dat'

# path from terminal: ../out/graph00000000.dat
# this is for plotting physical (renater) topology (using InetObserver) 
plot datafile with lines lc rgb "#0091ea" ,\
	 datafile u ($1):($2):($3) with labels point pt 7 offset char 0,0.5 lc rgb "black" ,\
     datafile u ($4):($5):6 with labels offset char 0,0.5
     
pause mouse close