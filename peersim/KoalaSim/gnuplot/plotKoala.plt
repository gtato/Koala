#!/usr/bin/gnuplot


if (!exists("filename")) filename=''

unset key
set size ratio -1 1,1
set title "Koala topology" 
r = 0.01
types = 6
keyx = -137.0
keyy = -15.0
keyr = 25.0
i = 5

datafile = '../out/koala/topology'.filename.'.dat'


# this is for plotting the logical topology (using KoalaNodeObserver)
plot datafile with lines lc rgb "#0091ea" ,\
     datafile u ($1):($2):($3) with labels point pt 7 offset char 0,0.5 lc rgb "black"


pause mouse close