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
	
	// ��ü�帧
	// 1. ��������
	// 2. ����� ȯ������
	// 3. ó�� ������ �޼��� ����� ���� ID�� �ν�[�������� ID ���]
	// 4. ���� �޼������� ä������ ���[�������� ALL ���]
	// 5. ����� ��ȭ�� ���� + �ð� ��������
	
	//���η���
	// 1. socket�� �̿��ؿ���
	// 2. writer�� �̿��� �޼��� ������ ����
	// 3. reader�� �̿��� ��� �޼��� ���
	// 3-1. ���������� ��ȭ���� List�� ����
	// 4. ����� �ð��� �Բ� ��系�� ���Ϸ�����
	
	private final static String TAG = "ChatClient :";
	private ChatClient chatClinet = this;

	private static final int PORT = 10001;

	private JButton btnConnect, btnSend; // �տ� btn �������� ������Ʈ �̸������� ���߿� ���� ����
	private JTextField tfHost, tfChat; //tfHost - IP�ּ�, tfChat - ���� �޼���
	private JTextArea taChatList; // ���� ä��â����
	private ScrollPane scrollPane; // ��ũ�ѹ�

	private JPanel topPanel, bottomPanel; //���� �г�, �� �Ʒ� �г� 

	private Socket socket; 
	private PrintWriter writer;
	private BufferedReader reader;
	
	private int protocolCount =0; // ���� 0�̸� ID , else ALL�� �Ǵ��ϴ� ���� 
	private FileWriter fout = null; // ���ϸ���� ����
	private List<String> fileList; // �޽����� �о�ö����� List�� �߰��ϴ� �迭
	
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
			fout = new FileWriter("d://��Ʈ��ũ ���α׷��� ����.txt"); //���� ��� �� �������� �̸�
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setting() {
		setTitle("ä�� �ٴ�� Ŭ���̾�Ʈ");
		setSize(500, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null); // ����� �߾����� ����
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
			public void windowClosing(WindowEvent e) { // X��ư ������ �߻��ϴ� ������
				try {
					for(int i = 0; i < fileList.size(); i++ ) { //fileList�����ŭ  text�� �����ϴ� �ݺ���
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
			
			//Reader ������
			//1. taChatList�Ѹ���
			//2. �Ѹ��� �ð����� ��簪�� file�� ����
			Thread readerThread = new Thread(new ReaderThread()); 
			readerThread.start();
		
		} catch (Exception e1) {
			System.out.println(TAG + "���� ���� ����" + e1.getMessage());
			e1.printStackTrace();
		}
	}
	
	private void send() {

		
		String chat = tfChat.getText(); // ä��â�� ������ �������� ����

		//Write�������� ����
		//1. �������ݰ� �Բ� �޼����� ����������
		//2. Ÿ��Ʋ ����
		//3. tfChat����
		Thread writerThread = new Thread(new WriteThread(chat));
		writerThread.start();


	}
	

	class ReaderThread implements Runnable{
		//while�� ���鼭 ������ ���� �޽����� �޾Ƽ� taChatList�� �Ѹ���
		@Override
		public void run() { //Reader ������- ���ۿ��� ���� �о�ͼ� ���		
						
			try {
				String input = null;
				while((input = reader.readLine()) != null) {
					//  ä��â�� ������ ���
					taChatList.append(input + "\n");
					
					// ����ð� ���ϴ� ����
					LocalDateTime now = LocalDateTime.now(); 

					//fileList�� �о�ö����� ����ð��� ������ ���� ����
					fileList.add("(" + now.getHour() + "�� " + now.getMinute() + "��)"+input);
				}
			} catch (Exception e) {
				System.out.println(TAG + "ReaderThread ����");
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
			
			if(protocolCount == 0) { // [if�� ��ü]�������� ī���� == 0�Ͻ� tfChat(����ä��)�� �������� ID�� ������ ���� 
				writer.println(Chat.ID+":"+chat); //[����1] ID���·� �޼����� ������ �����°�
				tfChat.setText(""); //[����2]������ tfChat �ʱ�ȭ�ϴ°�
				setTitle("���̵� : ["+chat+"]"); //[����3]Ÿ��Ʋ ���� ����
				protocolCount++;
			}
				else { //[else�� ��ü] �������� ī���� != 0 �Ͻ� tfCaht(����ä��)�� ��������ALL(��üä��)�� ������ ����
					writer.println(Chat.ALL+":"+chat);  //[����1] ALL(ä��)���·� �޼����� ������ �����°� 
					tfChat.setText("");//[����2] ������ tfChat �ʱ�ȭ�ϴ°� 
				}
		
		}
		}
	

	
	public static void main(String[] args) {
		new ChatClient();
	}
}
