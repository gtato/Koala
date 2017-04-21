#!/bin/bash

rm -f /tmp/*.mpl



group=100



####################  EXP1  ########################3
#comparision of koala latency for different C
f1="tmp/1f1.mpl"
f2="tmp/1f2.mpl" 
f3="tmp/1f3.mpl" 
#./group.py -f '../exp1/resultsC1A0.5.dat' -n $group > $f1 ;\
#./group.py -f '../exp1/resultsC2A0.5.dat' -n $group > $f2 ;\
#./group.py -f '../exp1/resultsC4A0.5.dat' -n $group > $f3 ;\
#gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'; outfile='out/exp1latency_nosmooth.pdf' " latency.plt &

#comparision of koala hops for different C
#gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'; outfile='out/exp1hops_nosmooth.pdf' " hops.plt &


group=1000
####################  EXP2.1  ########################3

#comparision of koala latency for different C
f1="tmp/2.1f1.mpl"
f2="tmp/2.1f2.mpl" 
f3="tmp/2.1f3.mpl" 
#./group.py -f '../exp2.1/resultsC1A1.0.dat' -n $group > $f1 ;\
#./group.py -f '../exp2.1/resultsC2A1.0.dat' -n $group > $f2 ;\
#./group.py -f '../exp2.1/resultsC4A1.0.dat' -n $group > $f3 ;\
#gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'; outfile='out/exp2.1latency.pdf'" latency.plt &

#comparision of koala hops for different C
#gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'; outfile='out/exp2.1hops.pdf'" hops.plt &


####################  EXP2.2  ########################3
f1="tmp/2.2f1.mpl"
f2="tmp/2.2f2.mpl"
f3="tmp/2.2f3.mpl"
f4="tmp/2.2f4.mpl"
f5="tmp/2.2f5.mpl"
f6="tmp/2.2f6.mpl"
#./group.py -f '../exp2.2/resultsC2A0.0.dat' -n $group > $f1 ;\
#./group.py -f '../exp2.2/resultsC2A0.25.dat' -n $group > $f2 ;\
#./group.py -f '../exp2.2/resultsC2A0.5.dat' -n $group > $f3 ;\
#./group.py -f '../exp2.2/resultsC2A0.75.dat' -n $group > $f4 ;\
#./group.py -f '../exp2.2/resultsC2A1.0.dat' -n $group > $f5 ;\
#./group.py -f '../exp2.1/resultsC1A1.0.dat' -n $group > $f6 ;\
#gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'; filename4='$f4'; filename5='$f5'; filename6='$f6'; outfile='out/exp2.2latency.pdf'" plotLatencyComparison.plt &

#gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'; filename4='$f4'; filename5='$f5'; filename6='$f6'; outfile='out/exp2.2hops.pdf'" plotHopsComparison.plt &


####################  EXP3  ########################
group=200
# f1="tmp/3.1f1.mpl"
# f2="tmp/3.1f2.mpl"
# f3="tmp/3.1f3.mpl"
# f4="tmp/3.1f4.mpl"
# ./group_fails.py -f '../exp3/resultsC2CH0A0.5.dat' -n $group  > $f1 ;\
# ./group_fails.py -f '../exp3/resultsC2CH2A0.5.dat' -n $group  > $f2 ;\
# ./group_fails.py -f '../exp3/resultsC2CH4A0.5.dat' -n $group  > $f3 ;\
# ./group_fails.py -f '../exp3/resultsC2CH8A0.5.dat' -n $group  > $f4 ;\
# gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'; filename4='$f4'; outfile='out/exp3.1latency.pdf'" churn.plt &
#gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'; filename4='$f4'; outfile='out/exp3.1fails.pdf'" fails.plt &

group=1000
f1="tmp/3.11f1.mpl"
f2="tmp/3.11f2.mpl"
f3="tmp/3.11f3.mpl"
f4="tmp/3.11f4.mpl"
f5="tmp/3.11f5.mpl"
./group_fails.py -f '../exp3.1/resultsC2CH0A0.5.dat' -n $group  > $f1 ;\
./group_fails.py -f '../exp3.1/resultsC2CH1A0.5.dat' -n $group  > $f2 ;\
./group_fails.py -f '../exp3.1/resultsC2CH2A0.5.dat' -n $group  > $f3 ;\
./group_fails.py -f '../exp3.1/resultsC2CH4A0.5.dat' -n $group  > $f4 ;\
./group_fails.py -f '../exp3.1/resultsC2CH8A0.5.dat' -n $group  > $f5 ;\
gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'; filename4='$f4'; filename5='$f5'; outfile='out/exp3.11latency.pdf'" churn.plt &
gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'; filename4='$f4'; filename5='$f5'; outfile='out/exp3.11fails.pdf'" fails.plt &




group=1000
f1="tmp/3.22f1.mpl"
f2="tmp/3.22f2.mpl"
f3="tmp/3.22f3.mpl"
f4="tmp/3.22f4.mpl"
f5="tmp/3.22f5.mpl"
f6="tmp/3.22f6.mpl"
f7="tmp/3.22f7.mpl"
# ./group.py -f '../exp3.2/0.dat' -n $group  > $f1 ;\
# ./group.py -f '../exp3.2/0.0625.dat' -n $group  > $f2 ;\
# ./group.py -f '../exp3.2/0.125.dat' -n $group  > $f3 ;\
# ./group.py -f '../exp3.2/0.25.dat' -n $group  > $f4 ;\
# ./group.py -f '../exp3.2/0.5.dat' -n $group  > $f5 ;\
# ./group.py -f '../exp3.2/0.75.dat' -n $group  > $f6 ;\
# ./group.py -f '../exp3.2/0.95.dat' -n $group  > $f7 ;\
#gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'; filename4='$f4'; filename5='$f5'; filename6='$f6'; filename7='$f7'; outfile='out/exp3.22latency.pdf'" churn2.plt &
# gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'; filename4='$f4'; filename5='$f5'; filename6='$f6'; filename7='$f7'; outfile='out/exp3.22fails.pdf'" fails2.plt &
