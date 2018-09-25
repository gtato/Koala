import numpy as np
import matplotlib.pyplot as plt
import matplotlib.cbook as cbook
import json
import pylab
from pip._vendor.pyparsing import line
from z3.z3 import fpLT


def main():

    files = ['../out/results/results.C1.0.VC1.0.1000x10.CCL10K.A0.5.dat'] #edge/core 50 ms update_text
    group = 1
#     filters={'physical':1, 'chord':2, 'koala':3, 'flat koala': 4, 'hierarchy koala': 5}
    filters={'physical':1, 'chord':2, 'koala':3}
    data = get_data(files, filters, group)
    
#     filters={'koala/physical':1, 'chord/physical':2}
#     data['koala/physical'] = []
#     data['chord/physical'] = []
#     for i in range(len(data['physical'])):
#         data['koala/physical'].append(data['koala'][i]/float(data['physical'][i]))
#         data['chord/physical'].append(data['chord'][i]/float(data['physical'][i]))
     
    fig, ax = plt.subplots()
    
    for k,v in filters.iteritems():
        x = np.sort(data[k])
        y = np.arange(1, len(x)+1) /float(len(x))
        line, = ax.plot(x, y, marker='.', linestyle='none',label=k)
#         line, = ax.plot(data[k], marker='.', linestyle='none', label=k)
#         line, = ax.plot(data[k], label=k)
     

    ax.set_ylim(ymin=0)
    ax.set_xlim(xmin=0) 
    ax.legend()
    plt.show()


def get_data(files, filters, group):
    data ={}
    sums = {}
    for f,v in filters.iteritems():
        data[f]=[]
        sums[f]=0
    
    for file in files:
        with open(file, "r") as f:
            content = f.read()
            lines = content.split('\n')
            lineI = 0
            for line in lines:
                if "cycle" in line: continue
                fields = line.split("\t")
                if len(fields) < 5: continue
                i = 0
                for f,v in filters.iteritems():
#                     data[i].append(float(fields[f]))
                    sums[f] += float(fields[v])
                    
                    if lineI > 0 and lineI%group==0:
                        data[f].append(sums[f]/group)
                        sums[f]=0
                    
                    i+=1    
                lineI += 1
 
    return data


main()

