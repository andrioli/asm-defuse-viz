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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Project {

    private final Path root;

    private final List<Path> classes;

    private final List<Path> sources;

    public Project(final Path root, final List<Path> classes, final List<Path> sources) {

        Objects.requireNonNull(root);
        Objects.requireNonNull(classes);
        Objects.requireNonNull(sources);

        if (!root.isAbsolute()) {
            throw new IllegalArgumentException(String.format(
                    "'%s' should be an absolute directory", root));
        }

        if (!Files.isDirectory(root)) {
            throw new IllegalArgumentException(String.format("'%s' should be a directory", root));
        }

        this.root = root;
        this.classes = absolutefy(classes);
        this.sources = absolutefy(sources);
    }

    private List<Path> absolutefy(final List<Path> paths) {
        final List<Path> result = new ArrayList<Path>(paths.size());
        for (Path path : paths) {
            if (path.isAbsolute()) {
                if (!path.startsWith(root)) {
                    throw new IllegalArgumentException(String.format(
                            "'%s' should be relative to '%s'", path, root));
                }
            } else {
                path = root.resolve(path);
                if (!Files.exists(path)) {
                    throw new IllegalArgumentException(String.format(
                            "'%s' should be a relative to '%s'", path, root));
                }
            }
            if (!Files.isDirectory(path)) {
                throw new IllegalArgumentException(String.format(
                        "'%s' should be a valid directory", path));
            }
            result.add(path);
        }
        return result;
    }

    public Path getRootPath() {
        return root;
    }

    public List<Path> getClassPaths() {
        return Collections.unmodifiableList(classes);
    }

    public List<Path> getSourcePaths() {
        return Collections.unmodifiableList(sources);
    }

}
