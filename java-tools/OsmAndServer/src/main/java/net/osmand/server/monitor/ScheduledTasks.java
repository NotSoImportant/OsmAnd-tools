package net.osmand.server.monitor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import net.osmand.server.TelegramBotManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

	private static final Log LOG = LogFactory.getLog(TelegramBotManager.class);
	@Autowired
	TelegramBotManager telegram;
	private static final int SECOND = 1000;
	private static final int MINUTE = 60 * SECOND;
	private static final int HOUR = 60 * MINUTE;

	long previousOsmAndLiveDelay = 0;
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	
    @Scheduled(fixedRate = 10 * MINUTE)
    public void checkOsmAndLiveStatus() {
    	try {
			URL url = new URL("http://osmand.net/api/osmlive_status");
			InputStream is = url.openConnection().getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String osmlivetime = br.readLine();
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			Date dt = format.parse(osmlivetime);
			br.close();
			long currentDelay = System.currentTimeMillis() - dt.getTime();
			if(currentDelay - previousOsmAndLiveDelay > 30 * MINUTE && currentDelay > HOUR) {
				int roundUp = (int) (currentDelay / HOUR * 100);
				telegram.sendMonitoringAlertMessage("OsmAnd Live is delayed by " + (roundUp / 100.f) + " hours");
				previousOsmAndLiveDelay = currentDelay;
			}
		} catch (Exception e) {
			telegram.sendMonitoringAlertMessage("Exception while checking the server status.");
			LOG.error(e.getMessage(), e);
		}
    	
    }
}