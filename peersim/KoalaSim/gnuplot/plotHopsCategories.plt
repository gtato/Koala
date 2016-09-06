set title "Hop categories"
if (!exists("filename")) filename='../out/results/results.dat'
C = "forest-green"; K = "#4671d5"
set auto x
set yrange [ 0 : * ]
set ylabel 'Latency'
set style data histogram
set style histogram cluster gap 1
set style fill solid border -1
set boxwidth 0.5
set xtic scale 0
# 2, 3, 4, 5 are the indexes of the columns; 'fc' stands for 'fillcolor'
plot filename using 2:xtic(1) ti col fc rgb C, '' u 3 ti col fc rgb K

pause mouse close
