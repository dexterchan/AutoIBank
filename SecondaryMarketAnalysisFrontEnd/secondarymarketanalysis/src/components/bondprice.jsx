import React, { Component } from "react";

import { getBondPrice, getBondSecurities } from "../services/RestfulBondPrice";
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
    const securities = await getBondSecurities("*");

    this.setState({ securities });
    setInterval(async () => {
      Promise.all(
        securities.map(async security => await getBondPrice(security))
      ).then(bondPriceLstRaw => {
        //console.log(bondPriceLstRaw);
        const bondPriceLst = bondPriceLstRaw.filter(p => p !== undefined);
        this.setState({ bondPriceLst });
      });
    }, 2000);
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
        <ToastContainer />
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
