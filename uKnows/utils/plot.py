from dowhy import gcm
import numpy as np, pandas as pd
from dowhy.gcm.uncertainty import estimate_variance
from pycausal.pycausal import pycausal as pc
from pycausal import search as s
import networkx as nx
import pandas as pd
import pydot
from IPython.display import SVG
import sys
from pycausal import prior as p


causal_model = nx.DiGraph(nx.nx_pydot.read_dot("./temp/graph.dot"))

print(causal_model)

gcm.util.plot(causal_model)
