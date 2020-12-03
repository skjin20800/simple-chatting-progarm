package chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import protocol.Chat;

public class ChatClient extends JFrame {
	
	// 전체흐름
	// 1. 서버연결
	// 2. 연결시 환영문구
	// 3. 처음 보내는 메세지 사용자 고유 ID로 인식[프로토콜 ID 사용]
	// 4. 다음 메세지부터 채팅으로 사용[프로토콜 ALL 사용]
	// 5. 종료시 대화한 내용 + 시간 파일저장
	
	//내부로직
	// 1. socket을 이용해연결
	// 2. writer를 이용해 메세지 서버로 전송
	// 3. reader를 이용해 모든 메세지 출력
	// 3-1. 읽을때마다 대화내용 List에 저장
	// 4. 종료시 시간과 함께 모든내용 파일로저장
	
	private final static String TAG = "ChatClient :";
	private ChatClient chatClinet = this;

	private static final int PORT = 10001;

	private JButton btnConnect, btnSend; // 앞에 btn 공통적인 컴포넌트 이름지으면 나중에 보기 편함
	private JTextField tfHost, tfChat; //tfHost - IP주소, tfChat - 보낼 메세지
	private JTextArea taChatList; // 메인 채팅창변수
	private ScrollPane scrollPane; // 스크롤바

	private JPanel topPanel, bottomPanel; //맨위 패널, 맨 아래 패널 

	private Socket socket; 
	private PrintWriter writer;
	private BufferedReader reader;
	
	private int protocolCount =0; // 값이 0이면 ID , else ALL로 판단하는 변수 
	private FileWriter fout = null; // 파일만드는 변수
	private List<String> fileList; // 메시지를 읽어올때마다 List에 추가하는 배열
	
	public ChatClient() {

		init(); 
		setting(); 
		batch(); 
		listener(); 
		
		setVisible(true);
	}

	private void init() {
		btnConnect = new JButton("Connet");
		btnSend = new JButton("send");
		tfHost = new JTextField("127.0.0.1", 20);
		tfChat = new JTextField(20);
		taChatList = new JTextArea(10, 30); // row,column
		scrollPane = new ScrollPane();
		topPanel = new JPanel();
		bottomPanel = new JPanel();
		fileList = new ArrayList<>();
		
		try {
			fout = new FileWriter("d://네트워크 프로그래밍 구현.txt"); //파일 경로 및 저장파일 이름
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setting() {
		setTitle("채팅 다대다 클라이언트");
		setSize(500, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null); // 실행시 중앙으로 모임
		taChatList.setBackground(Color.ORANGE);
		taChatList.setForeground(Color.BLUE);
	}

	private void batch() {
		topPanel.add(tfHost);
		topPanel.add(btnConnect);
		bottomPanel.add(tfChat);
		bottomPanel.add(btnSend);
		scrollPane.add(taChatList);
		
		add(topPanel, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
		
	}

	private void listener() {
		btnConnect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				connet();
			}
		});
		
		btnSend.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				send();
			}
		});
		
		this.addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent e) { // X버튼 누를때 발생하는 리스너
				try {
					for(int i = 0; i < fileList.size(); i++ ) { //fileList사이즈만큼  text에 저장하는 반복문
						fout.write(fileList.get(i)+"\n");	
					}
					fout.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

		});
		
		
	}
	
	
		

	private void connet() {

		try {
			String host = tfHost.getText();
			socket = new Socket(host, PORT);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(),true);
			
			//Reader 쓰레드
			//1. taChatList뿌리기
			//2. 뿌릴때 시간포함 모든값을 file에 저장
			Thread readerThread = new Thread(new ReaderThread()); 
			readerThread.start();
		
		} catch (Exception e1) {
			System.out.println(TAG + "서버 연결 에러" + e1.getMessage());
			e1.printStackTrace();
		}
	}
	
	private void send() {

		
		String chat = tfChat.getText(); // 채팅창의 내용을 가져오는 변수

		//Write쓰레드의 역할
		//1. 프로토콜과 함께 메세지를 서버로전송
		//2. 타이틀 변경
		//3. tfChat비우기
		Thread writerThread = new Thread(new WriteThread(chat));
		writerThread.start();


	}
	

	class ReaderThread implements Runnable{
		//while을 돌면서 서버로 부터 메시지를 받아서 taChatList에 뿌리기
		@Override
		public void run() { //Reader 쓰레드- 버퍼에서 값을 읽어와서 사용		
						
			try {
				String input = null;
				while((input = reader.readLine()) != null) {
					//  채팅창에 순차적 출력
					taChatList.append(input + "\n");
					
					// 현재시간 구하는 변수
					LocalDateTime now = LocalDateTime.now(); 

					//fileList에 읽어올때마다 현재시간을 포함한 값을 저장
					fileList.add("(" + now.getHour() + "시 " + now.getMinute() + "분)"+input);
				}
			} catch (Exception e) {
				System.out.println(TAG + "ReaderThread 오류");
				e.printStackTrace();
			}
		}
		}
	
	@NoArgsConstructor
	@AllArgsConstructor
	class WriteThread extends Thread{
		private String chat;
		
		@Override
		public void run() {
			
			if(protocolCount == 0) { // [if문 전체]프로토콜 카운터 == 0일시 tfChat(보낼채팅)을 프로토콜 ID로 서버에 전송 
				writer.println(Chat.ID+":"+chat); //[세부1] ID형태로 메세지를 서버에 보내는것
				tfChat.setText(""); //[세부2]전송후 tfChat 초기화하는것
				setTitle("아이디 : ["+chat+"]"); //[세부3]타이틀 네임 변경
				protocolCount++;
			}
				else { //[else문 전체] 프로토콜 카운터 != 0 일시 tfCaht(보낼채팅)을 프로토콜ALL(전체채팅)로 서버에 전송
					writer.println(Chat.ALL+":"+chat);  //[세부1] ALL(채팅)형태로 메세지를 서버에 보내는것 
					tfChat.setText("");//[세부2] 전송후 tfChat 초기화하는것 
				}
		
		}
		}
	

	
	public static void main(String[] args) {
		new ChatClient();
	}
}
