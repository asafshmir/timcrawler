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
	public float lastDLRate1;
	public float lastDLRate2;
	public float lastDLRate3;
	public float totalDownloadRate;
	// TODO consider how to track average DR. I think the value is average anyway
	// public Rate averageDownloadRate;
	public float completionRate;
	public int lastNumSeeders;
	public int lastNumLeechers;
	public Boolean isDisconnectedByCrawler;	
}
