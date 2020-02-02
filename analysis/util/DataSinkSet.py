import abc
import requests
import pandas as pd
class DataSink(abc.ABC):
    @abc.abstractmethod
    def insertJsonData(self, json):
        pass

class PandasDataSink(DataSink):
    def __init__(self):
        self.outDF = pd.DataFrame()
    def insertJsonData(self, json):
        _df = pd.DataFrame.from_records(json)
        self.outDF = self.outDF.append(_df, ignore_index=True)


from pymongo import MongoClient


class MongoDBDataSink(DataSink):
    def __init__(self, connectStr, database, collection):
        self.client = MongoClient(connectStr)
        self.database = database
        self.collection = collection
        self.db = self.client[database]

    def insertJsonData(self, json):
        result = self.db[self.collection].insert_many(json)
        return result