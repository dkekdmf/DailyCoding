n = int(input())
count = 0
stack = []
result = []
for i in range(1,n+1):
    data = int(input())
    print(data)
    while count<=data:
        stack.append(count)
        count+=1
        result.append('+')
        if stack[-1] == data:
            stack.pop()
            result.append('-')
        else:
            print('NO')
            exit(0)

print('\n'.join(result))


    