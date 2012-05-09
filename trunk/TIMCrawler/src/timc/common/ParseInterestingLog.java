package timc.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseInterestingLog {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String inName = args[0];
		String outName = inName + ".csv";
		
		File inFile = new File(inName);
		File outFile = new File(outName);
		if (!inFile.exists()) {
			System.out.println("oof");
		}
		if (!outFile.createNewFile()) {
			System.out.println("basa");
		}
		FileWriter fw = new FileWriter(outFile);
		
		BufferedReader br = new BufferedReader(new FileReader(inFile));
		
		Pattern p = Pattern.compile(" (peer://\\d+\\.\\d+\\.\\d+\\.\\d+:\\d+/([0-9]?[A-F]?){1,6}) ");
		
		String newline;
		String line;
		while ((line = br.readLine()) != null) {
			Matcher m = p.matcher(line);
			if (m.find()) {
				newline = m.group(1);
			} else {
				newline = "";
			}
			newline += "," + line;
			fw.write(newline + "\n");
		}
		
		fw.flush();
		fw.close();
		br.close();
	}
}
