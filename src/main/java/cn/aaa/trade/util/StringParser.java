package cn.aaa.trade.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.aaa.trade.convert.IConvert;

public class StringParser {
    public static List<Map<String, Object>> parse(String content, String[] columns, String[] fields, Object[] classes) {
        String[][] datas = toArray(content);
        if (datas.length <= 1) {
            return new ArrayList<Map<String, Object>>();
        }
        List<Map<String, Object>> result = find(datas, columns, fields, classes);
        return result;
    }

    public static List<Map<String, Object>> find(String[][] datas, String[] columns, String[] fields, Object[] classes) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        int[] map = getMapIndex(datas[0], columns);
        for (int i = 1, len = datas.length; i < len; i++) {
            String[] rowData = datas[i];
            Map<String, Object> row = new HashMap<String, Object>();
            for (int j = 0, jlen = fields.length; j < jlen; j++) {
                String strValue = map[j] >= rowData.length ? "" : rowData[map[j]];
                Object value = convert(row, strValue, classes[j]);
                row.put(fields[j], value);
            }
            result.add(row);
        }
        return result;
    }

    public static Object convert(Map<String, Object> row, String strValue, Object cla) {
        if (cla == null || strValue == null || strValue.trim().length() == 0) {
            return strValue;
        }
        if (cla == int.class) {
            return Integer.parseInt(strValue);
        }
        else if (cla == double.class) {
            return Double.parseDouble(strValue);
        }
        else if (cla instanceof IConvert) {
            return ((IConvert) cla).convert(row, strValue);
        }
        return strValue;
    }

    private static int[] getMapIndex(String[] headers, String[] columns) {
        int[] map = new int[columns.length];
        for (int i = 0, len = columns.length; i < len; i++) {
            String column = columns[i];
            for (int j = 0, jlen = headers.length; j < jlen; j++) {
                if (column.equals(headers[j])) {
                    map[i] = j;
                    break;
                }
            }
        }
        return map;
    }

    public static String[][] toArray(String content) {
        String[] rows = content.split("\n");
        String[][] datas = new String[rows.length][];
        for (int i = 0, len = rows.length; i < len; i++) {
            datas[i] = rows[i].split("\t");
        }
        return datas;
    }
}
