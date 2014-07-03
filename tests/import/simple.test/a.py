import b
import c
import c.d
import c as x
import c.d as y
import b, c as z, c.d as w

b.func()
b.C()

from b import func
from b import func as f
from b import func as f, C
from b import *
from c import d
from c.d import v

func()
f()
C()
d

# from . import b as b_alias
# from . import *
from .b import func as g
g()
# b_alias.func()
