package cn.aaa.trade.proc;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.aaa.trade.proc.HxTradeProcessor.NumConvert;
import cn.aaa.trade.proc.HxTradeProcessor.TradeTypeConvert;
import cn.aaa.trade.util.PropertiesUtil;
import cn.aaa.trade.util.StringParser;
import cn.skypark.code.MyCheckCodeTool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.Base64;

/**
 * 华泰web接口
 */
public class HtWebTradeProcessor extends AbsTradeProc {
    private Logger logger = LoggerFactory.getLogger(HtWebTradeProcessor.class);
	private CloseableHttpClient Exchanger = HttpClientBuilder.create().build();
	private String[] szPres = new String[]{"sz", "00", "111", "112", "115", "123001", "128", "1318", "15", "16", "18", "200", "30", "39"};
    private String login_page = "https://service.htsc.com.cn/service/login.jsp";
    private String login_api = "https://service.htsc.com.cn/service/loginAction.do?method=login";
    private String trade_info_page = "https://service.htsc.com.cn/service/flashbusiness_new3.jsp?etfCode=";
    private String verify_code_api = "https://service.htsc.com.cn/service/pic/verifyCodeImage.jsp";
    private String prefix = "https://tradegw.htsc.com.cn/?";
    private String version = "1";
    private String userName = null;
    private String trdpwdEns = null;
    private String custid = null;
    private String op_entrust_way = "7";
    private String password = null;
    private String servicePwd = null;
    private String identity_type = "";
    private String sh_exchange_type = null;
    private String sh_stock_account = null;
    private String sz_exchange_type = null;
    private String sz_stock_account = null;
    private String fund_account = null;
    private String client_risklevel = null;
    private String op_station = null;
    private String trdpwd = null;
    private String uid = null;
    private String branch_no = null;
    private String op_branch_no = null;
    
    private Timer timer = null;

    public HtWebTradeProcessor() {
        userName = PropertiesUtil.get("username");
        servicePwd = PropertiesUtil.get("verify");
        trdpwd = PropertiesUtil.get("pass");
        custid = userName;
        fund_account = userName;
        trdpwdEns = trdpwd;
        password = trdpwd;
        login();
    }

	public void login() {
		HttpGet getLoginPage = new HttpGet(login_page);
		try {
            Exchanger.execute(getLoginPage); // 发起对登录页的GET请求
            List<NameValuePair> params = getLoginParam();
            HttpPost postLoginInfo = new HttpPost(login_api);
            postLoginInfo.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
            HttpResponse responseLogin = Exchanger.execute(postLoginInfo); // 向登录页传递账号信息
            HttpEntity responseEntity = responseLogin.getEntity();
            if (responseEntity != null) {
                String loginInfo = EntityUtils.toString(responseEntity, "utf-8");
                if (loginInfo.indexOf("欢迎您") != -1) {
                    logger.info("login success.");
                    getTradeInfo();
                    keepAlive();
                } else {
                    logger.error("login failed.");
                    JOptionPane.showMessageDialog(null, "登录失败！", "消息提示", JOptionPane.ERROR_MESSAGE);
                }
            }
		} catch (Exception e) {
		    logger.error(e.getMessage(), e);
		}
	}
	
