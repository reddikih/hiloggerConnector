package jp.ac.titech.cs.de.hilogger;

public class Main {

	public static void main(String[] args) throws InterruptedException {
		HiLoggerConnector hlc = new HiLoggerConnector("config/hilogger.properties");
		
		hlc.start();
		
		Thread.sleep(1000 * 10);
		
		hlc.stop();
	}

}
