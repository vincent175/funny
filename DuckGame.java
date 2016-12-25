import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DuckGame {
	
	//游戏开始时间和结束时间
	private static long beginTime;
	private static long endTime;
	
	//顶层面板,jPanel添加在这个上面
	private static JFrame jFrame = new JFrame();
	//绘画面板,组件都添加在这个上面
	private static JPanel jPanel = new JPanel();
	
	//选择游戏难度模态框&按钮
	private static JDialog jDialog;
	private static JButton easyButton;
	private static JButton mediumButton;
	private static JButton hardButton;
	
	//玩家实体
	private static JButton playBody;
	//重新开始按钮
	private static JButton resetGameButton;
	
	//标记上下左右四个方向键是否被按下 
	private static boolean kup;
	private static boolean kdown;
	private static boolean kleft;
	private static boolean kright;
	
	//定义玩家走的步伐
	private static int step = 7;
	
	//玩家坐标信息
	private static Point p;
	private static double x = 0.0;
	private static double y = 0.0;
	
	//定义子弹个数
	private static int bulletNum = 40;
	//用来控制线程中的语句是否执行
	private static boolean gameContinue = true;
	
	//游戏信息标签
	private static JLabel jLabel = null;
	//子弹集合
	private static ArrayList<JButton> buttonl = new ArrayList<JButton>();
	//子弹线程集合
	private static ArrayList<Thread> threadl = new ArrayList<Thread>();
	
//	//贴图URL
//	private static URLClassLoader urlClassLoader = (URLClassLoader)new DuckGame().getClass().getClassLoader();
//	private static URL fileLive = urlClassLoader.findResource("MyGameIcons/sun.gif");
//	private static URL fileDie = urlClassLoader.findResource("MyGameIcons/water.gif");
	
	//other
	private static Random random = new Random();
	
	public static void main(String[] args) {
		initJFrame();
//		selectDifficulty();
		initGame();
		activateBulletThread();
	}


	//初始化面板信息
	private static void initJFrame() {
		//设置顶层面板大小
		jFrame.setSize(700, 700);
		//设置关闭按钮可用
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//设置为不可改变容器大小
		jFrame.setResizable(false);
		jFrame.setTitle("vincent_lau_game~");
		jPanel = initJPanel();
		jFrame.setContentPane(jPanel);
		
		jFrame.setVisible(true);
		selectDifficulty();
		jDialog.setModal(true);
		jDialog.setVisible(true);
		
	}

	//初始化JPanel
	private static JPanel initJPanel() {
		//if (jPanel == null)????
		
		//设置游戏信息标签JLabel
		jLabel = new JLabel();
		jLabel.setBounds(new Rectangle(200,100,300,100));
		jLabel.setFont(new Font("Dialog",Font.BOLD,24));
		jLabel.setForeground(new Color(10,10,200));
		//可以接受信息进行相应
		jLabel.setEnabled(true);
		jLabel.setVisible(false);
		
		//初始化JPanel(绘画面板,把所有组件都添加在这个上面)
		//把布局设为null是什么效果,不设null又会怎么样???
		jPanel.setLayout(null);
		jPanel.setForeground(new Color(1,1,1));
		jPanel.setBackground(new Color(1,1,1));
		jPanel.add(jLabel,null);
		//初始化玩家实体
		playBody = getJbutton();
		jPanel.add(playBody,null);
		resetGameButton = getResetGameButton();
		jPanel.add(resetGameButton,null);
		jPanel.setVisible(true);
		
		return jPanel;
	}

	//设置重新开始按钮
	private static JButton getResetGameButton() {
		resetGameButton = new JButton();
		resetGameButton.setBounds(new Rectangle(478,300,120,40));
		resetGameButton.setBackground(new Color(250,100,100));
		resetGameButton.setText("~再次挑战~");
		resetGameButton.setVisible(false);
		resetGameButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetGameButton.setVisible(false);
				jLabel.setVisible(false);
				try {
					Thread.sleep(1000);
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				resetGame();
			}
		});
		return resetGameButton;
	}


	//初始化玩家实体
	private static JButton getJbutton() {
		//需要if (jButton == null) 吗?????
		
		playBody = new JButton();
		playBody.setBounds(new Rectangle(335,335,30,30));
		playBody.setBackground(new Color(1,1,1));
		//获取玩家坐标信息
		p = playBody.getLocation();
		x = p.getX();
		y = p.getY();
		
		playBody.setIcon(new ImageIcon(pngArr1));
		//为游戏实体添加监听器,只要实体存在就会一直保持监听状态
		playBody.addKeyListener(new KeyAdapter() {
			@Override
			//检测到键盘按键被按下,需要做的处理
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == 37) {
					kleft = true;
				}
				if (e.getKeyCode() == 38) {
					//System.out.println("我被按下了~~~");
					kup = true;
				}
				if (e.getKeyCode() == 39) {
					kright = true;
				}
				if (e.getKeyCode() == 40) {
					kdown = true;
				}
			}
			
			@Override
			//检测到按键被释放,需要做的处理
			public void keyReleased(KeyEvent e) {
				//10是enter按键
				if (e.getKeyCode() == 10) {
					//如果按了enter键,并且游戏已经停止
					if (!gameContinue) {
						resetGameButton.setVisible(false);
						jLabel.setVisible(false);
						resetGame();
					}
				}
				
				if (e.getKeyCode() == 37) {
					kleft = false;
				}
				if (e.getKeyCode() == 38) {
//					System.out.println("我被弹起来了~~~");
//					System.out.println("kup:"+ kup);
					kup = false;
				}
				if (e.getKeyCode() == 39) {
					kright = false;
				}
				if (e.getKeyCode() == 40) {
					kdown = false;
				}
			}
		});
		
		return playBody;
	}
	
	//初始化游戏(添加子弹(线程),实体线程)
	public static void initGame(){
		//已经被创建的子弹数目
		int establishBullet = 0;
		while(establishBullet < bulletNum){
			JButton bullet = new JButton();
			//禁用此组件(不再响应用户输入并生成事件)
			bullet.setEnabled(false);
			DuckGame.BulletThread bt = new DuckGame().new BulletThread(bullet);
			Thread th = new Thread(bt);
			buttonl.add(bullet);
			threadl.add(th);
			establishBullet += 1;
		}
		//设置开始时间
		beginTime = new Date().getTime();
		
		//启动玩家实体线程
		DuckGame.PlayBodyThread pt = new DuckGame().new PlayBodyThread();
		
		/*
		//启动bullet线程,最好是把bullet线程start()放在main方法中,不然在游戏开始的时候玩家实体可能移动不了
		activateBulletThread();
		*/
		
		Thread pbt = new Thread(pt);
		pbt.start();
	}
	
	//重置游戏
	public static void resetGame(){
		kup = false;
		kdown = false;
		kright = false;
		kleft = false;
		//在玩家死亡时,将子弹隐藏
		for(int i=0;i<bulletNum;i++){
			buttonl.get(i).setBounds(new Rectangle(-50,-50,10,10));
		}
		playBody.setIcon(new ImageIcon(pngArr1));
		playBody.setLocation(335, 335);
		p = playBody.getLocation();
		x = p.getX();
		y = p.getY();
		beginTime = new Date().getTime();
		gameContinue = true;
	}
	
	//启动子弹线程
	private static void activateBulletThread() {
		for (Thread bt : threadl) {
			bt.start();
			try {
				Thread.sleep(150);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	
	//玩家实体线程
	class PlayBodyThread implements Runnable{

		@Override
		public void run() {
//			System.out.println("进入实体线程~~~~~~~~~~");
			//死循环保证线程不会结束
			while(true){
				if (gameContinue) {
					//每次都要获取一下玩家实体坐标,这样才能使playBody连续的移动
					p = playBody.getLocation();
					x = p.getX();
					y = p.getY();
					if (kup) {
						if (kleft) {
							if(x>0 && y>0){
								playBody.setLocation((int)x-step, (int)y-step);
							}
							
						}
						if(kright){
							if (x+40<700 && y>0) {
								playBody.setLocation((int)x+step,(int)y-step);
							}
							
						}else{
							if(y>0){
								playBody.setLocation((int)x, (int)y-step);
							}
						}
					}
					if (kdown) {
						if(kleft){
							if(x>0 && y+60<700){
								playBody.setLocation((int)x-step, (int)y+step);
							}
						}else if(kright){
							if(x+40<700 && y+60<700){
								playBody.setLocation((int)x+step, (int)y+step);
							}
						}else{
							if(y+60<700){
								playBody.setLocation((int)x, (int)y+step);
							}
						}
					}
					if (kleft) {
						if(kup){
							if(x>0 && y>0){
								playBody.setLocation((int)x-step, (int)y-step);
							}
						}else if(kdown){
							if(x>0 && y+60<700){
								playBody.setLocation((int)x-step, (int)y+step);
							}
						}else{
							if(x>0){
								playBody.setLocation((int)x-step,(int)y);
							}
						}
					}
					if (kright) {
						if(kup){
							if(x+40<700 && y>0){
								playBody.setLocation((int)x+step,(int)y-step);
							}
							
						}else if(kdown){
							if(x+40<700 && y+60<700){
								playBody.setLocation((int)x+step,(int)y+step);
							}
						}else{
							if(x+40<700){
								playBody.setLocation((int)x+step,(int)y);
							}
						}
					}
					try {
						Thread.sleep(10);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				try {
					Thread.sleep(10);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}


	//负责bullet移动的内部类
	//判断是否相撞,只能交给bullet线程去判断
	class BulletThread implements Runnable{
		private JButton bullet;
		private boolean first = true;
		private Random random= new Random();
		
		public BulletThread(JButton bullet){
			this.bullet = bullet;
		}
		@Override
		public void run() {
			/*
			//while(gameContinue){
			while(true){
				bulletMove();
			}
			*/
			bulletMove();
		}
		//子弹运动
		public void bulletMove(){
			//bullet发射的坐标
			int launchX = 0;
			int launchY = 0;
			int speedX = 0;
			int speedY = 0;
			while(true){
				//if判断里面的只执行一次,然后就进入bullet的移动循环
				if(gameContinue){
					//随机产生一个数字来作为bullet发射的边
					int direction = random.nextInt(4)+1;
					switch (direction) {
					case 1:
						launchX = 0;
						launchY = (int) (random.nextDouble()*701);
						break;
					case 2:
						launchX = (int) (random.nextDouble()*701);
						launchY = 0;
						break;
					case 3:
						launchX = 700;
						launchY = (int) (random.nextDouble()*701);
						break;
					case 4:
						launchX = (int) (random.nextDouble()*701);
						launchY = 700;
						break;
					}
					/*
					//????????????????
					if (first) {
						jPanel.add(bullet);
						first = false;
					}
					*/
					jPanel.add(bullet);
					first = false;
					//设置bullet出现的位置和大小
					bullet.setBounds(new Rectangle(launchX,launchY,10,10));
					int r = random.nextInt(251);
					int g = random.nextInt(251);
					int b = random.nextInt(251);
					bullet.setBackground(new Color(r, g, b));
					//移动的步长(以此来控制bullet移动的速度)
					//其实这个可以随便设置,根据距离不同速度也就不同
					speedX = (int) (((x + 15) - launchX) / 40);
					speedY = (int) (((y + 15) - launchY) / 40);
				}
				//下面就是bullet的移动
				while(gameContinue){
					try {
						int r = random.nextInt(251);
						int g = random.nextInt(251);
						int b = random.nextInt(251);
						bullet.setBackground(new Color(r, g, b));
						
						launchX += speedX;
						launchY += speedY;
						bullet.setLocation(launchX, launchY);
						
						//判断是否相撞
						if(Math.abs(launchX-x-10)<17 && Math.abs(launchY-y-10)<17){
						/*if ((launchX+3 > x && launchX+3 < x + 30 && 
								launchY+3 > y && launchY+3 < y + 30)||
								(launchX+10+3 > x && launchX+10+3 < x + 30 
								&& launchY+10+3 > y && launchY+10+3 < y + 30)) {*/
							playBody.setIcon(new ImageIcon(pngArr2));
							gameContinue = false;
							first = true;
							resetGameButton.setVisible(true);
							//获取游戏结束的时间,游戏开始时间在gameInit时或在gameRest中获取得到
							endTime = new Date().getTime();
//							System.out.println(beginTime + "-----------------------beginTime");
//							System.out.println(endTime + "---------------------------endTime");
//							System.out.println("游戏难度为:" + bulletNum + "~~~~~~~~~~~~~~~");
							Date time= new Date(endTime - beginTime);
							/*
							GregorianCalendar calendar = new GregorianCalendar();
							calendar.setTime(time);
							*/
							//获取游戏时间的分(min)和秒(sec)
							/*int min = time.getMinutes();
							int sec = time.getSeconds();
							String gameTime = "";
							if (min != 0) {
								gameTime = min + "分 " + sec + "秒";
							}else{
								gameTime = sec + "秒";
							}*/
							//上面的计算时间的方法,不够精确,真实情况是玩不到一分钟以上
							long haoMiao = endTime - beginTime;
							String gameTime = (haoMiao/1000)+"."+(haoMiao%1000/100)+"秒";
							jLabel.setText("   游戏结束       用时:"+gameTime);
							//显示游戏结果信息标签
							jLabel.setVisible(true);
							break;
						}
						// 超出边线停止循环(停止移动,跳出循环,继续随机发射)
						if (launchX > 700 | launchY > 700 | launchX < 0 | launchY < 0) {
							break;
						}
						//这样可以保证线程的交替
						Thread.sleep(60);
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				try {
					Thread.sleep(30);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
		
	}
	
	//为游戏添加选择难度按钮
	public static void selectDifficulty(){
		jDialog = new JDialog(jFrame,"selectDifficulty");
		jDialog.setSize(300,100);
		jDialog.setLocation(200, 300);
		jDialog.setLayout(new FlowLayout());
		easyButton = new JButton("简单");
		mediumButton = new JButton("中等");
		hardButton = new JButton("困难");
		easyButton.setSize(100,100);
		mediumButton.setSize(100, 100);
		hardButton.setSize(100,100);
		jDialog.add(easyButton);
		jDialog.add(mediumButton);
		jDialog.add(hardButton);
		
		JLabel jLabel = new JLabel();
		jLabel.setText("游戏默认难度为中等");
		jDialog.add(jLabel);
		
		//为难度按钮添加监听
		easyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				bulletNum = 15;
				/*
				 * dispose()
				 * 释放由此 Window、其子组件及其拥有的所有子组件所使
				 * 用的所有本机屏幕资源。即这些 Component 的资源将被
				 * 破坏，它们使用的所有内存都将返回到操作系统，并将它
				 * 们标记为不可显示。 
				 */
				jDialog.dispose();
			}
		});
		
		mediumButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				bulletNum = 40;
				jDialog.dispose();
			}
		});
		
		hardButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				bulletNum = 100;
				jDialog.dispose();
			}
		});
	}
	
	
	
	private static byte[] pngArr1 = 
		{-119,80,78,71,13,10,26,10,0,0,0,13,73,72,68,82,0,0,0,
		16,0,0,0,15,8,6,0,0,0,-19,115,79,47,0,0,0,1,115,82,71,66,0,
		-82,-50,28,-23,0,0,0,4,103,65,77,65,0,0,-79,-113,11,-4,97,5,
		0,0,0,9,112,72,89,115,0,0,14,-60,0,0,14,-60,1,-107,43,14,27,
		0,0,1,9,73,68,65,84,56,79,109,83,-55,1,-61,48,8,-13,62,125,
		50,75,87,-56,28,93,-61,43,100,-106,124,59,15,69,8,8,113,-3,
		32,56,24,-124,56,60,-114,-9,-95,59,17,-77,95,-25,-116,59,81,
		-4,-17,-4,-122,27,63,-72,20,-41,-45,69,84,-65,-121,-86,-102,
		-104,-66,78,-38,-95,9,100,-66,1,72,0,19,121,33,96,70,32,116,
		8,108,101,55,-96,5,36,0,-104,-99,89,51,56,-2,97,115,-71,-19,
		-78,99,112,59,-63,57,74,40,-35,-64,-126,-115,-77,54,32,103,
		-16,-92,-66,102,-34,49,33,35,7,-64,7,-51,41,16,-100,-117,-10,
		122,6,51,-12,-124,77,-107,87,48,-104,103,92,-4,5,102,41,11,-112,
		-107,-127,73,-112,-127,117,-97,12,-78,81,-69,-96,110,-117,73,-95,
		4,50,-80,75,-67,90,-89,-105,108,-113,-110,-18,18,-80,100,14,-112,
		-117,-15,100,-47,39,-47,1,-71,19,88,52,4,-41,20,112,-16,69,-54,30,
		-20,118,-95,-39,-58,96,48,38,-40,54,81,124,-53,106,26,-71,60,29,12,
		-35,-57,38,34,-69,37,68,-7,5,-112,-5,-19,108,76,48,-90,124,15,-34,
		113,15,-94,44,111,-127,84,-22,-75,25,50,75,74,-25,-56,24,101,38,-11,
		-44,63,17,-23,-118,-20,41,44,-58,4,0,0,0,0,73,69,78,68,-82,66,96,-126};
	
	private static byte[] pngArr2 = 
		{-119,80,78,71,13,10,26,10,0,0,0,13,73,72,68,82,0,
		0,0,16,0,0,0,16,8,6,0,0,0,31,-13,-1,97,0,0,0,1,115,82,
		71,66,0,-82,-50,28,-23,0,0,0,4,103,65,77,65,0,0,-79,
		-113,11,-4,97,5,0,0,0,9,112,72,89,115,0,0,14,-60,0,0,
		14,-60,1,-107,43,14,27,0,0,1,32,73,68,65,84,56,79,109,
		83,-55,17,-61,32,12,116,63,121,82,75,90,-96,-114,-76,65,
		11,-82,-123,-81,-21,33,-69,58,-112,-64,100,-58,-111,97,-48,
		94,-62,87,-3,-43,81,-65,120,-10,-22,123,-66,-49,-11,-31,-36,
		-59,-51,-106,-102,91,62,-12,-116,81,-58,-48,-58,19,9,-6,4,-32,
		-56,-2,-12,81,0,80,39,72,9,-96,68,122,121,-13,84,65,-64,-69,73,
		-77,2,24,-48,-35,1,96,32,73,-15,4,88,84,56,-21,4,81,43,-27,-41,2,
		-60,108,-83,22,-80,-71,51,23,40,16,27,110,103,83,-79,-123,8,6,50,
		-15,48,-61,67,109,-10,62,-127,-78,21,16,-122,5,97,87,-65,13,-75,
		-77,-114,46,0,92,55,7,-90,-102,-92,98,-75,96,-84,29,-75,-93,-119,
		63,-2,43,-120,2,-118,-59,-108,-123,2,-92,-28,121,80,0,4,-124,-43,
		84,-40,90,-20,97,74,-11,-93,19,73,83,-64,-24,-124,77,27,-40,-88,0,
		-79,71,5,18,40,114,112,27,-95,0,-77,85,-1,-108,-22,-78,61,-125,-40,
		83,11,80,-3,82,-64,96,102,-120,17,92,0,26,59,115,66,-13,-86,-64,110,
		86,97,22,118,-119,102,112,102,-55,71,-53,51,-59,-40,-41,12,36,76,67,
		70,-54,114,-13,-4,70,-14,-99,-66,-55,44,-51,-15,93,-68,110,-94,78,37,
		14,82,-22,-87,-47,63,-62,63,-82,63,-100,62,-39,102,112,85,0,0,0,0,73,
		69,78,68,-82,66,96,-126};

}
