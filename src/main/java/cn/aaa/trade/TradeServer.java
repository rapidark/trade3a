package cn.aaa.trade;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.aaa.trade.util.PropertiesUtil;

/**
 * 连通js与java的基于websocket的交易网关，TradeServer->TradeHandler->TradeWebSocket->TradeInvoker->AbsTradeProc
 */
public class TradeServer {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private int port;
    private Server server;

    public TradeServer() {
        this.port = PropertiesUtil.get("listened", 8888);
        this.server = new Server(port);
    }

    public void start() {
        TradeHandler tradeHandler = new TradeHandler();
        tradeHandler.getWebSocketFactory().setMaxIdleTime(1000 * 60 * 60 * 24 * 30);
        tradeHandler.getWebSocketFactory().setMaxTextMessageSize(1024 * 1024 * 1024);
        this.server.setHandler(tradeHandler);
        try {
            this.server.start();
            this.server.join();
            logger.info("trade proxy start, listen on port:" + this.port);
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void stop() {
        try {
            this.server.stop();
            logger.info("trade proxy stop.");
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        TradeServer server = new TradeServer();
        server.start();
    }
}
