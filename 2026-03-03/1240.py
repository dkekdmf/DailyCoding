n,m = map(int,input().split())
graph = []
for i in range(1,n+1): graph[i] = []
for i in range(1,n):
    x,y,cost = map(int,input().split())
    graph[x].append([y,cost])
    graph[y].append([x,cost])

for i in range(m):
    x,y = map(int,input().split())
    visited = [False] * (n+1)
    distance = [-1] * (n+1)
    dfs(x,0)
    print(distance[y])



def dfs(x,dist):
    if visited[x] : return
    visited[x] = True
    distance[x] = dist
    for [y,cost] in graph[x]:
        dfs(y,dist+cost)

