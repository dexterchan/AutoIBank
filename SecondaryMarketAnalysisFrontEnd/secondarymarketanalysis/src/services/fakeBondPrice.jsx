import _ from "lodash";
const bondprice = [
  {
    _id: "ISIN4324",
    bid: 96.28247143861591,
    ask: 108.65176029967543
  },
  {
    _id: "CISP23434",
    bid: 96.78444315908888,
    ask: 103.02217257335185
  },
  {
    _id: "ISIN1234",
    bid: 93.62312922890003,
    ask: 106.54801691784124
  }
];

var counter = 0;
const getBondPrice = () => {
  counter++;
  let _bondprice = _.cloneDeep(bondprice);
  if (counter % 4) {
    _bondprice = _bondprice.map(bondprice => {
      if (bondprice._id === "ISIN4324") {
        bondprice.bid += 2;
      }
      return bondprice;
    });
  } else {
    _bondprice = bondprice;
  }
  return _bondprice;
};

export { getBondPrice };
