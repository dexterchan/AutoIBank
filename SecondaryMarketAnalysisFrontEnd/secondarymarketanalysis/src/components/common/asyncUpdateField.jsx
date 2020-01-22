import React, { Component } from "react";

class AsyncUpdateField extends Component {
  state = {
    myValue: ""
  };
  async componentDidMount() {
    const { asyncFunc, period } = this.props;

    setInterval(async () => {
      try {
        const value = await asyncFunc();
        this.setState({ myValue: value });
      } catch (ex) {
        console.log(ex);
      }
    }, period);
  }
  render() {
    const { myValue } = this.state;
    return <React.Fragment>{myValue}</React.Fragment>;
  }
}

export default AsyncUpdateField;
