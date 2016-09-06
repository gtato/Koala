#!/usr/bin/env python
import sys, subprocess, optparse

parser = optparse.OptionParser()
parser.add_option('-f', '--file', action="store", dest="file",
    help="data file", default="../out/results/resultsA0.5.dat")
parser.add_option('-n', '--n', action="store", dest="n", type=int,
    help="goup by", default=100)

options, args = parser.parse_args()

 

groupby = 100
data = {'rlat':0,'clat':0, 'klat':0, 'rhop':0, 'chop':0, 'khop':0}
i = -1;
 
with open(options.file) as f:
    for line in f:
        i += 1
        words = line.split('\t')
        if len(words) <= 1 or i == 0:
            continue
         
        data['rlat'] += float(words[1])
        data['clat'] += float(words[2])
        data['klat'] += float(words[3])
        data['rhop'] += float(words[4])
        data['chop'] += float(words[5])
        data['khop'] += float(words[6])
         
#         print line
        if i%options.n == 0:
            print "%s %s %s %s %s %s %s" % (words[0], 
                                            data['rlat']/options.n,
                                            data['clat']/options.n,
                                            data['klat']/options.n,
                                            data['rhop']/options.n,
                                            data['chop']/options.n,
                                            data['khop']/options.n
                                            )
            data = {'rlat':0,'clat':0, 'klat':0, 'rhop':0, 'chop':0, 'khop':0}

