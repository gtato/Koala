import numpy as np
import matplotlib.pyplot as plt
import matplotlib.cbook as cbook
import json
import pylab


def main():

    files = ['../out/results/results.C1.0.AC1.0.1000x1.CCL50K.COL0.T100.A0.5.dat'] #edge/core 50 ms update_text
# 
# 
    filters=['update_text']
    data00,lbs = get_data(files, filters)
# 
#     filters=['update_cursor_position']
#     data10,lbs = get_data(files, filters)
# 
#     filters=['check_spelling']
#     data20,lbs = get_data(files, filters)
# 
#     filters=['receive_chat_message']
#     data01,lbs = get_data(files, filters)
# 
#     # files = ['remote/raw.core.1800secs.2users.1koala.7058','remote/raw.edge.1800secs.2users.1koala.bb8c'] #compil
#     # files = ['remote/raw.core.1800secs.2users.1koala.a2c8','remote/raw.edge.1800secs.2users.1koala.7540'] #compil
#     files = ['grid5k/exp1/raw.core.1800secs.2users.1koala.45d8','grid5k/exp1/raw.edge.1800secs.2users.1koala.5ff4'] #compil
# 
#     filters=['full_compile']
#     data11,lbs = get_data(files, filters)
# 
#     filters=['document_diff']
#     data21,lbs = get_data(files, filters)
# 
# 
#     labels=['core', 'edge']
#     fliers = False
# 
#     font = {'family': 'serif', 'weight': 'normal', 'size': 11}
# 
#     titles = [['Update text', 'Chat'], ['Update cursor','Compile'], ['Spelling','History'] ]
#     data = [[data00,data01], [data10,data11], [data20,data21]]
#     rows = 3
#     cols = 2
#     fig, axes = plt.subplots(nrows=rows, ncols=cols, figsize=(4, 8))
# 
#     color1 = '#D7191C'
#     color2 = '#2C7BB6'
# 
#     for i in range(rows):
#         for j in range(cols):
#             axes[i,j].set_title(titles[i][j],fontdict=font)
#             bp = axes[i,j].boxplot(data[i][j], showfliers=fliers)
#             paint(bp, color1, color2)
# 
#             start, end = axes[i,j].get_ylim()
#             step = 10
#             if titles[i][j] == 'Chat': step = 20
#             if titles[i][j] == 'Compile': step = 200
#             if titles[i][j] == 'History': step = 50
#             axes[i,j].set_yticks(np.arange(0, end, step))
#             # axes[i,j].set_xticks(np.arange(len(labels)),('core','edge'))
#             axes[i,j].set_xticklabels(labels, fontdict=font)
#         axes[i,0].set_ylabel('Latency (ms)',fontdict=font)
# 
# 
# 
#   
#     fig.subplots_adjust(hspace=0.4, wspace=0.5)
#     plt.show()

    x = np.linspace(0, 10, 500)
    y = np.sin(x)
    
    fig, ax = plt.subplots()
    
    # Using set_dashes() to modify dashing of an existing line
    line1, = ax.plot(x, y, label='Using set_dashes()')
#     line1.set_dashes([2, 2, 10, 2])  # 2pt line, 2pt break, 10pt line, 2pt break
    
    # Using plot(..., dashes=...) to set the dashing when creating a line
    line2, = ax.plot(x, y - 0.2, label='Using the dashes parameter')
    
    ax.legend()
    plt.show()

def paint(bp, color1, color2):
    pylab.setp(bp['boxes'][0], color=color1)
    pylab.setp(bp['caps'][0], color=color1)
    pylab.setp(bp['caps'][1], color=color1)
    pylab.setp(bp['whiskers'][0], color=color1)
    pylab.setp(bp['whiskers'][1], color=color1)
    pylab.setp(bp['medians'][0], color=color1)

    pylab.setp(bp['boxes'][1], color=color2)
    pylab.setp(bp['caps'][2], color=color2)
    pylab.setp(bp['caps'][3], color=color2)
    pylab.setp(bp['whiskers'][2], color=color2)
    pylab.setp(bp['whiskers'][3], color=color2)
    pylab.setp(bp['medians'][1], color=color2)

def setlegend(axes, color1, color2):
    hB, = pylab.plot([1,1],color1)
    hR, = pylab.plot([1,1],color2)
    if axes:
        axes.legend((hB, hR),('core project', 'edge1 project'),loc='upper center', bbox_to_anchor=(0.5, 1.35), ncol=2)
    else:
        pylab.legend((hB, hR),('core project', 'edge1 project'))
    hB.set_visible(False)
    hR.set_visible(False)


def get_data(files, filters):
    res = {}
    ids = []
    for file in files:
#         file_lab = file.split('.')[1] #core or edge
        # file_lab = 'with koala' if file.split('.')[4] == '1koala' else 'no koala'
#         unique = file.split('.')[5]
        with open(file) as f:
            res['%s-%s' %(file_lab,unique)] = json.load(f)
            ids.append('%s-%s' %(file_lab,unique))


    data =[]
    labels = []
    for l in ids:
        for k in res[l]:
            if k in filters:
                data.append(res[l][k])
                # print len(res[l][k])
                labels.append('%s\n%s' % (k,l))

    return data, labels


main()

