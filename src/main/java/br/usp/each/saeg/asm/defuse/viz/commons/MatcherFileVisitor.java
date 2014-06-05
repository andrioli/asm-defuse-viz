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
package br.usp.each.saeg.asm.defuse.viz.commons;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class MatcherFileVisitor implements FileVisitor<Path> {

    private final PathMatcherChain include;

    private final PathMatcherChain exclude;

    private final FileVisitor<Path> visitor;

    public MatcherFileVisitor(final PathMatcherChain include, final PathMatcherChain exclude,
            final FileVisitor<Path> visitor) {

        if (include == null) {
            this.include = new PathMatcherChain(PathMatchers.ACCEPT_ALL);
        } else {
            this.include = include;
        }

        if (exclude == null) {
            this.exclude = new PathMatcherChain();
        } else {
            this.exclude = exclude;
        }

        this.visitor = visitor;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
            throws IOException {

        final Path name = dir.getFileName();

        if (exclude.matches(name)) {
            return FileVisitResult.SKIP_SUBTREE;
        }

        return visitor.preVisitDirectory(dir, attrs);
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
            throws IOException {

        final Path name = file.getFileName();

        if (include.matches(name) && !exclude.matches(name)) {
            return visitor.visitFile(file, attrs);
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(final Path file, final IOException exc)
            throws IOException {
        return visitor.visitFileFailed(file, exc);
    }

    @Override
    public FileVisitResult postVisitDirectory(final Path dir, final IOException exc)
            throws IOException {
        return visitor.postVisitDirectory(dir, exc);
    }

}
