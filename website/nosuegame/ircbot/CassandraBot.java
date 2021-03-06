package website.nosuegame.ircbot;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.PrivateMessageEvent;



public class CassandraBot extends ListenerAdapter {
	//Just to let you know, we changed these ages ago
	private static final String DB_USERNAME = "root";
	private static final String DB_PASSWORD = "12apples";
	private static final String DB_NAME     = "osuserver";
	
	@Override
	public void onPrivateMessage(PrivateMessageEvent event) {
		try{
			String url = "jdbc:mysql://localhost:3306/";
			Connection mysql = DriverManager.getConnection(url+DB_NAME, DB_USERNAME, DB_PASSWORD);
			
			System.out.println(event.getUser().getNick() + "(PM) : " + event.getMessage());
			String command = event.getMessage().toLowerCase();
			String[] message = command.split(" ");
			if(message[0].equals("whoami")){
				PreparedStatement query = mysql.prepareStatement(
						"SELECT * FROM users WHERE username = ?");
				
				query.setString(1, event.getUser().getNick());
				ResultSet result = query.executeQuery();
				
				if(result.last()){
					int playerID = result.getInt("ID");
					String playerName = result.getString("displayName");
					event.respond(String.format("You are [http://nosuegame.website/u/%d %s] on nosue!", playerID, playerName));
				}else{
					event.respond("Account not found on nosue!");
					event.respond("[http://nosuegame.website/register.php Click here to register]");
				}
			}else if(message[0].equals("activate")){
				PreparedStatement query = mysql.prepareStatement(
						"SELECT * FROM users WHERE username = ?");
				
				query.setString(1, event.getUser().getNick());
				ResultSet result = query.executeQuery();
				
				if(result.last()){
					if(result.getBoolean("accountActive")){
						event.respond("Your account is already activated");
					}else{
						PreparedStatement query2 = mysql.prepareStatement(
								"UPDATE users SET accountActive = 1 WHERE username = ?");
						
						query2.setString(1, event.getUser().getNick());
						if(query2.executeUpdate() != 0){
							event.respond("Your account has been activated, enjoy nosue!");
						}else{
							event.respond("An error has occoured, contact the nosue! admins");
						}
					}
				}else{
					event.respond("Account not found on nosue!");
					event.respond("[http://nosuegame.website/register.php Click here to register]");
				}
			}else if(message[0].equals("help")){
				event.respond("whoami - Get nosue! username");
				event.respond("activate - Activate nosue! account");
				event.respond("help - Returns this page");
			}else{
				event.respond("Command not found, type 'help' for help.");
			}
			mysql.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException, IrcException {
        Configuration configuration = new Configuration.Builder()
        	.setName("CassandraBot") //Set the nick of the bot.
        	.setServerHostname("irc.ppy.sh") //Join bancho
        	.setServerPassword("9569c696") //CassandraBot's IRC password
        	.addListener(new CassandraBot()) //Add our listener that will be called on Events
        	.buildConfiguration();
        
        PircBotX bot = new PircBotX(configuration);
        bot.startBot();
	}
}
