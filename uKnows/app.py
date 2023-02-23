from flask import Flask
from flask_restful import Resource, Api, reqparse
import pandas as pd
import ast
import requests
from compute import compute
import os, glob


app = Flask(__name__)
api = Api(app)
host = "host.docker.internal"

class retrieve(Resource):
    # methods go here
    def get(self):
        # info
        url = "http://"+host+":11112/getInfo?format=csv"
        response = requests.get(url)

        infofile = open("./data/info.csv","w")
        infofile.write(str(response.text))
        infofile.close()

        # Dependencies
        url = "http://"+host+":11112/getDependencies?option=all"
        response = requests.get(url)

        infofile = open("./data/dependencies.json","w")
        infofile.write(str(response.text))
        infofile.close()
        
        return "Setup done"
    
class clear(Resource):
    # methods go here
    def get(self):
        data_files = glob.glob("./data/*")
        output_bfiles = glob.glob("./output/*")
        output_mfiles = glob.glob("./output/report_multi/*")

        for file in data_files:
            os.remove(file)

        for file in output_bfiles:
            if 'report_multi' not in file:
                os.remove(file)

        for file in output_mfiles:
            os.remove(file)

        return "Clear done"    


api.add_resource(retrieve, '/setup')
api.add_resource(compute, '/compute')
api.add_resource(clear, '/clear')

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=11113)  # run our Flask app