#!/usr/bin/env python
from group import groupby 
import os, sys, subprocess, optparse, subprocess, tempfile
import numpy as np
 
 
def foo_callback(option, opt, value, parser):
  setattr(parser.values, option.dest, value.split(','))
 
 
parser = optparse.OptionParser()
parser.add_option('-f', '--file', action="callback", callback=foo_callback, dest="files", type="string", help="data files")
parser.add_option('-x', '--x', action="store", dest="x", type="string", help="x-axis", default="x-axis")
parser.add_option('-y', '--y', action="store", dest="y", type="string", help="y-axis", default="y-axis")
parser.add_option('-s', '--s', action="store", dest="s", type="string", help="smooth function", default="bezier")
parser.add_option('-o', '--out', action="store", dest="out", type="string", help="output pdf", default="")
options, _ = parser.parse_args()



# p=['../out/results/results.C1.0.VC1.0.100x10.CCL10K.A0.5.dat:4:koala:orange',
#    '../out/results/results.C1.0.VC1.0.100x10.CCL10K.A0.5.dat:5:flat:blue',
#    '../out/results/results.C1.0.VC1.0.100x10.CCL10K.A0.5.dat:6:leader:red']
# 
# if not options.files:
#     options.files = p


g=-1
if ':' not in options.s: smooth = 'smooth %s'%options.s
else: 
    g = int(options.s.split(':')[1])
    smooth = ''
    
gnuplot = '#!/usr/bin/gnuplot\n'
# gnuplot += 'set term wxt enhanced dashed "arial,16"\n'

if len(options.out) > 0:
    gnuplot += 'set terminal pdf\n'
    gnuplot += 'set output "%s.pdf"\n' % options.out

gnuplot += 'set xlabel "%s"\n' % options.x
gnuplot += 'set ylabel "%s"\n' % options.y
# gnuplot += 'set yrange [ 0 : * ]\n'
gnuplot += 'set yrange [ 0 : 800]\n'
 

gnuplot += 'plot ' 
i = 0
for fn in options.files:
    vals = fn.split(':')
    file = vals[0].strip()
    column = vals[1].strip()
    title = 'line%s'%i if len(vals) < 3 else vals[2].strip()
    color = '' if len(vals) < 4 else 'rgb "%s"'%vals[3].strip()
    type = '1' if len(vals) < 5 else vals[4].strip()
    
    if g > 0:
        new_file, filename = tempfile.mkstemp()
        f = open(filename, 'w')
        saveout = sys.stdout
        sys.stdout = f
        groupby(file,g)
        file = filename
        sys.stdout = saveout
        
    if i > 0: gnuplot += ', \\\n\t'
    gnuplot += '"%s" using %s title "%s" %s with lines lw 1.3 dashtype %s lc %s ' % (file, column, title,smooth, type, color)
    i += 1
     
gnuplot += '\npause mouse close\n'    
# print gnuplot
 
new_file, filename = tempfile.mkstemp()
 
print(filename)
 
os.write(new_file, gnuplot)
os.close(new_file)
     
# print gnuplot
 
subprocess.Popen('gnuplot %s' % filename, shell=True)
 
# os.remove(filename)