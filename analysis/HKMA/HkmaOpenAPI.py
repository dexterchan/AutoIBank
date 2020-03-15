
def queryHKMA_API(url, inputparm={}, pageSize=100, offset=0, limit=0):
    import requests
    import json
    params = dict(**inputparm)
    params["pagesize"] = pageSize
    cnt = 0

    while True:
        params["offset"] = offset + cnt * pageSize
        if limit > 0 and limit < params["offset"]:
            return None
        res = requests.get(url, params)
        if res.status_code == 200:
            jsonResponse = res.json()
        else:
            return None
        if not jsonResponse["header"]["success"]:
            print(f"ErrCode:{jsonResponse['header']['err_code']} Err msg:{jsonResponse['header']['err_msg']}")
            return None
        result = jsonResponse["result"]
        if result["datasize"] == 0:
            return None
        yield result["records"]
        cnt += 1