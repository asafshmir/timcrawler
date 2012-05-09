package timc.statsLogger;

import java.util.Date;
import java.util.BitSet;

import com.turn.ttorrent.client.peer.Rate;

public class StatsLoggerReccord {
	
	public String crawlerPeerID;
	public String peerID;
	public String peerIP;
	public int peerPort;
	public int sessionSeqNum;  // 0 for tracker initiated record
	public Date startTime;
	public Date lastSeen;
	public Date lastSeenByTracker;
	// TODO - consider why we need this
	// public Object testId;
	public BitSet initialBitfield;
	public BitSet lastBitfield;
	public Rate lastDownloadRate;
	// TODO consider how to track average DR. I think the value is average anyway
	// public Rate averageDownloadRate;
	public double completionRate;
	public int initialNumOfSeeds;
	public int lastNumOfSeeds;
	public int initialNumOfLeeches;
	public int lastNumOfLeeches;
	public Boolean isDisconnectedByCrawler;
	
	// TODO - add getters and setters

}
