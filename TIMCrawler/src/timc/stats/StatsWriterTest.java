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
		} catch (Exception x) {
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

		byte[] peerId = { '-', 'A', 'Z', '1', '0', '0', '3', '-' };
		Peer peer1 = new Peer("192.168.0.111", 6882, ByteBuffer.wrap(peerId));
		Peer peer2 = new Peer("192.168.0.222", 6885, ByteBuffer.wrap(peerId));

		String infoHash = "just_a_hash";
		Date lastSeen = new Date();

		TestRecord tr = new TestRecord();
		tr.mode = 0;
		tr.startTime = start;
		tr.infoHash = infoHash;
		tr.totalSize = 1024;
		tr.pieceSize = 512;
		tr.numPieces = 2;
		Object testId = sw.writeTestStats(tr);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		SessionRecord sr1 = new SessionRecord();
		sr1.peerIdStr = peer1.getPeerIdStr();
		sr1.peerIdHex = peer1.getHexPeerId();
		sr1.peerIP = peer1.getIp();
		sr1.peerPort = peer1.getPort();
		sr1.startTime = start;
		sr1.lastSeen = lastSeen;
		sr1.crawlerPeerID = "-UT2002-";

		SessionRecord sr2 = new SessionRecord();
		sr2.peerIdStr = peer2.getPeerIdStr();
		sr2.peerIdHex = peer2.getHexPeerId();
		sr2.peerIP = peer2.getIp();
		sr2.peerPort = peer2.getPort();
		sr2.startTime = start;
		sr2.lastSeen = lastSeen;
		sr2.crawlerPeerID = "-UT2002-";

		sw.writeSessionStats(testId, sr1);
		sw.writeSessionStats(testId, sr2);

		sr1.lastSeenByTracker = new Date();
		sw.writeTrackerSessionStats(testId, sr1);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		sr1.lastSeenByTracker = new Date();
		sw.writeTrackerSessionStats(testId, sr1);

		tr.endTime = new Date();
		sw.updateTestStats(testId, tr);
		sw.closeWriter();
	}
}
