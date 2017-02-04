#!/usr/bin/gnuplot


if (!exists("filename1")) filename1='../out/results/resultsA0.0.dat'
if (!exists("filename2")) filename2='../out/results/resultsA0.5.dat'
if (!exists("filename3")) filename3='../out/results/resultsA1.0.dat'

if (!exists("filename4")) filename4='../out/results/resultsA0.25.dat'
if (!exists("filename5")) filename5='../out/results/resultsA0.75.dat'
if (!exists("filename6")) filename6='../out/results/resultsA-1.0.dat'

set title "Comparison of latencies"
set xlabel "Time"
set ylabel 'Latency'


titler = "Physical"
titlec = "Chord"
title1 = "Koala A=0"
title2 = "Koala A=0.5"
title3 = "Koala A=1"
title4 = "Koala A=0.25"
title5 = "Koala A=0.75"
title6 = "Koala random"


#set ytics "10"
# set yrange [ 0 : * ]
set logscale y

#firstrows = system('head -2 '.datafile)
#set title substr(firstrows, 0, strstrt(firstrows, "\n"))
#set ylabel substr(firstrows, strstrt(firstrows, "\n")+1, strlen(firstrows))

plot filename1 using 2 title titler smooth csplines with lines lc rgb "forest-green" lw 1, \
	 filename3 using 3 title titlec smooth csplines with lines lc rgb "red"  lw 1, \
	 filename1 using 4 title title1 smooth csplines with lines lc rgb "web-blue"  lw 1, \
	 filename2 using 4 title title2 smooth csplines with lines lc rgb "blue"  lw 1, \
	 filename3 using 4 title title3 smooth csplines with lines lc rgb "navy"  lw 1 ,\
 	 filename4 using 4 title title4 smooth csplines with lines lc rgb "black" lw 1,\
 	 filename5 using 4 title title5 smooth csplines with lines lc rgb "black" lw 1 #,\
# 	 filename6 using 4 title title6 smooth csplines with lines lc rgb "black"
	 
pause mouse close