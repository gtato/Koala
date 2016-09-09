#!/usr/bin/env python
import sys, subprocess, optparse
import fileinput

ext = '.dat'
key = 'AVG'
# files=[]
cats={}
path=''
for arg in sys.argv:
    if ext in arg and key not in arg:
#         files.append(arg)
        a = arg[arg.index('A'):arg.index(ext)]
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
                if '[' not in line:
                    continue
                if len(lines) <= i:
                    lines.append([])
                lines[i].append(line[0:line.index('[')])
                i += 1
        i = 0        
    
    
    
    for i in range(len(lines)):
        totrlat = 0 
        totclat= 0
        totklat= 0
        totrhop= 0
        totchop= 0
        totkhop= 0 
        
        nr = len(lines[i])
        for j in range(nr):
            words = lines[i][j].split('\t')
            totrlat += float(words[1])
            totclat += float(words[2])
            totklat += float(words[3])
            totrhop += float(words[4])
            totchop += float(words[5])
            totkhop += float(words[6]) 
            
        final[k].append("%s\t%s\t%s\t%s\t%s\t%s\t%s" % (i, totrlat/float(nr), totclat/float(nr), totklat/float(nr), totrhop/float(nr), totchop/float(nr), totkhop/float(nr) ) )
     
    
                

for k in final:
    f = open(path+key+k+ext, 'w')
    for i in range(len(final[k])):
        f.write(final[k][i]+'\n')
