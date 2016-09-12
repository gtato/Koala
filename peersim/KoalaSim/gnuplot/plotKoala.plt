#!/usr/bin/gnuplot

if (!exists("filename")) filename='../out/koala/topologyA0.0.dat'

unset key
set size square
set title "Koala topology" 



# this is for plotting the logical topology (using KoalaNodeObserver)
plot filename with lines lc rgb "#0091ea" ,\
     '' u ($1):($2):($3) with labels point pt 7 offset char 0,0.5 lc rgb "black"


pause mouse close