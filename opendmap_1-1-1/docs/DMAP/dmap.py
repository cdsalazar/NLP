"""Python introspection functions; treating Classes as Frames.

Very primilinary...
"""

__author__ = "Will Fitzgerald (wfitzg@kzoo.edu)"
__version__ = "$Revision: 1.1 $"
__date__ = "$Date: 2005/07/16 01:25:21 $"
__copyright__ = "Copyright (c) 2003 Will Fitzgerald"
__license__ = "Python"

# perhaps this will get bigger...
class Frame(object):
    pass

# is it a class?
def is_class(x):
    return isinstance(x,type)

# is it an instance? cf. insstance(x,y)
def is_instance(x):
    if isinstance(x,type):
        return 0
    else:
        return 1

# what is this class it a subclass of?
# x.__bases__

# what is this an instance of?
# type(x)

# what are the attributes ... ???
# instance -- x.__dict__ has instance attributes + values 
# class    -- x.__dict__ has class attributes + values??

def attribute_value(obj,attribute):
    for abst in all_abstractions(obj):
        val = None
        try:
            val = abst.__dict__.get(attribute,None)
        except AttributeError:
            pass
        if val != None:
            return val
    return None

# what are the methods?
# dir(x)


# not quite right ...
def methods_of(parent):
    return filter(None,dict(parent.__dict__).itervalues())

# is this an instance of that?
# isinstance(x,y)

# is this a subclass of that?
# issubclass(child,parent)

# what is the name of this class?
# x.__name__

def name(x):
    name = x
    try:
        name = x.__name__
    finally:
        return name

# what is the class that has this name?
# either a variable or eval('name')
# eval looks name up in globals ...

def set_name(x,name):
    if is_instance(x):
        x.__name__ = name
        
# is this one of those?
def isa(child,parent):
    # print "isa:",child," one of these ",parent
    if (child==parent):
        return 1
    elif is_class(parent) and isinstance(child,parent):
        return 1
    elif is_class(child) and is_class(parent) and issubclass(child,parent):
        return 1
    else:
        return 0

# what are the (all of) the parents of this object?
def parents(x):
    if is_class(x):
        return list(x.__bases__)
    else:
        return [x.__class__]

def allparents(x):
    if is_class(x):
        return list(x.__mro__)
    else:
        return list(type(x).__mro__)

def all_abstractions(x):
    if isinstance(x,Description):
        return all_abstractions(x.base)
    elif is_class(x):
        return list(x.__mro__)
    else:
        return [x] + list(type(x).__mro__)
    
# what are the subclasses of this class?
# ??? -- everything is in globals()

def subclasses_of(parent):
    #print dir()
    #print globals()
    return filter(lambda (x):is_class(x) and x!=parent and issubclass(x,parent),globals().itervalues())

# what are the instances of this class?
# ??? -- everything is in globals()


def instances_of(parent):
    return filter(lambda (x): isinstance(x,parent),globals().itervalues())


def isAttributeSpecifier(x): return type(x)==tuple

def attributeSpecifier(x):
    return x[0]

def makeAttributeSpecifier(x):
    return (x,)

class Feature(object):
  """
  Feature:  

  Attributes:
  attribute:   --
  value:   --
  """
  def __init__(self,attribute=None,value=None):
    self.__attribute=attribute
    self.__value=value
    # don't worry about descriptions that are featureless
    if isinstance(value,Description) and value.features==[]:
        self.__value = value.base

  # accessors for Feature
  def get_attribute(self):
    return self.__attribute
  def set_attribute(self,attribute):
    self.__attribute=attribute
  attribute=property(get_attribute,set_attribute)

  def get_value(self):
    return self.__value
  def set_value(self,value):
    self.__value=value
  value=property(get_value,set_value)

  def __repr__(self):
    return '<Feature: ' + repr(self.attribute) + ':' + repr(self.value) + '>'

class Description(object):
  """
  Description:  

  Attributes:
  base:  --
  features:  --
  """
  def __init__(self,base=None,features=list()):
            # print "Initializing with ", base, "; features=",features
            self.__base=base
            self.__features=list(features) # hmm...

  # accessors for Description
  def get_base(self):
            return self.__base
  def set_base(self,base):
            self.__base=base
  base=property(get_base,set_base)

  def get_features(self):
            return self.__features
  def set_features(self,features):
            self.__features=features
  features=property(get_features,set_features)

  def all_abstractions(self):
     return all_abstractions(self.base)

  def __repr__(self):
    return '<Description: '  + repr(self.base) + ' ' + repr(self.features) + '>'

class Prediction(object):
  """
  Prediction: 

  Attributes:
  base:  --
  pattern:   --
  start:   --
  next:  --
  description:   --
  """
  def __init__(self,base=None,pattern=None,start=None,next=None,features=list()):
            # print "Init. prediction: ", id(self), '; base=', base, "; pat=", pattern,"; start=",start,"; next=",next,";features=",features
            self.__base=base
            self.__pattern=pattern
            self.__start=start
            self.__next=next
            self.__features=list(features) # hmm...

  # accessors for Prediction
  def get_base(self):
            return self.__base
  def set_base(self,base):
            self.__base=base
  base=property(get_base,set_base)

  def get_pattern(self):
    return self.__pattern
  def set_pattern(self,pattern):
    self.__pattern=pattern
  pattern=property(get_pattern,set_pattern)

  def get_start(self):
    return self.__start
  def set_start(self,start):
    self.__start=start
  start=property(get_start,set_start)

  def get_next(self):
    return self.__next
  def set_next(self,next):
    self.__next=next
  next=property(get_next,set_next)

  def get_features(self):
    return self.__features
  def set_features(self,features):
    self.__features=features
  features=property(get_features,set_features)

  def target(self):
            spec = self.pattern[0]
            if isAttributeSpecifier(spec):
                base = self.base
                attribute = attributeSpecifier(spec)
                value = attribute_value(base,attribute)
                if (attribute==None):
                    error("Not an attribute")
                else:
                    return value
            else:
                return spec

