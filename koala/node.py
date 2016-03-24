# from koala.protocol import Koala
# from protocol import Koala
import sys, math, random
from routing_table import RoutingTable, NeighborEntry
from message import Message
from protocol import Koala

class Node(object):

    DC_SIZE = 100
    WORLD_SIZE = 100
    # OVERSHOOTING = 0.7
    OVERSHOOTING = 0
    MAGIC=2

    """docstring for Node"""
    def __init__(self, nid, dc_id):
        super(Node, self).__init__()
        self.node_id = nid
        self.dc_id = dc_id
        self.id = "%s-%s" % (dc_id, nid)
        self.rt = RoutingTable()
        # self.nr_local_nodes = 1
        # self.lpos = int(nid)
        # self.neighbor_distance = 0

    @staticmethod
    def from_dict(json_node):
        node=Node(json_node['node_id'],json_node['dc_id'])
        node.rt = RoutingTable.from_dict(json_node['rt'])

        return node
    def __str__(self):
        return "(id: %s)" % (self.id)

    def join(self, boot_id):
        Koala.add_node(self)
        if boot_id and len(boot_id):
            self.try_set_neighbour(boot_id, 0)
            self.send(boot_id, Message('rt', {'c':True, 'rt': self.rt}))

    def send(self, dest_id, msg):
        # print '%s talked to %s' % (self.id, dest_id)
        return Koala.send_to(self.id, dest_id, msg)


    def dispatch_to_local_neighbors(self, msg, sender_id):
        if self.rt.locals.successor and self.rt.locals.successor.id != sender_id:
            self.send(self.rt.locals.successor.id, msg)

        if self.rt.locals.predecessor and self.rt.locals.predecessor.id != sender_id:
            self.send(self.rt.locals.predecessor.id, msg)


    def on_receive(self, source, msg):
        # self.update_local_postion(source)
        if msg.type == 'route':
            # self.update_local_postion(source, msg.referrer)
            return self.on_route(msg)

        if msg.type == 'rt':
            self.update_rt(source, msg)

        if msg.type == 'ngn':
            self.on_new_global_neighbours(source, msg)

        return None

    # messy ring
    # def update_rt(self, source, msg):
    #     rt = msg.content
    #
    #     # then update my rt with the nodes i received
    #     new_neighbours = []
    #     rec_neighbours = rt.get_all_neighbours()
    #     rec_neighbours.append(source) # maybe source could be a potential neighbour
    #
    #     neigh_before = self.rt.get_neighbours_ids()
    #
    #     for rec_neighbour in rec_neighbours:
    #         if rec_neighbour.id != self.id:
    #             l = msg.latency if rec_neighbour.id == source.id  else 0
    #             res = self.try_set_neighbour(rec_neighbour.id, l)
    #
    #             # if res > 0:
    #             if res == 2 or (res == 1 and rec_neighbour.id == source.id and source.is_joining()):
    #                 new_neighbours.append(rec_neighbour.id)
    #
    #             elif res < 0 and rec_neighbour.id == source.id:
    #             #    sender was not a neigour so route it to the right neighbour
    #                 dest = self.route_to(source.id, msg)
    #                 source.send(dest, msg)
    #                 # self.send(source.id, Message('route', dest))
    #
    #     #  some neighbours might have been overwritten, we send only to the neighbors .
    #     neigh_after = self.rt.get_neighbours_ids()
    #     for new_n in new_neighbours:
    #         if new_n in neigh_after and new_n not in neigh_before or new_n==source.id:
    #         # if self.can_be_neighbour(new_n) and new_n not in neigh_before:
    #             self.send(new_n, Message('rt', self.rt))


    # too many messages
    # def update_rt(self, source, msg):
    #     rt = msg.content
    #
    #     # then update my rt with the nodes i received
    #     new_neighbours = []
    #     rec_neighbours = rt.get_all_neighbours()
    #     rec_neighbours.append(source) # maybe source could be a potential neighbour
    #
    #     neigh_before = self.rt.get_neighbours_ids()
    #     dc_before = [nb.split('-')[0] for nb in neigh_before]
    #
    #     for rec_neighbour in rec_neighbours:
    #         if rec_neighbour.id != self.id:
    #             l = msg.latency if rec_neighbour.id == source.id  else 0
    #             res = self.try_set_neighbour(rec_neighbour.id, l)
    #
    #             # if res > 0:
    #             if res == 2 or (res == 1 and rec_neighbour.id == source.id and source.is_joining()):
    #                 new_neighbours.append(rec_neighbour.id)
    #
    #             elif res < 0 and rec_neighbour.id == source.id:
    #             #    sender was not a neigour so route it to the right neighbour
    #                 dest = self.route_to(source.id, msg)
    #                 source.send(dest, msg)
    #                 # self.send(source.id, Message('route', dest))
    #
    #     new_gn = self.get_new_global_neighbors(neigh_before)
    #
    #     #  some neighbours might have been overwritten, we send only to the neighbors .
    #     neigh_after = self.rt.get_neighbours_ids()
    #     for new_n in new_neighbours:
    #         if new_n in neigh_after and new_n not in neigh_before or new_n==source.id:
    #             self.send(new_n, Message('rt', self.rt))
    #             if new_n.split('-')[0] not in dc_before and self.rt.locals.successor:
    #                 self.send(self.rt.locals.successor.id, Message('rt', self.rt))

    def update_rt(self, source, msg):
        rt = msg.content['rt']
        chain = msg.content['c']

        redirect = False
        # then update my rt with the nodes i received
        new_neighbours = []
        rec_neighbours = rt.get_all_neighbours()
        rec_neighbours.append(source) # maybe source could be a potential neighbour

        neigh_before = self.rt.get_neighbours_ids()
        dc_before = [Node.dc_id(nb) for nb in neigh_before]
        dc_before.append(self.dc_id)

        source_joining = source.is_joining()
        self_joining = self.is_joining()

        for rec_neighbour in rec_neighbours:
            if rec_neighbour.id != self.id:

                if self_joining and self.is_local(source.id):
                    dc_before.append(Node.dc_id(rec_neighbour.id))

                l = msg.latency if rec_neighbour.id == source.id else 0
                res = self.try_set_neighbour(rec_neighbour.id, l)

                # if res > 0:
                if res == 2 or (res == 1 and rec_neighbour.id == source.id and source_joining):
                    new_neighbours.append(NeighborEntry(rec_neighbour.id, l))

                elif res < 0 and rec_neighbour.id == source.id:
                    redirect = True

        # new_gn = self.get_new_global_neighbors(neigh_before)

        # self.update_local_postion(source, msg.referrer)

        if redirect:
            dest = self.route_to(source.id, msg)
            msg.referrer = self
            msg.content['c'] = True
            source.send(dest, msg)


        dc_before = list(set(dc_before))


        #  some neighbours might have been overwritten, we send only to the neighbors .
        neigh_after = self.rt.get_neighbours_ids()
        new_dc = False
        for new_n in new_neighbours:
            if new_n.id in neigh_after and new_n.id not in neigh_before or new_n.id==source.id:
                if self.is_local(new_n.id):
                    self.send(new_n.id, Message('rt', {'c':True, 'rt': self.rt}))
                else:
                    new_dc = Node.dc_id(new_n.id) not in dc_before
                    if new_dc:
                        self.broadcast_global_neighbor(new_n)
                    if not self.is_local(source.id) and (chain or new_dc):
                        self.send(new_n.id, Message('rt', {'c':False, 'rt': self.rt}))
                # else:
                #     self.send(new_n.id, Message('rt', {'c':True, 'rt': self.rt}))


    def broadcast_global_neighbor(self, gn):
        print 'broadcast dc %s in dc %s' % (Node.dc_id(gn.id), self.dc_id)
        candidates = self.create_random_ids(Node.MAGIC)
        ln = [self.rt.locals.predecessor, self.rt.locals.successor]
        cnt = {'candidates':candidates, 'gn':gn}
        msg = Message('ngn', cnt)
        for n in ln:
            if n:
                self.send(n.id, msg)

    def on_new_global_neighbours(self, source, msg):
        cands = msg.content['candidates']
        gn = msg.content['gn']

        if self.try_set_neighbour(gn.id, gn.latency) != 2:
            return

        respees = []
        for c in cands:
            if self.is_responsible(c):
                if len(respees) == 0:
                    self.send(gn.id, Message('rt', {'c':False, 'rt': self.rt}))
                respees.append(c)

        new_cands = list(set(cands)-set(respees))
        add_cands = self.create_random_ids(len(respees)-1)
        new_cands.extend(add_cands)
        msg.content['candidates'] = new_cands

        target = self.rt.locals.predecessor.id
        if source.id == self.rt.locals.predecessor.id:
            target = self.rt.locals.successor.id

        self.send(target, msg)

    def is_responsible(self, id):
        if not self.rt.locals.successor:
            return False
        return Node.distance(self.id, id) < Node.distance(self.rt.locals.successor.id, id) \
                and Node.distance(self.id, id) < Node.distance(self.rt.locals.predecessor.id, id)

    def create_random_ids(self, nr):
        if nr <= 0:
            return []
        rids =[]
        while len(rids) != nr:
            rand_id = '%s-%s' % (self.dc_id, random.randint(0, Node.DC_SIZE))
            if not self.is_responsible(rand_id):
                rids.append(rand_id)
            rids = list(set(rids))
        return rids

    # def update_local_postion(self, neighbor, referrer):
    #
    #     print 'for node %s' % self.id
    #     print 'before -> p: %s, l: %s, s: %s' % (self.rt.locals.predecessor.lpos,self.lpos, self.rt.locals.successor.lpos)
    #     consider = False
    #     for n in (neighbor, referrer):
    #         if not n:
    #             continue
    #         n_pos = n.lpos if n.lpos > 0 else int(n.node_id)
    #         if self.rt.locals.successor and self.rt.locals.successor.id == n.id:
    #             self.rt.locals.successor.lpos = n_pos
    #             # if self.rt.locals.predecessor.lpos == -1:
    #             #     self.rt.locals.predecessor.lpos = n.rt.locals.old_predecessor.lpos
    #             consider= True
    #         if self.rt.locals.predecessor and self.rt.locals.predecessor.id == n.id:
    #             self.rt.locals.predecessor.lpos = n_pos
    #             # if self.rt.locals.successor.lpos == -1:
    #             #     self.rt.locals.successor.lpos = n.rt.locals.old_successor.lpos
    #             consider = True
    #
    #     if not consider:
    #         return
    #
    #     if self.rt.locals.predecessor.id == self.rt.locals.successor.id:
    #         self.nr_local_nodes = 2
    #         return
    #
    #     pos = (self.rt.locals.predecessor.lpos + ((self.rt.locals.successor.lpos - self.rt.locals.predecessor.lpos) % Node.DC_SIZE)/2) % Node.DC_SIZE
    #
    #     self.lpos = (pos + (pos - self.lpos) * Node.OVERSHOOTING) % Node.DC_SIZE
    #     self.lpos = int(math.ceil(self.lpos) if self.node_id > self.lpos else math.floor(self.lpos))
    #
    #
    #     pred_id = "%s-%s" % (self.dc_id, self.rt.locals.predecessor.lpos)
    #     succ_id = "%s-%s" % (self.dc_id, self.rt.locals.successor.lpos)
    #     my_id = "%s-%s" % (self.dc_id, self.lpos)
    #     d1 = Node.distance(pred_id, my_id)
    #     print "succ ", succ_id
    #     d2 = Node.distance(my_id, succ_id)
    #
    #     self.neighbor_distance = d1+d2
    #     if self.neighbor_distance > 0:
    #         self.nr_local_nodes = (2 * Node.DC_SIZE) / self.neighbor_distance
    #
    #     print 'after -> p: %s, l: %s, s: %s \n-----' % (self.rt.locals.predecessor.lpos, self.lpos, self.rt.locals.successor.lpos)

    def on_route(self, msg):
        nid = msg.content
        if nid != self.id:
            dest = self.route_to(nid, msg)
            return self.send(dest, msg)
        else:
            return msg.path

        print "%s is being routed to %s" % (self.id, nid)
        self.send(nid, Message('rt', self.rt))


    def is_joining(self):
        return len(self.rt.get_neighbours_ids()) == 1


    def try_set_neighbour(self, nid, latency):
        local = self.is_local(nid)
        neigbours = self.rt.locals if local else self.rt.globals
        added_p = added_s = -1

        ne = NeighborEntry(nid, latency)
        if self.is_successor(nid):
            added_s = int(self.rt.locals.set_successor(ne) if local else self.rt.globals.set_successor(ne))

        if self.is_predecessor(nid):
            added_p = int(self.rt.locals.set_predecessor(ne) if local else self.rt.globals .set_predecessor(ne))

        ret = max(added_s,added_p)

        if ret == 1:
            ret += 1

        if ret ==-1 and (self.can_be_predecessor(nid) or self.can_be_successor(nid)):
            ret = 1

        # 2: added, 1: potential neighbor, 0: updated , -1:not neighbor
        return ret

    # old one for routing when cycles are present
    # def route_to(self, nid, msg):
    #     local = self.is_local(nid)
    #
    #     if local:
    #         return self.rt.locals.successor.id if nid != self.rt.locals.predecessor.id else self.rt.locals.predecessor.id
    #     else:
    #         pred_dc = self.rt.globals.predecessor.id.split('-')[0]
    #         n_dc = nid.split('-')[0]
    #         if nid == self.rt.globals.predecessor.id or pred_dc == n_dc:
    #             return self.rt.globals.predecessor.id
    #
    #         if self.rt.locals.successor and self.rt.locals.successor.id not in msg.path:
    #              if (self.compare_global(self.rt.globals.successor.id, nid) > 0 and self.compare_global(nid ,self.id) > 0) or  \
    #                      (self.compare_global(self.rt.globals.successor.id, nid) < 0 and self.compare_global(self.rt.globals.successor.id, self.id) < 0):
    #                 return self.rt.locals.successor.id
    #
    #         return self.rt.globals.successor.id
    #
    #     return None



    def route_to(self, nid, msg):
        local = self.is_local(nid)

        succ = self.rt.locals.successor.id if local else self.rt.globals.successor.id
        pred = self.rt.locals.predecessor.id if local else self.rt.globals.predecessor.id

        d_from_succ = Node.distance(nid, succ)
        d_from_pred = Node.distance(nid, pred)
        if d_from_pred < d_from_succ:
            return pred
        else:
            return succ


        # if local:
        #     d_from_succ = Node.distance(nid, self.rt.locals.successor.id)
        #     d_from_pred = Node.distance(nid, self.rt.locals.predecessor.id)
        #     if d_from_pred == 0 or d_from_pred < d_from_succ:
        #         return self.rt.locals.predecessor.id
        #     else:
        #         return self.rt.locals.successor.id
        #
        # else:
        #     pred_dc = Node.dc_id(self.rt.globals.predecessor.id)
        #     n_dc = Node.dc_id(nid)
        #     if nid == self.rt.globals.predecessor.id or pred_dc == n_dc:
        #         return self.rt.globals.predecessor.id
        #
        #     if self.rt.locals.successor and self.rt.locals.successor.id not in msg.path:
        #          if (self.compare_global(self.rt.globals.successor.id, nid) > 0 and self.compare_global(nid ,self.id) > 0) or  \
        #                  (self.compare_global(self.rt.globals.successor.id, nid) < 0 and self.compare_global(self.rt.globals.successor.id, self.id) < 0):
        #             return self.rt.locals.successor.id
        #
        #     return self.rt.globals.successor.id
        #
        # return None


    def get_rt_entry(self, node_id):
        merged =  self.rt.get_all_neighbours()
        for e in merged:
            if e.id == node_id:
                return e
        return None


    def to_dict(self):
        return {
            'node_id': self.node_id, 'dc_id': self.dc_id, 'id':self.id, 'rt': self.rt.to_dict()
            # 'nr_local_nodes': self.nr_local_nodes, 'lpos':self.lpos,
            # 'neighbor_distance': self.neighbor_distance
        }

    def is_local(self, nid):
        ndc = Node.dc_id(nid)
        return self.dc_id == ndc

    def is_neighbour(self, nid):
        if self.is_successor(nid):
            return True

        if self.is_predecessor(nid):
            return True
        return False

    def is_successor(self, nid):
        local = self.is_local(nid)
        successor = self.rt.locals.successor if local else self.rt.globals.successor
        predecessor = self.rt.locals.predecessor if local else self.rt.globals.predecessor
        if self.can_be_successor(nid):
            if not successor or local  or self.compare_global(nid, successor.id) != 0:
                return True
            if predecessor.id == successor.id:
                return True
            if nid != predecessor.id and Node.distance(self.id, nid) <= Node.distance(self.id, successor.id):
                return True

        return False


    def is_predecessor(self, nid):
        local = self.is_local(nid)
        predecessor = self.rt.locals.predecessor if local else self.rt.globals.predecessor
        successor = self.rt.locals.successor if local else self.rt.globals.successor
        if self.can_be_predecessor(nid):
            if not predecessor or local or self.compare_global(nid, predecessor.id) != 0:
                return True
            if predecessor.id == successor.id:
                return True
            if nid != successor.id and Node.distance(self.id, nid) <= Node.distance(self.id, predecessor.id):
                return True

        return False


    def can_be_neighbour(self, nid):
        if self.can_be_successor(nid):
            return True

        if self.can_be_predecessor(nid):
            return True
        return False


    def can_be_successor(self, node_id):
        local = self.is_local(node_id)
        successor = self.rt.locals.successor if local else self.rt.globals.successor
        cpr_fnct = self.compare_local if local else self.compare_global
        if not successor:
            return True
        else:
            # at the moment we keep only one successor
            if (cpr_fnct(node_id, successor.id) <= 0 and cpr_fnct(successor.id, self.id) < 0) or \
               (cpr_fnct(node_id, successor.id) >= 0 and cpr_fnct(successor.id, self.id) < 0 and cpr_fnct(node_id, self.id) > 0) or \
               (cpr_fnct(node_id, successor.id) <= 0 and cpr_fnct(node_id, self.id) > 0) :
                return True

        return False

    def can_be_predecessor(self, node_id):
        local = self.is_local(node_id)
        predecessor = self.rt.locals.predecessor if local else self.rt.globals.predecessor
        cpr_fnct = self.compare_local if local else self.compare_global

        if not predecessor:
            return True
        else:
            # at the moment we keep only one successor
            if (cpr_fnct(node_id, predecessor.id) >= 0 and cpr_fnct(predecessor.id, self.id) > 0) or \
               (cpr_fnct(node_id, predecessor.id) <= 0 and cpr_fnct(predecessor.id, self.id) > 0 and cpr_fnct(node_id, self.id) < 0) or \
               (cpr_fnct(node_id, predecessor.id) >= 0 and cpr_fnct(node_id, self.id) < 0) :
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

    def compare_local(self, id1, id2):
        splt1 = id1.split('-')
        splt2 = id2.split('-')

        n_id1 = int(splt1[1])
        n_id2 = int(splt2[1])

        if n_id1 == n_id2:
            return 0

        ret = int(n_id1 > n_id2)
        ret = ret if ret > 0 else -1
        return ret

    def compare_global(self, id1, id2):
        splt1 = id1.split('-')
        splt2 = id2.split('-')

        dc_id1 = splt1[0]
        dc_id2 = splt2[0]

        if dc_id1 != dc_id2:
            ret = int(dc_id1 > dc_id2)
            ret = ret if ret > 0 else -1
            return ret

        return 0

    # def distance(self, id):
    #     id1 = int(self.node_id)
    #     id2 = int(id.split('-')[1])
    #     return abs(id2-id1)

    @staticmethod
    def dc_id(nid):
        return nid.split('-')[0]

    @staticmethod
    def n_id(nid):
        return int(nid.split('-')[1])

    @staticmethod
    def distance(src_id, target_id):
        local = False
        if Node.dc_id(src_id) == Node.dc_id(target_id):
            local = True
        src_id = Node.n_id(src_id) if local else Node.dc_id(src_id) 
        target_id = Node.n_id(target_id) if local else Node.dc_id(target_id)
        a = src_id if local else ord(src_id)
        b = target_id if local else ord(target_id)
        if src_id > target_id:
            a = target_id if local else ord(target_id)
            b = src_id if local else ord(src_id)

        size = Node.DC_SIZE if local else Node.WORLD_SIZE
        d1 = b-a
        d2 = (size - b + a) % size

        return min(d1, d2)