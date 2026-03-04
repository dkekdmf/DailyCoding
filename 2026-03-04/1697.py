
def BFS():
    queue = []
    queue.append(n)
    while(len(queue)!=0):
        v = queue.pop(0)
        if v == k:
            return visited[v]
        dist = [v+1,v-1,v*2]
        for nxt in dist:
            if nxt<=0 or nxt>MAX:
                continue
            if visited[nxt] ==0:
                visited[nxt] = visited[v]+1
                queue.append(nxt)
  
MAX = 100001
visited = [0] * MAX
n,k = map(int,input().split())
              
print(BFS())