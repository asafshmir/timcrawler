package timc.stats;

import java.util.BitSet;
import java.util.Date;

import com.turn.ttorrent.common.Peer;

public interface StatsWriter {
	
	public void writeSessionStats(Peer peer, Date startTime, Date lastSeen, BitSet initialBitfield);
	
	public boolean initWriter();
	
	public void closeWriter();

}
