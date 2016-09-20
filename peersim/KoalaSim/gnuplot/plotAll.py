#!/usr/bin/env python
import sys, subprocess

import Tkinter

graphs = {'rt':[
                {'id':'size', 'name':'rt size', 'args':'args', 'cmd':'gnuplot  -e "filename=\'../out/koala/rtA0.5.dat\'" plotRT.plt'}
            ], 
          'topology':[
                      
            ], 
          'comparison':[
                        
            ]}

class simpleapp_tk(Tkinter.Tk):
    def __init__(self,parent):
        Tkinter.Tk.__init__(self,parent)
        self.parent = parent
        self.initialize()

    def initialize(self):
        self.grid()
        
        
        self.graphdata = {} 
        rou = 0
        for k,v in graphs.iteritems():
            if len(v):
                glabel = Tkinter.StringVar()
                label = Tkinter.Label(self,textvariable=glabel,anchor="w")
                label.grid(column=0,row=rou,columnspan=2,sticky='EW')
                glabel.set(k)
                rou += 1
                for g in v:
                    glabel = Tkinter.StringVar()
                    label = Tkinter.Label(self,textvariable=glabel,anchor="w")
                    label.grid(column=0,row=rou,columnspan=2,sticky='EW')
                    glabel.set(g['name'])
                    
                    
                    did = 'arg%s-%s' % (k, g['id'])
                    self.graphdata[did] = Tkinter.StringVar()
                    self.entry = Tkinter.Entry(self,textvariable=self.graphdata[did])
                    self.entry.grid(column=1,row=rou,sticky='EW')
#                     self.entry.bind("<Return>", self.OnPressEnter)
                    self.graphdata[did].set(g['args'])

                    did = 'check%s-%s' % (k, g['id'])
                    self.graphdata[did] = Tkinter.IntVar()
                    self.entry = Tkinter.Checkbutton(self,variable=self.graphdata[did])
                    self.entry.grid(column=2,row=rou,sticky='EW')
#                     self.entry.bind("<Return>", self.OnPressEnter)
                    
                    
                    rou += 1
        
#         self.labelVariable = Tkinter.StringVar()
#         label = Tkinter.Label(self,textvariable=self.labelVariable,anchor="w")
#         label.grid(column=0,row=0,columnspan=2,sticky='EW')
#         self.labelVariable.set(u"Hello !")
 
#         self.entryVariable = Tkinter.StringVar()
#         self.entry = Tkinter.Entry(self,textvariable=self.entryVariable)
#         self.entry.grid(column=0,row=1,sticky='EW')
#         self.entry.bind("<Return>", self.OnPressEnter)
#         self.entryVariable.set(u"Enter text here.")
 
        rou += 1
        button = Tkinter.Button(self,text=u"Plot", command=self.OnButtonClick)
        button.grid(column=1,row=rou,columnspan=3)
        
        rou += 1
        self.outVariable = Tkinter.StringVar()
        label = Tkinter.Label(self,textvariable=self.outVariable,anchor="w")
        label.grid(column=0,row=rou,columnspan=3,sticky='EW')
        self.outVariable.set(u"Hello !")

#         self.labelVariable = Tkinter.StringVar()
#         label = Tkinter.Label(self,textvariable=self.labelVariable,anchor="w")
#         label.grid(column=0,row=rou,columnspan=2,sticky='EW')
#         self.labelVariable.set('mut')

        self.grid_columnconfigure(0,weight=1)
        self.resizable(True,False)
        self.update()
        self.geometry(self.geometry())       
        #self.entry.focus_set()
        #self.entry.selection_range(0, Tkinter.END)

    def OnButtonClick(self):
        
#         self.entry.focus_set()
#         self.entry.selection_range(0, Tkinter.END)
        checkedg = ''
        for k,v in self.graphdata.iteritems():
            if 'check' in k:
                if self.graphdata[k].get():
                    ids = k.replace("check","").split('-')
                    for kk in graphs[ids[0]]:
                        if kk['id'] == ids[1]:
                            graph = kk
                            break
                    checkedg += graph['id'] + ' '
        self.outVariable.set( checkedg )

    def OnPressEnter(self,event):
        self.labelVariable.set( self.entryVariable.get()+" (You pressed ENTER)" )
        self.entry.focus_set()
        self.entry.selection_range(0, Tkinter.END)

if __name__ == "__main__":
    app = simpleapp_tk(None)
    app.title('Plot All')
    app.mainloop()



#rm -f /tmp/*.mpl

#plot the size of the routing table
#gnuplot  -e "filename='../out/koala/rtA0.5.dat'" plotRT.plt &

#plot how many nodes know a certain percentage of other nodes
f1="/tmp/$RANDOM.mpl"
#./ll_knowledge.py -f '../out/koala/rt_fineA0.5.dat' -n 5 > $f1 ; gnuplot -e "filename='$f1'" plotLLKnowledge.plt &


#plot the renater (physical) topology
#gnuplot -e "filename='../out/renater/topologyA0.5.dat'" plotRenater.plt &


#plot the koala topology (there are some problems with this one)
#gnuplot -e "filename='../out/koala/topologyA0.5.dat'" plotKoala.plt &


group=200


