package timc.statsLogger;

import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import timc.stats.StatsWriter;
import timc.stats.db.DBStatsWriter;
import timc.statsLogger.StatsLoggerReccord;

import com.turn.ttorrent.bcodec.BEValue;
import com.turn.ttorrent.client.Announce;
import com.turn.ttorrent.client.AnnounceResponseListener;
import com.turn.ttorrent.client.Piece;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.client.peer.PeerActivityListener;
import com.turn.ttorrent.client.peer.SharingPeer;

import java.util.Date;

public class StatsLogger implements Runnable, 
	AnnounceResponseListener, PeerActivityListener {
	
	// TODO: add getNumSeeders and getNumLeechers methods to SharedTorrent or Announce
	
	private static final Logger logger =
			LoggerFactory.getLogger(StatsLogger.class);
	
	private boolean stop; // TODO - need to enable stopping from Client
	private ConcurrentMap<String, SharingPeer> connectedPeers;  // same instance as Client's "connected"
	private ConcurrentMap<String, Map<Integer, StatsLoggerReccord>> sessionsMap;
	private String crawlerPeerID;
	private SharedTorrent torrent;
	private int sleepIntervalMiliSecs;
	private Announce announce;
	private Thread thread;
	private StatsWriter statsWriter;
	
	// peers and connected must not be null
	public StatsLogger(ConcurrentMap<String, SharingPeer> connected, SharedTorrent torrent, 
			Announce announce, String crawlerPeerID)
			throws IOException {
		this.torrent = torrent;
		this.connectedPeers = connected;
		this.crawlerPeerID = crawlerPeerID;
		this.thread = null;
		this.announce = announce;
		this.announce.register(this);
		this.statsWriter = new DBStatsWriter();
		this.statsWriter.initWriter();
		
		logger.info("StatsLogger [..{}] for {} started.",
				new Object[] {
					this.crawlerPeerID,
					this.torrent.getName()
				});					
	}
	
	public void start() {
		this.stop = false;
		if (this.thread == null || !this.thread.isAlive()) {
			this.thread = new Thread(this);
			this.thread.setName("bt-statsLogger");
			this.thread.start();
		}
	}
	
	public void stop() {
		this.stop = true;
	}

	@Override
	public void handlePeerChoked(SharingPeer peer) { /* ignore */ }

	@Override
	public void handlePeerReady(SharingPeer peer) { /* ignore */ }

	@Override
	public void handleBitfieldAvailability(SharingPeer peer, BitSet availablePieces) { /* ignore */ }

	@Override
	public void handlePeerDisconnected(SharingPeer peer) {
		// TODO - make sure that every time we disconnect from a peer this method is called, including all scenarios
		
		Map<Integer, StatsLoggerReccord> peerSessions = sessionsMap.get(peer.getHexPeerId());
				
		StatsLoggerReccord rec = peerSessions.get(peerSessions.size() - 1);
		
		updateRecordOnDisconnection(rec, peer);
		
		try {
			logStatsRecord(rec);
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}

	// log it to the writer
	private void logStatsRecord(StatsLoggerReccord rec) {
		// TODO Auto-generated method stub
		
	}

	private void updateRecordOnDisconnection(StatsLoggerReccord rec, SharingPeer peer) {
		// TODO - implement !!!!!!!!!
		
	}

	@Override
	public void handleIOException(SharingPeer peer, IOException ioe) {  /* ignore */ }

	@Override
	public void handleAnnounceResponse(Map<String, BEValue> answer) {
		/* TODO - update the zero record for all relevant peers !!!!!!!!!
		use similar method in Client
		for each peer do:
		first check if contains. if not - create new zero rec anyway, in case this peer changed ID
		Map<Integer, StatsLoggerReccord> peerSessions = sessionsMap.get(peer.hasPeerId() 
				? peer.getHexPeerId() : peer.getHostIdentifier());
				
		StatsLoggerReccord rec0 = peerSessions.get(0);
		
		updateZeroRecordOnDisconnection(rec0, peer);
		
		*/ 
		
	}

	@Override
	public void run() {
		logger.info("Starting statsLogger thread for " +
				torrent.getName() + " to " +
				torrent.getAnnounceUrl() +
				"with CrawlerID " + this.crawlerPeerID + "...");
		
		
		sessionsMap = new ConcurrentHashMap<String, Map<Integer, StatsLoggerReccord>>();
		
		int numSeedsInTorrent = getNumSeedsInTorrent();
		int numLeechInTorrent = getNumLeechInTorrent();
		
		for (SharingPeer peer : this.connectedPeers.values()) {
			// init zero record (belongs to info received from tracker)
			StatsLoggerReccord rec0 = new StatsLoggerReccord();
			initZeroRecord(rec0, peer);
			
			Map<Integer, StatsLoggerReccord> peerSessions = new HashMap<Integer, StatsLoggerReccord>();
			peerSessions.put(0, rec0);
						
			// init first record
			StatsLoggerReccord rec1 = createNewRecord(numSeedsInTorrent,
					numLeechInTorrent, peer);
			
			peerSessions.put(1, rec1);
			this.sessionsMap.put(peer.getHexPeerId(), peerSessions);		
		}
		
		this.sleepIntervalMiliSecs = 3;
		
		while (!this.stop) {		
			
			// TODO - update sessionsMap every interval?
						
			try {
				logger.trace("going to sleep for " + this.sleepIntervalMiliSecs +
					   	" seconds.");
				
				Thread.sleep(this.sleepIntervalMiliSecs * 1000);
			} catch (InterruptedException ie) {
				// Ignore
			}
		}
		
		// TODO  - consider flushing all data to Writer ??
		
	}

	private StatsLoggerReccord createNewRecord(int numSeedsInTorrent,
			int numLeechInTorrent, SharingPeer peer) {
		StatsLoggerReccord rec = new StatsLoggerReccord();
		rec.lastDownloadRate = peer.getDLRate();
		rec.completionRate = peer.getAvailablePieces().cardinality() / torrent.getPieceCount();
		rec.initialBitfield = peer.getAvailablePieces();
		rec.initialNumOfLeeches = numLeechInTorrent;
		rec.initialNumOfSeeds = numSeedsInTorrent;
		rec.crawlerPeerID = this.crawlerPeerID;
		rec.lastNumOfLeeches = numLeechInTorrent;
		rec.lastNumOfSeeds = numSeedsInTorrent;
		rec.lastBitfield = peer.getAvailablePieces();
		rec.peerID = peer.getPeerIdStr();
		rec.peerIP = peer.getIp();
		rec.peerPort = peer.getPort();
		rec.sessionSeqNum = 1;
		rec.startTime = new Date();
		rec.lastSeen = new Date();
		return rec;
	}

	private int getNumLeechInTorrent() {
		// TODO Auto-generated method stub
		// TODO - implement!!!!
		return 0;
	}

	private int getNumSeedsInTorrent() {
		// TODO Auto-generated method stub
		// TODO - implement!!!!
		return 0;
	}

	@Override
	public void handlePieceAvailability(SharingPeer peer, Piece piece) { /* ignore */ }

	@Override
	public void handlePieceSent(SharingPeer peer, Piece piece) { /* ignore */ }

	@Override
	public void handlePieceCompleted(SharingPeer peer, Piece piece)
			throws IOException { /* ignore */ }

	public void addNewConnectedPeer(SharingPeer peer) {
		int numSeedsInTorrent = getNumSeedsInTorrent();
		int numLeechInTorrent = getNumLeechInTorrent();
		
		if (sessionsMap.containsKey(peer.getHexPeerId()))
		{
			Map<Integer, StatsLoggerReccord> peerSessions = sessionsMap.get(peer.getHexPeerId());
			StatsLoggerReccord rec0 = peerSessions.get(0);
			updateZeroRecordOnConnection(rec0, peer);
		} 
		else
		{
			StatsLoggerReccord rec0 = new StatsLoggerReccord();
			initZeroRecord(rec0, peer);
			
			Map<Integer, StatsLoggerReccord> peerSessions = new HashMap<Integer, StatsLoggerReccord>();
			peerSessions.put(0, rec0);
						
			// init first record
			StatsLoggerReccord rec1 = createNewRecord(numSeedsInTorrent,
					numLeechInTorrent, peer);
			
			peerSessions.put(1, rec1);
			this.sessionsMap.put(peer.getHexPeerId(), peerSessions);
			
		}
		
	}

	private void initZeroRecord(StatsLoggerReccord rec0, SharingPeer peer) {
		rec0.lastSeenByTracker = new Date();
		rec0.sessionSeqNum = 0;
		rec0.peerID = peer.getPeerIdStr();
		rec0.peerIP = peer.getIp();
		rec0.peerPort = peer.getPort();
	}

	private void updateZeroRecordOnConnection(StatsLoggerReccord rec0,
			SharingPeer peer) {
		// TODO Auto-generated method stub
		// TODO - in general, dont confuse getPeerIdStr (for log records) 
		// and peer.getHexPeerId (which is for MAPs)
		
	}

	// consider if there should be a difference between this method and the one above
	private void updateZeroRecordOnDisconnection(StatsLoggerReccord rec0,
			SharingPeer peer) {
		// TODO Auto-generated method stub
		
	}	

}
