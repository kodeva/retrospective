package kodeva.retrospective.controller.websockets;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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

import org.glassfish.tyrus.client.ClientManager;

@javax.websocket.ClientEndpoint
public class ClientWebSocketsEndpoint {
	private static final Logger LOGGER = Logger.getLogger(ClientWebSocketsEndpoint.class.getName());

	private final MessageBroker messageBroker;
	private final Session session;

	@OnOpen
    public void onOpen(Session session) {
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
    	close();
    }
    
    public ClientWebSocketsEndpoint(MessageBroker messageBroker) {
    	this.messageBroker = messageBroker;
    	final ClientManager client = ClientManager.createClient();
		try {
			session = client.connectToServer(this, new URI("ws://localhost:8025/retrospective/controller"));
		} catch (DeploymentException | URISyntaxException | IOException e) {
			throw new RuntimeException(e);
		}
	}
    
    public void send(Message message) {
    	try {
			session.getBasicRemote().sendText(message.toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }

    public void close() {
    	try {
			session.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }
}