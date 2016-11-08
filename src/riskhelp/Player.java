package riskhelp;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Player {
	
	private String username;
	private String phoneNumber;

	public Player(String playerUsername, String playerPhoneNumber) {
		username = playerUsername;
		phoneNumber = playerPhoneNumber;
	}
	
	//needed to separate this out because jsoup was conflicting with bson
	public static String getCurrentPlayer(String gameNumber) throws IOException{
		Document doc = Jsoup.connect("https://www.conquerclub.com/game.php?game=" + gameNumber).get();
		
		Element els = doc.getElementsByClass("status_green").first();
		
		if(els == null) {
			els = doc.getElementsByClass("status_yellow").first();
			if(els == null) {
				return null;
			}
		}
		
		String currentPlayer = els.text();
		
		currentPlayer = currentPlayer.substring(currentPlayer.indexOf(':') + 1, currentPlayer.length());
		
		return currentPlayer;
	}
}
