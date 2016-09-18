import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;


public class WebServer {
	public static void main(String arvg[]) throws Exception {
		// Ajusta o n�mero da porta.
		int port = 6787;
		
		// Estabelece o socket de escuta.
		ServerSocket servidor = new ServerSocket(port);
		//Esta funcionando no localhost - 127.0.0.1
		//porta 6787
		//para testar: http://127.0.0.1:6787
		
		// Processa a requisi��o de servi�o HTTP em um la�o infinito.
		while (true)  {
			//Recebe a requisi��o do cliente
			Socket cliente = servidor.accept();
			
			//Constroi um objeto para processar a mensagem de requisi��o HTTP.
			HttpRequest request = new HttpRequest(cliente);
			// Cria uma nova thread para processar a requisi��o.
			Thread thread = new Thread(request);
			//Inicia a thread.
			thread.start();
		}
	}
}

final class HttpRequest implements Runnable {
	final static String CRLF = "\r\n";
	Socket socket;
	
	// Construtor
	public HttpRequest(Socket socket) throws Exception {
		this.socket = socket;
	}
	
	// Implementacao do m�todo run() da interface Runnable.
	public void run() {
		try {
			processRequest();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private static void sendBytes(FileInputStream fis, OutputStream os)
			throws Exception
			{
				// Construir um buffer de 1K para comportar os bytes no caminho para o socket.
			byte[] buffer = new byte[1024];
				int bytes = 0;
				// Copiar o arquivo requisitado dentro da cadeia de sa�da do socket.
				while((bytes = fis.read(buffer)) != -1 ) {
					os.write(buffer, 0, bytes);
				}
			}
	
	private static String contentType(String fileName)
	{
		if(fileName.endsWith(".htm") || fileName.endsWith(".html")) {
			return "text/html";
		}
		if(fileName.endsWith(".gif")) {
			return "image/gif";
		}
		if(fileName.endsWith(".jpeg")) {
			return "image/jpeg";
		}
		return "application/octet-stream";
	}
	private void processRequest() throws Exception {
		// Obtem uma refer�ncia para os trechos de entrada e sa�da do socket.
		InputStream is = socket.getInputStream();
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());
		
		// Ajusta os filtros do trecho de entrada.
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		
		// Obtem a linha de requisi��o da mensagem de requisi��o HTTP.
		String requestLine = br.readLine();
		
		//  Exibi a linha de requisi��o.
		System.out.println();
		System.out.println(requestLine);
		
		// Obtem e exibir as linhas de cabe�alho.
		String headerLine = null;
		while ((headerLine = br.readLine()).length() != 0) {
			System.out.println(headerLine);
		}
		
		//Parte 2 do EP - Responder a requisi��o
		StringTokenizer tokens = new StringTokenizer(requestLine);
		tokens.nextToken();
		String fileName = tokens.nextToken();
		fileName = "."+ fileName;
		
		// Abrir o arquivo requisitado.
		FileInputStream fis = null;
		Boolean fileExists = true;
		try {
			fis = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			fileExists = false;
		}
		
		// Construir a mensagem de resposta.
		String statusLine = null;
		String contentTypeLine = null;
		String entityBody = null;
		if (fileExists){
			statusLine= "HTTP/1.1 200 OK"; 
			contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
			os.writeBytes(statusLine);
			os.writeBytes(contentTypeLine);
			os.writeBytes(CRLF);
			sendBytes(fis, os);
			fis.close();
		}
		else{
			statusLine = "HTTP/1.1 404 Not Found";
			contentTypeLine = "Content-type: " + contentType(fileName) + CRLF; // N�o sei se est� certo
			entityBody = "<HTML><HEAD><TITTLE>Not Found</TITTLE></HEAD><BODY><p>Not Found</p></BODY></HTML>";
			os.writeBytes(statusLine);
			os.writeBytes(contentTypeLine);
			os.writeBytes(CRLF);
			os.writeBytes(entityBody);
		}
		
		// Feche as cadeias e socket.
		os.close();
		br.close();
		socket.close();
	}
	

}
