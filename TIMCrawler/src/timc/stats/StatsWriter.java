package timc.stats;

import java.util.BitSet;
import java.util.Date;

import com.turn.ttorrent.common.Peer;

public interface StatsWriter {
	
	public Object writeTestStats(Date startTime, Date endTime, String infoHash, 
			int totalSize, int pieceSize, int numPieces);
	
	public void writeSessionStats(Object testId, Peer peer, Date startTime, Date lastSeen, BitSet initialBitfield);
	
	public boolean initWriter();
	
	public void closeWriter();

}
