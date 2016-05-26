class Util(object):



    @staticmethod
    def consistent_ring(arr):
        down = 0
        up = 0

        for i in range(len(arr)-1):
            if arr[i+1] > arr[i]:
                up += 1
            else:
                down += 1

        # print up,down

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

    @staticmethod
    def normalize_latency(latency, tot_distance):
        x1 = 1
        y1 = 1
        x2 = 1000
        y2 = 1 / float(tot_distance)

        sl = (y2-y1)/float(x2 - x1)

        #y is the normalized latency
        y = sl * (latency - x1) + y1
        return y

