import random
import numpy as np
import uuid
import re
from typing import Dict
import time


class PrimaryInvestorRobot:
    def __init__(self, name: str, TenorVsBond: Dict[int, Dict], tenorWeight: Dict[int, float], notional: int,
                 std: float, bidask="ASK", ccy="HKD"):
        self.name = name
        self.TenorVsBond = TenorVsBond
        self.tenorWeight = tenorWeight
        self.notional = notional
        self.bidask = bidask
        self.std = std
        self.ccy = "HKD"
        self.YMDMatcher = re.compile(r"(\d{4})-(\d{2})-(\d{2})")

    def generateTrade(self):
        tenor = random.choices(list(self.tenorWeight.keys()), weights=list(self.tenorWeight.values()), k=1)[0]
        tradeid = str(uuid.uuid4())
        trade = {}
        asset = {}

        trade["id"] = tradeid
        trade["tradeType"] = "BOND"
        trade["timestamp"] = int(time.time() * 1000)
        trade["cust"] = self.name
        security = random.choice(list(self.TenorVsBond[tenor]))

        mDate = self.YMDMatcher.match(security['expected_maturity_date'])
        if mDate is not None:
            year = int(mDate.group(1)) - tenor
            trade["tradeDate"] = f"{year}-{mDate.group(2)}-{mDate.group(3)}"

        asset['securityId'] = security['isin_code']
        asset['currency'] = self.ccy
        asset['price'] = float(security["coupon"])
        asset["bidask"] = self.bidask
        asset["notional"] = int(np.round(np.abs(np.random.normal(0, self.std, 1)), -3)[0]) + self.notional
        trade['asset'] = asset

        return trade