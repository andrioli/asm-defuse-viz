/**
 * asm-defuse-viz: visualize definition-use-chains
 * Copyright (c) 2014 Roberto Araujo (roberto.andrioli@gmail.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package br.usp.each.saeg.asm.defuse.viz.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

public class SourcePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public static final Color ORANGE_DARK = new Color(250, 100, 30);

    public static final Color ORANGE = new Color(250, 165, 30);

    public static final Color YELLOW = new Color(250, 245, 30);

    private final List<Object> highlights = new LinkedList<>();

    private final RSyntaxTextArea textArea;

    private final RTextScrollPane scrollPane;

    public SourcePanel() {
        textArea = new RSyntaxTextArea();
        textArea.setEditable(false);
        textArea.setHighlightCurrentLine(false);
        textArea.setAutoscrolls(true);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);

        scrollPane = new RTextScrollPane(textArea, true);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setIconRowHeaderEnabled(true);

        setLayout(new BorderLayout());
        add(scrollPane);
    }

    public void setText(final String string) {
        textArea.setText(string);
        textArea.setCaretPosition(0);
    }

    public void highlightLine(final int line, final Color color) throws BadLocationException {
        highlights.add(textArea.addLineHighlight(line - 1, color));
    }

    public void clearHighlights() {
        for (final Object o : highlights) {
            textArea.removeLineHighlight(o);
        }
        highlights.clear();
    }

}
