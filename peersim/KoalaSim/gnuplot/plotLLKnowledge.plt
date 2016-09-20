#!/usr/bin/gnuplot

if (!exists("filename")) filename='../out/koala/rt_fineA0.5.dat'


set title "Node knowledge"
set xlabel "Percentage of known nodes"
set ylabel 'Number of nodes'

set yrange [ 0.1 : * ]

set logscale y 10

 


datafile = filename


set style data histograms
set style fill solid 0.5
set bars front
#set boxwidth 2.5
set xtics rotate by -90 out
#plot datafile  using 1:2 notitle with points 
plot datafile using 2:xticlabels(1) linecolor rgb "dark-blue" notitle ,\
	 '' using 0:2:2 with labels offset char 0.5,0.5 notitle
pause mouse close