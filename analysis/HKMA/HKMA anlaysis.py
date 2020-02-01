#!/usr/bin/env python
# coding: utf-8

# In[1]:


import requests
import pandas as pd
import abc


# In[2]:


urls={}
urls["outstandingGovBond"]='https://api.hkma.gov.hk/public/market-data-and-statistics/monthly-statistical-bulletin/gov-bond/list-outstanding-govbonds'
urls["DailyGovBondPrice"]="https://api.hkma.gov.hk/public/market-data-and-statistics/monthly-statistical-bulletin/gov-bond/instit-bond-price-yield-daily"
urls["TenderResult"]="https://api.hkma.gov.hk/public/market-data-and-statistics/monthly-statistical-bulletin/gov-bond/tender-results-gov-bonds-ibip"
urls["NewIssuanceGovBond"]="https://api.hkma.gov.hk/public/market-data-and-statistics/monthly-statistical-bulletin/gov-bond/new-issuance-amt-gov-bonds"
urls["outstandingAmtGovBond"]="https://api.hkma.gov.hk/public/market-data-and-statistics/monthly-statistical-bulletin/gov-bond/out-amt-gov-bonds-remaining-tenor"
urls["outstandingAmtGovBond_OrgMat"] ="https://api.hkma.gov.hk/public/market-data-and-statistics/monthly-statistical-bulletin/gov-bond/out-amt-gov-bonds-original-maturity"


# In[3]:


class DataSink(abc.ABC):
    @abc.abstractmethod
    def insertJsonData(self, json):
        pass


# In[4]:


def queryHKMA_API(url,inputparm={}, pageSize=100,offset=0, limit=0):
    import requests
    import json
    params=dict(**inputparm)
    params["pagesize"] = pageSize
    cnt = 0
    
    
    while True:
        params["offset"] = offset + cnt * pageSize
        if limit > 0 and limit < params["offset"]:
            return None
        res=requests.get(url, params)
        if res.status_code == 200:
            jsonResponse = res.json() 
        else: 
            return None
        if not jsonResponse["header"]["success"]:
            print (f"ErrCode:{jsonResponse['header']['err_code']} Err msg:{jsonResponse['header']['err_msg']}")
            return None
        result = jsonResponse["result"]
        if result["datasize"] == 0:
            return None
        yield result["records"]
        cnt += 1

     

def getHKMA_data(url,inputparm={}, pageSize=5,offset=0, limit=0):
    outDF = pd.DataFrame()
    print("Start", end="")
    for json in queryHKMA_API(url,inputparm, pageSize,offset, limit): 
        _df = pd.DataFrame.from_records(json)
        outDF = outDF.append(_df, ignore_index = True) 
        print("=", end="")
    print("finish")
    return outDF 
    
    


# In[5]:


def getHKMASync(url:str, dataSink:DataSink,inputparm={}, pageSize=5,offset=0, limit=0):
    print("Start", end="")
    for json in queryHKMA_API(url,inputparm, pageSize,offset, limit): 
        dataSink.insertJsonData(json)
        print("=", end="")
    print("finish")


# In[25]:


class PandasDataSink(DataSink):
    def __init__(self):
        self.outDF = pd.DataFrame()
    def insertJsonData(self, json):
        _df = pd.DataFrame.from_records(json)
        self.outDF = self.outDF.append(_df, ignore_index = True)


# In[29]:


from pymongo import MongoClient
class MongoDBDataSink(DataSink):
    def __init__(self, connectStr, database, collection):
        self.client = MongoClient(connectStr)
        self.database = database
        self.collection = collection
        self.db = self.client[database]
    
    def insertJsonData(self, json):
        result=self.db[self.collection].insert_many(json)
        return result

# In[30]:


mongoDBDataSink = MongoDBDataSink("mongodb://mongoadmin:secret@localhost:27017", "hkma","DailyGovBondPrice" )

getHKMASync(urls["DailyGovBondPrice"], mongoDBDataSink, {"segment":"Benchmark"} ,100,0)



# In[ ]:



