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

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.ascending;
import static java.util.Arrays.asList;
import com.mongodb.BasicDBObject;


public class Main {	

	public static final String ACCOUNT_SID = "";
	public static final String AUTH_TOKEN = "";	

	public static void main(String [] args) throws IOException{
		//infinite while loop to make the application run indefinitely
		while(true) {
			//run program every 5 minutes
			try  {
				System.out.println("sleep for 5 minutes");
				Thread.sleep(1000 * 300);
			} catch (Exception e) {
			}
			checkAllGames();
		}
	}

	private static void checkAllGames() throws IOException{
		// ***** begin creating arraylist of gameNumber *****
		MongoClient mongoClient = new MongoClient();
		MongoDatabase db = mongoClient.getDatabase("riskhelp");
		FindIterable<Document> iterable = db.getCollection("games").find(
				new Document("status", "active"));
		ArrayList<String> list = new ArrayList<String>();
		iterable.forEach(new Block<Document>() {
			@Override
			public void apply(final Document document) {
				list.add(document.getString("gameNumber"));
				System.out.println(document);
			}
		});
		// ***** end creating arraylist of gameNumber *******
		
		//if there are no active games, kill program
		if(list.size() == 0) {
			System.out.println("There are no active games. Program killed.");
			System.exit(1);
		}
		
		//check all the active game
		for(int i = 0; i < list.size(); ++i) {
			checkGame(list.get(i));
		}
	}

	/*
	 * Requires: gameNumber is a valid gameNumber which exists in the mongo database
	 *           game is active
	 * Effects: Checks to see if its a different player's turn in the game. If it is, change last
	 * 			player and send a text message. */
	private static void checkGame(String gameNumber) throws IOException{
		//get last player and currentplayer
		String lastPlayer = getLastPlayer(gameNumber);
		String currentPlayer = Player.getCurrentPlayer(gameNumber);
		
		//if no player is found, set game to inactive and end function
		if(currentPlayer == null) {
			System.out.println("This game is being set to inactive.");
			setGameInactive(gameNumber);
			return;
		}
		
		//print player for testing purposes
		System.out.println("Current player: " + currentPlayer);

		//send text message, or don't
		if(!currentPlayer.equals(lastPlayer)) {
			System.out.println("Current Player: " + currentPlayer + ", Last Player: " + lastPlayer);
			String number = getNumber(currentPlayer);
			if(!number.equals("")) {
				System.out.println("Hello " + currentPlayer + 
									", it's your turn in Game " + gameNumber + "!");;
				sendSMS(currentPlayer, number, gameNumber);
			} else {
				System.out.println("No number saved for player. No text message sent.");
			}
			//change last player in database
			setLastPlayer(gameNumber, currentPlayer);
		}
	}

	/* Requires: None
	 * Effects: Sends text message*/
	private static void sendSMS(String playerName, String number,  String gameNumber) {
		Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
		Message message = Message.creator(new PhoneNumber("+1" + number),
				new PhoneNumber("+17342355035"), "Hello " + playerName + 
				", it's your turn in Game " + gameNumber + "!").create();
		System.out.println(message.getSid());
	}

	/* Requires: username is a valid username which is stored in the Mongo database
	 * Effects: returns the phone number of username*/
	private static String getNumber(String username) {
		// ***** begin searching for number *****
		MongoClient mongoClient = new MongoClient();
		MongoDatabase db = mongoClient.getDatabase("riskhelp");
		FindIterable<Document> iterable = db.getCollection("phoneNumbers").find(
				new Document("username", username));
		ArrayList<String> list = new ArrayList<String>();
		iterable.forEach(new Block<Document>() {
			@Override
			public void apply(final Document document) {
				list.add(document.getString("phoneNumber"));
			}
		});
		// ***** end searching for number *****
		//if no player was found in database, just say there's no number
		if(list.size() == 0) {
			return "";
		}

		return list.get(0);
	}

	/* Requires: gameNumber is a valid game stored in the database 
	 * Effects: returns the lastPlayer stored in gameNumber*/
	private static String getLastPlayer(String gameNumber){
		// ***** begin searching for lastPlayer *****
		MongoClient mongoClient = new MongoClient();
		MongoDatabase db = mongoClient.getDatabase("riskhelp");
		FindIterable<Document> iterable = db.getCollection("games").find(
				new Document("gameNumber", gameNumber));
		ArrayList<String> list = new ArrayList<String>();
		iterable.forEach(new Block<Document>() {
			@Override
			public void apply(final Document document) {
				list.add(document.getString("lastPlayer"));
			}
		});
		// ***** end searching for lastPlayer *****

		return list.get(0);
	}

	/* Requires: None
	 * Effects: sets lastPlayer in database */
	private static void setLastPlayer(String gameNumber, String playerName) {
		MongoClient mongoClient = new MongoClient();
		MongoDatabase db = mongoClient.getDatabase("riskhelp");
		db.getCollection("games").updateOne(new Document("gameNumber", gameNumber),
				new Document("$set", new Document("lastPlayer", playerName)));
	}
	
	//sets game number to inactive when the game is over
	private static void setGameInactive(String gameNumber) {
		MongoClient mongoClient = new MongoClient();
		MongoDatabase db = mongoClient.getDatabase("riskhelp");
		db.getCollection("games").updateOne(new Document("gameNumber", gameNumber),
				new Document("$set", new Document("status", "inactive")));
	}
}
