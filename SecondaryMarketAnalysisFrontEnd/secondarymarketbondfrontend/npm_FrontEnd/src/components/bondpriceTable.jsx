import React, { Component } from "react";
import { getBondPrice } from "../services/RestfulBondPrice";
import Table from "./common/table";
import "./css/pricetable.css";
import "./common/asyncUpdateField";
import AsyncUpdateField from "./common/asyncUpdateField";

class BondpriceTable extends Component {
  rounding = 2;
  columns = [
    { path: "_id", label: "Identifier" },
    {
      path: "price",
      label: "Bid/Ask",
      content: sec => (
        <AsyncUpdateField
          asyncFunc={async () => {
            const price = await getBondPrice(sec._id);
            return (
              price.bid.toFixed(this.rounding) +
              "/" +
              price.ask.toFixed(this.rounding)
            );
          }}
          period={500}
        />
      )
    }
  ];
  render() {
    const { securities, onSort, sortColumn } = this.props;
    return (
      <Table
        columns={this.columns}
        data={securities}
        sortColumn={sortColumn}
        onSort={onSort}
      />
    );
  }
}

export default BondpriceTable;
