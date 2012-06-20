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
	public BitSet initialBitfield;
	public BitSet lastBitfield;
	public boolean bitfieldReceived;
	public float lastDLRate1;
	public float lastDLRate2;
	public float lastDLRate3;
	public float totalDownloadRate;
	public float completionRate;
	public int lastNumSeeders;
	public int lastNumLeechers;
	public Boolean isDisconnectedByCrawler;	
}
