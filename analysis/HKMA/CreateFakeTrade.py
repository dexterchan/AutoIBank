
import json
from util.InvestorRobot import PrimaryInvestorRobot
from random_word import RandomWords
import random
from util.AvroIO import AvroFileWriter, Writer

def __getHKMAisin(securityInfoJsonFile):
    with open(securityInfoJsonFile) as json_file:
        hkmaisin = json.load(json_file)

        def transformYear(b):
            b['original_maturity'] = int(b['original_maturity'].split("-")[0])
            return b

        hkmaisin_ = list(map(lambda b: transformYear(b), hkmaisin))
        return hkmaisin_





def __generatePrimaryTrades(TenorVsBond, numOfInvestors, numOfTradesEach, writer:Writer):
    randomWords = RandomWords()

    words = randomWords.get_random_words()

    for n in range(numOfInvestors):
        investorName = words[n%len(words)]
        tenorWeight = {15: random.random(), 10: random.random(), 5: random.random()}
        primaryInvestorBot = PrimaryInvestorRobot(investorName,TenorVsBond, tenorWeight, 10000000,10000000)

        for t in range(numOfTradesEach):
            trade = primaryInvestorBot.generateTrade()
            #print(trade)
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


import argparse

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("-o", "--output", help="output bond trade as avro file")
    parser.add_argument("-n", "--numInvestorTrades", help="number of trade per investor")
    parser.add_argument("-i", "--numInvestor", help="number of investors")
    parser.add_argument("-V", "--version", help="show program version", action="store_true")

    args = parser.parse_args()

    avroFile = args.output #"./sample/bondtrade.avro"

    numOfInvestors = 10 if args.numInvestor is None else int(args.numInvestor)
    numOfTradesEach = 1000 if args.numInvestorTrades is None else int(args.numInvestorTrades)
    print("%d investors, each investor %d trades"%(numOfInvestors, numOfTradesEach))
    generateHKMATrades(numOfInvestors, numOfTradesEach, "./HKMA/SelectedSecurity.json", avroFile)
