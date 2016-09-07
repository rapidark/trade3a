package cn.aaa.trade;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.aaa.trade.proc.AbsTradeProc;
import cn.aaa.trade.util.PropertiesUtil;
import cn.aaa.trade.util.SerialUtil;

public class TradeInvoker {
    private static Logger logger = LoggerFactory.getLogger(TradeInvoker.class);
    private AbsTradeProc tradeProc;

    public AbsTradeProc getTradeProc() {
        return tradeProc;
    }

    public void setTradeProc(AbsTradeProc proc) {
        this.tradeProc = proc;
    }

    public Map load(Map params) throws Exception {
        String path = PropertiesUtil.get("policypath");
        if (path == null || path.trim().length() == 0) {
            return null;
        }
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        StringBuilder jsout = new StringBuilder();
        StringBuilder cssout = new StringBuilder();
        StringBuilder tplout = new StringBuilder();
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                String name = f.getName();
                if (name.endsWith(".js")) {
                    readFile(f, jsout);
                    jsout.append("\n");
                } else if (name.endsWith(".css")) {
                    readFile(f, cssout);
                    cssout.append("\n");
                } else if (name.endsWith(".tpl")) {
                    readFile(f, tplout);
                    tplout.append("\n");
                }
            }
        } else {
            readFile(file, jsout);
        }
        Map<String, String> result = new HashMap<String, String>();
        result.put("js", jsout.toString());
        result.put("css", cssout.toString());
        result.put("tpl", tplout.toString());
        return result;
    }

    private void readFile(File file, StringBuilder out) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line).append("\n");
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public String login(Map params) throws Exception {
        tradeProc.login();
        return "登录成功";
    }

    public String queryJiaoge(Map params) throws Exception {
        String path = PropertiesUtil.get("importpath");
        if (path == null || path.trim().length() == 0) {
            path = "C://";
        }
        File file = new File(path + "tradeimport.data");
        if (file.exists()) {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
            Object result = in.readObject();
            in.close();
            return SerialUtil.serialize(result);
        } else {
            String startTime = (String) params.get("start");
            String endTime = (String) params.get("end");
            List<Map<String, Object>> result = tradeProc.queryJiaoge(startTime, endTime);
            return SerialUtil.serialize(result);
        }
    }

    public String exportJiaoge(Map params) throws Exception {
        String path = PropertiesUtil.get("importpath");
        if (path == null || path.trim().length() == 0) {
            path = "C://";
        }
        Object datas = params.get("datas");
        ObjectOutput out = new ObjectOutputStream(new FileOutputStream(path + "tradeexport.data"));
        out.writeObject(datas);
        out.close();
        return "导出成功";
    }

    public String importJingzhi(Map params) throws Exception {
        String path = PropertiesUtil.get("importpath");
        if (path == null || path.trim().length() == 0) {
            path = "C://";
        }
        File file = new File(path + "jingzhiimport.data");
        if (file.exists()) {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
            Object result = in.readObject();
            in.close();
            return SerialUtil.serialize(result);
        } else {
        	throw new Exception("无净值数据文件");
        }
    }

    public String exportJingzhi(Map params) throws Exception {
        String path = PropertiesUtil.get("importpath");
        if (path == null || path.trim().length() == 0) {
            path = "C://";
        }
        Object datas = params.get("datas");
        ObjectOutput out = new ObjectOutputStream(new FileOutputStream(path + "jingzhiexport.data"));
        out.writeObject(datas);
        out.close();
        return "导出成功";
    }

    public String importSetting(Map params) throws Exception {
        String path = PropertiesUtil.get("importpath");
        if (path == null || path.trim().length() == 0) {
            path = "C://";
        }
        File file = new File(path + "settingimport.data");
        if (file.exists()) {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
            Object result = in.readObject();
            in.close();
            return SerialUtil.serialize(result);
        } else {
        	throw new Exception("无设置数据文件");
        }
    }

    public String exportSetting(Map params) throws Exception {
        String path = PropertiesUtil.get("importpath");
        if (path == null || path.trim().length() == 0) {
            path = "C://";
        }
        Object datas = params.get("setting");
        ObjectOutput out = new ObjectOutputStream(new FileOutputStream(path + "settingexport.data"));
        out.writeObject(datas);
        out.close();
        return "导出成功";
    }

    public String buy(Map params) throws Exception {
        String code = (String) params.get("code");
        double price = Double.parseDouble(String.valueOf(params.get("price")));
        int num = Integer.parseInt(String.valueOf(params.get("num")));
        tradeProc.buy(code, price, num);
        return "买入下单成功";
    }

    public String sell(Map params) throws Exception {
        String code = (String) params.get("code");
        double price = Double.parseDouble(String.valueOf(params.get("price")));
        int num = Integer.parseInt(String.valueOf(params.get("num")));
        tradeProc.sell(code, price, num);
        return "卖出下单成功";
    }

    public String exchange(Map params) throws Exception {
        String buyCode = (String) params.get("buyCode");
        double buyPrice = Double.parseDouble(String.valueOf(params.get("buyPrice")));
        int buyNum = Integer.parseInt(String.valueOf(params.get("buyNum")));
        String sellCode = (String) params.get("sellCode");
        double sellPrice = Double.parseDouble(String.valueOf(params.get("sellPrice")));
        int sellNum = Integer.parseInt(String.valueOf(params.get("sellNum")));
        tradeProc.exchange(buyCode, buyPrice, buyNum, sellCode, sellPrice, sellNum);
        return "换仓下单成功";
    }
}