    private List<NameValuePair> getLoginParam() {
        String ip = getIp(); // 得到ip地址
        String mac = getMac(); // 得到mac地址
        String verifycode = getVerifyCode(); // 得到验证码
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("loginEvent", "1")); // easytrader发送的头
        params.add(new BasicNameValuePair("topath", "null"));
        params.add(new BasicNameValuePair("accountType", "1"));
        params.add(new BasicNameValuePair("userType", "jy"));
        params.add(new BasicNameValuePair("userName", userName));
        params.add(new BasicNameValuePair("trdpwd", trdpwd));
        params.add(new BasicNameValuePair("trdpwdEns", trdpwdEns));
        params.add(new BasicNameValuePair("servicePwd", servicePwd));
        params.add(new BasicNameValuePair("macaddr", mac));
        params.add(new BasicNameValuePair("lipInfo", ip));
        params.add(new BasicNameValuePair("vcode", verifycode));
        return params;
    }
    
    private String getIp() { // 获取本机ip
        String sIP = null;
        try {
            InetAddress address = InetAddress.getLocalHost();
            sIP = address.getHostAddress();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return sIP;
    }

    private String getMac() { // 获取本机mac
        String sMAC = null;
        try {
            InetAddress address = InetAddress.getLocalHost();
            NetworkInterface ni = NetworkInterface.getByInetAddress(address);
            byte[] mac = ni.getHardwareAddress();
            Formatter formatter = new Formatter();
            for (int i = 0; i < mac.length; i++) {
                sMAC = formatter.format(Locale.getDefault(), "%02X%s", mac[i], (i < mac.length - 1) ? "-" : "").toString();
            }
            formatter.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return sMAC;
    }

	private String getVerifyCode() { // 通过调用getcode_jdk1.8.jar包获取验证码
		Random random = new Random();
		int r = random.nextInt();
		HttpGet getVerifyImage = new HttpGet(verify_code_api + "?" + r);
		String verifyCode = null;
		try {
			HttpResponse responseVerifyImage = Exchanger.execute(getVerifyImage); // 发起GET请求
			InputStream inputVerifyImage = responseVerifyImage.getEntity().getContent();
			FileOutputStream outputVerifyImage = new FileOutputStream("VerifyCode.jpg"); // 将GET到的验证码保存为图片
			byte[] data = new byte[1024];
			int len = 0;
			while ((len = inputVerifyImage.read(data)) != -1) {
				outputVerifyImage.write(data, 0, len);
			}
			outputVerifyImage.close();
			verifyCode = image2Code(); // 运行验证码识别包
		} catch (Exception e) {
		    logger.error(e.getMessage(), e);
		}
		return verifyCode;
	}

	private String image2Code() { // 识别验证码
		String result = null;
		String codeImage = "VerifyCode.jpg";
		String[] getCodeArg = new String[] { codeImage };
		try {
			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(bao);
			PrintStream so = System.out;
			System.setOut(ps);
			MyCheckCodeTool.main(getCodeArg); // 截取命令行返回结果
			result = bao.toString();
			System.setOut(so);
			result = result.substring(26, 30); // 仅保留验证码部分
		} catch (Exception e) {
		    logger.error(e.getMessage(), e);
		}
		return result;
	}

	public void getTradeInfo() { // 获取交易用的uid 和 password信息
		HttpGet getTradeInfo = new HttpGet(trade_info_page);
		String tradeInfo = null;
		try {
			HttpResponse responseTradeInfo = Exchanger.execute(getTradeInfo); // 发起对登录信息页的GET请求
			HttpEntity responseEntity = responseTradeInfo.getEntity();
			if (responseEntity != null) {
				tradeInfo = EntityUtils.toString(responseEntity, "utf-8");
			}
			String jsonBase64 = "	var data = \"([/=\\w\\+]+)\""; // 得到base64的正则表达式
			Pattern base64Pattern = Pattern.compile(jsonBase64);
			Matcher matchBase64 = base64Pattern.matcher(tradeInfo); // 匹配交易信息中的正则表达式
			matchBase64.find();
			String temp = matchBase64.toString();
			int TradeInfoJsonLength = temp.length(); // 输出匹配后的base64的交易信息长度
			String TradeInfoJsonBase64 = temp.substring(95, TradeInfoJsonLength - 2); // 使用偏移量移除匹配的var data = 字段
			byte[] tradeInfoJsonByte = Base64.decodeFast(TradeInfoJsonBase64); // 将base64格式解码
			String tradeInfoJson = null;
			try {
				tradeInfoJson = new String(tradeInfoJsonByte, "utf-8"); // 对解码后使用utf-8编码得到json格式交易信息
				logger.info("get trade info success.");
			} catch (UnsupportedEncodingException e) {
			    logger.error(e.getMessage(), e);
			}
			JSONObject jo = JSON.parseObject(tradeInfoJson);
			JSONArray ja = jo.getJSONArray("item");
			JSONObject shanghai = ja.getJSONObject(0); // 读取上海交易账号信息
			sh_exchange_type = shanghai.getString("exchange_type");
			sh_stock_account = shanghai.getString("stock_account");
			JSONObject shenzhen = ja.getJSONObject(1); // 读取深圳交易账号信息
			sz_exchange_type = shenzhen.getString("exchange_type");
			sz_stock_account = shenzhen.getString("stock_account");
			client_risklevel = jo.getString("branch_no");
			op_station = jo.getString("op_station");
			trdpwd = jo.getString("trdpwd");
			uid = jo.getString("uid");
			branch_no = jo.getString("branch_no");
			op_branch_no = jo.getString("branch_no");
		} catch (IOException e) {
		    logger.error(e.getMessage(), e);
		}
	}

	public String getPosition() { // 获取账户持仓状况
	    String position = send(getPositionParam().toString(), null);
	    logger.info("get position response:" + position);
		return position;
	}

    private static List<NameValuePair> getPositionParam() {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("cssweb_type", "GET_STOCK_POSITION"));
        params.add(new BasicNameValuePair("function_id", "403"));
        params.add(new BasicNameValuePair("exchange_type", ""));
        params.add(new BasicNameValuePair("stock_account", ""));
        params.add(new BasicNameValuePair("stock_code", ""));
        params.add(new BasicNameValuePair("query_direction", ""));
        params.add(new BasicNameValuePair("query_mode", "0"));
        params.add(new BasicNameValuePair("request_num", "100"));
        params.add(new BasicNameValuePair("position_str", ""));
        return params;
    }

	public String getBalance() { // 获取账户资金状况
	    String balance = send(getBalanceParam().toString(), null);
        logger.info("get blance response:" + balance);
        return balance;
	}

    private  List<NameValuePair> getBalanceParam() {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("cssweb_type", "GET_FUNDS"));
        params.add(new BasicNameValuePair("function_id", "405"));
        params.add(new BasicNameValuePair("identity_type", ""));
        params.add(new BasicNameValuePair("money_type", ""));
        return params;
    }

	public String getEntrust() { // 获取当日委托单
	    String entrust = send(getEntrustParam().toString(), null);
        logger.info("get entrust response:" + entrust);
        return entrust;
	}

    private static List<NameValuePair> getEntrustParam() {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("cssweb_type", "GET_CANCEL_LIST"));
        params.add(new BasicNameValuePair("function_id", "401"));
        params.add(new BasicNameValuePair("exchange_type", ""));
        params.add(new BasicNameValuePair("stock_account", ""));
        params.add(new BasicNameValuePair("stock_code", ""));
        params.add(new BasicNameValuePair("query_direction", ""));
        params.add(new BasicNameValuePair("sort_direction", "0"));
        params.add(new BasicNameValuePair("request_num", "100"));
        params.add(new BasicNameValuePair("position_str", ""));
        return params;
    }

	public String cancelEntrust(String entrustNo) { // 取消委托单
	    String cancelentrust = send(getCancelEntrustParam(entrustNo).toString(), null);
        logger.info("cancel entrust response:" + cancelentrust);
        return cancelentrust;
	}

    private List<NameValuePair> getCancelEntrustParam(String entrustNo) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("cssweb_type", "STOCK_CANCEL"));
        params.add(new BasicNameValuePair("function_id", "304"));
        params.add(new BasicNameValuePair("exchange_type", ""));
        params.add(new BasicNameValuePair("stock_code", ""));
        params.add(new BasicNameValuePair("entrust_bs", "2"));
        params.add(new BasicNameValuePair("batch_flag", "0"));
        params.add(new BasicNameValuePair("entrust_no", entrustNo));
        return params;
    }

	public void buy(String stockCode, double price, int amount) throws Exception { // 买
        int exchange_type = selectSHSZ(stockCode); // 选择上海深圳
        String stock_account = null;
        if (exchange_type == 1) {
            stock_account = sh_stock_account;
        } else {
            stock_account = sz_stock_account;
        }
        int entrust_prop = 0; // 委托类型，暂未实现，默认为限价委托
        String extendParam = "&" + "exchange_type=" + exchange_type 
                + "&" + "stock_account=" + stock_account 
                + "&" + "stock_code=" + stockCode 
                + "&" + "entrust_amount=" + amount 
                + "&" + "entrust_price=" + price 
                + "&" + "entrust_prop=" + entrust_prop;
        String buy = send(getBuyParam().toString(), extendParam);
        logger.info("buy response:" + buy);
        
        JSONObject jo = JSON.parseObject(buy);
        String cssweb_code = jo.getString("cssweb_code");
        if (!"success".equals(cssweb_code)) {
            throw new Exception(jo.getString("cssweb_msg"));
        }
	}

    private List<NameValuePair> getBuyParam() {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("cssweb_type", "STOCK_BUY"));
        params.add(new BasicNameValuePair("function_id", "302"));
        params.add(new BasicNameValuePair("query_direction", ""));
        params.add(new BasicNameValuePair("sort_direction", "0"));
        params.add(new BasicNameValuePair("request_num", "100"));
        params.add(new BasicNameValuePair("entrust_bs", "1"));
        return params;
    }

	public void sell(String stockCode, double price, int amount) throws Exception { // 卖
        int exchange_type = selectSHSZ(stockCode); // 选择上海深圳
        String stock_account = null;
        if (exchange_type == 1) {
            stock_account = sh_stock_account;
        } else {
            stock_account = sz_stock_account;
        }
        int entrust_prop = 0; // 委托类型，暂未实现，默认为限价委托
        String extendParam = "&" + "exchange_type=" + exchange_type 
                + "&" + "stock_account=" + stock_account 
                + "&" + "stock_code=" + stockCode 
                + "&" + "entrust_amount=" + amount 
                + "&" + "entrust_price=" + price 
                + "&" + "entrust_prop=" + entrust_prop;
        String sell = send(getSellParam().toString(), extendParam);
        logger.info("sell response:" + sell);
        
        JSONObject jo = JSON.parseObject(sell);
        String cssweb_code = jo.getString("cssweb_code");
        if (!"success".equals(cssweb_code)) {
            throw new Exception(jo.getString("cssweb_msg"));
        }
	}

    private List<NameValuePair> getSellParam() {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("cssweb_type", "STOCK_SELL"));
        params.add(new BasicNameValuePair("function_id", "302"));
        params.add(new BasicNameValuePair("query_direction", ""));
        params.add(new BasicNameValuePair("sort_direction", "0"));
        params.add(new BasicNameValuePair("request_num", "100"));
        params.add(new BasicNameValuePair("entrust_bs", "2"));
        return params;
    }
    
    public List<Map<String, Object>> queryJiaoge(String startTime, String endTime) throws Exception {
        SimpleDateFormat formatTo = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat formatFrom = new SimpleDateFormat("yyyy-MM-dd");
        String extendParam = "&" + "start_date=" + formatTo.format(formatFrom.parse(startTime)) 
                + "&" + "end_date=" + formatTo.format(formatFrom.parse(endTime));
        String jiaoge = send(getJiaogeParam().toString(), extendParam);
        logger.info("query jiaoge response:" + jiaoge);
        
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        JSONObject jo = JSON.parseObject(jiaoge);
        JSONArray ja = jo.getJSONArray("item");
        TradeTypeConvert typeC = new TradeTypeConvert();
        NumConvert numC = new NumConvert();
        for (Object item : ja.toArray()) {
            JSONObject itemjo = (JSONObject) item;
            if (itemjo.getString("entrust_no") == null) {
                continue;
            }
            Map<String, Object> row = new HashMap<String, Object>();
            row.put("ID", itemjo.getString("entrust_no"));
            row.put("DATE", itemjo.getString("date"));
            row.put("TYPE", typeC.convert(row, itemjo.getString("business_name")));
            row.put("CODE", itemjo.getString("stock_code"));
            row.put("NAME", itemjo.getString("stock_name"));
            row.put("PRICE", StringParser.convert(row, itemjo.getString("business_price"), double.class));
            row.put("NUM", StringParser.convert(row, itemjo.getString("occur_amount"), numC));
            row.put("VALUE", StringParser.convert(row, itemjo.getString("occur_balance"), double.class));
            row.put("YONGJIN", StringParser.convert(row, itemjo.getString("fare0"), double.class));
            row.put("YINHUA", StringParser.convert(row, itemjo.getString("fare1"), double.class));
            row.put("ZHANGHU", itemjo.getString("stock_account"));
            result.add(row);
        }
        logger.info("query jiaoge result:" + result);
        return result;
    }
    
    private List<NameValuePair> getJiaogeParam() {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("cssweb_type", "GET_EXCHANGEBILL"));
        params.add(new BasicNameValuePair("function_id", "308"));
        params.add(new BasicNameValuePair("request_num", "100"));
        params.add(new BasicNameValuePair("exchange_type", ""));
        params.add(new BasicNameValuePair("stock_code", ""));
        params.add(new BasicNameValuePair("deliver_type", "1"));
        params.add(new BasicNameValuePair("query_direction", ""));
        params.add(new BasicNameValuePair("stock_account", ""));
        params.add(new BasicNameValuePair("position_str", ""));
        return params;
    }

    private String send(String requestString, String extendParam) {
        Pattern dot = Pattern.compile(", ");
        Matcher matchDot = dot.matcher(requestString); // 把,_改成&并且去掉[]
        String temp = matchDot.replaceAll("&");
        temp = temp.substring(1, temp.length() - 1);
        String params = creatBasicParams() + temp + (extendParam == null ? "" : extendParam) + "&" + "ram=" + (new Random().nextInt());
        String url = prefix + org.apache.commons.codec.binary.Base64.encodeBase64String(params.getBytes());
        HttpGet request = new HttpGet(url);
        request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
        try {
            HttpResponse response = Exchanger.execute(request);
            String jsonBase64 = EntityUtils.toString(response.getEntity(), "gb2312");
            byte[] jsonByte = Base64.decodeFast(jsonBase64);
            String json = new String(jsonByte, "gb2312");
            return json;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

	private String creatBasicParams() { // 返回交易所需的基本参数
		String basicParams = "uid=" + uid 
		        + "&" + "version=" + version 
		        + "&" + "custid=" + custid 
		        + "&" + "op_branch_no=" + branch_no 
		        + "&" + "branch_no=" + branch_no 
		        + "&" + "op_entrust_way=" + op_entrust_way 
		        + "&" + "op_station=" + op_station 
		        + "&" + "fund_account=" + fund_account 
		        + "&" + "password=" + password 
		        + "&" + "identity_type=" + identity_type;
		return basicParams;
	}

	private int selectSHSZ(String stockCode) { // 识别股票代码首字符判断深沪市
	    for (String pre : szPres) {
	        if (stockCode.startsWith(pre)) {
	            return 2; // 2是深圳
	        }
	    }
	    return 1; // 1是上海
	}

	private void keepAlive() { // 每30秒尝试获取一次账户资金信息以保持会话
	    if (timer != null) {
	        timer.cancel();
	        timer = null;
	    }
		timer = new Timer();
		timer.schedule(new TimerTask() {
		    public void run() {
		        String balance = send(getBalanceParam().toString(), null);
		        logger.info("timer get blance response:" + balance);
		    }
		}, 30 * 1000, 30 * 1000);
		logger.info("timer start.");
	}
	
	public static void main(String[] args) throws Exception {
	    HtWebTradeProcessor processor = new HtWebTradeProcessor();
//	    processor.login();
//	    processor.getPosition();
//	    processor.getBalance();
//	    processor.getEntrust();
//      processor.sell("300182", 18, 1000);
//	    processor.buy("300182", 16, 1000);
//	    processor.cancelEntrust("96");
	    processor.queryJiaoge("2016-04-25", "2016-04-28");
	}
}