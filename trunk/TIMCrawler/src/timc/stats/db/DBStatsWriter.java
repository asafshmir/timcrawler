package timc.stats.db;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.BitSet;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import timc.common.TIMConfigurator;
import timc.stats.SessionRecord;
import timc.stats.StatsWriter;
import timc.stats.TestRecord;

public class DBStatsWriter implements StatsWriter {

	private static final Logger logger =
			LoggerFactory.getLogger(DBStatsWriter.class);
	
	protected boolean debug;
	protected Connection conn; 
	protected String userName;
	protected String password;
	protected String serverName;
	protected int portNumber;
	
	public DBStatsWriter() {
		this.debug = TIMConfigurator.getProperty("db_debug").equals("1");
		this.userName = TIMConfigurator.getProperty("db_username");
		this.password = TIMConfigurator.getProperty("db_password");
		this.serverName = TIMConfigurator.getProperty("db_server_name");
		this.portNumber = Integer.parseInt(TIMConfigurator.getProperty("db_server_port"));		
	}
	  
	@Override
	public String writeTestStats(TestRecord test) {
		
		if (this.debug)
			return null;
		
		int testId = 0;
		
		try {
			testId = insertTestRecord(test.crawlerPeerID, test.mode, test.modeSettings, new Timestamp(test.startTime.getTime()),
					test.infoHash, test.totalSize, test.pieceSize, test.numPieces, test.initialNumSeeders, test.initialNumLeechers);
		} catch (SQLException e) {
			logger.error("Unable to insert a record into 'tests' table: {}", e.getMessage());
		}
		
		return new Integer(testId).toString();
	}
	
	@Override
	public void updateTestStats(String testId, TestRecord test) {

		if (this.debug)
			return;

		int testIdIntVal = Integer.parseInt(testId);
		
		try {
			updateTestRecord(testIdIntVal, new Timestamp(test.endTime.getTime()), test.initialNumSeeders, test.initialNumLeechers);
		} catch (SQLException e) {
			logger.error("Unable to insert a record into 'sessions' table: {}", e.getMessage());
		}
	}
	
	@Override
	public void writeSessionStats(String testId, SessionRecord session) {
		
		if (this.debug)
			return;

		int testIdIntVal = Integer.parseInt(testId);
		String peerClientStr = session.peerIdStr.substring(0, 8);
		InputStream initialBitfieldIs = toInputStream(session.initialBitfield);
		InputStream lastlBitfieldIs = toInputStream(session.lastBitfield);
		
		try {
			insertSessionRecord(testIdIntVal, session.peerIdHex, session.peerIP, session.peerPort, 
					new Timestamp(session.startTime.getTime()), new Timestamp(session.lastSeen.getTime()),
					session.totalDownloadRate, session.lastDownloadRate, session.completionRate,
					peerClientStr, initialBitfieldIs, lastlBitfieldIs);
		} catch (SQLException e) {
			logger.error("Unable to insert a record into 'sessions' table: {}", e.getMessage());
		} finally {
			try {
				initialBitfieldIs.close();
				lastlBitfieldIs.close();
			} catch (IOException e) { /* ignore */ }
		}
	}
	
	@Override
	public void writeTrackerSessionStats(String testId, SessionRecord session) {

		if (this.debug)
			return;

		int testIdIntVal = Integer.parseInt(testId);
		
		try {
			insertTrackerSessionRecord(testIdIntVal, session.peerIdStr, session.peerIP, 
					session.peerPort, new Timestamp(session.lastSeenByTracker.getTime()));
		} catch (SQLException e) {
			logger.error("Unable to insert a record into 'sessions' table: {}", e.getMessage());
		}		
	}
	
