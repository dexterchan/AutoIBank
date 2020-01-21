//import _ from "lodash";
import httpservices from "./utils/httpservices";
import config from "../config.json";
import { toast } from "react-toastify";

const getBondSecurities = async filter => {
  try {
    const { data: securities } = await httpservices.get(
      `${config.apiEndpoint}/bondsecurity/${filter}`
    );
    return securities;
  } catch (ex) {
    if (ex.response && ex.response.status === 404) {
      alert("Bond securities fail to retrieve");
    }
    toast.error(`An unexpected error occurred` + ex);
    throw ex;
  }
};
const getBondPrice = async identifier => {
  try {
    const { data: bondprice, status } = await httpservices.get(
      `${config.apiEndpoint}/bondprice/${identifier}`
    );
    status === 200 && (bondprice["_id"] = bondprice["identifier"]);
    return bondprice;
  } catch (ex) {
    if (ex.response && ex.response.status === 404) {
      alert("Bond price fail to retrieve");
    }
    toast.error(`An unexpected error occurred` + ex);
  }
};

export { getBondSecurities, getBondPrice };
