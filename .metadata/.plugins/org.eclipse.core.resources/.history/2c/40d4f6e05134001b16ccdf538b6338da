package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ChatServer {

	private static final String TAG = "ChatServer";
	private ServerSocket serverSocket;
	private Vector<ClientInfo> vc; // 연결된 클라이언트 클레스(소켓)를 담는 컬렉션
	
	public ChatServer( ) {
		try {
			vc = new Vector<>();
			serverSocket = new ServerSocket(10000);
			System.out.println(TAG + "클라이언트 연결 대기중...");
			
			//메인 스레드의 역할
			while(true) {			
			Socket socket = serverSocket.accept();// 클라이언트 연결 대기
			ClientInfo clientInfo = new ClientInfo(socket);
			System.out.println(TAG + "클라이언트 연결 완료...");
			clientInfo.start();
			vc.add(clientInfo);
			}
			
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	class ClientInfo extends Thread {
		Socket socket;
		BufferedReader reader;
		PrintWriter writer;// BufferedWriter와 다른 점은 내려쓰기 함수를 지원
		
		public ClientInfo(Socket socket) {
		this.socket = socket;
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream());
		} catch (Exception e) {
			System.out.println("서버 연결 실패 :"+e.getMessage());
		}
		}
		
		// 역할 : 클라이언트로 부터 받은 메시지를 모든 클라이언트에게 재전송 
		@Override
		public void run() {
			
		}
	}
	public static void main(String[] args) {
		new ChatServer();
	}
	
}
