package equa.code;

import java.io.Serializable;

public class IndentedLine implements Serializable {

    private static final long serialVersionUID = 1L;
    private String line;
    private final int indentation;

    public IndentedLine(String line, int indentation) {
        this.line = line;
        this.indentation = indentation;
    }

    public String getLine() {
        return line;
    }

    public int getIndentation() {
        return indentation;
    }

    public void addString(String s) {
        line += s;
    }
    
    @Override
    public boolean equals(Object object){
        if (object instanceof IndentedLine){
            IndentedLine indentedLine = (IndentedLine)object;
            return indentedLine.line.equalsIgnoreCase(line);
        } else {
            return false;
        }
    }
}
