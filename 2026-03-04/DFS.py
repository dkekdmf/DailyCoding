def DFS(graph,v,visited):
    visited[v] = True
    print(v)
    for i in graph[v]:
        if visited[i] == False:
            DFS(graph,i,visited)
graph = [
    [],
    [2,3,4],
    [1],
    [1,5,6],
    [1,7],
    [3,8],
    [3],
    [4],
    [5]
]

visited = [False] * 9
DFS(graph,1,visited)