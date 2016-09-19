#!/bin/bash

#plot the size of the routing table
#gnuplot  -e "filename='../out/koala/rtA0.5.dat'" plotRT.plt &


#plot the renater (physical) topology
#gnuplot -e "filename='../out/renater/topologyA0.5.dat'" plotRenater.plt &


#plot the koala topology (there are some problems with this one)
#gnuplot -e "filename='../out/koala/topologyA0.5.dat'" plotKoala.plt &


group=200
rm -f /tmp/*.mpl

#plot occurrences in message paths
f1="/tmp/$RANDOM.mpl"
#./path_occurrences.py -f '../out/results/resultsA0.5.dat' -n 1 > $f1 ; gnuplot -e "filename='$f1'" plotPathOccurrence.plt &

#comparision of latency for the 3 protocols 
f1="/tmp/$RANDOM.mpl"
#./group.py -f '../out/results/resultsA0.5.dat' -n $group > $f1 ; gnuplot -e "filename='$f1'" plotLatency.plt &

#comparision of hops for the 3 protocols
f1="/tmp/$RANDOM.mpl" 
#./group.py -f '../out/results/resultsA0.5.dat' -n $group > $f1 ; gnuplot -e "filename='$f1'" plotHops.plt &

group=500

#comparision of latency when ALPHA changes
f1="/tmp/$RANDOM.mpl"
f2="/tmp/$RANDOM.mpl"
f3="/tmp/$RANDOM.mpl"
./group.py -f '../out/results/resultsA0.0.dat' -n $group > $f1 ;\
./group.py -f '../out/results/resultsA0.5.dat' -n $group > $f2 ;\
./group.py -f '../out/results/resultsA1.0.dat' -n $group > $f3 ;\
gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'" plotLatencyComparison.plt &

#comparision of latency when ALPHA changes (averaged)
#f1="/tmp/$RANDOM.mpl"
#f2="/tmp/$RANDOM.mpl"
#f3="/tmp/$RANDOM.mpl"
#./group.py -f '../out/results/AVGA0.0.dat' -n $group > $f1 ;\
#./group.py -f '../out/results/AVGA0.5.dat' -n $group > $f2 ;\
#./group.py -f '../out/results/AVGA1.0.dat' -n $group > $f3 ;\
#gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'" plotLatencyComparison.plt &


#comparision of latency when ALPHA changes with 5 alphas
#f1="/tmp/$RANDOM.mpl"
#f2="/tmp/$RANDOM.mpl"
#f3="/tmp/$RANDOM.mpl"
#f4="/tmp/$RANDOM.mpl"
#f5="/tmp/$RANDOM.mpl"
#./group.py -f '../out/results/resultsA0.0.dat' -n $group > /tmp/la1.mpl ;\
#./group.py -f '../out/results/resultsA0.5.dat' -n $group > /tmp/la2.mpl ;\
#./group.py -f '../out/results/resultsA1.0.dat' -n $group > /tmp/la3.mpl ;\
#./group.py -f '../out/results/resultsA0.25.dat' -n $group > /tmp/la4.mpl ;\
#./group.py -f '../out/results/resultsA0.75.dat' -n $group > /tmp/la5.mpl ;\
#gnuplot -e "filename1='/tmp/la1.mpl'; filename2='/tmp/la2.mpl'; filename3='/tmp/la3.mpl'; filename4='/tmp/la4.mpl'; filename5='/tmp/la5.mpl'" plotLatencyComparison.plt &



#comparision of hops when ALPHA changes
f1="/tmp/$RANDOM.mpl"
f2="/tmp/$RANDOM.mpl"
f3="/tmp/$RANDOM.mpl"
./group.py -f '../out/results/resultsA0.0.dat' -n $group > $f1 ;\
./group.py -f '../out/results/resultsA0.5.dat' -n $group > $f2 ;\
./group.py -f '../out/results/resultsA1.0.dat' -n $group > $f3 ;\
gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'" plotHopsComparison.plt &


#comparision of hops when ALPHA changes (averages)
#f1="/tmp/$RANDOM.mpl"
#f2="/tmp/$RANDOM.mpl"
#f3="/tmp/$RANDOM.mpl"
#./group.py -f '../out/results/AVGA0.0.dat' -n $group > $f1 ;\
#./group.py -f '../out/results/AVGA0.5.dat' -n $group > $f2 ;\
#./group.py -f '../out/results/AVGA1.0.dat' -n $group > $f3 ;\
#gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'" plotHopsComparison.plt &



lastn=2000

#latency based on message categories
#./categories.py -f '../out/results/resultsA0.5.dat' -c 'lat' -n $lastn > /tmp/lcat.mpl ;\
#gnuplot -e "filename='/tmp/lcat.mpl'" plotLatencyCategories.plt &

#hops based on message categories
#./categories.py -f '../out/results/resultsA0.5.dat' -c 'hop' -n $lastn > /tmp/hcat.mpl ;\
#gnuplot -e "filename='/tmp/hcat.mpl'" plotHopsCategories.plt &



