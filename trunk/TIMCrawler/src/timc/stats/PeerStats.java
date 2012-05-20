package timc.stats;

import com.turn.ttorrent.client.peer.SharingPeer;
import com.turn.ttorrent.common.Peer;

import timc.stats.SessionRecord;;

public class PeerStats {
	
	private int currentSessionNum;
	private boolean isConnected;
	private SessionRecord currentSessionRecord;
	private SessionRecord zeroSessionRecord;
	private SharingPeer sharingPeer;
	private Peer basePeer;
	
	public int getCurrentSessionNum() {
		return currentSessionNum;
	}
	public void setCurrentSessionNum(int currentSessionNum) {
		this.currentSessionNum = currentSessionNum;
	}

	public boolean isConnected() {
		return isConnected;
	}
	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}
	public SessionRecord getCurrentSessionRecord() {
		return currentSessionRecord;
	}
	public void setCurrentSessionRecord(SessionRecord currentSessionRecord) {
		this.currentSessionRecord = currentSessionRecord;
	}
	public SessionRecord getZeroSessionRecord() {
		return zeroSessionRecord;
	}
	public void setZeroSessionRecord(SessionRecord zeroSessionRecord) {
		this.zeroSessionRecord = zeroSessionRecord;
	}
	public SharingPeer getSharingPeer() {
		return sharingPeer;
	}
	public void setSharingPeer(SharingPeer sharingPeer) {
		this.sharingPeer = sharingPeer;
	}
	public Peer getBasePeer() {
		return basePeer;
	}
	public void setBasePeer(Peer basePeer) {
		this.basePeer = basePeer;
	}

	

}
