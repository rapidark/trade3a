package cn.aaa.trade;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.aaa.trade.proc.AbsTradeProc;
import cn.aaa.trade.proc.HxTradeProcessor;
import cn.aaa.trade.util.PropertiesUtil;

public class TradeHandler extends WebSocketHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Object invoker = null;
    
    public TradeHandler() {
        String invoke = PropertiesUtil.get("invoker");//可通过配置文件定制invoker
        if (invoke == null || invoke.trim().length() == 0) {
            invoker = getDefaultInvoker();
        }
        else {
            try {
                invoker = Class.forName(invoke).newInstance();
            }
            catch (Exception e) {
                logger.error("无法创建指定的调用程序:" + invoke + ",将使用默认值");
                invoker = getDefaultInvoker();
            }
        }
    }

    @Override
    public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
        logger.debug("url=" + request.getRequestURL() + ",protocol=" + protocol);
        TradeWebSocket socket = new TradeWebSocket();
        socket.setInvoker(invoker);
        return socket;
    }

    private Object getDefaultInvoker() {
        TradeInvoker invoker = new TradeInvoker();
        AbsTradeProc tradeProc = null;
        String proc = PropertiesUtil.get("processor");//可通过配置文件定制processor
        if (proc == null || proc.trim().length() == 0) {
            tradeProc = new HxTradeProcessor();
        }
        else {
            try {
                tradeProc = (AbsTradeProc) Class.forName(proc).newInstance();
            }
            catch (Exception e) {
                logger.error("无法创建指定的处理程序:" + proc + ",将使用默认值");
                tradeProc = new HxTradeProcessor();
            }
        }
        invoker.setTradeProc(tradeProc);
        return invoker;
    }
}
