import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.io.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Cheat implements Runnable {
	private static GameBoard board;
	private static LinkedList<Word> possibles = new LinkedList<Word>();
	private static GameBoard[] virtualBoards;
	private static int numVBoards = 5;
	private static int startingVB = 0;
	private static boolean vBoardsLoaded = false;
	
	private static boolean p = false; //two players: true and false;
	private static ArrayList<Tile> currentWord = new ArrayList<Tile>(25);
	
//	private static LinkedList<Word> playedWords;
	private static Scanner kb = new Scanner(System.in);
	
	private static Rectangle submit = new Rectangle(650, 100, 115, 25);
	private static Rectangle clear = new Rectangle(650, 160, 115, 25);
	private static Rectangle backSpace = new Rectangle(650, 130, 115, 25);
	private static Rectangle next = new Rectangle(300*numVBoards-25, 300, 20, 20);
	private static Rectangle prev = new Rectangle(300*numVBoards-25, 330, 20, 20);
	
	private static Rectangle[] VBoardRects = new Rectangle[numVBoards];
	
	public static void main(String[] args) {
		collectInput();
		Cheat ex = new Cheat();
		new Thread(ex).start();
	}

	private static void collectInput() {
		System.out.print("Enter the 25 letters (in order)\n> ");
		String c25 = kb.next();
		
		while(c25.length() != 25) {
			System.out.print("Wrong number of letters. Try again.\n> ");
			c25 = kb.next();
		}
		c25 = c25.toUpperCase();
		
		System.out.print("New Game? (y/n)\n> ");
		
		if( kb.next().equals("n") ){
			
			System.out.print("Enter the 25 colors (b/w/g)\n> ");
			String colors = kb.next();
			while(colors.length() != 25) {
				System.out.print("Wrong number of colors. Try again.\n> ");
				colors = kb.next();
			}

			System.out.print("Whose turn is it? (b/r)\n> ");
			String player = kb.next();
			while( !player.equals("r") && !player.equals("b") ) {
				System.out.print("Please enter either \"b\" or \"r\"\n> ");
				player = kb.next();
			}
			
			p = player.equals("r");
			board = new GameBoard( c25, colors );
		}
		else {
			board = new GameBoard( c25 );
		}
	}
		
	private static void init() {
		
		for(int i = 0; i < VBoardRects.length; i++)
			VBoardRects[i] = new Rectangle(300*i, 300, 250, 250);
		
		loadDictionary();
		//showPossibles();
		getVirtualBoards();
	}
	
	private static void getVirtualBoards() {
		System.out.println("Loading...");
		vBoardsLoaded = false;
		LinkedList<GameBoard> q = new LinkedList<GameBoard>();
		GameBoard temp;
		
		//for(Word w : possibles) {
		int maxVBoards = 1000;
		for(int i = 0; i < maxVBoards && i < possibles.size(); i++) {
			Word w = possibles.get(possibles.size()-i-1);
			List<LinkedList<Tile>> ws = board.getTileSeqs(w);
			for(List<Tile> l : ws) {
				temp = new GameBoard(board);
				temp.addTileList( l );
				temp.submit(l, p);
				denseAdd(q, temp, l);
			}
		}
		
		virtualBoards = q.toArray( new GameBoard[q.size()] );
		vBoardsLoaded = true;
		System.out.println("Done! "+ virtualBoards.length);
	}
	private static void denseAdd(LinkedList<GameBoard> q, GameBoard temp, List<Tile> l) {
		int i=0;
		Iterator<GameBoard> itr = q.iterator();
		while(itr.hasNext()) {
			GameBoard other = itr.next();
			if( temp.equals(other) ) {
				other.addTileList( l );
				return;
			}
			if(temp.compareTo(other) > 0) {
				q.add(i, temp);
				return;
			}
			i++;
		}
		q.add(temp);
	}
	
	private static void showPossibles() {		
		for(Word w : possibles)
			System.out.println(w +"\t"+ w.size());
		System.out.println(possibles.size() +" possibilities");
	}
	
	private static void loadDictionary() {
		System.out.println("Loading Dictionary...");
		try{
			Scanner read = new Scanner( new File("LetterPressDictionary.txt") );
			while(read.hasNext()) {
				Word w = new Word(read.next());
				if( board.getBoardWord().contains(w) )
					possibles.add( w );
			}
			read.close();
		}
		catch(Exception e) {
			System.out.println(e);
			System.exit(1);
		}
		Collections.sort(possibles);
		System.out.println("Done!");
	}
	
	private static boolean isUsed(Tile t) {
		for(Tile t2 : currentWord)
			if( t.getLoc()[0] == t2.getLoc()[0] && t.getLoc()[1] == t2.getLoc()[1] )
				return true;
		return false;
	}
	
	protected void update(int deltaTime){}
	
	protected void render(Graphics2D g) {
		drawBoard(g, board, 0, 0);
		drawVBoards(g);
		
		g.drawString( new Word(currentWord).toString(), 300, 140);
		
		int padding = 5;
		g.setColor(Color.green);
		g.fill(submit);
		g.setColor(Color.black);
		g.drawString("Submit", submit.x+padding, submit.y+submit.height-padding);
		
		g.setColor(Color.red);
		g.fill(clear);
		g.setColor(Color.black);
		g.drawString("Clear", clear.x+padding, clear.y+clear.height-padding);
		
		g.setColor(Color.darkGray);
		g.fill(backSpace);
		g.setColor(Color.white);
		g.drawString("BackSpace", backSpace.x+padding, backSpace.y+backSpace.height-padding);
		
		g.setColor(Color.darkGray);
		g.fill(next);
		g.fill(prev);
		g.setColor(Color.white);
		g.drawString(">", next.x+5, next.y+next.height-2);
		g.drawString("<", prev.x+5, prev.y+prev.height-2);
		
		drawScores(g);
		
	}
	private void drawVBoards(Graphics2D g) {
		for(int i = startingVB; vBoardsLoaded && i < virtualBoards.length && i < numVBoards+startingVB; i++) {
//			GameBoard b = virtualBoards[virtualBoards.length-1-i];	//reverse order
			GameBoard b = virtualBoards[i];
			int x = 300*(i-startingVB);
			int y = 300;
			double dScore = (p) ? (b.getScores()[1] - b.getScores()[0]) - (board.getScores()[1] - board.getScores()[0]) :
								  (b.getScores()[0] - b.getScores()[1]) - (board.getScores()[0] - board.getScores()[1]);
			
			drawBoard(g, b, x, y);
			List<Word> ws = b.getWords();
			for(int j = 0; j < ws.size(); j++)
				g.drawString( ws.get(j).toString(), x, y+250+20*(j+1));
			g.drawString( String.format("%5.2f", dScore), x+200, y+250+20);
		}
	}
	private void drawBoard(Graphics2D g, GameBoard b, int x, int y) {
		g.setFont( new Font("SANS_SERIF", 0, 20) );
		for(int i = 0; i < 5; i++) {
			for(int j = 0; j < 5; j++) {
				Tile t = b.getTile(i,j);
				g.setColor( t.getColor() );
				g.fill( new Rectangle(j*50+x, i*50+y, 50, 50) );
				g.setColor(Color.BLACK);
				g.drawString(t.getChar()+"", j*50+18+x, (i+1)*50-18+y);
			}
		}
	}
	private void drawScores(Graphics2D g) {
		g.setColor( Tile.blue );
		g.drawString(String.format("%5.2f", board.getScores()[0]), 260, 50);
		if( !p )
			g.drawString("Blue player's turn.", 400, 50);
		g.setColor( Tile.red );
		g.drawString(String.format("%5.2f", board.getScores()[1]), 260, 100);
		if( p )
			g.drawString("Red player's turn.", 400, 50);
	}
	
	private static void submit(List<Tile> l) {
		board.submit(l, p);
		p = !p;
		possibles.remove( new Word(l) );
		currentWord.add(new Tile('S'));
		possibles.remove( new Word(l) );
		currentWord.clear();
		startingVB = 0;
		Cheat.getVirtualBoards();
	}

////////////////////////////////////////////////////////////////////////////////////////////	
	
	JFrame frame;
	Canvas canvas;
	BufferStrategy bufferStrategy;
	MouseControl mouse;

	public static final int WIDTH = 300*numVBoards;
	public static final int HEIGHT = 700;
	
	public Cheat() {
		frame = new JFrame("LetterPress Cheat");

		JPanel panel = (JPanel) frame.getContentPane();
		panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		panel.setLayout(null);

		canvas = new Canvas();
		canvas.setBounds(0, 0, WIDTH, HEIGHT);
		canvas.setIgnoreRepaint(true);

		panel.add(canvas);

		mouse = new MouseControl();
		canvas.addMouseListener(mouse);
		canvas.addMouseMotionListener(mouse);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);

		canvas.createBufferStrategy(2);
		bufferStrategy = canvas.getBufferStrategy();

		canvas.requestFocus();
	}
	
	private class MouseControl extends MouseAdapter {
		public void mouseClicked(MouseEvent e){
			int x = e.getX();
			int y = e.getY();
			if( x < 250 && y < 250 ) {
				Tile t = board.getTile(y/50, x/50);
				if( !isUsed(t) )
					currentWord.add(t);
			}
			
			if( next.contains(e.getPoint()) && startingVB <= virtualBoards.length-numVBoards )
				startingVB += numVBoards;
			if( prev.contains(e.getPoint()) && startingVB >= numVBoards )
				startingVB -= numVBoards;
			
			if( backSpace.contains(e.getPoint()) )
				if(currentWord.size() > 0)
					currentWord.remove(currentWord.size()-1);
			if( clear.contains(e.getPoint()) )
				currentWord.clear();
			if( submit.contains(e.getPoint()) && possibles.contains(new Word(currentWord)) )
				submit(currentWord);
			
			for(int i = 0; i < numVBoards; i++)
				if( vBoardsLoaded && VBoardRects[i].contains(e.getPoint()) && i+startingVB < virtualBoards.length )
					submit(virtualBoards[startingVB+i].getTileLists().get(0));
		}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mouseMoved(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
	}
	
	long desiredFPS = 60;
	long desiredDeltaLoop = (1000*1000*1000)/desiredFPS;
	boolean running = true;
	
	public void run() {
		long beginLoopTime;
		long endLoopTime;
		long currentUpdateTime = System.nanoTime();
		long lastUpdateTime;
		long deltaLoop;
		int deltaTime;

		init();

		while(running)
		{
			beginLoopTime = System.nanoTime();

			render();

			lastUpdateTime = currentUpdateTime;
			currentUpdateTime = System.nanoTime();
			deltaTime = (int) ((currentUpdateTime - lastUpdateTime)/(1000*1000));
			update(deltaTime);

			endLoopTime = System.nanoTime();
			deltaLoop = endLoopTime - beginLoopTime;

			if(deltaLoop <= desiredDeltaLoop)
			{
				try
				{
					Thread.sleep((desiredDeltaLoop - deltaLoop)/(1000*1000));
				} catch(InterruptedException e) { /* Do nothing */ }
			}
		}
	}
	private void render()
	{
		Graphics2D g = (Graphics2D)bufferStrategy.getDrawGraphics();
		g.clearRect(0, 0, WIDTH, HEIGHT);
		render(g);
		g.dispose();
		bufferStrategy.show();
	}
}
