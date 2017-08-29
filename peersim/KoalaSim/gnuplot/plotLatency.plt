#!/usr/bin/gnuplot


if (!exists("filename")) filename='../out/results/results.dat'


set title "Comparison of latencies"
set xlabel "Time"
set ylabel 'Latency'
set key above width -8 maxrows 4
#set ytics "10"
set yrange [ 0 : * ]
unset colorbox
datafile = filename

#plot "<(tail -n +2  ".koalafile.")"  using 4 title "Koala" smooth csplines with lines lc rgb "blue" , \
#	"<(tail -n +2  ".datafile.")"  using 5 title "Flatoala" smooth csplines with lines lc rgb "orange" , \
#	"<(tail -n +2  ".datafile.")"  using 6 title "Hierarkoala" smooth csplines with lines lc rgb "violet" , \
## 	"<(tail -n +2  ".datafile.")"  using 3 title "Chord" smooth csplines with lines lc rgb "red" , \
#	"<(tail -n +2  ".datafile.")"  using 2 title "Physical" smooth csplines with lines  lc rgb "forest-green"

plot koala0 using 4 title "Koala no collaboration" smooth csplines with lines lc rgb "blue"  , \
	koala2  using 4 title "Koala col 2" smooth csplines with lines dt 2 lc rgb "blue" , \
	koala4  using 4 title "Koala col 4" smooth csplines with lines dt 3 lc rgb "blue", \
	koala8  using 4 title "Koala col 8" smooth csplines with lines dt 4 lc rgb "blue" , \
	koala16  using 4 title "Koala col 16" smooth csplines with lines dt 5 lc rgb "blue", \
	koala32  using 4 title "Koala col 32" smooth csplines with lines dt "-" lc rgb "blue" , \
	datafile  using 5 title "Flatoala" smooth csplines with lines lc rgb "orange" , \
	datafile  using 6 title "Hierarkoala" smooth csplines with lines lc rgb "violet" , \
	datafile  using 2 title "Physical" smooth csplines with lines  lc rgb "forest-green"



# no smoothing
#plot "<(tail -n +2  ".koalafile.")"  using 12 title "Koala"  with lines lc rgb "blue" , \
#	"<(tail -n +2  ".datafile.")"  using 13 title "Flatoala"  with lines lc rgb "orange"
#	"<(tail -n +2  ".datafile.")"  using 6 title "Hierarkoala"  with lines lc rgb "violet" , \
#	"<(tail -n +2  ".datafile.")"  using 3 title "Chord"  with lines lc rgb "red" , \
#	"<(tail -n +2  ".datafile.")"  using 2 title "Physical"  with lines  lc rgb "forest-green"
	 
pause mouse close