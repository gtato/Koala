import numpy as np
import matplotlib.pyplot as plt


file = '../out/renater/topologyA0.5.dat' #edge/core 50 ms update_text
content = open(file).read()

x = []
y = []
names = []
lines = content.split('\n')
for line in lines:
    if len(line) == 0: continue
    split = line.split(' ')
    xc = float(split[0])
    yc = float(split[1])
    x.append(xc)
    y.append(yc)
    if len(split) > 2:
        name = split[2]
        names.append({'name': name, 'x': xc, 'y':yc})

# 
# x, y = np.random.random(size=(2,10))
# x = [1,2]
# y = [5,7]
for i in range(0, len(x), 2):
    plt.plot(x[i:i+2], y[i:i+2], 'ro-')

for n in names:
    plt.annotate(n['name'], xy= (n['x'],n['y']))
# plt.plot([1,5], [2,7], 'ro-')

plt.show()