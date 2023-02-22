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

# pc = pc()


# intervention_var = 'ts-security-service_f'

def init_model(df_in,dot_file):  
    causal_model = nx.DiGraph(nx.nx_pydot.read_dot(dot_file)) 
    nodes = causal_model.nodes
    graph = causal_model
    causal_model = gcm.StructuralCausalModel(causal_model)

    data = df_in

    gcm.auto.assign_causal_mechanisms(causal_model, data)

    gcm.fit(causal_model, data)

    # return causal_model, nodes, graph
    return causal_model

def init_model_pk(df_in,prior_knowledge,pc):  
    tetrad = s.tetradrunner()

    if prior_knowledge != 0:
        prior = p.knowledge(requiredirect=prior_knowledge)

        tetrad.run(algoId = 'fci', dfs = df_in, testId = 'fisher-z-test', 
        depth = -1, maxPathLength = -1, 
        completeRuleSetUsed = False, verbose = False, priorKnowledge=prior)

        # tetrad.run(algoId = 'pc-all', dfs = df_in, testId = 'fisher-z-test', 
        #        fasRule = 2, depth = 2, conflictRule = 1, concurrentFAS = True,
        #        useMaxPOrientationHeuristic = True, verbose = False, priorKnowledge=prior)

        # tetrad.run(algoId = 'fges', dfs = df_in, scoreId = 'bdeu-score', 
        #         dataType = 'discrete',
        #        maxDegree = 3, faithfulnessAssumed = True, 
        #        symmetricFirstStep = True, verbose = False, priorKnowledge=prior)        
    else:
        df_in = df_in.fillna(0)
        # tetrad.run(algoId = 'fci', dfs = df_in, testId = 'fisher-z-test', 
        # depth = -1, maxPathLength = -1, 
        # completeRuleSetUsed = False, verbose = False)

        # tetrad.run(algoId = 'pc-all', dfs = df_in, testId = 'fisher-z-test', 
        #     fasRule = 2, depth = 2, conflictRule = 1, concurrentFAS = True,
        #     useMaxPOrientationHeuristic = True, verbose = False)

        tetrad.run(algoId = 'fges', dfs = df_in, scoreId = 'bdeu-score', 
                dataType = 'discrete', maxDegree = 3, faithfulnessAssumed = True, 
                symmetricFirstStep = True, verbose = False)



    dot = pc.tetradGraphToDot(tetrad.getTetradGraph())
    dot_file = open("./utils/temp/graph.dot", 'w')
    dot_file.write(dot)
    dot_file.close()
    # # print(dot)
    # original_stdout = sys.stdout
    # with open("./utils/temp/graph.dot", 'w') as res:
    #     sys.stdout = res
    #     print(dot)
    #     sys.stdout = original_stdout

    causal_model = nx.DiGraph(nx.nx_pydot.read_dot("./utils/temp/graph.dot")) 
    nodes = causal_model.nodes
    graph = causal_model
    causal_model = gcm.StructuralCausalModel(causal_model)

    data = df_in
    gcm.auto.assign_causal_mechanisms(causal_model, data)
    gcm.fit(causal_model, data)

    # return causal_model, nodes, graph
    return causal_model,nodes



def query_model(causal_model, dictionary):    

    samples = gcm.interventional_samples(causal_model,
                                        dictionary,
                                        num_samples_to_draw=1000)

    return samples
    

def causal_influence(causal_model, feature):
    contributions = gcm.intrinsic_causal_influence(causal_model, feature,
                                                gcm.ml.create_linear_regressor(),
                                                lambda x, _: estimate_variance(x))
    return contributions



def visualize_graph(method):
    file = open("./temp_data/" + str(method) + ".dot", "r")
    dot_str = file.read()
    file.close()
    # print(dot_str)
    graphs = pydot.graph_from_dot_data(dot_str)
    svg_str = graphs[0].create_svg()
    # svg_str = svg_str.replace("\r\n", "")
    print(svg_str)
    # SVG(svg_str)
    # print(drawing)

# def text_finder(text):
#     file_in = open("./temp_data/sdg.dot",'r')
#     file_out = open('./temp_data/sdg_red.dot', 'w')

#     file_out.writelines("digraph g {\n")
#     for line in file_in:
#         if text in line:
#                 file_out.writelines(line)
#     file_out.writelines("}")


def intervention(causal_model,variable,value):
    dictionary = {}
    temp_dict = dict({variable: lambda y: value})
    dictionary.update(temp_dict) 
    samples = query_model(causal_model, dictionary)

    if variable == 'ts-food-map-service_f':
        samples.to_csv('readme.csv', index=False)

    return samples

# if __name__ == "__main__":
#     pc.start_vm()
#     df = pd.read_csv("./result_v2.csv")

#     causal_model, nodes, graph = init_model(df)
#     y = 2
#     dictionary = {}
    
#     temp_dict = dict({intervention_var: lambda y: 2})
#     dictionary.update(temp_dict) 

#     print(dictionary)

#     samples = query_model(causal_model, dictionary)

    
#     # visualize_graph("graph")

#     print (samples[intervention_var])
#     samples.to_csv("samples.csv",index=False)

#     print("END")