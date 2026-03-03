def dfs(graph,n,m,x,y):
    if x<=-1 or x>=n or y<=-1 or y>=m:
        return False
    if graph[x][y] == 1:
        graph[x][y] =-1
        dfs(graph,n,m,x+1,y)
        dfs(graph,n,m,x,y+1)
        dfs(graph,n,m,x-1,y)
        dfs(graph,n,m,x,y-1)
        return True
    return False
testcase = int(input())
line = 1

for i in range(testcase):
    m,n,k = map(int,input().split())
    graph = []
    for i in range(n):
        graph = [[0]*m for _ in range(n)]
    for i in range(k):
        y,x = map(int,input().split())
        graph[x][y] = 1
    answer = 0
    for i in range(n):
        for j in range(m):
            if dfs(graph,n,m,i,j): answer+=1
    
