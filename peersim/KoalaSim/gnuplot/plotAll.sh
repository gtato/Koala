#!/bin/bash

rm -f /tmp/*.mpl

group=1000

# gnuplot -e "koala0='../out/results/results.C2.0.1000x1.CCL50K.COL0.T100.A0.5.dat'; \
# koala1='../out/results/results.C1.0.VC1.0.1000x1.CCL50K.COL0.T100.A0.5.dat';" \
# newplots/plotLatency.plt &

p='../out/results/results'
fkl=5 #flat koala latency
fklh=13 #flat koala local hops
fkgh=14 #flat koala global hops

cl=3 #chord latency
clh=9 # chord local hops
cgh=10 # chord global hops

phl=2 #physical latency


p='../out/results/icfec/exp1/results'
false && ./plot.py -x "Time (1K cycles)" -y "Latency" -s "group:$group" -o "exp2.2l" \
		  -f "$p.C2.0.10000x1.CCL200K.COL0.T100.A0.5.dat:$fkl:Cll=2:brown,
 		      $p.C1.0.VC1.0.10000x1.CCL200K.COL0.T100.A0.5.dat:$fkl:Cll=1 Cpl=1:web-blue,
 		      $p.C1.0.RC1.0.10000x1.CCL200K.COL0.T100.A0.5.dat:$fkl:Cll=1 Cvl=1:red,
 		      $p.C1.0.AC1.0.10000x1.CCL200K.COL0.T100.A0.5.dat:$fkl:Cll=1 Cal=1:orange,
 		      $p.C1.0.AC1.0.10000x1.CCL200K.COL0.T100.A0.5.dat:$phl:Physical:forest-green"


false && ./plot.py -x "Time (1K cycles)" -y "Hops" -s "group:$group" -o "exp2.2h" \
		  -f "$p.C2.0.10000x1.CCL200K.COL0.T100.A0.5.dat:$fkgh:Cll=2:brown,
 		      $p.C1.0.VC1.0.10000x1.CCL200K.COL0.T100.A0.5.dat:$fkgh:Cll=1 Cpl=1:web-blue,
 		      $p.C1.0.RC1.0.10000x1.CCL200K.COL0.T100.A0.5.dat:$fkgh:Cll=1 Cvl=1:red,
 		      $p.C1.0.AC1.0.10000x1.CCL200K.COL0.T100.A0.5.dat:$fkgh:Cll=1 Cal=1:orange"


p='../out/results/icfec/exp2/results'
true && ./plot.py -x "Time (1K cycles)" -y "Latency" -s "group:$group"  \
		  -f "$p.C1.0.VC1.0.10000x1.CCL200K.COL0.T100.A0.25.dat:$fkl:Cll=1 Cpl=1:web-blue,
 		      $p.C1.0.VC1.0.10000x1.CCL200K.COL0.T100.A0.5.dat:$fkl:Cll=1 Cvl=1:red,
 		      $p.C1.0.VC1.0.10000x1.CCL200K.COL0.T100.A0.75.dat:$fkl:Cll=1 Cal=1:orange,
 		      $p.C1.0.VC1.0.10000x1.CCL200K.COL0.T100.A0.5.dat:$phl:Physical:forest-green"




p='../out/results/icfec/exp4/results'
false && ./plot.py -x "Time (1K cycles)" -y "Latency" -s "group:$group" -o "exp4l" \
		  -f "$p.C1.0.VC1.0.1000x10.CCL200K.COL0.T100.norand.A0.5.dat:$fkl:Koala consec.:web-blue,
 		      $p.C1.0.VC1.0.1000x10.CCL200K.COL0.T100.norand.A0.5.dat:$cl:Chord consec.:red,
 		      $p.C1.0.VC1.0.1000x10.CCL200K.COL0.T100.rand.A0.5.dat:$fkl:Koala rand.:blue,
 		      $p.C1.0.VC1.0.1000x10.CCL200K.COL0.T100.rand.A0.5.dat:$cl:Chord rand.:brown,
 		      $p.C1.0.VC1.0.1000x10.CCL200K.COL0.T100.norand.A0.5.dat:$phl:Physical:forest-green"


false && ./plot.py -x "Time (1K cycles)" -y "Hops" -s "group:$group" -o "exp4h" \
		  -f "$p.C1.0.VC1.0.1000x10.CCL200K.COL0.T100.norand.A0.5.dat:$fklh:Koala local:web-blue,
 		      $p.C1.0.VC1.0.1000x10.CCL200K.COL0.T100.norand.A0.5.dat:$fkgh:Koala global:blue,
 		      $p.C1.0.VC1.0.1000x10.CCL200K.COL0.T100.norand.A0.5.dat:$clh:Chord local:red,
 		      $p.C1.0.VC1.0.1000x10.CCL200K.COL0.T100.norand.A0.5.dat:$cgh:Chord global:brown"





# ./group.py -f "$p.C2.0.1000x1.CCL50K.COL0.T100.A0.5.dat" -n $group


#plot the size of the routing table
#gnuplot  -e "filename='../out/koala/rtA0.5.dat'" plotRT.plt &

#plot how many nodes know a certain percentage of other nodes
#for i in `seq 1 10`;
#do
#f1="/tmp/$RANDOM.mpl"
#./ll_knowledge.py -f "../out/koala/rt_fineE${i}A-1.0.dat" -n 5 > $f1 ; gnuplot -e "filename='$f1'" plotLLKnowledge.plt &
#sleep 1
#done    

#for a in '-1.0' '0.0' '0.5' '1.0';
#do
#f1="/tmp/$RANDOM.mpl"
#./ll_knowledge.py -f "../out/koala/AVGA${a}.dat" -n 5 > $f1 ; gnuplot -e "filename='$f1'" plotLLKnowledge.plt &
#sleep 1
#done




#plot the renater (physical) topology
#gnuplot -e "filename='../out/renater/topologyA0.5.dat'" plotRenater.plt &


#plot the koala topology (there are some problems with this one)
#gnuplot -e "filename='../out/koala/topologyA0.5.dat'" plotKoala.plt &

# group=22
# plot occurrences in message paths
# f1="/tmp/$RANDOM.mpl"
# f2="/tmp/$RANDOM.mpl"
#./path_occurrences.py -f '../out/results/resultsA0.5.dat' -n 1 > $f1 ; gnuplot -e "filename='$f1'" plotPathOccurrence.plt &
#./path_occurrences.py -f '../out/results/resultsA0.5.dat' -n 5 -p 'r' > $f1 ; gnuplot -e "filename='$f1'" plotPathOccurrence.plt &
#./path_occurrences.py -f '../out/results/resultsA0.5.dat' -n 5 -p 'k' > $f2 ; gnuplot -e "filename='$f2'" plotPathOccurrence.plt &

# comparision of latency for the 3 protocols 
#  f0="/tmp/$RANDOM.mpl"
#  f2="/tmp/$RANDOM.mpl"
#  f4="/tmp/$RANDOM.mpl"
#  f8="/tmp/$RANDOM.mpl"
#  f16="/tmp/$RANDOM.mpl"
#  f32="/tmp/$RANDOM.mpl"         
#  ./group.py -f '../out/results/workshop3/results.C1.RC1.VC0.0.1000x100.CCL50K.COL0.T100.A0.5.dat' -n $group > $f0
#  ./group.py -f '../out/results/temp/results.C1.RC1.1000x100.CCL500K.COL2.T50.A0.5.dat' -n $group > $f2
#  ./group.py -f '../out/results/temp/results.C1.RC1.1000x100.CCL500K.COL4.T50.A0.5.dat' -n $group > $f4
#  ./group.py -f '../out/results/temp/results.C1.RC1.1000x100.CCL500K.COL8.T50.A0.5.dat' -n $group > $f8
#  ./group.py -f '../out/results/temp/results.C1.RC1.1000x100.CCL500K.COL16.T50.A0.5.dat' -n $group > $f16
#  ./group.py -f '../out/results/results.C1.RC1.1000x100.CCL500K.COL32.T50.A0.5.dat' -n $group > $f32
#  gnuplot -e "koala0='$f0'; koala2='$f2' ; koala4='$f4' ; koala8='$f8'; koala16='$f16'; koala32='$f32'" plotLatency.plt &
#  ./group.py -f '../out/results/resultsC1CH1A0.5.datnos10' -n $group > $f1 ; gnuplot -e "filename='$f1'" paper1/plotLatency.plt &


# f10="/tmp/$RANDOM.mpl"
# f20="/tmp/$RANDOM.mpl"
# f30="/tmp/$RANDOM.mpl"
# f40="/tmp/$RANDOM.mpl"
# f50="/tmp/$RANDOM.mpl"
# f60="/tmp/$RANDOM.mpl"
# f70="/tmp/$RANDOM.mpl"
# f80="/tmp/$RANDOM.mpl"
# f90="/tmp/$RANDOM.mpl"         
# ./group_help.py -f '../out/results/help.C1.RC1.1000x100.CCL500K.COL4.T10.A0.5.dat' -n $group > $f10
# ./group_help.py -f '../out/results/help.C1.RC1.1000x100.CCL500K.COL4.T20.A0.5.dat' -n $group > $f20
# ./group_help.py -f '../out/results/help.C1.RC1.1000x100.CCL500K.COL4.T30.A0.5.dat' -n $group > $f30
# ./group_help.py -f '../out/results/help.C1.RC1.1000x100.CCL500K.COL4.T40.A0.5.dat' -n $group > $f40
# ./group_help.py -f '../out/results/help.C1.RC1.1000x100.CCL500K.COL4.T50.A0.5.dat' -n $group > $f50
# ./group_help.py -f '../out/results/help.C1.RC1.1000x100.CCL500K.COL4.T60.A0.5.dat' -n $group > $f60
# ./group_help.py -f '../out/results/help.C1.RC1.1000x100.CCL500K.COL4.T70.A0.5.dat' -n $group > $f70
# ./group_help.py -f '../out/results/help.C1.RC1.1000x100.CCL500K.COL4.T80.A0.5.dat' -n $group > $f80
# ./group_help.py -f '../out/results/help.C1.RC1.1000x100.CCL500K.COL4.T90.A0.5.dat' -n $group > $f90
# gnuplot -e "koala10='$f10'; koala20='$f20' ; koala30='$f30' ; koala40='$f40'; koala50='$f50'; koala60='$f60'; koala70='$f70'; koala80='$f80'; koala90='$f90'" plotThreshold.plt &

# f10="/tmp/$RANDOM.mpl"
# f100="/tmp/$RANDOM.mpl"
# ./group.py -f '../out/results/results.C1.RC1.VC0.0.1000x100.CCL50K.COL0.T100.A0.5.dat' -n $group > $f10
# ./group.py -f '../out/results/results.C1.RC1.VC0.0.1000x100.CCL50K.COL0.T100.A0.5.dat' -n $group > $f100
# gnuplot -e "koala10='$f10'; koala100='$f100';" paper1/plotLatNodeXPoP.plt &

#f0="/tmp/$RANDOM.mpl"
#./group.py -f '../out/results/results.C2.RC0.VC0.0.1000x1.CCL10K.COL0.T100.A1.0.dat' -n $group > $f0
# gnuplot -e "koala0='$f0';" workshopLatency.plt &
# gnuplot -e "koala0='../out/results/results.C2.RC0.VC0.0.1000x1.CCL10K.COL0.T100.A1.0.dat'; koala1='$f0'" workshopExp1Latency.plt &
#gnuplot -e "koala0='../out/results/results.C2.RC0.VC0.0.1000x1.CCL10K.COL0.T100.A1.0.dat'; koala1='$f0'" workshopExp1Hops.plt &

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

# 
# s=90000
# e=100000
# # fn='../out/results/workshop3/results.C4.RC0.VC0.0.1000x100.CCL50K.COL0.T100.A0.5.dat'
# fn='../out/results/results.C4.RC0.VC0.0.1000x100.CCL500K.COL0.T100.A0.5.dat'
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




# f10="/tmp/$RANDOM.mpl"
# f100="/tmp/$RANDOM.mpl"
# ./group.py -f '../out/results/results.C1.RC1.1000x10.CCL100K.COL0.T50.A0.5.dat' -n $group > $f10
# ./group.py -f '../out/results/results.C1.RC1.1000x100.CCL50K.COL0.T50.A0.5.dat' -n $group > $f100
# gnuplot -e "datafile='$f100';" plotHopsCategories.plt &



# f1="/tmp/$RANDOM.mpl"
#./group.py -f '../out/results/resultsC1CH1A0.5.dat' -n $group > $f1 ; gnuplot -e "filename='$f1'" plotHopsCategories.plt &

#comparision of hops for the 3 protocols
# f1="/tmp/$RANDOM.mpl"
# f2="/tmp/$RANDOM.mpl" 
# ./group.py -f '../out/results/resultsA0.5.dat' -n $group > $f1 ; gnuplot -e "filename='$f1'" plotHops.plt &
# ./group.py -f '../out/results/ws1resultsA0.5.dat' -n $group > $f2 ; gnuplot -e "filename='$f2'" plotHops.plt &
# ./group.py -f '../../../../backup_out/results/resultsA1.0.dat' -n $group > $f1 ; gnuplot -e "filename='$f1'" plotHops.plt &

#group=10000
#group=200

# f1="/tmp/$RANDOM.mpl"
# f2="/tmp/$RANDOM.mpl"
# f3="/tmp/$RANDOM.mpl"
# ./group.py -f '../out/results/resultsC21000x1randA0.5.dat' -n $group > $f1 ;\
# ./group.py -f '../out/results/resultsC21000x10norandA0.5.dat' -n $group > $f2 ;\
# ./group.py -f '../out/results/resultsC21000x100norandA0.5.dat' -n $group > $f3 ;\
#  gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'; outfile='paper1/out/norandom.pdf'" paper1/plotLatency.plt &
# 
# f1="/tmp/$RANDOM.mpl"
# f2="/tmp/$RANDOM.mpl"
# f3="/tmp/$RANDOM.mpl"
# ./group.py -f '../out/results/resultsC21000x1randA0.5.dat' -n $group > $f1 ;\
# ./group.py -f '../out/results/resultsC21000x10randA0.5.dat' -n $group > $f2 ;\
# ./group.py -f '../out/results/resultsC21000x100randA0.5.dat' -n $group > $f3 ;\
# gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'; outfile='paper1/out/random.pdf'" paper1/plotLatency.plt &
 
# f1="/tmp/$RANDOM.mpl"
# f2="/tmp/$RANDOM.mpl"
# ./group.py -f '../out/results/resultsC21000x10randA0.5.dat' -n $group > $f1 ;\
# ./group.py -f '../out/results/resultsC21000x10norandA0.5.dat' -n $group > $f2 ;\
# gnuplot -e "filename1='$f1'; filename2='$f2'; outfile='paper1/out/interlac210.pdf'" paper1/plotLatencyLA.plt &
  

 
 
#comparision of latency when ALPHA changes
# f1="/tmp/$RANDOM.mpl"
# f2="/tmp/$RANDOM.mpl"
# f3="/tmp/$RANDOM.mpl"
# f4="/tmp/$RANDOM.mpl"
# f5="/tmp/$RANDOM.mpl"
# f6="/tmp/$RANDOM.mpl"
# ./group.py -f ~/exps/results/resultsA0.0.dat -n $group > $f1 ;\
# ./group.py -f ~/exps/results/resultsA0.5.dat -n $group > $f2 ;\
# ./group.py -f ~/exps/results/resultsA1.0.dat -n $group > $f3 ;\
# ./group.py -f ~/exps/results/resultsA0.25.dat -n $group > $f4 ;\
# ./group.py -f ~/exps/results/resultsA0.75.dat -n $group > $f5 ;\
# # ./group.py -f '~/exps/results/resultsA-1.0.dat' -n $group > $f6 ;\
# gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'; filename4='$f4'; filename5='$f5'; filename6='$f6'" plotLatencyComparison.plt &

#comparision of latency when ALPHA changes (averaged)
#f1="/tmp/$RANDOM.mpl"
#f2="/tmp/$RANDOM.mpl"
#f3="/tmp/$RANDOM.mpl"
#./average.py ../out/results/*.dat
#./group.py -f '../out/results/AVGA0.0.dat' -n $group > $f1 ;\
#./group.py -f '../out/results/AVGA0.5.dat' -n $group > $f2 ;\
#./group.py -f '../out/results/AVGA1.0.dat' -n $group > $f3 ;\
#gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'" plotLatencyComparison.plt &


# f1="/tmp/$RANDOM.mpl"
# f2="/tmp/$RANDOM.mpl"
# f3="/tmp/$RANDOM.mpl"
# ./group.py -f ~/exps/results/resultsA0.0.dat -n $group > $f1 ;\
# ./group.py -f ~/exps/results/resultsA0.5.dat -n $group > $f2 ;\
# ./group.py -f ~/exps/results/resultsA1.0.dat -n $group > $f3 ;\
# gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'" plotLatencyComparison.plt &


#comparision of latency when ALPHA changes with 5 alphas
#f1="/tmp/$RANDOM.mpl"
#f2="/tmp/$RANDOM.mpl"
#f3="/tmp/$RANDOM.mpl"
#f4="/tmp/$RANDOM.mpl"
#f5="/tmp/$RANDOM.mpl"
#./group.py -f '../out/results/AVGA0.0.dat' -n $group > $f1 ;\
#./group.py -f '../out/results/AVGA0.5.dat' -n $group > $f2 ;\
#./group.py -f '../out/results/AVGA1.0.dat' -n $group > $f3 ;\
#./group.py -f '../out/results/AVGA-1.0.dat' -n $group > $f4 ;\
#./group.py -f '../out/results/resultsA0.75.dat' -n $group > $f5 ;\
#gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'; filename4='$f4'; filename5='$f5'" plotLatencyComparison.plt &
#gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'; filename4='$f4'" plotLatencyComparison.plt &


#comparision of hops when ALPHA changes
# f1="/tmp/$RANDOM.mpl"
# f2="/tmp/$RANDOM.mpl"
# f3="/tmp/$RANDOM.mpl"
# f4="/tmp/$RANDOM.mpl"
# f5="/tmp/$RANDOM.mpl"
# f6="/tmp/$RANDOM.mpl"
# ./group.py -f '../out/results/resultsC1CH1A0.0.dat' -n $group > $f1 ;\
# ./group.py -f '../out/results/resultsC1CH1A0.5.dat' -n $group > $f2 ;\
# ./group.py -f '../out/results/resultsC1CH1A1.0.dat' -n $group > $f3 ;\
# ./group.py -f '../out/results/resultsC1CH1A0.3.dat' -n $group > $f4 ;\
# ./group.py -f '../out/results/resultsA0.75.dat' -n $group > $f5 ;\
# ./group.py -f '../out/results/resultsA-1.0.dat' -n $group > $f6 ;\
# gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'; filename4='$f4';" plotLatencyComparison.plt &


#comparision of hops when ALPHA changes (averages)
#f1="/tmp/$RANDOM.mpl"
#f2="/tmp/$RANDOM.mpl"
#f3="/tmp/$RANDOM.mpl"
#f4="/tmp/$RANDOM.mpl"
#./average.py ../out/results/*.dat
#./group.py -f '../out/results/AVGA0.0.dat' -n $group > $f1 ;\
#./group.py -f '../out/results/AVGA0.5.dat' -n $group > $f2 ;\
#./group.py -f '../out/results/AVGA1.0.dat' -n $group > $f3 ;\
#./group.py -f '../out/results/AVGA-1.0.dat' -n $group > $f4 ;\
#gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'; filename4='$f4'" plotHopsComparison.plt &



# lastn=2000

#latency based on message categories
#./categories.py -f '../out/results/resultsA0.5.dat' -c 'lat' -n $lastn > /tmp/lcat.mpl ;\
#gnuplot -e "filename='/tmp/lcat.mpl'" plotLatencyCategories.plt &

#hops based on message categories
#./categories.py -f '../out/results/resultsA0.5.dat' -c 'hop' -n $lastn > /tmp/hcat.mpl ;\
#gnuplot -e "filename='/tmp/hcat.mpl'" plotHopsCategories.plt &
