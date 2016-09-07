package cn.aaa.trade.util;

import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;

public class SerialUtil
{
    private static String datePattern = "yyyy-MM-dd HH:mm:ss";
    private static final SerializerFeature[] serializerFeatures = { SerializerFeature.WriteMapNullValue, //输出空置字段
        SerializerFeature.WriteNullListAsEmpty, //list字段如果为null，输出为[]，而不是null
        SerializerFeature.WriteNullNumberAsZero, //数值字段如果为null，输出为0，而不是null
        SerializerFeature.WriteNullBooleanAsFalse, //Boolean字段如果为null，输出为false，而不是null
        SerializerFeature.WriteNullStringAsEmpty //字符类型字段如果为null，输出为""，而不是null
        };
    private static final Feature[] features = { Feature.UseBigDecimal };
    private static final SerializeConfig config; //json字符串序列化配置
    static
    {
        config = new SerializeConfig();
        config.put(java.util.Date.class, new SimpleDateFormatSerializer(datePattern));
        config.put(java.sql.Date.class, new SimpleDateFormatSerializer(datePattern));
    }

    public static String serialize(Object src)
    {
        return JSON.toJSONString(src, config, serializerFeatures);
    }

    public static Map deserialize2map(String json)
    {
        return JSON.parseObject(json, Map.class, features);
    }

}
