# from koala.protocol import Koala
# from protocol import Koala
import sys, math, random

from migrate.versioning.api import source

from routing_table import RoutingTable, NeighborEntry
from message import Message
from protocol import Koala


class Node(object):

    DC_SIZE = 100
    WORLD_SIZE = 100
    MAGIC = 2

    """docstring for Node"""
    def __init__(self, nid, dc_id):
        super(Node, self).__init__()
        self.node_id = nid
        self.dc_id = dc_id
        self.id = "%s-%s" % (dc_id, nid)
        self.rt = RoutingTable()

        self.latency_x_dc = {}


    @staticmethod
    def from_dict(json_node):
        node = Node(json_node['node_id'], json_node['dc_id'])
        node.rt = RoutingTable.from_dict(json_node['rt'])
        return node

    def __str__(self):
        return "(id: %s)" % self.id

    def join(self, boot_id):
        if not Koala.add_node(self):
            return
        if boot_id and len(boot_id):
            self.try_set_neighbour(boot_id, 0, 0)
            self.send(boot_id, Message('rt', {'c': True, 'rt': self.rt}))

    def send(self, dest_id, msg):
        # print '%s talked to %s' % (self.id, dest_id)
        return Koala.send_to(self.id, dest_id, msg)

    def on_receive(self, source, msg):

        # self.update_links(source, msg)
        # self.search_long_links_in_path(msg)

        if msg.type == 'route':
            return self.on_route(msg)

        if msg.type == 'rt':
            self.update_rt(source, msg)

        if msg.type == 'ngn':
            self.on_new_global_neighbours(source, msg)

        return None

    def update_rt(self, source, msg):
        rt = msg.content['rt']
        chain = msg.content['c']
        source_old_neig= msg.content.get('old_neighbors')

        # then update my rt with the nodes i received
        new_neighbours = []
        rec_neighbours = rt.get_all_neighbours()
        if source_old_neig:
            rec_neighbours.extend(source_old_neig)
        rec_neighbours.append(source) # maybe source could be a potential neighbour

        neigh_before = self.rt.get_neighbours_ids()
        dc_before = [Node.dc_id(nb) for nb in neigh_before]
        dc_before.append(self.dc_id)

        source_joining = source.is_joining()
        self_joining = self.is_joining()

        old_neighbors = []
        for rec_neighbour in rec_neighbours:
            is_source = rec_neighbour.id == source.id
            if rec_neighbour.id != self.id:

                if self_joining and self.is_local(source.id):
                    dc_before.append(Node.dc_id(rec_neighbour.id))

                l = msg.latency if is_source else rec_neighbour.latency
                lq = self.get_lq(is_source, source.id, rec_neighbour)
                res, oldies = self.try_set_neighbour(rec_neighbour.id, l, lq)
                old_neighbors.extend(oldies)
                self.update_latency_x_dc(rec_neighbour.id, l, lq)

                if res == 2 or (res == 1 and is_source and source_joining):
                    new_neighbours.append(NeighborEntry(rec_neighbour.id, l))

                elif res < 0 and rec_neighbour.id == source.id:
                    dest = self.route_to(source.id, msg)
                    msg.referrer = self
                    msg.content['c'] = True
                    source.send(dest, msg)

        dc_before = list(set(dc_before))
        self.update_latencies()

        #  some neighbours might have been overwritten, we send only to the neighbors .
        neigh_after = self.rt.get_neighbours_ids()
        for new_n in new_neighbours:
            if new_n.id in neigh_after and new_n.id not in neigh_before or new_n.id == source.id:
                if self.is_local(new_n.id):
                    self.send(new_n.id, Message('rt', {'c':True, 'rt': self.rt, 'old_neighbors':old_neighbors}))
                else:
                    new_dc = Node.dc_id(new_n.id) not in dc_before
                    if new_dc:
                        self.broadcast_global_neighbor(new_n)
                    if not self.is_local(source.id) and (chain or new_dc):
                        self.send(new_n.id, Message('rt', {'c': False, 'rt': self.rt, 'old_neighbors':old_neighbors}))


    def update_latency_x_dc(self, id, l, lq):
        if lq > 1:
            self.latency_x_dc[Node.dc_id(id)] = l

        # here we can update the long links and, if we care enough, the visited as well
        links = self.rt.get_all_neighbours(RoutingTable.GLOBALS)
        for ln in links:
            if ln and ln.id == id and lq >= ln.lq:
                ln.latency = l
                ln.lq = lq



    def update_latencies(self):

        ln = [self.rt.globals.predecessor, self.rt.globals.successor]
        ln.extend(self.rt.globals.visited)
        ln.extend(self.rt.globals.longlinks)

        for n in ln:
            if n and n.lq < 2 and Node.dc_id(n.id) in self.latency_x_dc.keys():
                n.lq = 2
                n.latency = self.latency_x_dc[Node.dc_id(n.id)]

    def search_long_links_in_path(self, msg):
        for id in msg.path:
            if self.is_long_link_worthy(id):
                self.rt.globals.add_to_longlinks(NeighborEntry(id, 0))

    def is_long_link_worthy(self, id):
        #
        if self.is_local(id):
            return False

        quality = 0
        # for i in reversed(range(3,7)):
        #     if Node.distance(self.id, id) >= (Node.WORLD_SIZE / i):
        #         quality = i
        #         break

        for i in range(3,7):
            if Node.distance(self.id, id) >= (Node.WORLD_SIZE / i):
                quality = i
                break

        if quality > 4:
            return True
        return False

    def get_lq(self, is_source, source_id, ne):
        if is_source:
            return 3
        if self.is_local(source_id) and ne.lq == 3:
            return 2
        return 1

    def broadcast_global_neighbor(self, gn):
        print 'broadcast dc %s in dc %s' % (Node.dc_id(gn.id), self.dc_id)
        candidates = self.create_random_ids(Node.MAGIC)
        ln = [self.rt.locals.predecessor, self.rt.locals.successor]
        cnt = {'candidates': candidates, 'gn': gn}
        msg = Message('ngn', cnt)
        for n in ln:
            if n:
                self.send(n.id, msg)

    def on_new_global_neighbours(self, source, msg):
        cands = msg.content['candidates']
        gn = msg.content['gn']

        respees = []
        for c in cands:
            if self.is_responsible(c):
                if len(respees) == 0:
                    self.send(gn.id, Message('rt', {'c': False, 'rt': self.rt}))
                respees.append(c)

        rnes, _ = self.try_set_neighbour(gn.id, gn.latency, 2)
        if rnes != 2:
            return

        new_cands = list(set(cands)-set(respees))
        add_cands = self.create_random_ids(len(respees) - 1)
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

    def on_route(self, msg):
        nid = msg.content
        if nid != self.id:
            dest = self.route_to(nid, msg)
            return self.send(dest, msg)
        else:
            return msg.path


    def is_joining(self):
        return len(self.rt.get_neighbours_ids()) == 1


    def try_set_neighbour(self, nid, latency, lq):
        local = self.is_local(nid)
        added_s = added_p = -1
        old_s = old_p = None

        ne = NeighborEntry(nid, latency, lq)
        if self.is_successor(nid):
            added_s, old_s = self.rt.locals.set_successor(ne) if local else self.rt.globals.set_successor(ne)

        if self.is_predecessor(nid):
            added_p, old_p = self.rt.locals.set_predecessor(ne) if local else self.rt.globals.set_predecessor(ne)

        ret = max(added_s, added_p)

        if ret == 1:
            ret += 1

        if ret ==-1 and (self.can_be_predecessor(nid) or self.can_be_successor(nid)):
            ret = 1

        old_neighs = [n for n in (old_s, old_p) if n is not None]

        # 2: added, 1: potential neighbor, 0: updated , -1:not neighbor
        return ret, old_neighs


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


    # def get_rt_entry(self, node_id):
    #     merged = self.rt.get_all_neighbours()
    #     for e in merged:
    #         if e.id == node_id:
    #             return e
    #     return None


    def to_dict(self):
        return {
            'node_id': self.node_id, 'dc_id': self.dc_id, 'id': self.id, 'rt': self.rt.to_dict()
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
            if not successor or local or Node.compare_global(nid, successor.id) != 0:
                return True
            if predecessor.id == successor.id:
                return True
            if nid != predecessor.id and Node.distance(self.id, nid, True) <= Node.distance(self.id, successor.id, True):
                return True

        return False


    def is_predecessor(self, nid):
        local = self.is_local(nid)
        predecessor = self.rt.locals.predecessor if local else self.rt.globals.predecessor
        successor = self.rt.locals.successor if local else self.rt.globals.successor
        if self.can_be_predecessor(nid):
            if not predecessor or local or Node.compare_global(nid, predecessor.id) != 0:
                return True
            if predecessor.id == successor.id:
                return True
            if nid != successor.id and Node.distance(self.id, nid, True) <= Node.distance(self.id, predecessor.id, True):
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
        cpr_fnct = Node.compare_local if local else Node.compare_global
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
        cpr_fnct = Node.compare_local if local else Node.compare_global

        if not predecessor:
            return True
        else:
            if (cpr_fnct(node_id, predecessor.id) >= 0 and cpr_fnct(predecessor.id, self.id) > 0) or \
               (cpr_fnct(node_id, predecessor.id) <= 0 and cpr_fnct(predecessor.id, self.id) > 0 and cpr_fnct(node_id, self.id) < 0) or \
               (cpr_fnct(node_id, predecessor.id) >= 0 and cpr_fnct(node_id, self.id) < 0):
                return True
        return False

    @staticmethod
    def compare(id1, id2):
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

    @staticmethod
    def compare_local(id1, id2):
        splt1 = id1.split('-')
        splt2 = id2.split('-')

        n_id1 = int(splt1[1])
        n_id2 = int(splt2[1])

        if n_id1 == n_id2:
            return 0

        ret = int(n_id1 > n_id2)
        ret = ret if ret > 0 else -1
        return ret

    @staticmethod
    def compare_global(id1, id2):
        splt1 = id1.split('-')
        splt2 = id2.split('-')

        dc_id1 = splt1[0]
        dc_id2 = splt2[0]

        if dc_id1 != dc_id2:
            ret = int(dc_id1 > dc_id2)
            ret = ret if ret > 0 else -1
            return ret

        return 0

    @staticmethod
    def dc_id(nid):
        return nid.split('-')[0]

    @staticmethod
    def n_id(nid):
        return int(nid.split('-')[1])

    @staticmethod
    def distance(src_id, target_id, forcelocal=False):
        local = False
        if Node.dc_id(src_id) == Node.dc_id(target_id) or forcelocal:
            local = True

        src_id = Node.n_id(src_id) if local else Node.dc_id(src_id) 
        target_id = Node.n_id(target_id) if local else Node.dc_id(target_id)
        a = src_id if local else ord(src_id)
        b = target_id if local else ord(target_id)
        if src_id > target_id:
            a = target_id if local else ord(target_id)
            b = src_id if local else ord(src_id)

        size = Node.DC_SIZE if local else Node.WORLD_SIZE
        d1 = b - a
        d2 = (size - b + a) % size

        return min(d1, d2)
