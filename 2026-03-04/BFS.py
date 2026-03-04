def BFS(graph,start,visited):
    queue = []
    queue.append(start)
    visited[start] = True
    while(len(queue)!=0):
        v = queue.pop(0)
        print(v)
        for i in graph[v]:
            if visited[i]== False:
                queue.append(i)
                visited[i] = True


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
BFS(graph,1,visited)