package test.jp.ac.titech.cs.de.hilogger;

import jp.ac.titech.cs.de.hilogger.HiLoggerConnector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class HiLoggerConnectorTest {

    @Test
    public void createInstance() {
        HiLoggerConnector hiloggerConnector =
                new HiLoggerConnector("config/hilogger.properties");
    }

    @Test
    public void startAndStop() {
        HiLoggerConnector hiloggerConnector =
                new HiLoggerConnector("config/hilogger.properties");

        hiloggerConnector.start();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        hiloggerConnector.stop();
    }
}
