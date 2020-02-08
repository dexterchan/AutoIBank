
import json
from util.InvestorRobot import PrimaryInvestorRobot
from random_word import RandomWords
import random
from util.AvroIO import AvroFileWriter, Writer

def __getHKMAisin(securityInfoJsonFile):
    with open('SelectedSecurity.json') as json_file:
        hkmaisin = json.load(json_file)

        def transformYear(b):
            b['original_maturity'] = int(b['original_maturity'].split("-")[0])
            return b

        hkmaisin_ = list(map(lambda b: transformYear(b), hkmaisin))
        return hkmaisin_





def __generatePrimaryTrades(TenorVsBond, numOfInvestors, numOfTradesEach, writer:Writer):
    randomWords = RandomWords()

    for n in range(numOfInvestors):
        investorName = randomWords.get_random_word()
        tenorWeight = {15: random.random(), 10: random.random(), 5: random.random()}
        primaryInvestorBot = PrimaryInvestorRobot(investorName,TenorVsBond, tenorWeight, 10000000,10000000)

        for t in range(numOfTradesEach):
            trade = primaryInvestorBot.generateTrade()
            print(trade)
            writer.write(trade)

def generateHKMATrades (numOfInvestors:int, numOfTradesEach:int, securityJsonFile:str, avroFileOutput):
    from collections import defaultdict

    hkmaisin_ = __getHKMAisin(securityJsonFile)
    TenorVsBond = defaultdict(list)
    for b in hkmaisin_:
        TenorVsBond[b['original_maturity']].append(b)
    schemaFile = "bondtrade.avsc"

    writer = AvroFileWriter(schemaFile,avroFileOutput )
    __generatePrimaryTrades(TenorVsBond, numOfInvestors, numOfTradesEach, writer)
    writer.close()

if __name__ == "__main__":
    avroFile = "bondtrade.avro"
    numOfInvestors = 1
    numOfTradesEach = 2
    generateHKMATrades(numOfInvestors, numOfTradesEach,"SelectedSecurity.json", avroFile)
