package websocket;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

import net.sf.json.JSONObject;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

import websocket.Word2VEC;

/**
 * @ServerEndpoint 注解是一个类层次的注解，它的功能主要是将目前的类定义成一个websocket服务器端,
 * 注解的值将被用于监听用户连接的终端访问URL地址,客户端可以通过这个URL来连接到WebSocket服务器端
 */
@ServerEndpoint("/word2vec")
public class WebSocket {
	//静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
	private static int onlineCount = 0;

	//concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
	public static CopyOnWriteArraySet<WebSocket> webSocketSet = new CopyOnWriteArraySet<WebSocket>();
	public static Word2VEC w1 = new Word2VEC(); 
	public static Word2VEC w2 = new Word2VEC(); 
	public static Word2VEC w3 = new Word2VEC(); 
	public static Word2VEC w4 = new Word2VEC(); 
	public static String m1d = "CBOW method: ";
	public static String m2d = "Skip gram method: ";
	public static String m3d = "Skip gram method: ";
	public static String m4d = "Structured Skip gram: ";
	public static boolean word2vecSet = false; //默认没有设置word2vec
	public static boolean word2vecSetting = false; //正在设置数据库
    //w1.loadJavaModelTxt("library/SkipgramSmall_data");

	//与某个客户端的连接会话，需要通过它来给客户端发送数据
	public Session session;

	public WebSocket() {
	}
	/**
	 * 连接建立成功调用的方法
	 * @param session  可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
	 * @throws IOException 
	 */
	@OnOpen
	public void onOpen(Session session) throws IOException{
		
		this.session = session;
		webSocketSet.add(this);     //加入set中
		addOnlineCount();           //在线数加1
		System.out.println("有新连接加入！当前在线人数为" + getOnlineCount());
		if (!this.word2vecSet && !this.word2vecSetting) {
			this.word2vecSetting = true;
			w1.loadJavaModelTxt("D:/学习/大三上/信息检索/Word2VEC_java/library/Cbow_data");
			w2.loadJavaModelTxt("D:/学习/大三上/信息检索/Word2VEC_java/library/Skipgram_data");
			//w3.loadJavaModelTxt("D:/学习/大三上/信息检索/Word2VEC_java/library/SkipgramSmall_data");
			//w4.loadJavaModelTxt("D:/学习/大三上/信息检索/Word2VEC_java/library/SkipgramV2Small_data");
			this.word2vecSet = true;
			this.word2vecSetting = false;
			System.out.println("the server is ready!");
		}		
	}

	/**
	 * 连接关闭调用的方法
	 */
	@OnClose
	public void onClose(){
		webSocketSet.remove(this);  //从set中删除
		subOnlineCount();           //在线数减1
		System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());
	}

	/**
	 * 收到客户端消息后调用的方法
	 * @param message 客户端发送过来的消息
	 * @param session 可选的参数
	 * @throws IOException 
	 */
	@SuppressWarnings("static-access")
	@OnMessage
	public void onMessage(String message, Session session) throws IOException {
		System.out.println("来自客户端的消息:" + message);
		JSONObject json1 = JSONObject.fromObject(message);
		if (json1.getInt("action") == 1 && this.word2vecSet) {
			String query = json1.getString("message");
			this.sendMessage(query);
		}
		if (json1.getInt("action") == 2 && this.word2vecSet) {
			String q1 = json1.getString("query1");
			String q2 = json1.getString("query2");
			String q3 = json1.getString("query3");
			this.sendMessage2(q1, q2, q3);
		}
	}

	/**
	 * 发生错误时调用
	 * @param session
	 * @param error
	 */
	@OnError
	public void onError(Session session, Throwable error){
		System.out.println("发生错误");
		error.printStackTrace();
	}

	/**
	 * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
	 * @param message
	 * @throws IOException
	 */
	public void sendMessage(String message) throws IOException{
		JSONObject json1 = new JSONObject();
		String m1 = w1.distance(message).toString();
		String m2 = w2.distance(message).toString();
		//String m3 = w3.distance(message).toString();
		//String m4 = w4.distance(message).toString();
		json1.put("method1", m1); //表示发送消息
		json1.put("method2", m2); //表示发送消息
		json1.put("method3", ""); //表示发送消息
		json1.put("method4", ""); //表示发送消息
		json1.put("method1d", m1d); //表示发送消息
		json1.put("method2d", m2d); //表示发送消息
		json1.put("method3d", ""); //表示发送消息
		json1.put("method4d", ""); //表示发送消息
		String messages = json1.toString();
		this.session.getBasicRemote().sendText(messages);
		//this.session.getAsyncRemote().sendText(message);
	}

	public void sendMessage2(String q1, String q2, String q3) throws IOException{
		JSONObject json1 = new JSONObject();
		String m1 = w1.calculate(q1, q2, q3).toString();
		String m2 = w2.calculate(q1, q2, q3).toString();
		//String m3 = w3.distance(message).toString();
		//String m4 = w4.distance(message).toString();
		json1.put("method1", m1); //表示发送消息
		json1.put("method2", m2); //表示发送消息
		json1.put("method3", ""); //表示发送消息
		json1.put("method4", ""); //表示发送消息
		json1.put("method1d", m1d); //表示发送消息
		json1.put("method2d", m2d); //表示发送消息
		json1.put("method3d", ""); //表示发送消息
		json1.put("method4d", ""); //表示发送消息
		String messages = json1.toString();
		this.session.getBasicRemote().sendText(messages);
		//this.session.getAsyncRemote().sendText(message);
	}
	
	public static synchronized int getOnlineCount() {
		return onlineCount;
	}

	public static synchronized void addOnlineCount() {
		WebSocket.onlineCount++;
	}

	public static synchronized void subOnlineCount() {
		WebSocket.onlineCount--;
	}
}
