#this is supposed to be called from a controller, change the paths if you use terminal

# set terminal pngcairo  background "#ffffff" font "arial,10" fontscale 1.0 size 500, 300 
# set output 'circles.5.png'
unset key
set size ratio -1 1,1
set title "Koala topology" 
r = 0.01
types = 6
keyx = -137.0
keyy = -15.0
keyr = 25.0
i = 5

# path from terminal: ../out/koalaToplogy00000000.dat
# this is for plotting the logical topology (using KoalaNodeObserver)
plot 'out/koalaToplogy00000000.dat' with lines lc rgb "#0091ea" ,\
     'out/koalaToplogy00000000.dat' u ($1):($2):($3) with labels point pt 7 offset char 0,0.5 lc rgb "black"


