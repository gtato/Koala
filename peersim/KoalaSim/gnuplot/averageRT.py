#!/usr/bin/env python
import sys, subprocess, optparse
import fileinput

ext = '.dat'
exp = 'E'
key = 'AVG'
alpha = 'A'
# files=[]
cats={}
path=''
for arg in sys.argv:
    if ext in arg and alpha in arg and key not in arg:
#         files.append(arg)
        
        a = arg[arg.index(alpha):arg.index(ext)]
        path = arg[0:arg.rfind('/')+1]
        if a not in cats:
            cats[a] = []
        cats[a].append(arg)

             
final = {}        
for k in cats:
    final[k] = []
#     print k
    files = []
    i = 0
    lines = []
    for file in cats[k]:
        with open(file) as f:
            for line in f:
#                 if '[' not in line:
#                     continue
                if len(lines) <= i:
                    lines.append([])
#                 lines[i].append(line[0:line.index('[')])
                lines[i].append(float(line))
                i += 1
        i = 0        
      
      
      
    for i in range(len(lines)):
        totrt = 0 
        nr = len(lines[i])
        for j in range(nr):
            totrt += lines[i][j]
            
        final[k].append("%s" % (totrt/float(nr)))
       
      
                  
  
for k in final:
    f = open(path+key+k+ext, 'w')
    for i in range(len(final[k])):
        f.write(final[k][i]+'\n')
