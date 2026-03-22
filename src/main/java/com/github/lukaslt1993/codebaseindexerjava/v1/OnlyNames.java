package com.github.lukaslt1993.codebaseindexerjava.v1;

import com.github.lukaslt1993.codebaseindexerjava.JavaIndexerSupport;

import java.io.File;
import java.io.IOException;

import static com.github.lukaslt1993.codebaseindexerjava.Application.ROOT;

public class OnlyNames {

    public static void main(String[] args) throws IOException {
        StringBuilder indexBuilder = new StringBuilder();

        JavaIndexerSupport.indexDirectory(new File(ROOT), indexBuilder, OnlyNames::indexFile);
        JavaIndexerSupport.writeIndex(indexBuilder);
    }

    private static void indexFile(File file, StringBuilder indexBuilder) {
        if (JavaIndexerSupport.isJavaFile(file)) {
            indexBuilder.append("FILE: ")
                    .append(JavaIndexerSupport.relativePath(file))
                    .append(System.lineSeparator());
            return;
        }

        JavaIndexerSupport.indexOtherFile(file, indexBuilder);
    }
}
