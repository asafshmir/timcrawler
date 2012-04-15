package timc.stats;

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
	public void writeSessionStats(Peer peer, java.util.Date startTime, java.util.Date lastSeen,
			BitSet initialBitfield) {

		try {
			insertSessionRecord(peer.getPeerIdStr(), peer.getIp(), peer.getPort(), 
					new Timestamp(startTime.getTime()), new Timestamp(lastSeen.getTime()));
		} catch (SQLException e) {
			logger.error("Unable to insert to the DB: {}", e.getMessage());
		}
	}
	
	protected void insertSessionRecord(String peerId, String peerIp, int peerPort, Timestamp startTime, Timestamp lastSeen) throws SQLException {
		
		
		PreparedStatement stmt = null;
		String insertSessionSQL = 	"INSERT INTO `tim`.`sessions`" +
				"(`peer_id`, `peer_ip`, `peer_port`, `session_num`, `start_time`, `last_seen`, `fk_test_id`) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?);";

		int sessionNum = getNextSessionNum(peerId, peerIp, peerPort);
		
		try {   
			stmt = this.conn.prepareStatement(insertSessionSQL);
			stmt.setString(1, peerId);
	        stmt.setString(2, peerIp);
	        stmt.setInt(3, peerPort);
	        stmt.setInt(4, sessionNum);
	        stmt.setTimestamp(5, startTime);
	        stmt.setTimestamp(6, lastSeen);
	        stmt.setInt(7, 1); // TODO change this
	        stmt.executeUpdate();
	        
	    } catch (SQLException e) {
	    	logger.error("Unable to execute insert statement: {}", e.getMessage());
	    } finally {
	        if (stmt != null) { stmt.close(); }
	    }
	}
	
	protected int getNextSessionNum(String peerId, String peerIp, int peerPort) throws SQLException {
		
		int sessionNum = 0;
		PreparedStatement stmt = null;
		String selectSessionNumSQL = 	"SELECT MAX(session_num) FROM `tim`.`sessions`" +
										"WHERE `peer_id`=? and `peer_ip`=? and `peer_port`=?";
		// TODO add test ID to the primary key

		try {
			
			stmt = this.conn.prepareStatement(selectSessionNumSQL);
			stmt.setString(1, peerId);
	        stmt.setString(2, peerIp);
	        stmt.setInt(3, peerPort);
	        ResultSet rs = stmt.executeQuery();
	        while (rs.next()) {
	        	sessionNum = rs.getInt(1);
	        }
	    } catch (SQLException e) {
	    	logger.error("Unable to execute query statement: {}", e.getMessage());
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