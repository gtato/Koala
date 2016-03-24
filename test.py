from koala.node import Node

def define_direction(arr):
    if arr[1] > arr[0] and arr[2] > arr[1]:
        return 'cc'

    if arr[1] < arr[0] and arr[2] < arr[1]:
        return 'ccc'

    if arr[1] < arr[0] and arr[1] < arr[2] and arr[2] < arr[0]:
        return 'cc'

    if arr[1] < arr[0] and arr[1] < arr[2] and arr[0] < arr[2]:
        return 'ccc'


    if arr[0] < arr[1] and arr[2] < arr[1] and arr[2] < arr[0]:
        return 'cc'

    if arr[0] < arr[1] and arr[2] < arr[1] and arr[0] < arr[2]:
        return 'ccc'



def consistent(arr):
    down = 0
    up = 0

    for i in range(len(arr)-1):
        if arr[i+1] > arr[i]:
            up += 1
        else:
            down += 1

    print up,down

    if up == down and up > 1:
        return False

    if up > down and down == 1 and arr[-1] > arr[0]:
        return False

    if down > up and up == 1 and arr[-1] < arr[0]:
        return False

    if up > down and down>1:
        return False

    if down > up and up >1:
        return False

    return True

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
    n = Node('10','c')

    # print n.try_set_neighbour('b-20',50)
    # print n.try_set_neighbour('d-30',50)
    # print n.try_set_neighbour('a-30',50)

    # np = Node('1','a')
    # np = Node('15','b')
    # n.rt.globals.predecessor = np
    #
    # # print n.id
    # print 'a-10', n.can_be_predecessor('a-10')
    # print 'a-20', n.can_be_predecessor('a-20')
    # print 'b-20', n.can_be_predecessor('b-20')
    # print 'b-6', n.is_predecessor('b-6' )
    # print 'e-20', n.is_predecessor('e-20')
    arr = [4,3,6]
    print consistent(arr)


