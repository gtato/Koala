#!/usr/bin/gnuplot



#if (!exists("outfile")) outfile='paper1/out/norandom.pdf'
#set terminal pdf
#set output outfile

#set title "Comparison of latencies"
set xlabel "Time (1K cycles)"
set ylabel 'Latency (ms)'

set ytics "100"
set yrange [ 0 : 1100 ]
#set key horizontal 
#set key at 250,300 cent


#set key maxrows 3
#firstrows = system('head -2 '.datafile)
#set title substr(firstrows, 0, strstrt(firstrows, "\n"))
#set ylabel substr(firstrows, strstrt(firstrows, "\n")+1, strlen(firstrows))
set macros
dummy="NaN title ' ' lt -3"

plot koala10  using 4 title "Koala x10" smooth csplines with lines lc rgb "blue" dt 1 lw 1.5, \
	 koala100 using 4 title "Koala x100" smooth csplines with lines lc rgb "blue" dt 2 lw 1.5, \
	 koala10  using 5 title "Flat x10" smooth csplines with lines  lc rgb "forest-green" dt 1 lw 1.5, \
	 koala100  using 5 title "Flat x100" smooth csplines with lines  lc rgb "forest-green" dt 2 lw 1.5, \
	 koala10  using 6 title "Hier x10" smooth csplines with lines  lc rgb "red" dt 1 lw 1.5, \
	 koala100  using 6 title "Hier x100" smooth csplines with lines  lc rgb "red" dt 2 lw 1.5, \
	 koala10  using 2 title "Physical" smooth csplines with lines  lc rgb "grey" lw 1.5 #, @dummy, @dummy


#	 koala1000  using 5 title "Koala x1000" smooth csplines with lines lc rgb "dark-blue" lw 1.5, \
#	 koala10  using 3 title "Flat x10" smooth csplines with lines  lc rgb "magenta" lw 1.5, \
#	 koala100  using 3 title "Flat x100" smooth csplines with lines  lc rgb "red" lw 1.5, \
#	 koala1000  using 3 title "Flat x1000" smooth csplines with lines  lc rgb "dark-red" lw 1.5, \
#	 koala10  using 3 title "Hier x10" smooth csplines with lines  lc rgb "magenta" lw 1.5, \
#	 koala100  using 3 title "Hier x100" smooth csplines with lines  lc rgb "red" lw 1.5, \
#	 koala1000  using 3 title "Hier x1000" smooth csplines with lines  lc rgb "dark-red" lw 1.5, \

 

# no smoothing
#plot datafile using 1 title "Koala" with lines, \
#	 datafile using 2 title "Physical" with lines  lc rgb "forest-green", \
#	 datafile using 3 title "Chord" with lines  lc rgb "blue"
	 
pause mouse close