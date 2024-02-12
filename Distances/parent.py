import sys
import os
import random

N = int(sys.argv[1])

for i in range(0, N):
    child_pid = os.fork()
    if child_pid == 0:
        arg = str(random.randint(5, 10))
        os.execl("./child.py", "child.py", arg)
    print(f"Parent [{os.getpid()}]: I ran children process with PID {child_pid}")

while N > 0:
    child_pid, status = os.wait()
    status = int(status/256)
    print(f"Parent[{os.getpid()}]: Child with PID {child_pid} terminated. Exit Status {status}.")
    if status == 0:
        N = N - 1
    else:
        child_pid = os.fork()
        if child_pid == 0:
            arg = str(random.randint(5, 10))
            os.execl("./child.py", "child.py", arg)
        print(f"Parent [{os.getpid()}]: I ran children process with PID {child_pid}")
