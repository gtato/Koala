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