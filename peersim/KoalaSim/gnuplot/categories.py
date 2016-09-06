#!/usr/bin/env python
import sys, subprocess, optparse


parser = optparse.OptionParser()
parser.add_option('-f', '--file', action="store", dest="file",
    help="data file", default="../out/results/resultsA0.5.dat")
parser.add_option('-c', '--category', action="store", dest="cat",
    help="latency or hops", default='lat')
parser.add_option('-n', '--n', action="store", dest="n",
    help="last n lines (cycles)", default=2000)

options, args = parser.parse_args()

 


command = 'tail -n %s %s' % (options.n, options.file)
#command = 'ls -l'
proc = subprocess.Popen(command, stdout=subprocess.PIPE, shell=True)
output = proc.stdout.read()
outlines = output.split('\n')

chordhopcat = {}
koalahopcat = {}
hopcatcount = {}

cattype = 8 #lat
if options.cat == 'hop':
    cattype = 7 #hops


for line in outlines:
    words = line.split('\t')
    if len(words) <= 1:
        continue
    #7:hopcat, 8:latcat, 2:chordlat, 3:koalalat
    #print words[7], words[8]
    if words[cattype] in chordhopcat:
        chordhopcat[words[cattype]] += float(words[2])
        koalahopcat[words[cattype]] += float(words[3])
        hopcatcount[words[cattype]] += 1
    else:
        chordhopcat[words[cattype]] = float(words[2])
        koalahopcat[words[cattype]] = float(words[3])
        hopcatcount[words[cattype]] = 1

for k in chordhopcat:
    chordhopcat[k] = chordhopcat[k]/hopcatcount[k]

for k in koalahopcat:
    koalahopcat[k] = koalahopcat[k]/hopcatcount[k]



print 'category chord koala'
for i in range(1, len(chordhopcat)+1):
    print '"Category %s" %s %s' % (i, chordhopcat[str(i)], koalahopcat[str(i)])
#print chordhopcat
#print koalahopcat
# print hopcatcount


#print 'done'
#print output 