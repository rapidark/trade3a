package cn.aaa.trade.proc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import cn.aaa.trade.convert.IConvert;
import cn.aaa.trade.util.ProcessUtil;
import cn.aaa.trade.util.StringParser;

public class TdxTradeProcessor extends HxTradeProcessor {

    public List<Map<String, Object>> queryJiaoge(String startTime, String endTime) throws Exception {
        String resultStr = ProcessUtil.run(getCmdPath("queryJiaoge", startTime, endTime));
        String[] rows = resultStr.split("\n");
        if (rows.length <= 2) {
            return new ArrayList<Map<String, Object>>();
        }
        String[][] datas = parseJiaoyi(rows);

        List<Map<String, Object>> result = StringParser.find(datas, 
                new String[] { "成交编号", "成交日期", "业务名称", "证券代码", "证券名称", "成交价格", "成交数量", "清算金额", "佣金", "印花税", "股东代码" }, 
                new String[] { "ID", "DATE", "TYPE", "CODE", "NAME", "PRICE", "NUM", "VALUE", "YONGJIN", "YINHUA", "ZHANGHU" }, 
                new Object[] { null, null, new TradeTypeConvert(), null, null, double.class, int.class, double.class, double.class, double.class, null });
        return result;
    }

    private static class TradeTypeConvert implements IConvert {
        @Override
        public Object convert(Map<String, Object> row, String strVal) {
            if ("证券买入".equals(strVal)) {
                return "买入";
            }
            else if ("证券卖出".equals(strVal)) {
                return "卖出";
            }
            return strVal;
        }
    }

    private String[][] parseJiaoyi(String[] rows) {
        StringBuilder[] newRows = new StringBuilder[rows.length - 2];
        AtomicInteger[] indexes = new AtomicInteger[newRows.length];
        int maxSize = 0;
        int count = 0;
        do {
            List<Integer> curILst = new ArrayList<Integer>();
            count++;
            maxSize = 0;
            for (int i = 0, len = newRows.length; i < len; i++) {
                if (newRows[i] == null) {
                    newRows[i] = new StringBuilder();
                    indexes[i] = new AtomicInteger(0);
                }
                String row = rows[i + 2];
                int size = checkFiled(row, indexes[i], newRows[i]);
                if (size > maxSize) {
                    maxSize = size;
                }
                curILst.add(size);
            }
            for (int i = 0, len = indexes.length; i < len; i++) {
                indexes[i].addAndGet(maxSize - curILst.get(i) + 8);
            }
        }
        while (maxSize > 0);
        String[][] datas = new String[newRows.length][count - 1];
        for (int i = 0, len = datas.length; i < len; i++) {
            datas[i] = newRows[i].toString().trim().split("\t");
        }
        return datas;
    }

    private int checkFiled(String row, AtomicInteger index, StringBuilder target) {
        int size = 0;
        for (int jlen = row.length(); index.get() < jlen; index.incrementAndGet()) {
            char c = row.charAt(index.get());
            if (c == ' ') {
                target.append("\t");
                break;
            }
            target.append(c);
            if (c == 'Ｒ') {
                size += 2;
            } else if (Character.isDigit(c) || Character.isUpperCase(c) || Character.isLowerCase(c) || c == '.' || c == '-') {
                size++;
            } else {
                size += 2;
            }
        }
        return size;
    }

}
