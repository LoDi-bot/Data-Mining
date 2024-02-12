#!/usr/bin/python
import sys
import os
import time
import random

s = int(sys.argv[1])

print(f"Child [{os.getpid()}]: I am started. PID {os.getpid()}. Parent PID {os.getppid()}")
time.sleep(s)
print(f"Child [{os.getpid()}]: I am ended. PID {os.getpid()}. Parent PID {os.getppid()}")

status = random.randint(0, 1)
sys.exit(status)
