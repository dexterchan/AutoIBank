import React, { Component } from "react";
import Table from "./common/table";
import "./css/pricetable.css";

class BondpriceTable extends Component {
  columns = [
    { path: "_id", label: "Identifier" },
    { path: "bid", label: "Bid", content: price => price.bid.toFixed(2) },
    { path: "ask", label: "Ask", content: price => price.ask.toFixed(2) }
  ];
  render() {
    const { bondPricelst, onSort, sortColumn } = this.props;
    return (
      <Table
        columns={this.columns}
        data={bondPricelst}
        sortColumn={sortColumn}
        onSort={onSort}
      />
    );
  }
}

export default BondpriceTable;
