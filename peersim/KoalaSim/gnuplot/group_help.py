#!/usr/bin/env python
import sys, subprocess, optparse

parser = optparse.OptionParser()
parser.add_option('-f', '--file', action="store", dest="file",
    help="data file", default="../out/results/resultsA0.5.dat")
parser.add_option('-n', '--n', action="store", dest="n", type=int,
    help="goup by", default=100)

options, args = parser.parse_args()

 
data = {'help':0,'total':0}
j = i = 0
with open(options.file) as f:
    for line in f:
        words = line.split('\t')
        if len(words) <= 1:
            continue
        i += 1 
        j += 1 
        data['help'] += float(words[0])
        data['total'] += float(words[1])
         
#         print line
        if i%options.n == 0:
            print "%s" % (
                            data['help']/data['total']
                            )
            data = {'help':0,'total':0}
            j=0

    if j != 0:
        print "%s" % ( 
                        data['help']/data['total']
                        )

