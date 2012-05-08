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
import java.util.HashSet;
import java.util.Iterator;
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

import java.util.Date;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import timc.common.TIMConfigurator;
import timc.common.Utils.OperationMode;

import com.turn.ttorrent.bcodec.BEValue;
import com.turn.ttorrent.bcodec.InvalidBEncodingException;
import com.turn.ttorrent.client.AnnounceResponseListener;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.IncomingConnectionListener;
import com.turn.ttorrent.client.Piece;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.client.peer.PeerActivityListener;
import com.turn.ttorrent.client.peer.SharingPeer;
import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.Torrent;

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
	private byte[] crawlerPeerID;
	private SharedTorrent torrent;
	
	
	public StatsLogger(ConcurrentMap<String, SharingPeer> peers, ConcurrentMap<String, 
			SharingPeer> connected, SharedTorrent torrent, byte[] crawlerPeerID)
			throws IOException {
		this.torrent = torrent;
		this.peers = peers;
		this.connectedPeers = connected;
		this.crawlerPeerID = crawlerPeerID;
					
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
	public void handlePieceAvailability(SharingPeer peer, Piece piece) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleBitfieldAvailability(SharingPeer peer,
			BitSet availablePieces) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handlePieceSent(SharingPeer peer, Piece piece) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handlePieceCompleted(SharingPeer peer, Piece piece)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handlePeerDisconnected(SharingPeer peer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleIOException(SharingPeer peer, IOException ioe) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleAnnounceResponse(Map<String, BEValue> answer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	

}
