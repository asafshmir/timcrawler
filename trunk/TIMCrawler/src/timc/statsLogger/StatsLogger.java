package timc.statsLogger;


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import timc.common.TIMConfigurator;
import timc.common.Utils.OperationMode;

import timc.statsLogger.StatsLoggerReccord;

import com.turn.ttorrent.bcodec.BEValue;
import com.turn.ttorrent.bcodec.InvalidBEncodingException;
import com.turn.ttorrent.client.AnnounceResponseListener;
import com.turn.ttorrent.client.IncomingConnectionListener;
import com.turn.ttorrent.client.Piece;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.client.peer.PeerActivityListener;
import com.turn.ttorrent.client.peer.SharingPeer;
import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.Torrent;

import java.util.Date;



import timc.common.TIMConfigurator;
import timc.common.Utils.OperationMode;



import com.turn.ttorrent.client.peer.SharingPeer;

public class StatsLogger implements Runnable, 
	AnnounceResponseListener, IncomingConnectionListener,
	PeerActivityListener {
	
	private static final Logger logger =
			LoggerFactory.getLogger(StatsLogger.class);
	
	private Thread thread;  // should I use?
	private boolean stop; // TODO - need to enable stopping from Client
	private ConcurrentMap<String, SharingPeer> peers; // same instance as Client's
	private ConcurrentMap<String, SharingPeer> connectedPeers;  // same instance as Client's "connected"
	private ConcurrentMap<String, Map<Integer, StatsLoggerReccord>> sessionsMap;
	private String crawlerPeerID;
	private SharedTorrent torrent;
	private int sleepIntervalMiliSecs;
	
	
	// peers and connected must not be null
	public StatsLogger(ConcurrentMap<String, SharingPeer> peers, ConcurrentMap<String, 
			SharingPeer> connected, SharedTorrent torrent, String crawlerPeerID)
			throws IOException {
		this.torrent = torrent;
		this.peers = peers;
		this.connectedPeers = connected;
		this.crawlerPeerID = crawlerPeerID;
		this.thread = null;

		
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
	public void handleNewPeerConnection(Socket s, byte[] peerId) {
		
		try {

		} catch (Exception e) {

		}
	}

	@Override
	public void handlePeerChoked(SharingPeer peer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handlePeerReady(SharingPeer peer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleBitfieldAvailability(SharingPeer peer,
			BitSet availablePieces) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handlePeerDisconnected(SharingPeer peer) {
		// TODO - make sure that every time we disconnect from a peer this method is called, including all scenarios
		StatsLoggerReccord rec = new StatsLoggerReccord();
		
		updateRecordOnDisconnection(rec, peer);
		
	}

	private void updateRecordOnDisconnection(StatsLoggerReccord rec, SharingPeer peer) {
		// TODO - implement !!!!!!!!!
		
	}

	@Override
	public void handleIOException(SharingPeer peer, IOException ioe) {  /* ignore */ }

	@Override
	public void handleAnnounceResponse(Map<String, BEValue> answer) {
		// TODO - update the zero record !!!!!!!!!
		
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
			rec0.lastSeenByTracker = new Date();
			rec0.sessionSeqNum = 0;
			
			Map<Integer, StatsLoggerReccord> peerSessions = new HashMap<Integer, StatsLoggerReccord>();
			peerSessions.put(0, rec0);
						
			// init first record
			StatsLoggerReccord rec1 = createNewRecord(numSeedsInTorrent,
					numLeechInTorrent, peer);
			
			peerSessions.put(1, rec1);
			this.sessionsMap.put(peer.hasPeerId() ? peer.getHexPeerId() : peer.getHostIdentifier()
					, peerSessions);		
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
		
		// TODO  -  flush all data to DB
		
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

}
