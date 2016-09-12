#!/usr/bin/env python
import sys, subprocess, optparse, re, collections


parser = optparse.OptionParser()
parser.add_option('-f', '--file', action="store", dest="file",
    help="data file", default="../out/results/resultsA0.5.dat")
parser.add_option('-n', '--n', action="store", dest="n", type=int,
    help="goup by", default=5)
parser.add_option('-d', '--debug', action="store", dest="debug", type=int,
    help="debug", default=0)

options, args = parser.parse_args()

data = {}
nr = 0;
 
with open(options.file) as f:
    for line in f:
        path = re.search('\[(.*?)\]',line)
        if not path:
            continue
        nr += 1
        path = path.group(1).split(', ')
        for id in path:
            if id not in data:
                data[id] = 0;
            else:
                data[id] += 1;

cats = range(0, 100+options.n, options.n)
final = { c : 0 for c in cats[:-1] }

for k in data:
    prc = float(data[k]*100/nr)
    if options.debug and prc > 50:
        print k
    for c in range(len(cats)-1):
        if prc >= cats[c] and prc < cats[c+1]:
            final[cats[c]] += 1

sd = sorted(final.items())

for k,v in sd:
#     print k+options.n/float(2),k, v
    print k, v