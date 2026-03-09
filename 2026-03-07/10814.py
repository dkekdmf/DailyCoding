N = int(input())

array = []

for _ in range(N):
    M= input().split()
    array.append(int(M[0],M[1]))

array.sort(array,key = lambda x: x[0])
for i in array:
    print(i[0],i[1])
