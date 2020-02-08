import unittest
from HKMA.CreateFakeTrade import generateHKMATrades

from avro.datafile import  DataFileReader
from avro.io import DatumReader
import datetime

class BondTradeTestSuite(unittest.TestCase):
    def test_HKMA_Bondtrades(self):
        avroFile = "testbondtrade.avro"
        numOfInvestors = 10
        numOfTradesEach = 100
        generateHKMATrades(numOfInvestors, numOfTradesEach, "HKMA/SelectedSecurity.json", avroFile)

        reader = DataFileReader(open(avroFile, "rb"), DatumReader())
        cnt = 0
        for bondtrade in reader:
            self.assertIsNotNone(bondtrade["cust"])
            self.assertIsNotNone(bondtrade["tradeDate"])
            self.assertIsNotNone(bondtrade["asset"]["securityId"])
            self.assertGreater(bondtrade["asset"]["notional"], 1000000)
            dt = datetime.datetime.fromtimestamp(bondtrade["timestamp"]/1000)
            nowdt = datetime.datetime.now()
            self.assertEqual(dt.year, nowdt.year)
            self.assertEqual(dt.month, nowdt.month)
            self.assertEqual(dt.day, nowdt.day)
            cnt += 1
        self.assertEqual(numOfInvestors * numOfTradesEach, cnt)

        reader.close()


if __name__ == '__main__':
    unittest.main()