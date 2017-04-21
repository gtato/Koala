#!/usr/bin/gnuplot

reset
unset key

set key above




if (!exists("outfile")) outfile='out/exp1latency.pdf'

set terminal pdf
set output outfile

#set title "Comparison of latencies"
set xlabel "Time (1K cycles)"
#set ylabel 'Latency (ms)'


titler = "Physical"
titlec = "Chord"
title1 = "Koala A=0"
title2 = "Koala A=0.25"
title3 = "Koala A=0.5"
title4 = "Koala A=0.75"
title5 = "Koala A=1"

bm = 0.15

#lm = 0.12
lm = 0.10
rm = 0.95
gap = 0.03
size = 0.81
y1 = 0.0; y2 = 11.5; y3 = 21; y4 = 54
tm = 0.7 #bm + size * (abs(y2-y1) / (abs(y2-y1) + abs(y4-y3) ) )

set multiplot
set border 1+2+8
set xtics nomirror
#set ytics nomirror
set lmargin at screen lm
set rmargin at screen rm
set bmargin at screen bm
set tmargin at screen tm


set yrange [y1:y2]
#plot [0:40] log(x)*10

plot filename6 using 6 title titlec smooth csplines with lines lc rgb "red"  lw 1, \
	0 title title1 smooth csplines with lines lc rgb "orange" lw 1 	, \
	filename2 using 7 title title2 smooth csplines with lines lc rgb "blue"  lw 1, \
	filename3 using 7 title title3 smooth csplines with lines lc rgb "brown"  lw 1 ,\
 	filename4 using 7 title title4 smooth csplines with lines lc rgb "gray" lw 1 ,\
 	filename5 using 7 title title5 smooth csplines with lines lc rgb "violet" lw 1 


unset xtics
unset xlabel
unset key
set border 2+4+8
set bmargin at screen tm + gap
set tmargin at screen bm + size + gap
set yrange [y3:y4]

set label 'Hops' at screen 0.03, bm + 0.5 * (size + gap) offset 0,-strlen("Hops")/4.0 rotate by 90

set arrow from screen lm - gap / 4.0, tm - gap / 4.0 to screen \
lm + gap / 4.0, tm + gap / 4.0 nohead

set arrow from screen lm - gap / 4.0, tm - gap / 4.0  + gap to screen \
lm + gap / 4.0, tm + gap / 4.0 + gap nohead

set arrow from screen rm - gap / 4.0, tm - gap / 4.0 to screen \
rm + gap / 4.0, tm + gap / 4.0 nohead

set arrow from screen rm - gap / 4.0, tm - gap / 4.0  + gap to screen \
rm + gap / 4.0, tm + gap / 4.0 + gap nohead

plot filename1 using 7 title title1 smooth csplines with lines lc rgb "orange"  lw 1


unset multiplot


pause mouse close
