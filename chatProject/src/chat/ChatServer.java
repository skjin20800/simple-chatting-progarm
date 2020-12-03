package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import protocol.Chat;

public class ChatServer {
//전체흐름
	//1. 클라이언트로부터 메세지를 읽어온다
	//2. ID와 ALL프로토콜을 판별한다 [프로토콜 사용]
	//3. ID이면 ID저장
	//4. ALL이면 메세지 저저장[프로토콜 사용]
	//5. ALL의 메세지를 각 클라이언트 별로 구분
	//6. 각 클라이언트는 개인ID를 가지고 각 클라이언트에게 해당하는 메세지 전송
	
	//내부로직
	//1. socket으로 서버연결
	//2.메인 쓰레드는 클라이언트 연결 담당
	//3. ClientInfo 쓰레드는 각 클라이언트의 메세지를 받는다
	//3-1. 쓰레드는 받은 메세지를 구분한다
	//3-2. 쓰레드는 각 클라이언트에게 알맞는 정보를 넘긴다
	//3-3 쓰레드는 각 클라이언트의 모든 정보를 담는다
	//4 Vectoer에 각 클라이언트의 모든정보를 저장한다
	
	
	private static final String TAG = "ChatServer";
	private ServerSocket serverSocket;
	private Vector<ClientInfo> vc; // 연결된 클라이언트 클레스(소켓)를 담는 컬렉션
	
	public ChatServer( ) {
		try {
			serverSocket = new ServerSocket(10001);
			vc = new Vector<>();
			
			//메인 스레드의 역할
			while(true) {
				System.out.println(TAG + "클라이언트 연결 대기중...");
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
		private String id;

		
		public ClientInfo(Socket socket) {
		this.socket = socket;

		}
		
		// 역할 : 클라이언트로 부터 받은 메시지를 모든 클라이언트에게 재전송 
		@Override
		public void run() {
			try {
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				writer = new PrintWriter(socket.getOutputStream(), true);
				
				writer.println("사용하실 아이디를 입력하세요.");
				String input = null;
				while((input = reader.readLine())!=null) { //클라이언트로부터 읽어온값 input에 저장
				routing(input); //routing메소드로 input값을 파싱하여 각 클라이언트들에게 메세지 전달
				}
			} catch (Exception e) {
				System.out.println("서버 연결 실패 :"+e.getMessage());
			}
	
			
		}
		
		void routing(String input) {
			String gubun[] = input.split(":");
			
			if(gubun[0].equals(Chat.ID))  {//[if문 전체] 프로토콜이 ID로 들어왔다면 실행
				if(this.id == null ) {
				
					//변수에 ID저장
					String tempId = gubun[1]; //ID값 임시저장
					this.id = tempId; //ID값 저장
					for (int i = 0; i < vc.size(); i++) { //모든 클라이언트에게 메세지 출력
						vc.get(i).writer.println("["+this.id+"]님께서 입장을 하셨습니다." );
				}
				}
					return;
			}
			
			if(gubun[0].equals(Chat.ALL)) { // [if문 전체] 프로토콜이 ID로 들어왔다면 실행
				for (int i = 0; i < vc.size(); i++) { //[for문] 모든 클라이언트 사이즈만큼 실행
					if(vc.get(i).getId() != this.getId()) { //자신을 제외한 클라이언트에게 표시
						vc.get(i).writer.println("[" + this.id+"] "+ gubun[1]); //자신의 ID 표시
					}
					else {//나머지 클라이언트에게 표시
						vc.get(i).writer.println("[" + vc.get(i).id+"] "+ gubun[1]); //다른 클라이언트의 ID 표시
					}
				}			
			}
		
		}		
	}
	
	public static void main(String[] args) {
		new ChatServer();
	}
	
}
