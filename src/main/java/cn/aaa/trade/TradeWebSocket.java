package cn.aaa.trade;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.aaa.trade.util.InvokeUtil;
import cn.aaa.trade.util.SerialUtil;

public class TradeWebSocket implements WebSocket.OnTextMessage {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Connection connection;
    private Object invoker;

    public Object getInvoker() {
        return invoker;
    }

    public void setInvoker(Object invoker) {
        this.invoker = invoker;
    }

    @Override
    public void onOpen(Connection connection) {
        logger.debug("onOpen");
        this.connection = connection;

        Map<String, String> map = new HashMap<String, String>();
        map.put("type", "connect");
        send(SerialUtil.serialize(map));
    }

    @Override
    public void onClose(int closeCode, String message) {
        logger.debug("onClose");
        this.connection = null;
    }

    @Override
    public void onMessage(String data) {
        logger.info("onMessage:" + data);
        if (invoker == null) {
            sendErr(null, "未配置调用程序");
            return;
        }
        Map msg = SerialUtil.deserialize2map(data);
        final String id = String.valueOf(msg.get("id"));
        if (id == null) {
            sendErr(id, "非法请求");
            return;
        }
        final String cmd = String.valueOf(msg.get("cmd"));
        if (cmd == null) {
            sendErr(id, "无命令参数");
            return;
        }
        
        Object p = msg.get("params");
        if (p != null) {
            p = SerialUtil.deserialize2map(String.valueOf(p));
        }
        final Map params = (Map) p;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Object obj = InvokeUtil.invoke(invoker, cmd, params);
                    sendSuc(id, obj);
                }
                catch (NoSuchMethodException e) {
                    sendErr(id, "无效命令:" + cmd);
                    return;
                }
                catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    sendErr(id, "调用异常:" + cmd + ",异常信息:" + e.getMessage());
                }
                catch (Exception e) {
                    Throwable t = e.getCause();
                    logger.error(e.getMessage(), e);
                    sendErr(id, "执行失败:" + (t != null ? t : e).getMessage());
                    return;
                }
            }
        }).start();
    }

    private void sendSuc(String id, Object msg) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", id);
        map.put("type", "trade");
        map.put("result", true);
        map.put("msg", msg);
        send(SerialUtil.serialize(map));
    }

    private void sendErr(String id, String msg) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", id);
        map.put("type", "trade");
        map.put("result", false);
        map.put("msg", msg);
        send(SerialUtil.serialize(map));
    }

    public void send(String msg) {
        if (this.connection == null) {
            logger.warn("client has not connect.");
            return;
        }
        try {
            logger.info("send:" + msg);
            this.connection.sendMessage(msg);
        }
        catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
