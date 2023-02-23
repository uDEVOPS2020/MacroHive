import os
from utils.util import *
from utils.parser import *
from utils.query import *
from pycausal.pycausal import pycausal as pc
import pandas as pd
from flask import Flask
from flask_restful import Resource, Api, reqparse

pc = pc()


class compute(Resource):
    
    def get(self):

        # parse param
        parser = reqparse.RequestParser()  # initialize
        
        parser.add_argument('mode', required=True,location='args')  # add args
        args = parser.parse_args()  # parse arguments to dictionary
        mode = args['mode']

        pc.start_vm()

        info_path = "./data/"
        dot_path = "./output/report_multi"
        dot_out = "./output/sdg.dot"
        csv_out = "./output/sdg.csv"
        info_out_file = "./output/result.csv"
        multi_dot = "./output/multi_dot.dot"
        multi_img = "./output/multi_svg.svg"
        
        multi_file_out = open(multi_dot, 'w')
        multi_file_out.writelines("digraph g {\n")
        multi_file_out.close()

        for filename in os.listdir(info_path):
            if "info" in filename:
                info_file = "./data/"+filename
                # report_dot_out = "./output/report_multi/report_"+filename.split(".")[0]+".dot"
                # report_img_out = "./output/report_multi/report_"+filename.split(".")[0]+"_graph.svg"
                report_dot_out = "./output/report_multi/report.dot"
                report_img_out = "./output/report_multi/report_graph.svg"                
                
                # iteration = filename.split("_")[1].split(".")[0]
                # dependencies_file = "./data/dependencies/dependencies_"+iteration+".json"
                dependencies_file = "./data/dependencies.json"

                if mode == "functional" or mode == "Functional":
                    parse_classes(info_file, info_out_file)
                elif mode == "robustness" or mode == "Robustness":
                    parse(info_file, info_out_file)
                else:
                    return "ERROR - Specify mode"

                prior_knowledge = convert(dependencies_file,dot_out,csv_out)

                # causal_model = init_model(pd.read_csv(info_out_file),dot_out)
                causal_model,nodes = init_model_pk(pd.read_csv(info_out_file),prior_knowledge,pc)
                # print(causal_model)

                services_list = pd.read_csv(csv_out)['cause'].to_list()
                services_list = list(dict.fromkeys(services_list))
                # print(services_list)

                causes_dict = {}

                file_out = open(report_dot_out, 'w')
                file_out.writelines("digraph g {\n")

                # for service in services_list:
                for service in nodes:
                    if check_service_fails(service, pd.read_csv(info_out_file)):

                        samples = intervention(causal_model, service,2)

                        samples.to_csv("./output/samples.csv",index=False)
                        # filtered_samples = filter(samples,pd.read_csv(csv_out),service)
                        filtered_samples = filter_dot(samples,service)

                        filtered_samples = filtered_samples.mean()

                        filtered_samples = filtered_samples.loc[filtered_samples > 1.55].index.values.tolist()


                        if filtered_samples:
                            for e_service in filtered_samples:
                                line_to_write = "   \"" + e_service + "\" -> \"" + service + "\" [arrowtail=none, arrowhead=normal]; \n"
                                file_out.writelines(line_to_write)
                                if check_not_in_file(multi_dot,line_to_write):
                                    multi_file_out = open(multi_dot, 'a')
                                    multi_file_out.writelines(line_to_write)
                                    multi_file_out.close()

                            temp_dict = {}
                            temp_dict[service] = filtered_samples
                            causes_dict.update(temp_dict)

                file_out.writelines("}")
                file_out.close()

                build_img(report_dot_out,report_img_out)

                
        multi_file_out = open(multi_dot, 'a')
        multi_file_out.writelines("}")
        multi_file_out.close()
        build_img(multi_dot,multi_img)

        # build intersection (for each line in sum_dot, write it in new dot if it is present in each individual dot)
        # for iteration in range(1,5):
        #     if iteration == 4:
        #         file = open(multi_dot, 'r')
        #         technique = mapping(iteration)
        #         multi_dot_inters = "./output/multi_dot_inters_"+technique+".dot"
        #         multi_img_inters = "./output/multi_svg_inters_"+technique+".svg"
        #         if os.path.exists(multi_dot_inters):
        #             os.remove(multi_dot_inters)
        #         for line in file:
        #             to_write = True
        #             # count_not_in = 0
        #             for filename in os.listdir(dot_path):
        #                 if ".dot" in filename and to_write:
        #                     file_iteration = filename.split("_")[2].split(".")[0]
        #                     op = int(file_iteration) % 4
        #                     if op == iteration or (op == 0 and iteration == 4):
        #                         if check_not_in_file(dot_path+"/"+filename, line):
        #                             to_write = False
        #                             # count_not_in += 1

        #             if to_write:
        #             # if count_not_in <= 5:
        #                 multi_inters_file_out = open(multi_dot_inters, 'a')
        #                 multi_inters_file_out.writelines(line)
        #                 multi_inters_file_out.close()
        #         file.close()
        #         build_img(multi_dot_inters,multi_img_inters)

        
        # print("END")
        # pc.stop_vm()