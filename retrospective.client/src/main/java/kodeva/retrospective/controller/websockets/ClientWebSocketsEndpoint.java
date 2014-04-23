package kodeva.retrospective.controller.websockets;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import kodeva.retrospective.messaging.Message;
import kodeva.retrospective.messaging.MessageBroker;

import org.glassfish.tyrus.client.ClientManager;

@javax.websocket.ClientEndpoint
public class ClientWebSocketsEndpoint {
	private final MessageBroker messageBroker;
	private final Session session;

	@OnOpen
    public void onOpen(Session session) {
    }
 
    @OnMessage
    public void onMessage(String message, Session session) {
    	messageBroker.sendMessage(new Message.Builder().string(message).build());
    }
 
    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
    	close();
    	//TODO: send notification message that session was terminated (to local view - to notify user)
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