

class Koala(object):

    nodes = []

    @staticmethod
    def add_node(node):
        Koala.nodes.append(node)

    @staticmethod
    def send_to(source_id, dest_id, msg):
        # try:
        # print 'nodes %s' % [n.to_dict() for n in Koala.nodes]
        source = filter(lambda n: n.id == source_id, Koala.nodes)[0]
        dest = filter(lambda n: n.id == dest_id, Koala.nodes)[0]
        msg.set_rand_latency(source_id, dest_id)
        dest.on_receive(source, msg)
        # except Exception, e:
        #     print 'source or destination not valid'
        #     raise e
        
