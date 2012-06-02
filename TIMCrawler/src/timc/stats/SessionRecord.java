package timc.stats;

import java.util.BitSet;
import java.util.Date;

public class SessionRecord {
	
	public String peerIdHex;
	public String peerIdStr;
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
	public boolean bitfieldReceived;
	public float lastDownloadRate;
	public float totalDownloadRate;
	// TODO consider how to track average DR. I think the value is average anyway
	// public Rate averageDownloadRate;
	public float completionRate;
	public int lastNumOfSeeds;
	public int lastNumOfLeeches;
	public Boolean isDisconnectedByCrawler;	
}
