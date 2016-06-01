# set terminal pngcairo  background "#ffffff" font "arial,10" fontscale 1.0 size 500, 300 
# set output 'circles.5.png'
unset key
set size ratio -1 1,1
set title "Peersim generated topology" 
r = 0.01
types = 6
keyx = -137.0
keyy = -15.0
keyr = 25.0
i = 5
#plot '~/Downloads/gnuplot-master/demo/delaunay-edges.dat' with lines lc rgb "forest-green",      '~/Downloads/gnuplot-master/demo/hemisphr.dat' u (100*$1):(100*$2) with points pt 7 lc rgb "black"
plot '../out/graph00000000.dat' with lines lc rgb "forest-green", '../out/graph00000000.dat' u ($1):($2) with points pt 7 lc rgb "black"
#plot '../configs/renaternodes.txt' u ($1):($2) with points pt 7 lc rgb "black"