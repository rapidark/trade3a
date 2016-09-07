package cn.aaa.trade.convert;

import java.util.Map;

public interface IConvert {
    public Object convert(Map<String, Object> row, String strVal);
}
