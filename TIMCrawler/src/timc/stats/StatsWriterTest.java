package timc.stats;

import java.nio.ByteBuffer;
import java.util.Date;

import org.apache.log4j.xml.DOMConfigurator;

import timc.stats.db.DBStatsWriter;

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
			e.printStackTrace();
		}
		
		byte[] peerId = {'-','A','Z','1','0','0','3','-'};
		Peer peer1 = new Peer("192.168.0.111", 6882, ByteBuffer.wrap(peerId));
		Peer peer2 = new Peer("192.168.0.222", 6885, ByteBuffer.wrap(peerId));
		Peer peer3 = new Peer("192.168.0.333", 6886, ByteBuffer.wrap(peerId));
		String infoHash = "just_a_hash";
		Date lastSeen = new Date();
		
		
		Object id = sw.writeTestStats(start, new Date(), infoHash, 1024, 512, 2);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		sw.writeSessionStats(id, peer1, start, lastSeen, null);
		sw.writeSessionStats(id, peer2, start, lastSeen, null);
		sw.writeSessionStats(id, peer3, start, lastSeen, null);
		
		sw.closeWriter();

	}

}
