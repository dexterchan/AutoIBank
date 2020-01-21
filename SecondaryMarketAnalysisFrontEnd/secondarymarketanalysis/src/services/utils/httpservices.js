import axios from "axios";
import { toast } from "react-toastify";
//import logger from "./logServices";
axios.interceptors.response.use(null, error => {
  console.log("Interceptor called");
  const expectedErr =
    error.response &&
    error.response.status >= 400 &&
    error.response.status < 500;
  if (!expectedErr) {
    console.log("Logging the error", error);

    toast.error(`An unexpected error occurred`);
    //logger.log(error);
  }
  return Promise.reject(error);
});

export default {
  get: axios.get,
  post: axios.post,
  put: axios.put,
  delete: axios.delete
};
