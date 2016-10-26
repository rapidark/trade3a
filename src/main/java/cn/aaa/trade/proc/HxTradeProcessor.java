package cn.aaa.trade.proc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.aaa.trade.convert.IConvert;
import cn.aaa.trade.util.ProcessUtil;
import cn.aaa.trade.util.PropertiesUtil;
import cn.aaa.trade.util.StringParser;

public class HxTradeProcessor extends AbsTradeProc {
	
	public static void main(String[] args) throws Exception {
		HxTradeProcessor tradeProcessor = new HxTradeProcessor();
		tradeProcessor.login();
		List<Map<String, Object>> result = tradeProcessor.queryJiaoge("2016-10-01", "2016-10-20");
		System.out.println(result);
	}
	
    private String scriptType;
    private String autoit;
    private String xiadan;
    private String account;
    private String pass;
    private String verify;

    public HxTradeProcessor() {
        this.autoit = PropertiesUtil.get("autoit");
        this.scriptType = PropertiesUtil.get("scriptType");
        this.xiadan = PropertiesUtil.get("xiadan");
        this.pass = PropertiesUtil.get("pass");
        this.account = PropertiesUtil.get("account");
        
        this.verify = PropertiesUtil.get("verify");
    }

    protected String getCmdPath(String cmd) {
        return autoit + " script/" + scriptType + "/" + cmd + ".au3";
    }

    protected String getCmdPath(String cmd, Object... params) {
        cmd = autoit + " script/" + scriptType + "/" + cmd + ".au3";
        for (Object param : params) {
            if (param instanceof String) {
                cmd += " \"" + param + "\"";
            }
            else {
                cmd += " " + param;
            }
        }
        return cmd;
    }

    public void login() throws Exception {
        ProcessUtil.run(getCmdPath("login", xiadan, account, pass, verify));
    }

    public List<Map<String, Object>> queryJiaoge(String startTime, String endTime) throws Exception {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        String jiaogeStr = ProcessUtil.run(getCmdPath("queryJiaoge", startTime, endTime));
        System.out.println(jiaogeStr);
        result.addAll(StringParser.parse(jiaogeStr,
                        new String[]{ "合同编号", "成交日期", "摘要", "证券代码", "证券名称", "成交均价", "成交数量", "发生金额", "手续费", "印花税", "股东帐户" }, 
                        new String[]{ "ID", "DATE", "TYPE", "CODE", "NAME", "PRICE", "NUM", "VALUE", "YONGJIN", "YINHUA", "ZHANGHU" }, 
                        new Object[]{ null, null, new TradeTypeConvert(), null, null, double.class, new NumConvert(), double.class, double.class, double.class, null }));
//        String lishizijinStr = ProcessUtil.run(getCmdPath("queryLishizijin", startTime, endTime));
//        result.addAll(parseLishizijin(lishizijinStr));
        return result;
    }

    private List<Map<String, Object>> parseLishizijin(String content) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        String[][] datas = StringParser.toArray(content);
        if (datas.length <= 1) {
            return result;
        }
        for (int i = 1, len = datas.length; i < len; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            String type = datas[i][1];
            if (type.startsWith("银行转存")) {
                map.put("DATE", datas[i][0]);
                map.put("TYPE", "银行转证券");
                map.put("VALUE", Double.parseDouble(datas[i][2]));
                result.add(map);
            } else if (type.startsWith("银行转取")) {
                map.put("DATE", datas[i][0]);
                map.put("TYPE", "证券转银行");
                map.put("VALUE", -Double.parseDouble(datas[i][3]));
                result.add(map);
            } else if (type.startsWith("利息归本")) {
                map.put("DATE", datas[i][0]);
                map.put("TYPE", type);
                map.put("VALUE", Double.parseDouble(datas[i][2]));
                result.add(map);
            }
        }
        return result;
    }
    
    public static class NumConvert implements IConvert {
        @Override
        public Object convert(Map<String, Object> row, String strVal) {
            String code = (String) row.get("CODE");
            if (code != null && (code.startsWith("12") || code.startsWith("11") || code.startsWith("204"))) {
                return ((int)Float.parseFloat(strVal)) * 10;
            }
            return (int)Float.parseFloat(strVal);
        }
    }

    public static class TradeTypeConvert implements IConvert {
        @Override
        public Object convert(Map<String, Object> row, String strVal) {
            if ("证券买入".equals(strVal)) {
                return "买入";
            }
            else if ("证券卖出".equals(strVal)) {
                return "卖出";
            }
            else if ("质押回购拆出".equals(strVal)) {
                return "融券回购";
            }
            else if ("拆出质押购回".equals(strVal)) {
                return "融券购回";
            }
            else if ("质押回购拆入".equals(strVal)) {
                return "融资回购";
            }
            else if ("拆入质押购回".equals(strVal)) {
                return "融资购回";
            }
            return strVal;
        }
    }

    @Override
    public void buy(String code, double price, int num) throws Exception {
        ProcessUtil.run(getCmdPath("buy", code, price, num));
    }

    @Override
    public void sell(String code, double price, int num) throws Exception {
        ProcessUtil.run(getCmdPath("sell", code, price, num));
    }

    @Override
    public void exchange(String buyCode, double buyPrice, int buyNum, String sellCode, double sellPrice, int sellNum) throws Exception {
        ProcessUtil.run(getCmdPath("exchange", buyCode, buyPrice, buyNum, sellCode, sellPrice, sellNum));
    }
}
