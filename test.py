
class Node(object):
    def __init__(self, nid, dc_id):
        super(Node, self).__init__()
        self.node_id = nid
        self.dc_id = dc_id
        self.id = "%s-%s" % (dc_id, nid)
        self.successors = []
        self.predecessors = []

    def is_successor(self, node_id):
        successors = self.successors
        if len(successors) == 0:
            return True
        else:
            # at the moment we keep only one successor
            if (self.compare(node_id, successors[0].id) <= 0 and self.compare(successors[0].id, self.id) < 0) or \
               (self.compare(node_id, successors[0].id) >= 0 and self.compare(successors[0].id, self.id) < 0 and self.compare(node_id, self.id) > 0) or \
               (self.compare(node_id, successors[0].id) <= 0 and self.compare(node_id, self.id) > 0) :
                return True
            
        return False

    def is_predecessor(self, node_id):
        predecessors = self.predecessors

        if len(predecessors) == 0:
            return True
        else:
            # at the moment we keep only one successor
            if (self.compare(node_id, predecessors[0].id) >= 0 and self.compare(predecessors[0].id, self.id) > 0) or \
               (self.compare(node_id, predecessors[0].id) <= 0 and self.compare(predecessors[0].id, self.id) > 0 and self.compare(node_id, self.id) < 0) or \
               (self.compare(node_id, predecessors[0].id) >= 0 and self.compare(node_id, self.id) < 0) :
                return True
        return False

    def compare(self, id1, id2):
        splt1 = id1.split('-')
        splt2 = id2.split('-')
    
        dc_id1 = splt1[0]
        dc_id2 = splt2[0]

        if dc_id1 != dc_id2:
            ret = int(dc_id1 > dc_id2)
            ret = ret if ret > 0 else -1
            return ret

        n_id1 = int(splt1[1])
        n_id2 = int(splt2[1])
        if n_id1 == n_id2:
            return 0
        
        ret = int(n_id1 > n_id2)
        ret = ret if ret > 0 else -1
        return ret

if __name__ == "__main__":
    # test successors
    # n = Node('15','a')
    # # ns = Node('5','a')
    # ns = Node('20','a')
    # n.successors.append(ns)

    # # print n.id
    # print 'a-4', n.is_successor('a-4')
    # print 'a-6', n.is_successor('a-6')
    # print 'a-17', n.is_successor('a-17')
    # print 'a-25', n.is_successor('a-25')


    # test predecessors
    n = Node('5','a')
    # np = Node('1','a')
    np = Node('15','a')
    n.predecessors.append(np)

    # print n.id
    print 'a-4', n.is_predecessor('a-4')
    print 'a-6', n.is_predecessor('a-6')
    print 'a-20', n.is_predecessor('a-20')
    


