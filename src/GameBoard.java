import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GameBoard implements Comparable<GameBoard> {
	private Tile[][] tiles = new Tile[5][5];
	private double[] charVals;
	private Word boardWord;
	private int blueCount;
	private int redCount;
	private double blueScore;
	private double redScore;
	private List<Word> words = new LinkedList<Word>();
	private List<List<Tile>> tileLists = new LinkedList<List<Tile>>();
	private boolean currentPlayer;
	
	public GameBoard(String letters) {
		if(letters.length() != 25)
			throw new IllegalStateException("Wrong number of letters.");
		boardWord = new Word(letters);
		for(int i = 0; i < 5; i++)
			for( int j = 0; j < 5; j++)
				tiles[i][j] = new Tile( letters.charAt(j + 5*i), i, j );
		if( charVals == null )
			assignCharVals();
	}
	public GameBoard(String letters, String colors) {
		this(letters);
		int colorVal;
		for(int i = 0; i < 5; i++)
			for( int j = 0; j < 5; j++) {
				colorVal = 0;
				if(colors.charAt(j + 5*i) == 'b')
					colorVal = 1;
				else if( colors.charAt(j + 5*i) == 'r')
					colorVal = -1;
				tiles[i][j].setVal(colorVal);				
			}
		updateDarkColors();
		updateCountAndScore();
	}
	public GameBoard(GameBoard g) {
		boardWord = g.boardWord;
		for(int i = 0; i < 5; i++)
			for( int j = 0; j < 5; j++)
				tiles[i][j] = new Tile(g.tiles[i][j]);
		blueCount = g.blueCount;
		redCount = g.redCount;
		blueScore = g.blueScore;
		redScore = g.redScore;
		charVals = g.charVals;
	}
	
	private void assignCharVals() {
		charVals = new double[26];
		for(int i = 0; i < charVals.length; i++)		//everyone starts at 1
			charVals[i] = 1;
		
		charVals['A'-'A']++;							//vowels are worth 2
		charVals['E'-'A']++;
		charVals['I'-'A']++;
		charVals['O'-'A']++;
		charVals['U'-'A']++;
		
		int[] inv = boardWord.getLetterInventory();
		for(int i = 0; i < charVals.length; i++)		//divide each letter's score by the frequency of that letter
			if( inv[i] != 0 )							//letters that appear more are worth less
				charVals[i] /= inv[i];
	}
	
	private List<LinkedList<Tile>> seqs = new LinkedList<LinkedList<Tile>>();
	public List<LinkedList<Tile>> getTileSeqs(Word w) {
		seqs.clear();
		seqHelper(w.toString(), new LinkedList<Tile>(), 0);
		return seqs;
	}
	private void seqHelper(String whole, LinkedList<Tile> part, int c) {
		if( c == whole.length() ) {
			seqs.add(new LinkedList<Tile>(part));
			return;
		}
		List<int[]> candidateLocs = find( new Tile(whole.charAt(c)) );
		for(int[] l : candidateLocs) {
			Tile t = tiles[l[0]][l[1]];
			if( !contains(part, t) ) {
				part.add( t );
				seqHelper(whole, part, c+1);
				part.removeLast();
			}
		}
	}
	private List<int[]> find(Tile t) {
		List<int[]> locs = new LinkedList<int[]>();
		for(int i = 0; i < 5; i++)
			for(int j = 0; j < 5; j++)
				if( t.getChar() == tiles[i][j].getChar() )
					locs.add( new int[]{i,j} );
		return locs;
	}
		
	public void submit(List<Tile> w, boolean player) {
		currentPlayer = player;
		int p = (player)? -1 : 1;
		for(Tile t : w) {
			t = getTile(t.getLoc()[0], t.getLoc()[1]);
			int val = p*t.getVal();
			if(val == -1 || val == 0) {
				t.setVal(p);
			}
		}
		updateDarkColors();
		updateCountAndScore();
	}
	private void updateDarkColors() {
		ArrayList<Tile> toDark = new ArrayList<Tile>();
		for(int i = 0; i < 5; i++)
			for(int j = 0; j < 5; j++) {
				Tile t = getTile(i,j);
				if( t.getVal() != 0 ) {
					int p = t.getVal()/Math.abs(t.getVal());
					t.setVal(p);
					if( (i == 0 || i > 0 && getTile(i-1,j).getVal()*p > 0) &&		//above
						(i == 4 || i < 4 && getTile(i+1,j).getVal()*p > 0) &&		//below
						(j == 0 || j > 0 && getTile(i,j-1).getVal()*p > 0) &&		//left
						(j == 4 || j < 4 && getTile(i,j+1).getVal()*p > 0) )		//right
						toDark.add(t);
				}
			}
		for(Tile t : toDark)
			t.setVal( (t.getVal()/Math.abs(t.getVal()))*2 );
	}
	private void updateCountAndScore() {
		blueCount = 0;
		redCount = 0;
		blueScore = 0;
		redScore = 0;
		
		for(int i = 0; i < 5; i++)
			for(int j = 0; j < 5; j++) {
				Tile t = tiles[i][j];
				
				switch(t.getVal()) {
				case -2: redCount++;
						 redScore += 3*charVals[t.getChar()-'A'];
						 break;
				case -1: redCount++;
						 redScore += charVals[t.getChar()-'A'];
						 break;
				case  1: blueCount++;
						 blueScore += charVals[t.getChar()-'A'];
						 break;
				case  2: blueCount++;
						 blueScore += 3*charVals[t.getChar()-'A'];
						 break;
				}
			}		
	}
	
	public int compareTo(GameBoard o) {
		int diff = (int)(100*((blueScore - redScore) - (o.blueScore - o.redScore)));
		return (currentPlayer) ? -diff : diff;
	}
	
	public boolean equals(Object other) {
		if( other == null || !(other instanceof GameBoard) )
			return false;
		GameBoard o = (GameBoard)other;
		for(int i = 0; i < 5; i++)
			for(int j = 0; j < 5; j++)
				if( !tiles[i][j].equals(o.tiles[i][j]) )
					return false;
		return true;
	}
	
	private boolean contains(LinkedList<Tile> l, Tile out) {
		for(Tile in : l)
			if( out.getLoc()[0] == in.getLoc()[0] && out.getLoc()[1] == in.getLoc()[1] )
				return true;
		return false;
	}
	
	public Word getBoardWord() {
		return boardWord;
	}
	public Tile getTile(int i, int j) {
		return tiles[i][j];
	}
	public double[] getScores() {
		return new double[]{blueScore, redScore};
	}
	public void addTileList(List<Tile> l) {
		Word w = new Word(l);
		if(!words.contains(w)) {
			words.add(w);
			tileLists.add(l);
		}		
	}
	public boolean hasWord() {
		return words.size() != 0;
	}
	public List<List<Tile>> getTileLists() {
		return tileLists;
	}
	public List<Word> getWords() {
		return words;
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		for(int i = 0; i < 5; i++) {
			for(int j = 0; j < 5; j++)
				s.append( tiles[i][j].getChar() +" " );
			s.append("\n\n");
		}
		return s.toString();
	}
}