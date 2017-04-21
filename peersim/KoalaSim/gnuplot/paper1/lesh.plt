reset
unset key
bm = 0.15
lm = 0.12
rm = 0.95
gap = 0.03
size = 0.75
y1 = 0.0; y2 = 11.5; y3 = 58.5; y4 = 64.0

set multiplot
set xlabel 'Time [ns]'
set border 1+2+8
set xtics nomirror
set ytics nomirror
set lmargin at screen lm
set rmargin at screen rm
set bmargin at screen bm
set tmargin at screen bm + size * (abs(y2-y1) / (abs(y2-y1) + abs(y4-y3) ) )

set yrange [y1:y2]
plot [0:40] 20.0*atan(x-20.0) + 32 + sin(x)

#unset xtics
#unset xlabel
#set border 2+4+8
#set bmargin at screen bm + size * (abs(y2-y1) / (abs(y2-y1) + abs(y4-y3) ) ) + gap
#set tmargin at screen bm + size + gap
#set yrange [y3:y4]

#set label 'Power [mW]' at screen 0.03, bm + 0.5 * (size + gap) offset 0,-strlen("Power [mW]")/4.0 rotate by 90

#set arrow from screen lm - gap / 4.0, bm + size * (abs(y2-y1) / (abs(y2-y1)+abs(y4-y3) ) ) - gap / 4.0 to screen \
#lm + gap / 4.0, bm + size * (abs(y2-y1) / (abs(y2-y1) + abs(y4-y3) ) ) + gap / 4.0 nohead

#set arrow from screen lm - gap / 4.0, bm + size * (abs(y2-y1) / (abs(y2-y1)+abs(y4-y3) ) ) - gap / 4.0  + gap to screen \
#lm + gap / 4.0, bm + size * (abs(y2-y1) / (abs(y2-y1) + abs(y4-y3) ) ) + gap / 4.0 + gap nohead

#set arrow from screen rm - gap / 4.0, bm + size * (abs(y2-y1) / (abs(y2-y1)+abs(y4-y3) ) ) - gap / 4.0 to screen \
#rm + gap / 4.0, bm + size * (abs(y2-y1) / (abs(y2-y1) + abs(y4-y3) ) ) + gap / 4.0 nohead

#set arrow from screen rm - gap / 4.0, bm + size * (abs(y2-y1) / (abs(y2-y1)+abs(y4-y3) ) ) - gap / 4.0  + gap to screen \
#rm + gap / 4.0, bm + size * (abs(y2-y1) / (abs(y2-y1) + abs(y4-y3) ) ) + gap / 4.0 + gap nohead

#plot [0:40] 20.0*atan(x-20.0) + 32 + sin(x)

unset multiplot

pause mouse close
