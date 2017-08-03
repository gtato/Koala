#!/usr/bin/gnuplot


if (!exists("filename")) filename='../out/results/results.dat'
if (!exists("outfile")) outfile='paper1/out/random.pdf'
set terminal pdf
set output outfile

#set title "Comparison of latencies"
set xlabel "Time (1K cycles)"
set ylabel 'Latency (ms)'

#set ytics "10"
set yrange [ 0 : 1100 ]

datafile = filename
#firstrows = system('head -2 '.datafile)
#set title substr(firstrows, 0, strstrt(firstrows, "\n"))
#set ylabel substr(firstrows, strstrt(firstrows, "\n")+1, strlen(firstrows))

plot "<(tail -n +2  ".datafile.")"  using 5 title "Koala" smooth csplines with lines lc rgb "blue" , \
	 "<(tail -n +2  ".datafile.")" using 3 title "Chord" smooth csplines with lines  lc rgb "red", \
	 "<(tail -n +2  ".datafile.")" using 2 title "Physical" smooth csplines with lines  lc rgb "forest-green" 

# no smoothing
#plot datafile using 1 title "Koala" with lines, \
#	 datafile using 2 title "Physical" with lines  lc rgb "forest-green", \
#	 datafile using 3 title "Chord" with lines  lc rgb "blue"
	 
pause mouse close