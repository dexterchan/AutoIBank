#!/usr/bin/env python
import logging
from HKMA.HkmaOpenAPI import queryHKMA_API
import mysql.connector
import re
FORMAT = '%(asctime)-15s %(levelname)s %(message)s'
logging.basicConfig(format=FORMAT, level=logging.DEBUG)
# In[2]:

dailybondpriceURL = "https://api.hkma.gov.hk/public/market-data-and-statistics/monthly-statistical-bulletin/gov-bond/instit-bond-price-yield-daily"

class DailyBondPrice():

    def __init__(self, hostname: str, port:int, user: str, pwd:str):
        self.mydb = self.__connect(hostname, port, user, pwd)
        self.tenorMatch = re.compile(r"ind_pricing_(\w*)")

    def __connect(self, hostname: str, port:int, user: str, pwd:str):
        return mysql.connector.connect(
            host=hostname,
            port=port,
            user=user,
            passwd=pwd,
            database='hkma'
        )
        self.__dropTable()
        self.__createTable()

    def insertDailyMarketData(self,inputparm={"segment":"Benchmark"}, pageSize=100, offset=0, limit=0):
        print("Start", end="")
        for jsonlst in queryHKMA_API(dailybondpriceURL, inputparm, pageSize, offset, limit):
            #insert into database

            for json in jsonlst:
                asofdate = json["end_of_day"]

                for col in json.keys():
                    m = self.tenorMatch.match(col)
                    if m is not None:
                        tenor = m.group(1)
                        col = m.group(0)
                        if col in json and json[col] is not None:
                            print(f"{asofdate}, {tenor}, {json[col]} ")
                            try:
                                self.__insertRecord(asofdate, tenor, json[col])
                            except Exception as e:
                                logging.error(e)



            print("=", end="")
        print("finish")

    def __dropTable(self):
        sql = "drop table DailyGovBondPrice"
        mycursor = self.mydb.cursor()
        mycursor.execute(sql)

    def __createTable(self):
        sql = "create table DailyGovBondPrice( \
                AsOfDate varchar(12), \
                Tenor varchar(10), \
                Price decimal(10,2), \
                primary key(AsOfDate, Tenor) )"
        mycursor = self.mydb.cursor()
        mycursor.execute(sql)

    def __insertRecord(self, asofdate, tenor, price):
        sql = "INSERT INTO DailyGovBondPrice (AsOfDate, Tenor, Price) VALUES (%s, %s, %s)"
        val = (asofdate, tenor.upper(), price)
        mycursor = self.mydb.cursor()
        mycursor.execute(sql, val)
        self.mydb.commit()



if __name__ =="__main__":

    hostname = "localhost"
    port = 3306
    user = "agileintelligence"
    password="password"
    solu = DailyBondPrice("localhost", port, user, password)

    solu.insertDailyMarketData()
