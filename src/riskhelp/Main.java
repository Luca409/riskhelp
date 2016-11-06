package riskhelp;

import org.jsoup.*;
import org.jsoup.nodes.*;
import java.io.*;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import java.util.ArrayList;
import java.util.List;


public class Main {	
	
	public static final String ACCOUNT_SID = "";
	public static final String AUTH_TOKEN = "";
	
	public static void main(String [] args) throws IOException{
		
		String gameNumber = "16913205"; //temporary variable; hard coded in game number

		//holds last player found, so when player changes, text is sent
		String lastPlayer = "";
		
		Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
		
		String[][] firstGame = {{"Luca409", "7326429205"}, 
								{"McSpirittedAway", "7325352403"}, 
								{"myeager", "7325818122"}, 
								{"Ptamborino", "9083072256"},
								{"ZachY8s", "7324398680"},
								{"tommykirk", "9736507640"},
								{"brettclarke", "7327708479"},
								{"TheRyanator17", ""},
								{"mmk150", ""},
								{"alangou", "6097440734"},
								{"cWizzle", "6165897822"},
								{"LHBeers", "2694052260"}
								};
		
		
		//infinite while loop to make the application run indefinitely
		while(true) {
			
			//run turn check every 5 minutes	
			try  {
				System.out.println("sleep for 60 seconds");
				Thread.sleep(1000 * 300);
			} catch (Exception e) {
			}
			
			String currentPlayer = getCurrentPlayer();
			
			System.out.println("Current player: " + currentPlayer);
			
			if(!currentPlayer.equals(lastPlayer)) {
				
				System.out.println("Current Player: " + currentPlayer + ", Last Player: " + lastPlayer);
				
				String number = "";
				for(int i = 0; i < firstGame.length; i++){
					if(currentPlayer.equals(firstGame[i][0])) {
						number = firstGame[i][1];
					}
				}
				
				if(!number.equals("")) {
					System.out.println("Hello " + currentPlayer + ", it's your turn in Game " + gameNumber + "!");;
					//sendSMS(currentPlayer, number, gameNumber);
				} else {
					System.out.println("No number saved for player. No text message sent.");
				}
				
				lastPlayer = currentPlayer;
			}
		}
	}
	
	public static String getCurrentPlayer() throws IOException{
		Document doc = Jsoup.connect("https://www.conquerclub.com/game.php?game=16913205").get();
		
		Element els = doc.getElementsByClass("status_green").first();
		
		if(els == null) {
			els = doc.getElementsByClass("status_yellow").first();
		}
		
		String currentPlayer = els.text();
		
		currentPlayer = currentPlayer.substring(currentPlayer.indexOf(':') + 1, currentPlayer.length());
		
		return currentPlayer;
	}
	
	public static void sendSMS(String playerName, String number,  String gameNumber) {
		Message message = Message.creator(new PhoneNumber("+1" + number),
		        new PhoneNumber("+17342355035"), "Hello " + playerName + ", it's your turn in Game " + gameNumber + "!").create();

		    System.out.println(message.getSid());
	}
	
}

