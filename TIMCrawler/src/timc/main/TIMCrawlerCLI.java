package timc.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.xml.DOMConfigurator;
import org.naturalcli.Command;
import org.naturalcli.ExecutionException;
import org.naturalcli.ICommandExecutor;
import org.naturalcli.InvalidSyntaxException;
import org.naturalcli.NaturalCLI;
import org.naturalcli.ParseResult;
import org.naturalcli.commands.HelpCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.Client.ClientState;
import com.turn.ttorrent.client.SharedTorrent;

public class TIMCrawlerCLI {

	
	private static final Logger logger =
			LoggerFactory.getLogger(TIMCrawlerCLI.class);
	
	private Client client;
	private NaturalCLI cli;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		DOMConfigurator.configure(TIMCrawlerCLI.class.getResource("/log4j-cli.xml"));
		//DOMConfigurator.configure("config/log4j-cli.xml");

		if (args.length < 2) {
			System.err.println("usage: Client <torrent> <directory>");
			System.exit(1);
		}

		try {
			TIMCrawlerCLI tim = new TIMCrawlerCLI(args[0], args[1]);
			tim.startCLI();
		} catch (Exception e) {
			logger.error("Fatal error: {}", e.getMessage(), e);
			e.printStackTrace();
			System.exit(2);
		}
	}
	
	public TIMCrawlerCLI(String torrentName, String directory) 
			throws UnknownHostException, NoSuchAlgorithmException, IOException, InvalidSyntaxException {
		
		// Init the client
		this.client = new Client(
				InetAddress.getByName(System.getenv("HOSTNAME")),
				SharedTorrent.fromFile(
				new File(torrentName),
				new File(directory)));
		
		// Init the CLI
		Command stopClientCommand =
			    new Command("stop", "Stop the client", 
			    new ICommandExecutor () {
			       public void execute(ParseResult pr) {
			    	   System.out.print("Waiting for all threads to stop... ");
			    	   client.stop();
			    	   System.out.println("DONE");
			    	   System.exit(0);
			       }
			    }		
			  );
		
		Command clientInfoCommand =
			    new Command("info", "Print the client's info string", 
			    new ICommandExecutor () {
			       public void execute(ParseResult pr) {
			    	   System.out.println(client.infoStr());
			       }
			    }
			  );
		
		Command clientStatusCommand =
			    new Command("stat", "Print the client's status string", 
			    new ICommandExecutor () {
			       public void execute(ParseResult pr) {
			    	   System.out.println(client.statusStr());
			       }
			    }
			  );

		Set<Command> cs = new HashSet<Command>();
		cs.add(stopClientCommand);
		cs.add(clientInfoCommand);
		cs.add(clientStatusCommand);
		cs.add(new HelpCommand(cs));
		this.cli = new NaturalCLI(cs);		
	}
	
	public void startCLI() throws IOException {
		
		// Start the client
		this.client.share();
		if (ClientState.ERROR.equals(this.client.getState())) {
			System.exit(1);
		}
		
		// Start the CLI
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		do {
			System.out.print("> ");
			String command = br.readLine();
			try {
				this.cli.execute(command);
			} catch (ExecutionException e) {
				// ignore
			}
		} while (true);
	}
}