import java.util.LinkedList;
import java.util.List;

public class Word implements Comparable<Word>{
	private LinkedList<Tile> tileList = new LinkedList<Tile>();
	private LetterInventory	inv;
	private int score;
	
	public Word(String s) {
		s = s.toUpperCase();
		for(int i = 0; i < s.length(); i++)
			tileList.add( new Tile(s.charAt(i)) );
		inv = new LetterInventory(tileList);
		score = s.length();
	}
	public Word(Word o) {
		this(o.toString());
	}
	public Word(List<Tile> l) {
		for(Tile t : l)
			tileList.add( t );
		inv = new LetterInventory(tileList);
		score = tileList.size();
	}
	
	public boolean contains(Word small) {
		return small.inv.sub(inv);
	}
	
	public boolean equals(Object other) {
		if( other instanceof Word )
			return ((Word)(other)).toString().equals(toString());
		return false;
	}
	
	public int compareTo(Word o) {
		return score - o.score;
	}
	
	public int size() {
		return tileList.size();
	}
	
	public List<Tile> getTileList() {
		return tileList;
	}
	
	public int[] getLetterInventory() {
		return inv.counts;
	}
	
	public String toString() {
		String s = "";
		for(Tile t : tileList)
			s += t.getChar();
		return s;
	}
	
	private class LetterInventory {
		public int[] counts = new int[26];
		
		public LetterInventory(List<Tile> l) {
			for(Tile t : l) 
				counts[t.getChar() - 'A']++;				
		}
		
		public boolean sub(LetterInventory big) {
			for(int i = 0; i < counts.length; i++)
				if( counts[i] > big.counts[i] )
					return false;
			return true;
		}
		
		public String toString() {
			StringBuilder s = new StringBuilder("");
			for(int i = 0; i < counts.length; i++)
				s.append( ((char)('A'+i)) +", "+ counts[i] +"\n");
			return s.toString();
		}
	}
}