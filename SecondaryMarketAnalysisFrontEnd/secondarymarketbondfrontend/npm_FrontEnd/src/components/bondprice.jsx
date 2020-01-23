import React, { Component } from "react";

import { getBondSecurities } from "../services/RestfulBondPrice";
import "./css/bondprice.css";
import BondpriceTable from "./bondpriceTable";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

class BondPrice extends Component {
  state = {
    bondPriceLst: [],
    securities: [],
    searchQuery: "",
    currentPage: 1,
    sortColumn: { path: "_id", order: "asc" }
  };

  async componentDidMount() {
    const securityNames = await getBondSecurities("*");
    const securities = securityNames.map(name => {
      const obj = { _id: name, price: name };
      return obj;
    });
    this.setState({ securities });
  }

  handleSort = sortColumn => {
    this.setState({ sortColumn });
  };
  //<ToastContainer />
  render() {
    const { securities, sortColumn } = this.state;
    let count = securities.count;
    if (count === 0) return <p>There are no bond price to display.</p>;
    let totalCount = count;
    return (
      <div className="bondprice-header">
        <div className="row">
          <div className="col">
            {count === 0 && (
              <p className="m-5 "> Showing {totalCount} bonds.</p>
            )}
            <BondpriceTable
              onSort={this.handleSort}
              sortColumn={sortColumn}
              securities={securities}
            />
          </div>
        </div>
      </div>
    );
  }
}

export default BondPrice;
