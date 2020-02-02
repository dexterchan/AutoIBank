
import json
from util.InvestorRobot import PrimaryInvestorRobot
from random_word import RandomWords
import random

def getHKMAisin(securityInfoJsonFile):
    with open('SelectedSecurity.json') as json_file:
        hkmaisin = json.load(json_file)

        def transformYear(b):
            b['original_maturity'] = int(b['original_maturity'].split("-")[0])
            return b

        hkmaisin_ = list(map(lambda b: transformYear(b), hkmaisin))
        return hkmaisin_

def generatePrimaryTrades(TenorVsBond, numOfInvestors, numOfTradesEach):
    randomWords = RandomWords()

    for n in range(numOfInvestors):
        investorName = randomWords.get_random_word()
        tenorWeight = {15: random.random(), 10: random.random(), 5: random.random()}
        primaryInvestorBot = PrimaryInvestorRobot(investorName,TenorVsBond, tenorWeight, 10000000,10000000)

        for t in range(numOfTradesEach):
            trade = primaryInvestorBot.generateTrade()
            print (trade)

if __name__ == "__main__":
    from collections import defaultdict

    hkmaisin_ = getHKMAisin("SelectedSecurity.json")
    TenorVsBond = defaultdict(list)
    for b in hkmaisin_:
        TenorVsBond[b['original_maturity']].append(b)

    generatePrimaryTrades(TenorVsBond, 1, 2)