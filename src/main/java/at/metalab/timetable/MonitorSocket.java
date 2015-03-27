package at.metalab.timetable;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ClientEndpoint
@ServerEndpoint(value = "/monitors/")
public class MonitorSocket {

	private final static Set<Session> SESSIONS = Collections
			.synchronizedSet(new HashSet<Session>());

	private static volatile Monitor monitor;

	@OnOpen
	public void onWebSocketConnect(Session session) {
		System.out.println("Socket Connected: " + session);
		send(session, monitor.getJson());
		SESSIONS.add(session);
	}

	@OnMessage
	public void onWebSocketText(Session session, String message) {
		// not in use
	}

	@OnClose
	public void onWebSocketClose(Session session, CloseReason reason) {
		SESSIONS.remove(session);
		System.out.println("Socket Closed: " + reason);
	}

	@OnError
	public void onWebSocketError(Throwable cause) {
		cause.printStackTrace(System.err);
	}

	public static void setMonitor(Monitor monitor) {
		MonitorSocket.monitor = monitor;
		broadcast(monitor);
	}

	private static void send(Session session, String json) {
		try {
			session.getBasicRemote().sendText(json);
		} catch (IOException ioException) {
			// ignore
		}
	}

	private static void broadcast(Monitor monitor) {
		String json = monitor.getJson();

		for (Session session : SESSIONS) {
			send(session, json);
		}
	}
}