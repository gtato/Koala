class RoutingTable(object):

    ALL = 1
    LOCALS = 2
    GLOBALS = 3

    """docstring for RoutingTable"""
    def __init__(self):
        super(RoutingTable, self).__init__()
        self.globals = Neighbors(None, None)
        self.locals = Neighbors(None, None)
    
    def to_dict(self):
        return {'globals': self.globals.to_dict(), 'locals': self.locals.to_dict()}


    @staticmethod
    def from_dict(rtd):
        rt = RoutingTable()
        rt.globals = Neighbors.from_dict(rtd['globals'])
        rt.locals = Neighbors.from_dict(rtd['locals'])
        return rt

    def get_neighbours_ids(self):
        to_ret = [self.locals.predecessor, self.locals.successor, self.globals.predecessor, self.globals.successor]
        seen = {}
        for n in to_ret:
            if n and n.id not in seen.keys():
                seen[n.id] = n

        return seen.keys()

    def get_all_neighbours(self, which=None):
        which = RoutingTable.ALL if not which else which
        loc = which == RoutingTable.LOCALS
        glob = which == RoutingTable.GLOBALS
        if which == RoutingTable.ALL:
            loc = glob = True

        to_ret = []
        if loc:
            to_ret.extend([self.locals.predecessor, self.locals.successor])
            to_ret.extend(self.locals.visited)
            to_ret.extend(self.locals.longlinks)

        if glob:
            to_ret.extend([self.globals.predecessor, self.globals.successor])
            to_ret.extend(self.globals.visited)
            to_ret.extend(self.globals.longlinks)

        seen = {}
        for n in to_ret:
            if n and n.id not in seen.keys():
                seen[n.id] = n

        return seen.values()


class Neighbors(object):
    def __init__(self, succ, pred):
        super(Neighbors, self).__init__()
        self.successor = succ
        self.predecessor = pred
        self.visited = []
        self.longlinks = []

    def set_successor(self, succ):
        return self._set(succ, True)
    
    def set_predecessor(self, pred):
        return self._set(pred, False)

    def add_to_longlinks(self, to_add):
        self.add_to_list(to_add, self.longlinks)

    def add_to_visited(self, to_add):
        self.add_to_list(to_add, self.visited)

    def add_to_list(self, to_add, list):
        if to_add and to_add.id != self.successor.id and to_add.id != self.predecessor.id:
            present = False
            for v in list:
                if v.id == to_add.id:
                    present = True
                    if to_add.latency > 0:
                        v.latency = to_add.latency
            if not present:
                list.append(to_add)




    def _set(self, entry, succ):
        to_add = self.successor if succ else self.predecessor

        if to_add and to_add.id == entry.id:    
            # if entry.latency > 0:
            if succ and entry.lq >= self.successor.lq:
                self.successor.latency = entry.latency
                self.successor.lq = entry.lq
            elif not succ and entry.lq >= self.predecessor.lq:
                self.predecessor.latency = entry.latency
                self.predecessor.lq = entry.lq

            return 0, None
        else:

            if succ:
                self.successor = NeighborEntry(entry.id, entry.latency, entry.lq)
            else:
                self.predecessor = NeighborEntry(entry.id, entry.latency, entry.lq)

            #probably put the old value on the visited nodes
            # self.add_to_visited(to_add)

            #return oldie
            return 1, to_add

    def to_dict(self):
        ret = {'successor':None, 'predecessor': None, 'visited':[], 'longlinks':[]}

        if self.successor:
            ret['successor'] = self.successor.to_dict()

        if self.predecessor:
            ret['predecessor'] = self.predecessor.to_dict()


        for i in self.visited:
            ret['visited'].append(i.to_dict())

        for i in self.longlinks:
            ret['longlinks'].append(i.to_dict())

        return ret

    @staticmethod
    def from_dict(nd):
        n = Neighbors( NeighborEntry.from_dict(nd['successor']), NeighborEntry.from_dict(nd['predecessor']))
        for v in nd['visited']:
            n.visited.append(NeighborEntry.from_dict(v))

        for ll in nd['longlinks']:
            n.longlinks.append(NeighborEntry.from_dict(ll))

        return n

    # def contain(self, node_id):
    #     alln = self.successors + self.predecessors
    #     for n in alln:
    #         if n.id == node_id:
    #             return True
    #     return False

class NeighborEntry(object):
    """docstring for NeighborEntry"""
    def __init__(self, id, latency, lq=0):
        super(NeighborEntry, self).__init__()
        self.id = id
        self.latency = latency
        self.lq = lq #latency quality

    def __str__(self):   
        return "(id: %s, latency:%s, lq:%s)" % (self.id, self.latency, self.lq)

    def to_dict(self):
        return {'id': self.id, 'latency':self.latency, 'lq':self.lq}

    @staticmethod
    def from_dict(ned):
        if ned:
            return NeighborEntry(ned['id'], ned['latency'], ned['lq'])
        return None
