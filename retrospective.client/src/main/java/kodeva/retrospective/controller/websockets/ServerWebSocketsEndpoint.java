package kodeva.retrospective.controller.websockets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import kodeva.retrospective.controller.Constants;
import kodeva.retrospective.messaging.Message;
import kodeva.retrospective.messaging.MessageBroker;

import org.glassfish.tyrus.server.Server;

@javax.websocket.server.ServerEndpoint(value = "/controller")
public class ServerWebSocketsEndpoint {
	private static final Logger LOGGER = Logger.getLogger(ServerWebSocketsEndpoint.class.getName());
	
	private static Server server;
	private static MessageBroker messageBroker;
	private static Collection<Session> sessions;

	@OnOpen
    public void onOpen(Session session) {
		sessions.add(session);
    }
 
    @OnMessage
    public void onMessage(String message, Session session) {
    	if (LOGGER.isLoggable(Level.INFO)) {
    		LOGGER.info(String.format("Received message from session '%s': '%s'", session.getId(), message));
    	}
    	messageBroker.sendMessage(new Message.Builder().string(message).sender(Constants.Messaging.SENDER).build());
    }
 
    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
    	sessions.remove(session);
    	//TODO: send notification message that session was terminated (to all existing session - to refresh participants list)
    }

    public static void send(Message message) {
    	if (server == null) {
    		return;
    	}
    	try {
    		final String messageStr = message.toString();
    		synchronized(sessions) {
    			Iterator<Session> sessionsIter = sessions.iterator();
    			while (sessionsIter.hasNext()) {
        			sessionsIter.next().getBasicRemote().sendText(messageStr);
    			}
    		}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }
    
    public static synchronized void start(MessageBroker messageBroker) {
    	if (server != null) {
    		return;
    	}
    	ServerWebSocketsEndpoint.messageBroker = messageBroker;
    	sessions = Collections.synchronizedCollection(new ArrayList<Session>());
	    server = new Server("localhost", 8025, "/retrospective", null, ServerWebSocketsEndpoint.class);
	    try {
	        server.start();
	    } catch (DeploymentException e) {
	        throw new RuntimeException(e);
	    }
    }
    
    public static void stop() {
    	if (server != null) {
    		server.stop();
    	}
    	server = null;
    	sessions = null;
    	messageBroker = null;
    }
}
