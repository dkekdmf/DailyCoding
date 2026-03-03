n = int(input())
m = int(input())
count = 0
visited = [False] * (n+1)
graph = [[] for i in range(n+1)]
for _ in range(m):
    x,y = map(int,input().split())
    graph[x].append(y)
    graph[y].append(x)
def dfs(x):
    global count
    count+=1
    visited[x] = True
    for i in graph[x]:
        if not visited[i]:
            dfs(i)
dfs(1)
print(count-1)
    


