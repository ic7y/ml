#!/usr/bin/python
#coding:utf-8
#filename: mmlm.py

import math

BaseFile = '/home/db2admin/study/python/mmlm/RenMinData.txt'

MaxWordLen = 0
class LM():
  def __init__(self,basefile):
	global MaxWordLen
	self.words = []
	self.freq  = {}
	infile = file(basefile,'r')
	line = infile.readline().strip().decode('utf-8')
	while len(line)>0:
	  self.words.append('<s>')
	  self.words.extend(line.split(' '))
	  self.words.append('</s>')
	  line = infile.readline().strip().decode('utf-8')
	
	for i in range(1,len(self.words)):
	  key = self.words[i]+'|'+self.words[i-1]
	  if key in self.freq:
	     self.freq[key] += 1
	  else: self.freq[key] = 1
	for word in self.words:
	  if MaxWordLen < len(word):
	     MaxWordLen = len(word)
	  if word in self.freq:
	     self.freq[word] += 1
	  else: self.freq[word] = 1
	print len(self.words),MaxWordLen
# compute model's prob arguements 
  def get_transprob(self,word,condition):
	key = word+'|'+condition
	if key not in self.freq:
	   self.freq[key] = 0
	if condition not in self.freq:
	   self.freq[condition] = 0
	uniprob = self.freq[key]+1.0
	conprob = self.freq[condition]+len(self.words)
	return uniprob/conprob

  def get_initprob(self,word):
	return self.get_transprob(word,'<s>')

class Node:
  def __init__(self,word,len):
	self.word = word 
	self.len  = len 
	self.bestscore = 0.0
	self.Prenode   = None

class ViterGraph:
  def __init__(self,basefile):
	global MaxWordLen
	self.lm = LM(basefile)	
	self.graph = []
	self.cache = {}

  def CreatGraph(self,sentence):
	startNode = Node('<s>',1)
	startlist = []
	startlist.append(startNode)
	self.graph.append(startlist)
	print sentence
	for i in range(len(sentence)):
	   self.graph.append([])
	endlist = []
	endNode  = Node('</s>',1)
	endlist.append(endNode)
	self.graph.append( endlist )
		
	for i in range(len(sentence)):
	   for j in range(MaxWordLen):
	      if i+j > len(sentence):
		break
	      word = sentence[i:i+j]
	      if word in self.lm.freq:
	#	print word
		node = Node(word,j)
		self.graph[i+j].append(node)
	 
  def Viterbi(self):
	print 'Viterbi algorithm' 
	for i in range(len(self.graph)-2):
	   for curnode in self.graph[i+1]:
	      prelevel = i+1-curnode.len
	      prenode = self.graph[prelevel][0]
	      score = self.lm.get_transprob(curnode.word,prenode.word)
 	      score = prenode.bestscore + math.log(score)
	      MaxScore = score
	      curnode.bestscore = score
	      curnode.Prenode = prenode
	      for j in range(1,len(self.graph[prelevel]) ):
		 prenode = self.graph[prelevel][j] 
		 score = self.lm.get_transprob(curnode.word , prenode.word)
		 score = score + math.log(score)
		 if score > MaxScore:
			MaxScore = score
			curno de.Prenode = prenode
			curnode.bestscore = score
	size = len(self.graph)
	curnode  = self.graph[size-1][0]
	for n in range(1,len(self.graph[size-1])):
		if curnode.bestscore < self.graph[size-1][n].bestscore:
			curnode = self.graph[size-1][n]
	resultlist = []
	while curnode != None:
		resultlist.insert(0,curnode.word)
		curnode = curnode.Prenode
	print resultlist
  def split(self,sentence):
	CreateGraph(sentence)
	viterbi()
#-------------- main -----------
wordgraph = ViterGraph(BaseFile)	
st = '中国人在纽约开办银行'	
wordgraph.CreatGraph(st.decode('utf-8'))
wordgraph.Viterbi()

