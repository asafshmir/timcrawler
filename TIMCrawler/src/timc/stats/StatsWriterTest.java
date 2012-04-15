package timc.stats;

import java.nio.ByteBuffer;
import java.util.Date;

import org.apache.log4j.xml.DOMConfigurator;

import com.turn.ttorrent.common.Peer;

public class StatsWriterTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		DOMConfigurator.configure("config/log4j.xml");
		
		try {
			String driver = "com.mysql.jdbc.Driver";
			Class.forName(driver).newInstance();
		}
		catch(Exception x){
			System.out.println("Unable to load the driver class!");
		}
		
		
		StatsWriter sw = new DBStatsWriter();
		
		sw.initWriter();
		
		Date start = new Date();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		byte[] id = {'-','A','Z','1','0','0','3','-'};
		Peer peer = new Peer("192.168.0.104", 6882, ByteBuffer.wrap(id));
		
		sw.writeSessionStats(peer, start, new Date(), null);
		
		sw.closeWriter();

	}

}
