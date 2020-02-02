#!/usr/bin/env python
# coding: utf-8

# In[1]:


import requests
import pandas as pd
import abc
from util.DataSinkSet import DataSink, PandasDataSink, MongoDBDataSink

# In[2]:


urls = {}
urls[
    "outstandingGovBond"] = 'https://api.hkma.gov.hk/public/market-data-and-statistics/monthly-statistical-bulletin/gov-bond/list-outstanding-govbonds'
urls[
    "DailyGovBondPrice"] = "https://api.hkma.gov.hk/public/market-data-and-statistics/monthly-statistical-bulletin/gov-bond/instit-bond-price-yield-daily"
urls[
    "TenderResult"] = "https://api.hkma.gov.hk/public/market-data-and-statistics/monthly-statistical-bulletin/gov-bond/tender-results-gov-bonds-ibip"
urls[
    "NewIssuanceGovBond"] = "https://api.hkma.gov.hk/public/market-data-and-statistics/monthly-statistical-bulletin/gov-bond/new-issuance-amt-gov-bonds"
urls[
    "outstandingAmtGovBond"] = "https://api.hkma.gov.hk/public/market-data-and-statistics/monthly-statistical-bulletin/gov-bond/out-amt-gov-bonds-remaining-tenor"
urls[
    "outstandingAmtGovBond_OrgMat"] = "https://api.hkma.gov.hk/public/market-data-and-statistics/monthly-statistical-bulletin/gov-bond/out-amt-gov-bonds-original-maturity"


# In[3]:


def queryHKMA_API(url, inputparm={}, pageSize=100, offset=0, limit=0):
    import requests
    import json
    params = dict(**inputparm)
    params["pagesize"] = pageSize
    cnt = 0

    while True:
        params["offset"] = offset + cnt * pageSize
        if limit > 0 and limit < params["offset"]:
            return None
        res = requests.get(url, params)
        if res.status_code == 200:
            jsonResponse = res.json()
        else:
            return None
        if not jsonResponse["header"]["success"]:
            print(f"ErrCode:{jsonResponse['header']['err_code']} Err msg:{jsonResponse['header']['err_msg']}")
            return None
        result = jsonResponse["result"]
        if result["datasize"] == 0:
            return None
        yield result["records"]
        cnt += 1


def getHKMA_data(url, inputparm={}, pageSize=5, offset=0, limit=0):
    outDF = pd.DataFrame()
    print("Start", end="")
    for json in queryHKMA_API(url, inputparm, pageSize, offset, limit):
        _df = pd.DataFrame.from_records(json)
        outDF = outDF.append(_df, ignore_index=True)
        print("=", end="")
    print("finish")
    return outDF


# In[4]:


def getHKMASync(url: str, dataSink: DataSink, inputparm={}, pageSize=5, offset=0, limit=0):
    print("Start", end="")
    for json in queryHKMA_API(url, inputparm, pageSize, offset, limit):
        dataSink.insertJsonData(json)
        print("=", end="")
    print("finish")


# In[5]:


mongoDBDataSink_OutstandingGovBond = MongoDBDataSink("mongodb://mongoadmin:secret@localhost:27017", "hkma",
                                                     "OutstandingGovBond")

# In[6]:


getHKMASync(urls["outstandingGovBond"], mongoDBDataSink_OutstandingGovBond)

# In[ ]:


mongoDBDataSink_OutstandingAmtGovBond = MongoDBDataSink("mongodb://mongoadmin:secret@localhost:27017", "hkma",
                                                        "OutstandingAmtGovBond")

# In[ ]:


getHKMASync(urls["outstandingAmtGovBond"], mongoDBDataSink_OutstandingAmtGovBond)

# In[ ]:


outstandingBond = getHKMA_data(urls["outstandingGovBond"])
outstandingBond = outstandingBond.sort_values(by=['expected_maturity_date'], axis=0, ascending=False)
dailyGovBondPrice = getHKMA_data(urls["DailyGovBondPrice"], {"segment": "Benchmark"}, 100, 0, 300)
TenderResult3Y = getHKMA_data(urls["TenderResult"], {"segment": "3year"}, 100, 0, 300)
NewIssuanceGovBond = getHKMA_data(urls["NewIssuanceGovBond"], {}, 100, 0, 300)
outstandingAmtGovBond = getHKMA_data(urls["outstandingAmtGovBond"])

# In[ ]:


len(dailyGovBondPrice)

# In[ ]:


outstandingAmtGovBond_OrgMat = getHKMA_data(urls["outstandingAmtGovBond_OrgMat"], {}, 100, 0, 100)

# In[ ]:


pandasDataSink = PandasDataSink()
getHKMASync(urls["outstandingAmtGovBond_OrgMat"], pandasDataSink, {}, 100, 0, 100)
outstandingAmtGovBond_OrgMat2 = pandasDataSink.outDF

# In[ ]:


len(outstandingAmtGovBond_OrgMat) == len(outstandingAmtGovBond_OrgMat2)

# In[ ]:


dailyGovBondPrice.head()

# In[ ]:


outstandingBond.head()

# In[ ]:


TenderResult3Y.head()

# In[ ]:


NewIssuanceGovBond

# In[ ]:


outstandingAmtGovBond

# In[ ]:


outstandingAmtGovBond_OrgMat

# In[ ]:




