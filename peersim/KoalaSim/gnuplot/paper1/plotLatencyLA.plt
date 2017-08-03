#!/usr/bin/gnuplot


if (!exists("filename1")) filename1='../out/results/results.dat'
if (!exists("filename2")) filename2='../out/results/results.dat'


if (!exists("outfile")) outfile='paper1/out/intrala.pdf'
set terminal pdf
set output outfile

#set title "Comparison of latencies"
set xlabel "Time (1K cycles)"
set ylabel 'Latency (ms)'

#set ytics "10"
#set yrange [ 0 : 1100 ]
set key horizontal 
set key at 250,300 cent


set key vertical maxrows 2
#firstrows = system('head -2 '.datafile)
#set title substr(firstrows, 0, strstrt(firstrows, "\n"))
#set ylabel substr(firstrows, strstrt(firstrows, "\n")+1, strlen(firstrows))
set macros
dummy="NaN title ' ' lt -3"
#plot "<(tail -n +2  ".filename1.")"  using 5 title "Koala 10" smooth csplines with lines lc rgb "blue" , \
#	 "<(tail -n +2  ".filename2.")"  using 5 title "Koala 20" smooth csplines with lines lc rgb "cyan" , \
#	 "<(tail -n +2  ".filename3.")"  using 5 title "Koala 50" smooth csplines with lines lc rgb "red" , \
#	 "<(tail -n +2  ".filename1.")" using 3 title "Chord 10" smooth csplines with lines  lc rgb "red", \
#	 "<(tail -n +2  ".filename2.")" using 3 title "Chord 20" smooth csplines with lines  lc rgb "brown", \
#	 "<(tail -n +2  ".filename3.")" using 3 title "Chord 50" smooth csplines with lines  lc rgb "magenta", \
#	 "<(tail -n +2  ".filename1.")" using 2 title "Physical" smooth csplines with lines  lc rgb "forest-green" 

plot filename1  using 5 title "Koala rand." smooth csplines with lines lc rgb "web-blue" lw 1.5, \
	 filename2  using 5 title "Koala consec." smooth csplines with lines lc rgb "blue" lw 1.5, \
     filename1  using 3 title "Chord rand." smooth csplines with lines  lc rgb "light-red" lw 1.5, \
	 filename2  using 3 title "Chord consec." smooth csplines with lines  lc rgb "dark-red" lw 1.5, \
	 filename1  using 2 title "Physical" smooth csplines with lines  lc rgb "forest-green" lw 1.5

 

# no smoothing
#plot datafile using 1 title "Koala" with lines, \
#	 datafile using 2 title "Physical" with lines  lc rgb "forest-green", \
#	 datafile using 3 title "Chord" with lines  lc rgb "blue"
	 
pause mouse close