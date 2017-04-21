#!/usr/bin/env python
import sys, subprocess, optparse

parser = optparse.OptionParser()
parser.add_option('-f', '--file', action="store", dest="file",
    help="data file", default="../out/results/resultsA0.5.dat")
parser.add_option('-n', '--n', action="store", dest="n", type=int,
    help="goup by", default=100)

options, args = parser.parse_args()

 

groupby = 100
data = {'rlat':0,'clat':0, 'klat':0, 'fklat':0, 'lklat':0, 
        'rhop':0, 'chop':0, 'khop':0, 'fkhop':0, 'lkhop':0,
        'msg':0, 'cfail':0, 'kfail':0, 'fkfail':0, 'lkfail':0}
j = i = ln = ll = 0
 
with open(options.file) as f:
    for line in f:
        words = line.split('\t')
        if len(words) <= 1 or 'rlat' in line:
            continue
        i += 1 
        j += 1
        ln += float(words[0])-ll
        ll = float(words[0])
        data['rlat'] += float(words[1])
        data['clat'] += float(words[2])
        data['klat'] += float(words[3])
        data['fklat'] += float(words[4])
        data['lklat'] += float(words[5])
        data['rhop'] += float(words[6])
        data['chop'] += float(words[7])
        data['khop'] += float(words[8])
        data['fkhop'] += float(words[9])
        data['lkhop'] += float(words[10])
        if len(words) >= 12: 
            data['msg'] += float(words[11])
            data['cfail'] += float(words[12])
            data['kfail'] += float(words[13])
            data['fkfail'] += float(words[14])
            data['lkfail'] += float(words[15])
         
#        print line
        if i%options.n == 0:
#         if ln >= options.n:
            print "%s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s" % (words[0], 
                                            data['rlat']/options.n,
                                            data['clat']/options.n,
                                            data['klat']/options.n,
                                            data['fklat']/options.n,
                                            data['lklat']/options.n,
                                            data['rhop']/options.n,
                                            data['chop']/options.n,
                                            data['khop']/options.n,
                                            data['fkhop']/options.n,
                                            data['lkhop']/options.n,
                                            data['msg']/options.n,
                                            data['cfail']/ln,
                                            data['kfail']/ln,
                                            data['fkfail']/ln,
                                            data['lkfail']/ln
                                            )
            data = {'rlat':0,'clat':0, 'klat':0, 'fklat':0, 'lklat':0, 
                    'rhop':0, 'chop':0, 'khop':0, 'fkhop':0, 'lkhop':0,
                    'msg':0, 'cfail':0, 'kfail':0, 'fkfail':0, 'lkfail':0}
            ln=0
            j=0

    if j != 0:
        print "%s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s" % (words[0], 
                                        data['rlat']/j,
                                        data['clat']/j,
                                        data['klat']/j,
                                        data['fklat']/j,
                                        data['lklat']/j,
                                        data['rhop']/j,
                                        data['chop']/j,
                                        data['khop']/j,
                                        data['fkhop']/j,
                                        data['lkhop']/j,
                                        data['msg']/j,
                                        data['cfail']/ln,
                                        data['kfail']/ln,
                                        data['fkfail']/ln,
                                        data['lkfail']/ln
                                        )