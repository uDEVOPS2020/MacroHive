import pandas as pd
import json
import pydot
import networkx as nx

# TODO - considerare solo gli effetti (voglio prendere il subset di microservizi su cui l'intervention ha un impatto)
# Devo anche filtrare per livello ricorsivamente (e valutare l'impatto non solo sui microservizi direttamente connessi)

def filter(samples, dep, service):
    effects = dep[dep['cause']==service]['effect'].to_list()

    # print(effects)

    # causes = dep[dep['effect']==service]['cause'].to_list()
    # # print(causes)

    # unique = list(set(effects + causes + [service]))
    # print (unique)
    # unique = list(set(effects + [service]))

    # return samples[unique]
    return samples[effects]

def filter_dot(samples, service):

    graph = nx.Graph(nx.nx_pydot.read_dot("./utils/temp/graph.dot")) 
    # subG = nx.bfs_tree(causal_model, service, reverse=False)
    # print(subG)
    sub_G = nx.Graph()
    create_subgraph(graph, sub_G, service)
    # print("PRINT "+service+" "+str(sub_G.nodes))
    sub_G.remove_node(service)

    if service == 'ts-food-map-service_f':
        print(samples[list(sub_G.nodes)])

    return samples[list(sub_G.nodes)]

def create_subgraph(G,sub_G,start_node):
    sub_G.add_node(start_node)
    for n in G.neighbors(start_node):
        if n not in sub_G.neighbors(start_node):
            sub_G.add_path([start_node,n])
            create_subgraph(G,sub_G,n)


def convert(dependencies_file,dot_out,csv_out):
    file = open(dependencies_file)
    file_out = open(dot_out, 'w')

    data = json.load(file)
    df = pd.DataFrame(columns=['cause','effect'])
    prior_knowledge = []

    file_out.writelines("digraph g {\n")
    for key in data.keys():
        if data[key] :
            for value in data[key]:
                f_value = value + '_f'
                f_key = key + '_f'
                df = df.append(pd.DataFrame({'cause':[f_value], 'effect':[f_key]}))
                file_out.writelines("   \"" + f_value + "\" -> \"" + f_key + "\" [arrowtail=none, arrowhead=normal]; \n")
                prior_knowledge.append([f_value,f_key])

    # print(prior_knowledge)
    # print(df.reset_index(drop=True))
    file_out.writelines("}")
    df.to_csv(csv_out, index=False)
    return prior_knowledge

def check_service_fails(service, df):
    fails = False

    if 2 in df[service].values:
        fails = True

    return fails



def build_img(report_dot_out,report_img_out):
    dot_str = open(report_dot_out,'r').read()
    # print(dot_str)
    graphs = pydot.graph_from_dot_data(dot_str)
    svg_str = graphs[0].create_svg()
    svg_str = str(svg_str).replace("\\r\\n", "").replace("b\'","").replace("\'","").replace("ts&#45;","").replace("_f","")
    img_file = open(report_img_out, "w")
    img_file.write(svg_str)
    img_file.close()
    

# def combine_multi_dot(final_dot, new_dot):

#     return 0

def check_not_in_file(multi_dot, line_to_write):
    file = open(multi_dot, 'r')

    for line in file:
        if line == line_to_write:
            return False
    
    return True

def mapping(iteration):
    if iteration == 1:
        return "base_invalid"
    elif iteration == 2:
        return "base_valid"
    elif iteration == 3:
        return "pairwise"
    elif iteration == 4:
        return "pairwise_valid"
    else:
        return "NA"

# def adjust_ips(json_file, csv_file):
#     json_t = json.load(json_file)
#     csv_t = pd.read_csv(csv_file)

#     #parsing di json devo prendere l'ip del receiver