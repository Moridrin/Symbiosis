package equa.factbreakdown.gui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

public class AutoCompleteListener implements DocumentListener {

    private final List<String> words;
    // private Mode mode;
    private JTextField textField;

    public AutoCompleteListener(JTextField textField, List<String> words) {
        this.textField = textField;
        this.words = words;
        //    this.mode = Mode.INSERT;
    }

    @Override
    public void changedUpdate(DocumentEvent ev) {
    }

    @Override
    public void removeUpdate(DocumentEvent ev) {
    }

    @Override
    public void insertUpdate(DocumentEvent ev) {
        if (ev.getLength() != 1) {
            return;
        }

        int pos = ev.getOffset();
        String content = null;
        try {
            content = textField.getText(0, pos/* +1 */);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

//        // Find where the word starts
//        int w;
//        for (w = pos; w >= 0; w--) {
//            if (!Character.isLetter(content.charAt(w))) {
//                break;
//            }
//        }
//        if (pos - w < 2) {
//            // Too few chars
//            return;
//        }
        String prefix = content.trim();
        if (prefix.length() < 2) {
            return;
        }

        //String prefix = content.substring(w + 1).toLowerCase();
        int n = Collections.binarySearch(words, prefix, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        if (n < 0) {
            /* prefix is not found; 
             * if prefix is inserted in words at index -n, then the list stays ascending 
             */
            String match = words.get(-n - 1);
            if (match.toLowerCase().startsWith(prefix)) {
                // A completion is found
                String completion = match.substring(prefix.length());
                // We cannot modify Document from within notification,
                // so we submit a task that does the change later
                SwingUtilities.invokeLater(
                        new CompletionTask(completion, pos));
            }
        } else {
            // Nothing found
            //  mode = Mode.INSERT;
        }
    }

    private class CompletionTask implements Runnable {

        String completion;
        int position;

        CompletionTask(String completion, int position) {
            this.completion = completion;
            this.position = position;
        }

        @Override
        public void run() {
            try {
                textField.getDocument().insertString(position, completion, null);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            textField.setCaretPosition(position + completion.length());
            //textField.moveCaretPosition(position);
            //   mode = Mode.COMPLETION;
        }
    }
}
