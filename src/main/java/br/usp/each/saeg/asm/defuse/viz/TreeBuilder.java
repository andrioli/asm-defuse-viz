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
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import br.usp.each.saeg.asm.defuse.DefUseAnalyzer;
import br.usp.each.saeg.asm.defuse.DefUseChain;
import br.usp.each.saeg.asm.defuse.DepthFirstDefUseChainSearch;
import br.usp.each.saeg.asm.defuse.Field;
import br.usp.each.saeg.asm.defuse.Local;
import br.usp.each.saeg.asm.defuse.Variable;
import br.usp.each.saeg.asm.defuse.viz.swing.ExplorerTreeNode;
import br.usp.each.saeg.asm.defuse.viz.swing.NodeType;

public class TreeBuilder implements FileVisitor<Path> {

    private ExplorerTreeNode node;

    private String className;

    private String classPackage;

    private Path sourcePath;

    private final Project project;

    public TreeBuilder(final ExplorerTreeNode node, final Project project) {
        this.node = node;
        this.project = project;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
            throws IOException {

        ExplorerTreeNode n;
        if (project.getClassPaths().contains(dir)) {
            n = new ExplorerTreeNode(dir.getFileName(), NodeType.SRC_FOLDER);
        } else {
            n = new ExplorerTreeNode(dir.getFileName(), NodeType.PACKAGE);
        }
        node.add(n);
        node = n;
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
            throws IOException {

        final ClassReader classReader = new ClassReader(Files.readAllBytes(file));
        classReader.accept(new TreeBuilderClassVisitor(), ClassReader.SKIP_FRAMES);

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(final Path file, final IOException exc)
            throws IOException {

        throw exc;
    }

    @Override
    public FileVisitResult postVisitDirectory(final Path dir, final IOException exc)
            throws IOException {

        if (exc != null) {
            throw exc;
        }
        node = (ExplorerTreeNode) node.getParent();
        return FileVisitResult.CONTINUE;
    }

    private class TreeBuilderClassVisitor extends ClassVisitor {

        public TreeBuilderClassVisitor() {
            super(Opcodes.ASM4);
        }

        @Override
        public void visit(final int version, final int access, final String name,
                final String signature, final String superName, final String[] interfaces) {

            className = name;
            classPackage = "";
            final int i = name.lastIndexOf('/');
            if (i != -1) {
                classPackage = className.substring(0, i);
                className = className.substring(i + 1);
            }

            final ExplorerTreeNode n = new ExplorerTreeNode(className, NodeType.CLASS);
            node.add(n);
            node = n;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public void visitSource(final String source, final String debug) {
            if (source != null) {
                for (Path path : project.getSourcePaths()) {
                    path = path.resolve(classPackage).resolve(source);
                    if (Files.exists(path)) {
                        sourcePath = path;
                        break;
                    }
                }
            }
            node.setSource(sourcePath);
        }

        @Override
        public MethodVisitor visitMethod(final int access, final String name, final String desc,
                final String signature, final String[] exceptions) {

            return new TreeBuilderMethodVisitor(access, name, desc, signature, exceptions);
        }

        @Override
        public void visitEnd() {
            node = (ExplorerTreeNode) node.getParent();
            className = null;
            classPackage = null;
            sourcePath = null;
        }

    }

    private class TreeBuilderMethodVisitor extends MethodNode implements Opcodes {

        private final DefUseAnalyzer analyzer = new DefUseAnalyzer();
        private final DepthFirstDefUseChainSearch dfducs = new DepthFirstDefUseChainSearch();

        private int[] lines;

        public TreeBuilderMethodVisitor(final int access, final String name, final String desc,
                final String signature, final String[] exceptions) {

            super(access, name, desc, signature, exceptions);

            final NodeType type;
            switch (access & (ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED)) {
            case ACC_PUBLIC:
                type = NodeType.METHOD_PUBLIC;
                break;
            case ACC_PRIVATE:
                type = NodeType.METHOD_PRIVATE;
                break;
            case ACC_PROTECTED:
                type = NodeType.METHOD_PROTECTED;
                break;
            default:
                type = NodeType.METHOD_DEFAULT;
                break;
            }

            final ExplorerTreeNode n = new ExplorerTreeNode(name + desc, type);
            n.setSource(sourcePath);
            node.add(n);
            node = n;
        }

        @Override
        public void visitEnd() {
            lines = new int[instructions.size()];
            for (int i = 0; i < lines.length; i++) {
                if (instructions.get(i) instanceof LineNumberNode) {
                    final LineNumberNode insn = (LineNumberNode) instructions.get(i);
                    lines[instructions.indexOf(insn.start)] = insn.line;
                }
            }
            int line = 1;
            for (int i = 0; i < lines.length; i++) {
                if (lines[i] == 0)
                    lines[i] = line;
                else
                    line = lines[i];
            }

            DefUseChain[] duas;
            Variable[] vars;
            try {
                analyzer.analyze(className, this);

                // find all definition-use chains
                duas = dfducs.search(analyzer.getDefUseFrames(), analyzer.getVariables(),
                        analyzer.getSuccessors(), analyzer.getPredecessors());

                // only global definition-use chains
                duas = DefUseChain.globals(duas, analyzer.getLeaders(), analyzer.getBasicBlocks());

                vars = analyzer.getVariables();
            } catch (final AnalyzerException ignore) {
                duas = new DefUseChain[0];
                vars = new Variable[0];
            }

            for (final DefUseChain dua : duas) {
                final Variable var = vars[dua.var];
                final int def = lines[dua.def];
                final int use = lines[dua.use];
                String name;
                if (var instanceof Field) {
                    name = ((Field) var).name;
                } else {
                    try {
                        name = varName(dua.def, ((Local) var).var);
                    } catch (final Exception e) {
                        name = var.toString();
                    }
                }
                final ExplorerTreeNode n = new ExplorerTreeNode(new DUA(def, use, name),
                        NodeType.DEFUSE);
                n.setSource(sourcePath);
                node.add(n);
            }

            node = (ExplorerTreeNode) node.getParent();
        }

        private String varName(final int insn, final int index) {
            for (final LocalVariableNode local : localVariables) {
                if (local.index == index) {
                    final int start = instructions.indexOf(local.start);
                    final int end = instructions.indexOf(local.end);
                    if (insn + 1 >= start && insn + 1 <= end) {
                        return local.name;
                    }
                }
            }
            throw new RuntimeException("Variable not found");
        }

    }

    public static class DUA {

        public final int def;
        public final int use;
        public final String var;

        public DUA(final int def, final int use, final String var) {
            this.def = def;
            this.use = use;
            this.var = var;
        }

        @Override
        public String toString() {
            return String.format("(%d , %d, %s)", def, use, var);
        }

    }

}
