#!/usr/bin/gnuplot


if (!exists("filename1")) filename1='../out/results/results.dat'
if (!exists("filename2")) filename2='../out/results/results.dat'
if (!exists("filename3")) filename3='../out/results/results.dat'

if (!exists("outfile")) outfile='paper1/out/norandom.pdf'
set terminal pdf
set output outfile

#set title "Comparison of latencies"
set xlabel "Time (1K cycles)"
set ylabel 'Latency (ms)'

#set ytics "10"
#set yrange [ 0 : 1100 ]
set key horizontal 
set key at 250,300 cent


#set key maxrows 3
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

plot filename1  using 5 title "Koala x1" smooth csplines with lines lc rgb "web-blue" lw 1.5, \
	 filename2  using 5 title "Koala x10" smooth csplines with lines lc rgb "blue" lw 1.5, \
	 filename3  using 5 title "Koala x100" smooth csplines with lines lc rgb "dark-blue" lw 1.5, \
     filename1  using 3 title "Chord x1" smooth csplines with lines  lc rgb "magenta" lw 1.5, \
	 filename2  using 3 title "Chord x10" smooth csplines with lines  lc rgb "red" lw 1.5, \
	 filename3  using 3 title "Chord x100" smooth csplines with lines  lc rgb "dark-red" lw 1.5, \
	 filename1  using 2 title "Physical" smooth csplines with lines  lc rgb "forest-green" lw 1.5, @dummy, @dummy

 

# no smoothing
#plot datafile using 1 title "Koala" with lines, \
#	 datafile using 2 title "Physical" with lines  lc rgb "forest-green", \
#	 datafile using 3 title "Chord" with lines  lc rgb "blue"
	 
pause mouse close