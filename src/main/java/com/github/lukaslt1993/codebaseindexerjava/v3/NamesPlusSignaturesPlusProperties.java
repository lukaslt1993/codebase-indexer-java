package com.github.lukaslt1993.codebaseindexerjava.v3;

import com.github.lukaslt1993.codebaseindexerjava.JavaIndexerSupport;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.github.lukaslt1993.codebaseindexerjava.Application.ROOT;

public class NamesPlusSignaturesPlusProperties {

    public static void main(String[] args) {
        try {
            StringBuilder indexBuilder = new StringBuilder();

            JavaIndexerSupport.indexDirectory(new File(ROOT), indexBuilder, NamesPlusSignaturesPlusProperties::indexFile);
            JavaIndexerSupport.writeIndex(indexBuilder);
        } catch (IOException exception) {
            throw new RuntimeException("Failed to build codebase index", exception);
        }
    }

    private static void indexFile(File file, StringBuilder indexBuilder) {
        try {
            if (JavaIndexerSupport.isJavaFile(file)) {
                indexJavaFile(file, indexBuilder);
                return;
            }

            JavaIndexerSupport.indexOtherFile(file, indexBuilder);
        } catch (IOException exception) {
            throw new RuntimeException("Failed to index " + file.getAbsolutePath(), exception);
        }
    }

    private static void indexJavaFile(File file, StringBuilder indexBuilder) throws IOException {
        indexBuilder.append("FILE: ")
                .append(JavaIndexerSupport.relativePath(file))
                .append(System.lineSeparator());
        indexBuilder.append("FIELDS:")
                .append(System.lineSeparator());

        List<String> lines = JavaIndexerSupport.readLines(file);
        int[] indexRef = {0};
        int braceDepth = 0;

        while (indexRef[0] < lines.size()) {
            List<String> annotations = JavaIndexerSupport.collectAnnotations(lines, indexRef);
            if (indexRef[0] >= lines.size()) {
                break;
            }

            String trimmed = lines.get(indexRef[0]).trim();
            if (trimmed.isBlank()) {
                indexRef[0]++;
                continue;
            }

            if (JavaIndexerSupport.isTypeDeclaration(trimmed)) {
                String declaration = JavaIndexerSupport.collectDeclaration(lines, indexRef);
                JavaIndexerSupport.appendDeclaration(indexBuilder, annotations, declaration, "  ");
                braceDepth += JavaIndexerSupport.braceDelta(declaration);
                indexRef[0]++;
                continue;
            }

            if (braceDepth > 0 && JavaIndexerSupport.isFieldDeclaration(trimmed)) {
                String declaration = JavaIndexerSupport.collectDeclaration(lines, indexRef);
                JavaIndexerSupport.appendDeclaration(indexBuilder, annotations, declaration, "    ");
                braceDepth += JavaIndexerSupport.braceDelta(declaration);
                indexRef[0]++;
                continue;
            }

            if (braceDepth > 0 && (JavaIndexerSupport.isConstructorDeclaration(trimmed)
                    || JavaIndexerSupport.isMethodDeclaration(trimmed))) {
                String declaration = JavaIndexerSupport.collectDeclaration(lines, indexRef);
                JavaIndexerSupport.appendDeclaration(indexBuilder, annotations, declaration, "    ");
                braceDepth += JavaIndexerSupport.braceDelta(declaration);
                indexRef[0]++;
                continue;
            }

            braceDepth += JavaIndexerSupport.braceDelta(trimmed);
            indexRef[0]++;
        }

        indexBuilder.append(System.lineSeparator());
    }
}
