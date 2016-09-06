#!/usr/bin/gnuplot

if (!exists("filename")) filename='../out/renater/topologyA0.0.dat'

unset key
set size square
set title "Physical topology" 


set xrange [ 0 : * ]
set yrange [ 0 : * ]

# path from terminal: ../out/graph00000000.dat
# this is for plotting physical (renater) topology (using InetObserver) 
plot filename with lines lc rgb "#0091ea" ,\
	 filename u ($1):($2):($3) with labels point pt 7 offset char 0,0.5 lc rgb "black" ,\
     filename u ($4):($5):6 with labels offset char 0,0.5
     
pause mouse close