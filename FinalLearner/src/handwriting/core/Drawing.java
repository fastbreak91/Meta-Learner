package handwriting.core;
public class Drawing {
	private boolean[][] image;
	
	private void init(int width, int height) {
		image = new boolean[width][height];
	}
	
	public Drawing(int width, int height) {
		init(width, height);
	}
	
	public Drawing(String encoded) {
		String[] tokens = encoded.split("\\|");
		int width = Integer.parseInt(tokens[0]);
		int height = Integer.parseInt(tokens[1]);
		init(width, height);
		

		for (int t = 2, x = 0; t < tokens.length; ++t, ++x) {
			String line = tokens[t];
			for (int y = 0; y < line.length(); ++y) {
				set(x, y, line.charAt(y) == 'X');
			}
		}
	}
	
	public int getWidth() {
		return image.length;
	}
	
	public int getHeight() {
		return image[0].length;
	}
	
	public boolean inBounds(int x, int y) {
		return x >= 0 && x < getWidth() && y >= 0 && y < getHeight();
	}
	
	public void set(int x, int y, boolean on) {
		if (inBounds(x, y)) {
			image[x][y] = on;
		}
	}
	
	public boolean isSet(int x, int y) {
		return image[x][y];
	}
	
	public void clear() {
		for (int x = 0; x < getWidth(); ++x) {
			for (int y = 0; y < getHeight(); ++y) {
				set(x, y, false);
			}
		}
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(getWidth());
		result.append("|");
		result.append(getHeight());
		result.append("|");
		for (int x = 0; x < getWidth(); ++x) {
			for (int y = 0; y < getHeight(); ++y) {
				result.append(image[x][y] ? 'X' : 'O');
			}
			result.append("|");
		}
		return result.toString();
	}
	
	public int hashCode() {return toString().hashCode();}
	
	public boolean equals(Object other) {
		return this.toString().equals(other.toString());
	}
}
