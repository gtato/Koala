#!/bin/bash

#plot the size of the routing table
#gnuplot  -e "filename='../out/koala/rtA0.5.dat'" plotRT.plt &


#plot the renater (physical) topology
#gnuplot -e "filename='../out/renater/topologyA0.5.dat'" plotRenater.plt &


#plot the koala topology (there are some problems with this one)
#gnuplot -e "filename='../out/koala/topologyA0.5.dat'" plotKoala.plt &


group=100
rm -f /tmp/*.mpl

#comparision of latency for the 3 protocols 
#./group.py -f '../out/results/resultsA0.5.dat' -n $group > /tmp/lt.mpl ; gnuplot -e "filename='/tmp/lt.mpl'" plotLatency.plt &

#comparision of hops for the 3 protocols 
#./group.py -f '../out/results/resultsA0.5.dat' -n $group > /tmp/hop.mpl ; gnuplot -e "filename='/tmp/hop.mpl'" plotHops.plt &

#comparision of latency when ALPHA changes
#./group.py -f '../out/results/resultsA0.0.dat' -n $group > /tmp/la1.mpl ;\
#./group.py -f '../out/results/resultsA0.5.dat' -n $group > /tmp/la2.mpl ;\
#./group.py -f '../out/results/resultsA1.0.dat' -n $group > /tmp/la3.mpl ;\
#gnuplot -e "filename1='/tmp/la1.mpl'; filename2='/tmp/la2.mpl'; filename3='/tmp/la3.mpl'" plotLatencyComparison.plt &


#comparision of latency when ALPHA changes with 5 alphas
#./group.py -f '../out/results/resultsA0.0.dat' -n $group > /tmp/la1.mpl ;\
#./group.py -f '../out/results/resultsA0.5.dat' -n $group > /tmp/la2.mpl ;\
#./group.py -f '../out/results/resultsA1.0.dat' -n $group > /tmp/la3.mpl ;\
#./group.py -f '../out/results/resultsA0.25.dat' -n $group > /tmp/la4.mpl ;\
#./group.py -f '../out/results/resultsA0.75.dat' -n $group > /tmp/la5.mpl ;\
#gnuplot -e "filename1='/tmp/la1.mpl'; filename2='/tmp/la2.mpl'; filename3='/tmp/la3.mpl'; filename4='/tmp/la4.mpl'; filename5='/tmp/la5.mpl'" plotLatencyComparison.plt &



#comparision of hops when ALPHA changes
#./group.py -f '../out/results/resultsA0.0.dat' -n $group > /tmp/ha1.mpl ;\
#./group.py -f '../out/results/resultsA0.5.dat' -n $group > /tmp/ha2.mpl ;\
#./group.py -f '../out/results/resultsA1.0.dat' -n $group > /tmp/ha3.mpl ;\
#gnuplot -e "filename1='/tmp/ha1.mpl'; filename2='/tmp/ha2.mpl'; filename3='/tmp/ha3.mpl'" plotHopsComparison.plt &

lastn=2000

#latency based on message categories
#./categories.py -f '../out/results/resultsA0.5.dat' -c 'lat' -n $lastn > /tmp/lcat.mpl ;\
#gnuplot -e "filename='/tmp/lcat.mpl'" plotLatencyCategories.plt &

#hops based on message categories
#./categories.py -f '../out/results/resultsA0.5.dat' -c 'hop' -n $lastn > /tmp/hcat.mpl ;\
#gnuplot -e "filename='/tmp/hcat.mpl'" plotHopsCategories.plt &



