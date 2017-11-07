#!/usr/bin/env python
import sys, subprocess, optparse
import numpy as np

def foo_callback(option, opt, value, parser):
  setattr(parser.values, option.dest, value.split(','))


parser = optparse.OptionParser()
parser.add_option('-f', '--file', action="callback", callback=foo_callback, dest="files", type="string", help="data files")
parser.add_option('-c', '--c', action="store", dest="c", type=int, help="column", default=0)
parser.add_option('-s', '--s', action="store", dest="s", type=int, help="start", default=0)
parser.add_option('-e', '--e', action="store", dest="e", type=int, help="end", default=sys.maxsize)
parser.add_option('-n', '--n', action="store", dest="n", type="string", help="name", default="")

options, args = parser.parse_args()
# print options.s
# print options.e

i = 1
for fn in options.files:
    vals = []
    lineNR = 0
    with open(fn) as f:
        for line in f:
            words = line.split('\t')
            if len(words) <= 1 or 'rlat' in line:
                continue
            val = float(words[options.c])
            if lineNR >= options.s and lineNR <= options.e:  
                vals.append(val)
            lineNR += 1
#             print lineNR
             
    npvals = np.array(vals)        
    min = np.min(npvals)
    firstq = np.percentile(npvals, 25)
    median = np.percentile(npvals, 50)
    thirdq = np.percentile(npvals, 75)
    max = np.max(npvals)
    avg = np.average(npvals)
    alpha = fn[fn.index('A')+1:fn.index('.dat')]
    if len(options.n) == 0:
        options.n = alpha
    print '%s %s %s %s %s %s %s %s' % (i, min, firstq, median, thirdq, max, avg, options.n)
    i += 1