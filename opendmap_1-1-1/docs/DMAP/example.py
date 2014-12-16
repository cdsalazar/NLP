from dmap import *

class Human(object):
    pass

class FHuman(object):
    pass

class MHuman(object):
    pass

class Action(object):
    pass

class Loves(Action):
    actor=Human
    object=Human

class Believes(Action):
   actor=Human
   object=Action

class John(Human):
    pass

class Mary(Human):
    pass


p = DMAP()

p.associate(John,["John"])
p.associate(Mary,["Mary"])
p.associate(Loves,[("actor",), "loves",("object",)])
p.associate(Believes,[("actor",),'believes','that',("object",)])

p.parse("".split('Mary believes that John believes that John loves Mary'))
