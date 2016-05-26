from util import Util


class Koala(object):

    nodes = []
    nr_inter_msgs = 0
    nr_intra_msgs = 0

    @staticmethod
    def reset_msgs():
        Koala.nr_inter_msgs = 0
        Koala.nr_intra_msgs = 0

    @staticmethod
    def get_nr_msgs():
        return Koala.nr_inter_msgs+Koala.nr_intra_msgs,Koala.nr_inter_msgs, Koala.nr_intra_msgs

    @staticmethod
    def add_node(node):
        nodes_with_same_id = filter(lambda n: n.id == node.id, Koala.nodes)
        if len(nodes_with_same_id) == 0:
            Koala.nodes.append(node)
            return True
        return False

    @staticmethod
    def send_to(source_id, dest_id, msg):
        # try:
        # print 'nodes %s' % [n.to_dict() for n in Koala.nodes]
        print "%s->%s" % (source_id, dest_id)
        srcs = filter(lambda n: n.id == source_id, Koala.nodes)
        dests = filter(lambda n: n.id == dest_id, Koala.nodes)

        if len(srcs) > 1 or len(dests) > 1:
            print 'wtf'

        source = srcs[0]
        dest = dests[0]

        msg.set_rand_latency(source_id, dest_id)
        msg.path.append(dest_id)
        if source.dc_id == dest.dc_id:
            Koala.nr_intra_msgs += 1
        else:
            Koala.nr_inter_msgs += 1
        return dest.on_receive(source, msg)
        # except Exception, e:
        #     print 'source or destination not valid'
        #     raise e


    @staticmethod
    def detect_inconsitent_position():
        if len(Koala.nodes)< 3:
            return True

        node = Koala.nodes[0]
        seen =[]
        ring_pos = []

        while node.id not in seen:
            ring_pos.append(node.lpos)
            seen.append(node.id)
            node = filter(lambda n: n.id == node.rt.locals.successor.id, Koala.nodes)[0]


        return Util.consistent_ring(ring_pos)

