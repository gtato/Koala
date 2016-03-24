class RoutingTable(object):
    """docstring for RoutingTable"""
    def __init__(self):
        super(RoutingTable, self).__init__()
        self.globals = Neighbors(None, None)
        self.locals = Neighbors(None, None)
    
    def to_dict(self):
        ret = {'globals': {'successor':None, 'predecessor': None, 'visited':[]}, 'locals':{'successor':None, 'predecessor':None, 'visited':[]} }

        if self.locals.successor:
            ret['locals']['successor'] = self.locals.successor.to_dict()

        if self.locals.predecessor:
            ret['locals']['predecessor'] = self.locals.predecessor.to_dict()

        if self.globals.successor:
            ret['globals']['successor'] = self.globals.successor.to_dict()

        if self.globals.predecessor:
            ret['globals']['predecessor'] = self.globals.predecessor.to_dict()

        for i in self.locals.visited:
            ret['locals']['visited'].extend([{'id':i.id, 'latency':i.latency}])

        for i in self.globals.visited:
            ret['globals']['visited'].extend([{'id':i.id, 'latency':i.latency}])

        return ret

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

    def get_all_neighbours(self):
        to_ret = [self.locals.predecessor, self.locals.successor, self.globals.predecessor, self.globals.successor]
        to_ret.extend(self.locals.visited)
        to_ret.extend(self.globals.visited)

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

    def set_successor(self, succ):
        return self._set(succ, True)
    
    def set_predecessor(self, pred):
        return self._set(pred, False)

    def add_to_visted(self, to_add):
        if to_add and to_add.id != self.successor.id and to_add.id != self.predecessor.id:
            present = False
            for v in self.visited:
                if v.id == to_add.id:
                    present = True
                    if to_add.latency > 0:
                        v.latency = to_add.latency
            if not present:
                self.visited.append(to_add)


    def _set(self, entry, succ):
        to_add = self.successor if succ else self.predecessor

        if to_add and to_add.id == entry.id:    
            if entry.latency > 0:
                if succ:
                    self.successor.latency = entry.latency
                else:
                    self.predecessor.latency = entry.latency
            

            return False
        else:
            if succ:
                self.successor = NeighborEntry(entry.id, entry.latency) 
            else:
                self.predecessor = NeighborEntry(entry.id, entry.latency) 

            #probably put the old value on the visited nodes
            self.add_to_visted(to_add)

            return True

    @staticmethod
    def from_dict(nd):
        n = Neighbors( NeighborEntry.from_dict(nd['successor']), NeighborEntry.from_dict(nd['predecessor']))
        for v in nd['visited']:
            n.visited.append(NeighborEntry.from_dict(v))
        return n

    # def contain(self, node_id):
    #     alln = self.successors + self.predecessors
    #     for n in alln:
    #         if n.id == node_id:
    #             return True
    #     return False

class NeighborEntry(object):
    """docstring for NeighborEntry"""
    def __init__(self, id, latency, lpos=0):
        super(NeighborEntry, self).__init__()
        self.id = id
        self.latency = latency
        self.lpos = lpos

    def __str__(self):   
        return "(id: %s, latency:%s, lpos:%s)" % (self.id, self.latency, self.lpos)

    def to_dict(self):
        return {'id': self.id, 'latency':self.latency, 'lpos':self.lpos}

    @staticmethod
    def from_dict(ned):
        if ned:
            return NeighborEntry(ned['id'], ned['latency'], ned['lpos'])
        return None
