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
package br.usp.each.saeg.asm.defuse.viz;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;

import br.usp.each.saeg.asm.defuse.viz.TreeBuilder.DUA;
import br.usp.each.saeg.asm.defuse.viz.commons.MatcherFileVisitor;
import br.usp.each.saeg.asm.defuse.viz.commons.PathMatcherChain;
import br.usp.each.saeg.asm.defuse.viz.commons.PathMatchers;
import br.usp.each.saeg.asm.defuse.viz.swing.ExplorerPanel;
import br.usp.each.saeg.asm.defuse.viz.swing.ExplorerTreeNode;
import br.usp.each.saeg.asm.defuse.viz.swing.NodeType;
import br.usp.each.saeg.asm.defuse.viz.swing.SourcePanel;

public class SourceViz extends JFrame implements TreeSelectionListener {

    private static final long serialVersionUID = 1L;

    private final PathMatcherChain include = new PathMatcherChain(PathMatchers.get("glob:*.class"));

    private final ExplorerPanel pkgPanel;

    private final SourcePanel srcPanel;

    private Path current;

    public SourceViz(final Project project) throws IOException {
        super("Source-Viz");

        final Path rootPath = project.getRootPath().getFileName();
        final ExplorerTreeNode node = new ExplorerTreeNode(rootPath, NodeType.PROJECT);
        final TreeBuilder visitor = new TreeBuilder(node, project);

        for (final Path path : project.getClassPaths()) {
            Files.walkFileTree(path, new MatcherFileVisitor(include, null, visitor));
        }

        // Setting up package explorer panel
        pkgPanel = new ExplorerPanel(node);
        pkgPanel.addTreeSelectionListener(this);

        // Setting up source panel
        srcPanel = new SourcePanel();

        add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pkgPanel, srcPanel));
    }

    // -----------------------------
    // TreeSelectionListener methods

    @Override
    public void valueChanged(final TreeSelectionEvent e) {
        final ExplorerTreeNode node = pkgPanel.getLastSelectedPathComponent();
        final Path src = node.getSource();
        if (src != null && !src.equals(current)) {
            try {
                srcPanel.setText(new String(Files.readAllBytes(src)));
            } catch (final IOException ignore) {
            }
            current = src;
        }
        srcPanel.clearHighlights();
        if (node.getType() == NodeType.DEFUSE) {
            final DUA dua = (DUA) node.getUserObject();
            try {
                if (dua.def == dua.use) {
                    srcPanel.highlightLine(dua.def, SourcePanel.ORANGE_DARK);
                } else {
                    srcPanel.highlightLine(dua.def, SourcePanel.ORANGE);
                    srcPanel.highlightLine(dua.use, SourcePanel.YELLOW);
                }
            } catch (final BadLocationException ignore) {
            }
        }
    }

}