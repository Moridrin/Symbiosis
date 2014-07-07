package equa.code;

import java.util.Iterator;
import java.util.LinkedList;

public class IndentedList extends LinkedList<IndentedLine> {

	private static final long serialVersionUID = 1L;
	private int currentIndentation = 0;
	
	/**
	 * Adds the line without changing the indentation.
	 * 
	 * @param line
	 */
	public void add(String line) {
		add(new IndentedLine(line, currentIndentation));
	}

	/**
	 * Adds a line with changing the indentation
	 * 
	 * @param line
	 * @param indent
	 *            if true then adds to the indentation AFTERWORDS if false
	 *            subtracts from the indentation IMMEDIATELY.
	 * @param indentAgain
	 *            if indent is false and this is true then it indents again.
	 */
	public void add(String line, boolean indent, boolean indentAgain) {
		if (indent) {
			add(new IndentedLine(line, currentIndentation));
			currentIndentation++;
		} else {
			currentIndentation--;
			add(new IndentedLine(line, currentIndentation));
			if (indentAgain) {
				currentIndentation++;
			}
		}
	}

	/**
	 * See {@LINK #addLine(String, boolean, boolean)}
	 * 
	 * @param line
	 * @param indent
	 */
	public void add(String line, boolean indent) {
		add(line, indent, false);
	}

	/**
	 * Adds all lines in the indentedList with respecting the indentation.
	 * 
	 * @param list
	 *            not null
	 */
	public void add(IndentedList list) {
		Iterator<IndentedLine> lines = list.iterator();
		while (lines.hasNext()) {
			IndentedLine line = lines.next();
			add(new IndentedLine(line.getLine(), line.getIndentation() + currentIndentation));
		}
		currentIndentation += list.currentIndentation;
	}

	/**
	 * 
	 * @return The indented lines.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Iterator<IndentedLine> lines = iterator();
		while (lines.hasNext()) {
			IndentedLine line = lines.next();
			if (!line.getLine().equals("")) {
				for (int i = 0; i < line.getIndentation(); i++) {
					sb.append("\t");
				}
			}
			sb.append(line.getLine());
			if (lines.hasNext()) {
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	/**
	 * Adds the string to the last indented line.
	 * 
	 * @param string
	 */
	public void addString(String string) {
		IndentedLine line = getLast();
		line.addString(string);
	}

	/**
	 * Creates an indentedlist from a string. The indents must either be a \t or 4 spaces.
	 * Anything else will not be recognized.
	 * @param s the input
	 * @return IndentedList with indentation based on the input
	 */
	public static IndentedList fromString(String s, int startPoint) {
		IndentedList list = new IndentedList();
		String[] lines = s.split("\n");
		for (String line : lines) {
			int cSpatie = countOccurrenceStart(line, "    ");
			int cTab = countOccurrenceStart(line, "\t");
			if (cSpatie > cTab) {
				line = line.substring(cSpatie * 4);
			} else {
				line = line.substring(cTab * 1);
			}
			list.add(new IndentedLine(line, Math.max(cSpatie, cTab) + startPoint));
		}
		list.currentIndentation = list.getLast().getIndentation();
		return list;
	}
	
	/**
	 * Counts the occurrences from a certain string match in string s, from the beginning
	 * So (jkjkjjk, jk) will return 2
	 * @param s
	 * @param match
	 * @return the number of occurrences
	 */
	private static int countOccurrenceStart(String s, String match) {
		int result = 0;
		for (int i = 0; true; i += match.length()) {
			try {
				if (s.substring(i, i + match.length()).equals(match)) {
					result++;
				} else {
					return result;
				}
			} catch (StringIndexOutOfBoundsException e) {
				return result;
			}
		}
	}
}