#plot occurrences in message paths
f1="/tmp/$RANDOM.mpl"
f2="/tmp/$RANDOM.mpl"
#./path_occurrences.py -f '../out/results/resultsA0.5.dat' -n 1 > $f1 ; gnuplot -e "filename='$f1'" plotPathOccurrence.plt &
#./path_occurrences.py -f '../out/results/resultsA0.5.dat' -n 5 -p 'r' > $f1 ; gnuplot -e "filename='$f1'" plotPathOccurrence.plt &
#./path_occurrences.py -f '../out/results/resultsA0.5.dat' -n 5 -p 'k' > $f2 ; gnuplot -e "filename='$f2'" plotPathOccurrence.plt &

#comparision of latency for the 3 protocols 
f1="/tmp/$RANDOM.mpl"
#./group.py -f '../out/results/resultsA0.5.dat' -n $group > $f1 ; gnuplot -e "filename='$f1'" plotLatency.plt &

#comparision of hops for the 3 protocols
f1="/tmp/$RANDOM.mpl" 
#./group.py -f '../out/results/resultsA0.5.dat' -n $group > $f1 ; gnuplot -e "filename='$f1'" plotHops.plt &

group=500

#comparision of latency when ALPHA changes
#f1="/tmp/$RANDOM.mpl"
#f2="/tmp/$RANDOM.mpl"
#f3="/tmp/$RANDOM.mpl"
#./group.py -f '../out/results/resultsA0.0.dat' -n $group > $f1 ;\
#./group.py -f '../out/results/resultsA0.0.dat' -n $group > $f1 ;\
#./group.py -f '../out/results/resultsA0.5.dat' -n $group > $f2 ;\
#./group.py -f '../out/results/resultsA1.0.dat' -n $group > $f3 ;\
#gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'" plotLatencyComparison.plt &

#comparision of latency when ALPHA changes (averaged)
#f1="/tmp/$RANDOM.mpl"
#f2="/tmp/$RANDOM.mpl"
#f3="/tmp/$RANDOM.mpl"
#./average.py ../out/results/*.dat
#./group.py -f '../out/results/AVGA0.0.dat' -n $group > $f1 ;\
#./group.py -f '../out/results/AVGA0.5.dat' -n $group > $f2 ;\
#./group.py -f '../out/results/AVGA1.0.dat' -n $group > $f3 ;\
#gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'" plotLatencyComparison.plt &


#comparision of latency when ALPHA changes with 5 alphas
#f1="/tmp/$RANDOM.mpl"
#f2="/tmp/$RANDOM.mpl"
#f3="/tmp/$RANDOM.mpl"
#f4="/tmp/$RANDOM.mpl"
#f5="/tmp/$RANDOM.mpl"
#./group.py -f '../out/results/resultsA0.0.dat' -n $group > /tmp/la1.mpl ;\
#./group.py -f '../out/results/resultsA0.5.dat' -n $group > /tmp/la2.mpl ;\
#./group.py -f '../out/results/resultsA1.0.dat' -n $group > /tmp/la3.mpl ;\
#./group.py -f '../out/results/resultsA0.25.dat' -n $group > /tmp/la4.mpl ;\
#./group.py -f '../out/results/resultsA0.75.dat' -n $group > /tmp/la5.mpl ;\
#gnuplot -e "filename1='/tmp/la1.mpl'; filename2='/tmp/la2.mpl'; filename3='/tmp/la3.mpl'; filename4='/tmp/la4.mpl'; filename5='/tmp/la5.mpl'" plotLatencyComparison.plt &



#comparision of hops when ALPHA changes
#f1="/tmp/$RANDOM.mpl"
#f2="/tmp/$RANDOM.mpl"
#f3="/tmp/$RANDOM.mpl"
#./group.py -f '../out/results/resultsA0.0.dat' -n $group > $f1 ;\
#./group.py -f '../out/results/resultsA0.5.dat' -n $group > $f2 ;\
#./group.py -f '../out/results/resultsA1.0.dat' -n $group > $f3 ;\
#gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'" plotHopsComparison.plt &


#comparision of hops when ALPHA changes (averages)
#f1="/tmp/$RANDOM.mpl"
#f2="/tmp/$RANDOM.mpl"
#f3="/tmp/$RANDOM.mpl"
#./average.py ../out/results/*.dat
#./group.py -f '../out/results/AVGA0.0.dat' -n $group > $f1 ;\
#./group.py -f '../out/results/AVGA0.5.dat' -n $group > $f2 ;\
#./group.py -f '../out/results/AVGA1.0.dat' -n $group > $f3 ;\
#gnuplot -e "filename1='$f1'; filename2='$f2'; filename3='$f3'" plotHopsComparison.plt &



lastn=2000

#latency based on message categories
#./categories.py -f '../out/results/resultsA0.5.dat' -c 'lat' -n $lastn > /tmp/lcat.mpl ;\
#gnuplot -e "filename='/tmp/lcat.mpl'" plotLatencyCategories.plt &

#hops based on message categories
#./categories.py -f '../out/results/resultsA0.5.dat' -c 'hop' -n $lastn > /tmp/hcat.mpl ;\
#gnuplot -e "filename='/tmp/hcat.mpl'" plotHopsCategories.plt &



