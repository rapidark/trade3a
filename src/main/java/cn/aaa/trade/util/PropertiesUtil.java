package cn.aaa.trade.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesUtil
{
    private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);
    private static Properties prop = new Properties();
    static
    {
        try
        {
            InputStream in = PropertiesUtil.class.getResourceAsStream("/config.properties");
            prop.load(in);
            in.close();
            String type = prop.getProperty("scriptType");
            in = PropertiesUtil.class.getResourceAsStream("/config-" + type + ".properties");
            prop.load(in);
            in.close();
        }
        catch (IOException e)
        {
            logger.error(e.getMessage(), e);
        }
    }

    public static String get(String key)
    {
        return prop.getProperty(key);
    }

    public static String get(String key, String def)
    {
        String value = prop.getProperty(key);
        if (value == null || value.trim().length() == 0)
        {
            return def;
        }
        return value;
    }

    public static int get(String key, int def)
    {
        String value = prop.getProperty(key);
        if (value == null || value.trim().length() == 0)
        {
            return def;
        }
        try
        {
            return Integer.parseInt(value);
        }
        catch (Exception e)
        {
            return def;
        }
    }
}
