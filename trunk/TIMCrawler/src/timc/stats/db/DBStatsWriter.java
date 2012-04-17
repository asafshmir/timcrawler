package timc.stats.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.BitSet;
import java.util.Date;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import timc.stats.StatsWriter;

import com.turn.ttorrent.common.Peer;

public class DBStatsWriter implements StatsWriter {

	private static final Logger logger =
			LoggerFactory.getLogger(DBStatsWriter.class);
	
	protected Connection conn;
	protected String dbName; 
	protected String userName;
	protected String password;
	protected String serverName;
	protected int portNumber;
	
	public DBStatsWriter() {
		// TODO configure from properties?
		this.userName = "root";
		this.password = "root";
		this.serverName = "localhost";
		this.portNumber = 3306;
		this.dbName = "tim";		
	}
	  
	@Override
	public Object writeTestStats(Date startTime, Date endTime, String infoHash,
			int totalSize, int pieceSize, int numPieces) {
		
		int testId = 0;		
		
		try {
			testId = insertTestRecord(new Timestamp(startTime.getTime()), new Timestamp(endTime.getTime()),
					infoHash, totalSize, pieceSize, numPieces);
		} catch (SQLException e) {
			logger.error("Unable to insert a record into 'tests' table: {}", e.getMessage());
		}
		
		return new Integer(testId);
	}
	
	@Override
	public void writeSessionStats(Object testId, Peer peer, java.util.Date startTime, java.util.Date lastSeen,
			BitSet initialBitfield) {

		if (!(testId instanceof Integer)) return;
		int testIdIntVal = ((Integer)testId).intValue();
		
		try {
			insertSessionRecord(testIdIntVal, peer.getPeerIdStr(), peer.getIp(), peer.getPort(), 
					new Timestamp(startTime.getTime()), new Timestamp(lastSeen.getTime()));
		} catch (SQLException e) {
			logger.error("Unable to insert a record into 'sessions' table: {}", e.getMessage());
		}
	}
	
	protected int insertTestRecord(Timestamp startTime, Timestamp endTime, String infoHash,
			int totalSize, int pieceSize, int numPieces) throws SQLException {

		PreparedStatement stmt = null;
		String insertSessionSQL = 	"INSERT INTO `tim`.`tests`" +
				"(`start_time`, `end_time`, `info_hash`, `total_size`, `piece_size`, `num_pieces`) " +
				"VALUES (?, ?, ?, ?, ?, ?);";

		try {   
			stmt = this.conn.prepareStatement(insertSessionSQL);
	        stmt.setTimestamp(1, startTime);
	        stmt.setTimestamp(2, endTime);
	        stmt.setString(3, infoHash);
	        stmt.setInt(4, totalSize);
	        stmt.setInt(5, pieceSize);
	        stmt.setInt(6, numPieces);

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
	
	protected int getLastTestId(String infoHash) throws SQLException {
		
		int testId = 0;
		PreparedStatement stmt = null;
		String selectSessionNumSQL = 	"SELECT MAX(id) FROM `tim`.`tests` WHERE `info_hash`=?";

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
	
	protected void insertSessionRecord(int testId, String peerId, String peerIp, int peerPort, Timestamp startTime, Timestamp lastSeen) throws SQLException {
		
		PreparedStatement stmt = null;
		String insertSessionSQL = 	"INSERT INTO `tim`.`sessions`" +
				"(`peer_ip`, `peer_port`, `peer_id`, `session_num`, `start_time`, `last_seen`, `fk_test_id`) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?);";

		int sessionNum = getNextSessionNum(peerId, peerIp, peerPort, testId);
		
		try {   
			stmt = this.conn.prepareStatement(insertSessionSQL);
	        stmt.setString(1, peerIp);
	        stmt.setInt(2, peerPort);
	        stmt.setString(3, peerId);
	        stmt.setInt(4, sessionNum);
	        stmt.setTimestamp(5, startTime);
	        stmt.setTimestamp(6, lastSeen);
	        stmt.setInt(7, testId);
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
		String selectSessionNumSQL = 	"SELECT MAX(session_num) FROM `tim`.`sessions`" +
										"WHERE `peer_ip`=? and `peer_port`=? and `peer_id`=? and `fk_test_id`=?";
		
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
	    
	    logger.debug("Connected to database");
	    return conn;
	}

	@Override
	public boolean initWriter() {
		try {
			this.conn = getConnection();
		} catch (SQLException e) {
			logger.error("Unable to connect to the DB: {}", e.getMessage());
			return false;
		}
		return true;
	}

	@Override
	public void closeWriter() {
		try {
			this.conn.close();
			logger.debug("Disconnected from database");
		} catch (SQLException e) {
			logger.error("Unable to close DB connection: {}", e.getMessage());
		}
	}
}