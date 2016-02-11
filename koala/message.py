import random


class Message(object):
    """docstring for Message"""
    def __init__(self, ttype, content):
        super (Message, self).__init__()
        self.type = ttype
        self.content = content
        self.latency = 0

        
    def set_rand_latency(self, source_id, dest_id):
        ssplit = source_id.split('-')
        dsplit = dest_id.split('-')

        seed = '%s%s' % (source_id, dest_id) if source_id > dest_id else '%s%s' % (dest_id, source_id)
        random.seed(seed)
        if ssplit[0] == dsplit[0]:
            #local
            self.latency = random.randrange(5, 500)
        else:
            #remote
            self.latency = random.randrange(500, 2000)
    