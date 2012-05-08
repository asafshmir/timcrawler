package timc.statsLogger;

import java.util.Date;
import java.util.BitSet;

public class StatsLoggerReccord {
	
	public byte[] crawlerPeerID;
	public byte[] peerID;
	public String peerIP;
	public int peerPort;
	public int sessionSeqNum;  // 0 for tracker initiated record
	public Date startTime;
	public Date lastSeen;
	public Date lastSeenByTracker;
	public Object testId;
	public BitSet initialBitfield;
	public BitSet lastBitfield;
	public double lastDownloadRate;
	public double averageDownloadRate;
	public double completionRate;
	public int initialNumOfSeeds;
	public int lastNumOfSeeds;
	public int initialNumOfLeeches;
	public int lastNumOfLeeches;
	public Boolean isDisconnectedByCrawler;

}
