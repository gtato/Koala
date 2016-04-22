

rt = []
dc_id = 'a'
nid = '5'
id = "%s-%s" % (dc_id, nid)

MAX_VAL = 999999
a=b=c=d=1

def route(dest):
    max = 0
    ret = None
    for re in rt:
        v = get_route_value(dest, re)
        if v > max:
            max = v
            ret = re
    return ret

def get_route_value(dest, re):
    res = 0

    if dist(dc_id, dest.dc_id) < dist(re.dc_id, dest.dc_id): #current node is better than sending it to this entry
        res = -1

    if dc_id == re.dc_id: # the entry is local
        res = 1 + a * dist(nid, re.nid) # prefer long local links, they potentially know things I don't know

    if dist(dc_id, dest.dc_id) > dist(re.dc_id, dest.dc_id):
        res = b * 1 / dist(re.dc_id, dest.dc_id) + c * 1 / re.latency

    if dest.dc_id == re.dc_id:
        res = MAX_VAL - d * dist(re.nid, dest.nid)
    return res




def dist(to, frm):
    return 0