	protected int insertTestRecord(String crawlerId, int mode, String modeSettings, Timestamp startTime, String infoHash,
			long totalSize, int pieceSize, int numPieces, int numSeeders, int numLeechers) throws SQLException {

		PreparedStatement stmt = null;
		String insertSessionSQL = "INSERT INTO `tim`.`tests` " +
				"(crawler_id, mode, mode_settings, start_time, info_hash, total_size, piece_size, num_pieces, num_seeders, num_leechers) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

		try {   
			stmt = this.conn.prepareStatement(insertSessionSQL);
			stmt.setString(1, crawlerId);
			stmt.setInt(2, mode);
			stmt.setString(3, modeSettings);
			stmt.setTimestamp(4, startTime);
	        stmt.setString(5, infoHash);
	        stmt.setLong(6, totalSize);
	        stmt.setInt(7, pieceSize);
	        stmt.setInt(8, numPieces);
	        stmt.setInt(9, numSeeders);
	        stmt.setInt(10, numLeechers);

	        stmt.executeUpdate();
	        
	    } catch (SQLException e) {
	    	logger.error("Unable to insert a record into 'tests' table: {}", e.getMessage());
	    } finally {
	        if (stmt != null) { stmt.close(); }
	    }
		
		// Get the id of the inserted record
		int testId = getLastTestId(infoHash);
		return testId;
	}
	
	protected void updateTestRecord(int testId, Timestamp endTime, int numSeeders, int numLeechers)
			throws SQLException {

		PreparedStatement stmt = null;
		String insertSessionSQL = "UPDATE `tim`.`tests` " +
				"SET end_time=?, num_seeders=?, num_leechers=? " +
				"WHERE id=?";

		try {   
			stmt = this.conn.prepareStatement(insertSessionSQL);
			stmt.setTimestamp(1, endTime);
	        stmt.setInt(2, numSeeders);
			stmt.setInt(3, numLeechers);
			
			stmt.setInt(4, testId);

	        stmt.executeUpdate();
	        
	    } catch (SQLException e) {
	    	logger.error("Unable to update a record on 'tests' table: {}", e.getMessage());
	    } finally {
	        if (stmt != null) { stmt.close(); }
	    }
	}
	
	protected int getLastTestId(String infoHash) throws SQLException {
		
		int testId = 0;
		PreparedStatement stmt = null;
		String selectSessionNumSQL = "SELECT MAX(id) FROM `tim`.`tests` WHERE info_hash=?";

		try {
			
			stmt = this.conn.prepareStatement(selectSessionNumSQL);
			stmt.setString(1, infoHash);

	        ResultSet rs = stmt.executeQuery();
	        while (rs.next()) {
	        	testId = rs.getInt(1);
	        }
	    } catch (SQLException e) {
	    	logger.error("Unable to execute query on 'tests' table: {}", e.getMessage());
	    } finally {
	        if (stmt != null) { stmt.close(); }
	    }
		
		return testId;
	}
	
	protected void insertSessionRecord(int testId, String hexPeerId, String peerIp, int peerPort, 
										Timestamp startTime, Timestamp lastSeen, float totalDLRate, float lastDLRate,
										float completionRate, String peerClientStr, InputStream initialBitfield, InputStream lastBitfield) throws SQLException {
		
		PreparedStatement stmt = null;
		String insertSessionSQL = "INSERT INTO `tim`.`sessions` " +
				"(fk_test_id, peer_id, peer_ip, peer_port, session_num, start_time, last_seen, " +
				"total_download_rate, last_download_rate, completion_rate, peer_client, " +
				"initial_bitfield, last_bitfield) " +
				"VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?);";

		int sessionNum = getNextSessionNum(hexPeerId, peerIp, peerPort, testId);
		
		try {   
			stmt = this.conn.prepareStatement(insertSessionSQL);
			stmt.setInt(1, testId);
			stmt.setString(2, hexPeerId);
			stmt.setString(3, peerIp);
	        stmt.setInt(4, peerPort);
	        stmt.setInt(5, sessionNum);
	        stmt.setTimestamp(6, startTime);
	        stmt.setTimestamp(7, lastSeen);
	        stmt.setFloat(8, totalDLRate);
	        stmt.setFloat(9, lastDLRate);
	        stmt.setFloat(10, completionRate);
	        stmt.setString(11, peerClientStr);
	        stmt.setBlob(12, initialBitfield);
	        stmt.setBlob(13, lastBitfield);
	        
	        stmt.executeUpdate();
	        
	    } catch (SQLException e) {
	    	logger.error("Unable to insert a record into 'sessions' table: {}", e.getMessage());
	    } finally {
	        if (stmt != null) { stmt.close(); }
	    }
	}
	
