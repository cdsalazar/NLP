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

# constraints ...
# what are the constraints on this attribute?
# ???

# default values, instance variables, etc.

# facets ??


