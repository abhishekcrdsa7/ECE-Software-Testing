#!/usr/bin/env python3
import csv
reader = csv.reader(open(r"pr-data.csv"),delimiter=',')
filtered = filter(lambda p: 'NOD' == p[4], reader)
csv.writer(open(r"abx.csv",'w'),delimiter=',').writerows(filtered)
