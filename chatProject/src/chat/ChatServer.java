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
//��ü�帧
	//1. Ŭ���̾�Ʈ�κ��� �޼����� �о�´�
	//2. ID�� ALL���������� �Ǻ��Ѵ� [�������� ���]
	//3. ID�̸� ID����
	//4. ALL�̸� �޼��� ������[�������� ���]
	//5. ALL�� �޼����� �� Ŭ���̾�Ʈ ���� ����
	//6. �� Ŭ���̾�Ʈ�� ����ID�� ������ �� Ŭ���̾�Ʈ���� �ش��ϴ� �޼��� ����
	
	//���η���
	//1. socket���� ��������
	//2.���� ������� Ŭ���̾�Ʈ ���� ���
	//3. ClientInfo ������� �� Ŭ���̾�Ʈ�� �޼����� �޴´�
	//3-1. ������� ���� �޼����� �����Ѵ�
	//3-2. ������� �� Ŭ���̾�Ʈ���� �˸´� ������ �ѱ��
	//3-3 ������� �� Ŭ���̾�Ʈ�� ��� ������ ��´�
	//4 Vectoer�� �� Ŭ���̾�Ʈ�� ��������� �����Ѵ�
	
	
	private static final String TAG = "ChatServer";
	private ServerSocket serverSocket;
	private Vector<ClientInfo> vc; // ����� Ŭ���̾�Ʈ Ŭ����(����)�� ��� �÷���
	
	public ChatServer( ) {
		try {
			serverSocket = new ServerSocket(10001);
			vc = new Vector<>();
			
			//���� �������� ����
			while(true) {
				System.out.println(TAG + "Ŭ���̾�Ʈ ���� �����...");
			Socket socket = serverSocket.accept();// Ŭ���̾�Ʈ ���� ���
			ClientInfo clientInfo = new ClientInfo(socket);
			System.out.println(TAG + "Ŭ���̾�Ʈ ���� �Ϸ�...");
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
		PrintWriter writer;// BufferedWriter�� �ٸ� ���� �������� �Լ��� ����
		private String id;

		
		public ClientInfo(Socket socket) {
		this.socket = socket;

		}
		
		// ���� : Ŭ���̾�Ʈ�� ���� ���� �޽����� ��� Ŭ���̾�Ʈ���� ������ 
		@Override
		public void run() {
			try {
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				writer = new PrintWriter(socket.getOutputStream(), true);
				
				writer.println("����Ͻ� ���̵� �Է��ϼ���.");
				String input = null;
				while((input = reader.readLine())!=null) { //Ŭ���̾�Ʈ�κ��� �о�°� input�� ����
				routing(input); //routing�޼ҵ�� input���� �Ľ��Ͽ� �� Ŭ���̾�Ʈ�鿡�� �޼��� ����
				}
			} catch (Exception e) {
				System.out.println("���� ���� ���� :"+e.getMessage());
			}
	
			
		}
		
		void routing(String input) {
			String gubun[] = input.split(":");
			
			if(gubun[0].equals(Chat.ID))  {//[if�� ��ü] ���������� ID�� ���Դٸ� ����
				if(this.id == null ) {
				
					//������ ID����
					String tempId = gubun[1]; //ID�� �ӽ�����
					this.id = tempId; //ID�� ����
					for (int i = 0; i < vc.size(); i++) { //��� Ŭ���̾�Ʈ���� �޼��� ���
						vc.get(i).writer.println("["+this.id+"]�Բ��� ������ �ϼ̽��ϴ�." );
				}
				}
					return;
			}
			
			if(gubun[0].equals(Chat.ALL)) { // [if�� ��ü] ���������� ID�� ���Դٸ� ����
				for (int i = 0; i < vc.size(); i++) { //[for��] ��� Ŭ���̾�Ʈ �����ŭ ����
					if(vc.get(i).getId() != this.getId()) { //�ڽ��� ������ Ŭ���̾�Ʈ���� ǥ��
						vc.get(i).writer.println("[" + this.id+"] "+ gubun[1]); //�ڽ��� ID ǥ��
					}
					else {//������ Ŭ���̾�Ʈ���� ǥ��
						vc.get(i).writer.println("[" + vc.get(i).id+"] "+ gubun[1]); //�ٸ� Ŭ���̾�Ʈ�� ID ǥ��
					}
				}			
			}
		
		}		
	}
	
	public static void main(String[] args) {
		new ChatServer();
	}
	
}
