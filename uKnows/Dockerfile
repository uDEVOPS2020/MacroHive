FROM ubuntu:18.04

RUN mkdir -p /app
ADD . /app/
WORKDIR /app

RUN apt-get -y update
RUN apt-get -y install git

RUN apt-get install -y software-properties-common
RUN add-apt-repository ppa:deadsnakes/ppa
RUN apt-get install -y python3.8

RUN apt-get install -y openjdk-8-jdk
RUN apt-get install -y gcc python3.8-dev

RUN apt-get -y install python3-pip
RUN python3.8 -m pip install --upgrade pip

COPY requirements.txt requirements.txt
RUN pip install -r requirements.txt
RUN pip install git+https://github.com/bd2kccd/py-causal

COPY . .

CMD [ "python3.8", "app.py"]