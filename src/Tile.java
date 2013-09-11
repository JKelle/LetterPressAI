import java.awt.Color;

public class Tile {
	public static Color darkRed = new Color(153, 0, 0);
	public static Color red = new Color(255, 0, 51);
	public static Color white = Color.white;
	public static Color blue = new Color(0, 102, 255);
	public static Color darkBlue = new Color(0, 0, 152);
	
	private Color color;
	private char c;
	private int[] loc;
	
	/*	-2 is dark red
	 * 	-1 is red
	 * 	0 is neutral
	 * 	1 is blue
	 * 	2 is dark blue
	 */
	private int colorVal;
	
	public Tile(char letter) {
		c = letter;
		setVal(0);
	}
	public Tile(char letter, int i, int j) {
		this(letter);
		loc = new int[]{i,j};
	}
	public Tile(Tile t) {
		this(t.getChar(), t.getLoc()[0], t.getLoc()[1]);
		setVal(t.colorVal);
	}
	
	public char getChar() {
		return c;
	}
	public Color getColor() {
		return color;
	}
	public int[] getLoc() {
		return loc;
	}
	public int getVal() {
		return colorVal;
	}
	public void setVal(int v) {
		if( v < -2 || v > 2 )
			throw new IllegalArgumentException("Invalid colorValue");
		colorVal = v;
		switch(v) {
			case -2: color = darkRed; break;
			case -1: color = red; break;
			case  0: color = white; break;
			case  1: color = blue; break;
			case  2: color = darkBlue; break;
		}
	}
	
	public boolean equals( Object other ) {
		if( other != null && other instanceof Tile ) {
			Tile o = (Tile)(other);
			return c == o.c && colorVal == o.colorVal;
		}
		return false;
	}
	
	public String toString() {
		if( loc == null )
			return "["+ (c+"") +" (null, null)] ";
		return "["+ (c+"") +" ("+ loc[0] +","+ loc[1] +")] ";
	}
}
