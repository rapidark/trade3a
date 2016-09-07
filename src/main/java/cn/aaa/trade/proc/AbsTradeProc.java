package cn.aaa.trade.proc;

import java.util.List;
import java.util.Map;

public abstract class AbsTradeProc {
    public void login() throws Exception {
    }

    public List<Map<String, Object>> queryJiaoge(String startTime, String endTime) throws Exception {
        return null;
    }

    public void buy(String code, double price, int num) throws Exception {
    }

    public void sell(String code, double price, int num) throws Exception {
    }

    public void exchange(String buyCode, double buyPrice, int buyNum, String sellCode, double sellPrice, int sellNum) throws Exception {
    }
}