	protected void insertTrackerSessionRecord(int testId, String peerId, String peerIp, int peerPort, Timestamp lastSeenByTracker) throws SQLException {
		
		PreparedStatement stmt = null;
		String insertSessionSQL = "INSERT INTO `tim`.`sessions` " +
				"(peer_ip, peer_port, peer_id, session_num, last_seen_by_tracker, fk_test_id) " +
				"VALUES (?, ?, ?, ?, ?, ?) " +
				"ON DUPLICATE KEY UPDATE `last_seen_by_tracker` = ?;";

		int sessionNum = 0;
		
		try {   
			stmt = this.conn.prepareStatement(insertSessionSQL);
			stmt.setString(1, peerIp);
	        stmt.setInt(2, peerPort);
	        stmt.setString(3, peerId);
	        stmt.setInt(4, sessionNum);
	        stmt.setTimestamp(5, lastSeenByTracker);
	        stmt.setInt(6, testId);
	        stmt.setTimestamp(7, lastSeenByTracker);
	        stmt.executeUpdate();
	        
	    } catch (SQLException e) {
	    	logger.error("Unable to insert a record into 'sessions' table: {}", e.getMessage());
	    } finally {
	        if (stmt != null) { stmt.close(); }
	    }
	}
	
	protected int getNextSessionNum(String peerId, String peerIp, int peerPort, int testId) throws SQLException {
		
		int sessionNum = 0;
		PreparedStatement stmt = null;
		String selectSessionNumSQL = "SELECT MAX(session_num) FROM `tim`.`sessions` " +
									"WHERE peer_ip=? and peer_port=? and peer_id=? and fk_test_id=?";
		
		try {
			
			stmt = this.conn.prepareStatement(selectSessionNumSQL);
			stmt.setString(1, peerIp);
	        stmt.setInt(2, peerPort);
	        stmt.setString(3, peerId);
	        stmt.setInt(4, testId);
	        ResultSet rs = stmt.executeQuery();
	        while (rs.next()) {
	        	sessionNum = rs.getInt(1);
	        }
	    } catch (SQLException e) {
	    	logger.error("Unable to execute query on 'sessions' table: {}", e.getMessage());
	    } finally {
	        if (stmt != null) { stmt.close(); }
	    }
		
		return sessionNum + 1;
	}
	
	protected Connection getConnection() throws SQLException {
		Connection conn = null;
	    Properties connectionProps = new Properties();
	    connectionProps.put("user", this.userName);
	    connectionProps.put("password", this.password);

	    conn = DriverManager.getConnection(
	                   "jdbc:mysql://" + this.serverName + ":" + this.portNumber + "/", connectionProps);
	    return conn;
	}

	@Override
	public boolean initWriter() {
		if (this.debug) {
			logger.debug("db_debug mode - not performing any DB operations");
			return true;
		}
		
		try {
			this.conn = getConnection();
			logger.debug("Connected to database");
		} catch (SQLException e) {
			logger.error("Unable to connect to the DB: {}", e.getMessage());
			return false;
		}
		return true;
	}

	@Override
	public void closeWriter() {
		if (this.debug)
			return;
		
		try {
			
			this.conn.close();
			logger.debug("Disconnected from database");
		} catch (SQLException e) {
			logger.error("Unable to close DB connection: {}", e.getMessage());
		}
	}
	
	public static InputStream toInputStream(BitSet bits) {
		byte[] bytes = new byte[(bits.length()+7)/8 ];
		for (int i = 0; i < bits.length(); i++) {
			if (bits.get(i)) {
				bytes[i/8] |= 1<<(i%8);
			}
		}
		return new ByteArrayInputStream(bytes);
	}
}