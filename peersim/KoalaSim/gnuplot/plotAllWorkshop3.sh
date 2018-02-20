#!/bin/bash

rm -f /tmp/*.mpl


f0="/tmp/$RANDOM.mpl"
# ./group.py -f '../out/results/results.C2.RC0.VC0.0.1000x1.CCL10K.COL0.T100.A1.0.dat' -n $group > $f0
#  gnuplot -e "koala0='$f0';" workshopLatency.plt &
gnuplot -e "koala0='../out/results/workshop1/results.C4.RC0.VC0.0.1000x1.CCL10K.COL0.T100.A1.0.dat'; koala1='$f0'" workshopExp1Latency.plt &
gnuplot -e "koala0='../out/results/workshop1/results.C4.RC0.VC0.0.1000x1.CCL10K.COL0.T100.A1.0.dat'; koala1='$f0'" workshopExp1Hops.plt &

#f1="/tmp/$RANDOM.mpl"
#./boxplot.py -f '../out/results/workshop3/results.C2.RC0.VC0.0.1000x10.CCL120K.COL0.T100.A1.0.dat' -c 4 #> $f1
#gnuplot -e "f1='$f1';" workshopExp3Latency.plt &
# 
# f1="/tmp/$RANDOM.mpl"
# ./boxplot.py -f '../out/results/workshop2/results.C2.RC0.VC0.0.1000x1.CCL10K.COL0.T100.A1.0.dat,../out/results/workshop2/results.C2.RC0.VC0.0.1000x1.CCL10K.COL0.T100.A0.5.dat' -c 13 > $f1
# gnuplot -e "f1='$f1';" workshopExp2Hops.plt &

# gnuplot -e "koala0='../out/results/workshop3/results.C1.RC0.VC0.0.1000x100.CCL50K.COL0.T100.A0.5.dat';" workshopExp3Latency.plt &
# gnuplot -e "koala0='../out/results/workshop3/results.C2.RC0.VC0.0.1000x100.CCL50K.COL0.T100.A0.5.dat';" workshopExp3Latency.plt &
# gnuplot -e "koala0='../out/results/workshop3/results.C4.RC0.VC0.0.1000x100.CCL50K.COL0.T100.A0.5.dat';" workshopExp3Latency.plt &


# s=90000
# e=100000
# fn='../out/results/workshop3/results.C4.RC0.VC0.0.1000x100.CCL50K.COL0.T100.A0.5.dat'
# # fn='../out/results/results.C4.RC0.VC0.0.1000x100.CCL500K.COL0.T100.A0.5.dat'
# f1="/tmp/$RANDOM.mpl"
# ./boxplot.py -f $fn -c 4 -s $s -n "SR-flat" >> $f1 #single latency
# ./boxplot.py -f $fn -c 3 -s $s -n "MR-flat" >> $f1 #multi latency
# ./boxplot.py -f $fn -c 5 -s $s -n "hierarchical" >> $f1 #hiearchical latency
# gnuplot -e "f1='$f1'" workshopExp3Latency.plt &
# 
# f1="/tmp/$RANDOM.mpl"
# f2="/tmp/$RANDOM.mpl"
# 
# ./boxplot.py -f $fn -c 13 -s $s -n "SR-flat" >> $f1 #single hop
# ./boxplot.py -f $fn -c 11 -s $s -n "MR-flat" >> $f1 #multi hop
# ./boxplot.py -f $fn -c 15 -s $s -n "hierarchical" >> $f1 #hiearchical hop
# ./boxplot.py -f $fn -c 12 -s $s -n "SR-flat" >> $f2 #single hop
# ./boxplot.py -f $fn -c 10 -s $s -n "MR-flat" >> $f2 #multi hop
# ./boxplot.py -f $fn -c 14 -s $s -n "hierarchical" >> $f2 #hiearchical hop
# gnuplot -e "f1='$f1'; f2='$f2'" workshopExp3Hops.plt &