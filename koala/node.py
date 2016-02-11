# from koala.protocol import Koala
# from protocol import Koala
import sys
from routing_table import RoutingTable, NeighborEntry
from message import Message
from protocol import Koala

class Node(object):


    """docstring for Node"""
    def __init__(self, nid, dc_id):
        super(Node, self).__init__()
        self.node_id = nid
        self.dc_id = dc_id
        self.id = "%s-%s" % (dc_id, nid)
        self.rt = RoutingTable()

    def join(self, boot_id):
        Koala.add_node(self)
        if boot_id:
            self.try_set_neighbour(boot_id, 0)
            self.send(boot_id, Message('rt', self.rt))

    def send(self, dest_id, msg):
        print '%s talked to %s' % (self.id, dest_id)
        Koala.send_to(self.id, dest_id, msg)

    def on_receive(self, source, msg):
        if msg.type == 'route':
            self.on_routed(msg.content)

        if msg.type == 'rt':
            self.update_rt(source, msg)

    def update_rt(self, source, msg):
        rt = msg.content

        # then update my rt with the nodes i received
        new_neighbours = []
        rec_neighbours = rt.get_all_neighbours()
        rec_neighbours.append(source) # maybe source could be a potential neighbour

        for rec_neighbour in rec_neighbours:
            if rec_neighbour.id != self.id:
                l = msg.latency if rec_neighbour.id == source.id  else 0
                res = self.try_set_neighbour(rec_neighbour.id, l)

                if res > 0:
                    new_neighbours.append(rec_neighbour)

                elif res < 0 and rec_neighbour.id == source.id:
                #    sender was not a neigour so route it to the right neighbour
                    dest = self.route_to(source.id)
                    self.send(source.id, Message('route', dest))

        #  some neighbours might have been overwritten, we send only to the last ones.
        for new_n in new_neighbours:
            if self.is_neighbour(new_n.id):
                self.send(new_n.id, Message('rt', self.rt))

    def on_routed(self, nid):
        print "%s is being routed to %s" % (self.id, nid)
        self.send(nid, Message('rt', self.rt))


    def try_set_neighbour(self, nid, latency):
        local = self.is_local(nid)
        neigbours = self.rt.locals if local else self.rt.globals
        added_p = added_s = -1
        # if neigbours.contain(nid):
        #     return added

        ne = NeighborEntry(nid, latency)
        if self.is_successor(nid, local):
            added_s = int(self.rt.locals.set_successor(ne) if local else self.rt.globals .set_successor(ne))


        if self.is_predecessor(nid, local):
            added_s = int(self.rt.locals.set_predecessor(ne) if local else self.rt.globals .set_predecessor(ne))


        # if self.id == 'a-5' and nid == 'a-7':
        #     print 's:%s p:%s' % (added_s, added_p)

        # 1: added, 0: updated, -1:not added
        return max(added_s,added_p)

    def route_to(self, nid):
        # for the moment we route only to successor, next we will take visited in account

        nextn = self.rt.locals.successor if self.is_local(nid) else self.rt.globals.successor
        return nextn.id



    def get_rt_entry(self, node_id):
        merged =  self.rt.get_all_neighbours()
        for e in merged:
            if e.id == node_id:
                return e
        return None


    def to_dict(self):
        return  {
            'node_id': self.node_id, 'dc_id': self.dc_id, 'id':self.id, 'rt': self.rt.to_dict()
        }

    def is_local(self, nid):
        ndc = nid.split('-')[0]
        return self.dc_id == ndc

    def is_neighbour(self, nid):
        local = self.is_local(nid)
        if self.is_successor(nid, local):
            return True

        if self.is_predecessor(nid, local):
            return True
        return False

    def is_successor(self, node_id, local):
        successor = self.rt.locals.successor if local else self.rt.globals.successor
        if not successor:
            return True
        else:
            # at the moment we keep only one successor
            if (self.compare(node_id, successor.id) <= 0 and self.compare(successor.id, self.id) < 0) or \
               (self.compare(node_id, successor.id) >= 0 and self.compare(successor.id, self.id) < 0 and self.compare(node_id, self.id) > 0) or \
               (self.compare(node_id, successor.id) <= 0 and self.compare(node_id, self.id) > 0) :
                return True

        return False

    def is_predecessor(self, node_id, local):
        predecessor = self.rt.locals.predecessor if local else self.rt.globals.predecessor

        if not predecessor:
            return True
        else:
            # at the moment we keep only one successor
            if (self.compare(node_id, predecessor.id) >= 0 and self.compare(predecessor.id, self.id) > 0) or \
               (self.compare(node_id, predecessor.id) <= 0 and self.compare(predecessor.id, self.id) > 0 and self.compare(node_id, self.id) < 0) or \
               (self.compare(node_id, predecessor.id) >= 0 and self.compare(node_id, self.id) < 0) :
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