class DMAP(object):
  """
  DMAP: 

  Attributes:
  anytimePredictions:  --
  dynamicPredictions:  --
  position:  --
  callBacks:   --
  seen:  --
  complete:  --
  """
  def __init__(self):
    self.__anytimePredictions={}
    self.__dynamicPredictions={}
    self.__position=0
    self.__callBacks={}
    self.__seen=list()
    self.__complete=list()

  # accessors for DMAP
  def get_anytimePredictions(self):
        return self.__anytimePredictions
  def set_anytimePredictions(self,anytimePredictions):
        self.__anytimePredictions=anytimePredictions

  anytimePredictions=property(get_anytimePredictions,set_anytimePredictions)
            
  def get_dynamicPredictions(self):
            return self.__dynamicPredictions
  def set_dynamicPredictions(self,dynamicPredictions):
        self.__dynamicPredictions=dynamicPredictions
        
  dynamicPredictions=property(get_dynamicPredictions,set_dynamicPredictions)

  def get_position(self):
            return self.__position
  def set_position(self,position):
            self.__position=position

  position=property(get_position,set_position)

  def get_callBacks(self):
            return self.__callBacks
  def set_callBacks(self,callBacks):
            self.__callBacks=callBacks
            
  callBacks=property(get_callBacks,set_callBacks)

  def defineCallBack(self,class1,procedure):
    cbs = self.callBacks.get(class1,[])
    cbs.remove(procedure)
    self.callBacks[class1] = cbs + procedure

  def get_seen(self):
            return self.__seen
  def set_seen(self,seen):
            self.__seen=seen

  seen=property(get_seen,set_seen)

  def get_complete(self):
            return self.__complete
  def set_complete(self,complete):
            self.__complete=complete

  complete=property(get_complete,set_complete)

  def clear(self,anytime=0,callbacks=0):
            self.position=0
            self.seen=list()
            self.complete=list()
            self.dynamicPredictions={}
            if (anytime==1):
                self.anytimePredictions={}
            if (callbacks==1):
                self.callBacks={}

  def parse(self,sentence):
            for word in sentence:
                self.position = self.position + 1
                self.reference(word,self.position,self.position)

  def reference(self,item,start,end):
            print "Referencing" ,item, "from", start, "to", end
            # print 'All abstractions of ',item,' are ',all_abstractions(item)
            for abstraction in all_abstractions(item):
                for prediction in self.anytimePredictions.get(abstraction,list()):
                    self.advance(prediction,item,start,end)
                for prediction in self.dynamicPredictions.get(abstraction,list()):
                    self.advance(prediction,item,start,end)
                for callback in self.callBacks.get(abstraction,list()):
                    callback(item,start,end)

  def advance(self,prediction,item,start,end):
            # print 'advancing', prediction.pattern,' on ',item,' from start=',start
            if (prediction.next==None) or (prediction.next==start):
                # intialize prediction values
                base = prediction.base
                pattern = prediction.pattern[1:]
                start = start
                if (prediction.start!=None):
                    start = prediction.start
                # print "extending...", id(prediction), 'with features=',prediction.features
                features=self.extend(prediction,item)
                # print "features are now ..." , features
                # reference or create new
                if (pattern==[]):
                    self.reference(self.find(base,features),start,end)
                else:
                    self.indexDynamic(Prediction(base,pattern,start,(self.position+1),features))

  def find(self,base,features):
            # print "The features are:", features
            return Description(base,features)
            # return base
        
  def extend(self,prediction,item):
            specialization=prediction.pattern[0]
            # features=prediction.features
            # print 'pattern:',prediction.pattern, " features",prediction.features,'spec:',specialization,' isattr:', isAttributeSpecifer(specialization)
            if isAttributeSpecifer(specialization):
                itemis = item
                if isinstance(itemis,Description):
                    itemis = itemis.base
                if isa(prediction.target(),itemis):
                    return features
                else:
                    #print "Appending ", item
                    fea = Feature(attributeSpecifier(specialization),item)
                    #print "Appending ", fea
                    prediction.features.append(fea)
                    #print "Features are now: ",prediction.features
                    return prediction.features
            else:
                return prediction.features
                                     
  def associate(self,base,pattern):
            if base==pattern[0]:
                print "Can't associate ", base, "with itself."
            else:
                prediction = Prediction(base=base,pattern=pattern)
                self.indexAnytime(prediction)
        
  def indexAnytime(self,prediction):
            target = prediction.target()
            predictions = self.anytimePredictions.get(target,list())
            predictions.append(prediction)
            self.anytimePredictions[target] = predictions

  def indexDynamic(self,prediction):
            target = prediction.target()
            predictions = self.dynamicPredictions.get(target,list())
            predictions.append(prediction)
            self.dynamicPredictions[target] = predictions
