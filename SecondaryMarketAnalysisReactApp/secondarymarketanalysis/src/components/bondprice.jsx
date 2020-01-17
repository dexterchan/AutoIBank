import React, { Component } from "react";

import { getBondPrice } from "../services/fakeBondPrice";
import "./css/bondprice.css";
import BondpriceTable from "./bondpriceTable";

class BondPrice extends Component {
  state = {
    bondPriceLst: [],
    searchQuery: "",
    currentPage: 1,
    sortColumn: { path: "_id", order: "asc" }
  };

  componentDidMount() {
    setInterval(() => {
      this.setState({ bondPriceLst: getBondPrice() });
    }, 500);
  }

  handleSort = sortColumn => {
    this.setState({ sortColumn });
  };

  render() {
    const { length: count } = this.state.bondPriceLst;
    if (count === 0) return <p>There are no bond price to display.</p>;
    let totalCount = count;

    const { bondPriceLst, sortColumn } = this.state;
    return (
      <div className="bondprice-header">
        <div className="row">
          <div className="col">
            {count === 0 && (
              <p className="m-5 "> Showing {totalCount} bonds.</p>
            )}
            <BondpriceTable
              bondPricelst={bondPriceLst}
              onSort={this.handleSort}
              sortColumn={sortColumn}
            />
          </div>
        </div>
      </div>
    );
  }
}

export default BondPrice;
