"""Check pixel spacing of consecutive red rank badges in the selftest capture (GUI scale 3)."""
import sys
from PIL import Image
im=Image.open(sys.argv[1]).convert('RGB')
runs=[]
for y in range(300,min(660,im.height)):
 if sum(1 for x in range(15,85) if (lambda c: c[0]>180 and c[1]<110 and c[2]<130)(im.getpixel((x,y))))>8:
  if not runs or y>runs[-1][-1]+1: runs.append([y])
  else: runs[-1].append(y)
assert len(runs)>=3, 'Missing consecutive rank badges'
assert runs[1][0]-runs[0][0]==42 and runs[2][0]-runs[1][0]==42, 'Chat rows are not 14 GUI pixels apart'
print('PASS: chat screenshot row pitch 42 physical pixels / 14 GUI pixels')
