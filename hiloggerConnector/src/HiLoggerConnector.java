

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

public class HiLoggerConnector {
	private final static Properties config = new Properties();
	
	private String hostname;
	private int port;
	private long measurementInterval;
	private long takeInterval;
	
	private Socket socket;
	private InputStream is;
	private InputStreamReader isr;
	private BufferedReader br;
	private BufferedInputStream bis;
	private OutputStream os;
	
	public HiLoggerConnector(String configfilePath) {
		try {
			config.load(new FileInputStream(configfilePath));
			
			this.hostname = config.getProperty("hilogger.info.hostname");
			this.port = Integer.parseInt(config.getProperty("hilogger.info.port"));
			this.measurementInterval = Long.parseLong(config.getProperty("hilogger.info.measurementInterval"));
			this.takeInterval = Long.parseLong(config.getProperty("hilogger.info.takeInterval"));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static void main(String[] args) {
		if(args.length < 1) {
			System.out.println("Usage: HiLoggerConnector <config file>");
			System.exit(1);
		}
		String configfilePath = args[0];
		
		HiLoggerConnector hlc = new HiLoggerConnector(configfilePath);
		
	}
	
	public void start() {
		startConnection();
	}
	
	private void startConnection() {
		try {
			socket = new Socket(hostname, port);
			socket.setSoTimeout((int) takeInterval + 1000);
			
			is = socket.getInputStream();
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			bis = new BufferedInputStream(is);

			// ソケットオープン時の応答処理
			is = socket.getInputStream();
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			while(is.available() == 0);
			char[] line = new char[is.available()];
			br.read(line);
			
			os = socket.getOutputStream();
			
			// データの取得間隔を設定
			if(measurementInterval == 10L) {
				command(Command.SAMP_10ms);
			}else if(measurementInterval == 50L) {
				command(Command.SAMP_50ms);
			}else {
				command(Command.SAMP_100ms);
			}
			
			// 測定開始
			command(Command.START);
			
			// 状態が変化するのを待つ
			Thread.sleep(1000);
			byte state = (byte) 0xff;
			while(state != 65){
				byte[] rec = command(Command.REQUIRE_STATE);
				state = getState(rec);
			}
			state = (byte) 0xff;
			
			// システムトリガー
			command(Command.SYSTRIGGER);
			Thread.sleep(1000);
			while(state != 35){
				byte[] rec = command(Command.REQUIRE_STATE);
				state = getState(rec);
			}
			state = (byte) 0xff;
			
			// データ要求
			// TODO Record Classを追加する
//			req = Command.REQUIRE_DATA;
//			command(req);
//			while(is.available() == 0);
//			raw = new byte[is.available()];
//			bis.read(raw);
			
			Thread.sleep(takeInterval);
			
//			req[20] = (byte) datanum;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private byte[] command(byte[] cmd){
		sendCommand(cmd);
		return getCommand();
	}
	
	// MemoryHiLoggerにコマンドを送信
	private void sendCommand(byte[] cmd) {
		try{
			os.write(cmd);
			os.flush();
		}catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	// MemoryHiLoggerからコマンドを受信
	private byte[] getCommand() {
		try{
			while(is.available() == 0);
			byte[] record = new byte[is.available()];
			is.read(record);
			return record;
		}catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}
	
	// MemoryHiLoggerの状態を取得 TODO Record Classを追加する
	private byte getState(byte[] rec) {
		if(rec[0] == 0x02 && rec[1] == 0x01) {
			switch(rec[2]) {
			case 0x50:	// スタートコマンド
			case 0x51:	// ストップコマンド
			case 0x57:	// 測定状態要求コマンド
			case 0x58:	// アプリシステムトリガコマンド
				return rec[5];
			}
		}
		return (byte) 0xff;	// 不明なコマンド
	}
	
}
