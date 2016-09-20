#!/usr/bin/env python
import sys, subprocess, optparse, re, collections
from mercurial.ui import path


parser = optparse.OptionParser()
parser.add_option('-f', '--file', action="store", dest="file",
    help="data file", default="../out/results/resultsA0.5.dat")
parser.add_option('-p', '--path', action="store", dest="path",
    help="which path r or k", default="r")
parser.add_option('-n', '--n', action="store", dest="n", type=int,
    help="goup by", default=10)
parser.add_option('-d', '--debug', action="store", dest="debug", type=int,
    help="debug", default=0)

options, args = parser.parse_args()

rdata = {}
kdata = {}
nr = 0;

obrc = '['
cbrc = ']'

 
with open(options.file) as f:
    for line in f:
        i = 0

        if obrc not in line:
            continue
        
        rpath = line[line.index(obrc,i)+1:line.index(cbrc,i)] 
        i += line.index(cbrc,i)+1
        kpath = line[line.index(obrc,i)+1:line.index(cbrc,i)]
         
        nr += 1
        rpath = rpath.split(', ')
        for id in rpath:
            if id not in rdata:
                rdata[id] = 0;
            else:
                rdata[id] += 1;
        kpath = kpath.split(', ')
        for id in kpath:
            if id not in kdata:
                kdata[id] = 0;
            else:
                kdata[id] += 1;

cats = range(0, 100+options.n, options.n)
final = { c : 0 for c in cats[:-1] }

if options.path == 'r':
    data = rdata
else:
    data = kdata
    
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