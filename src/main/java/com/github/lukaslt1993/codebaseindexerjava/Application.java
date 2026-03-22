package com.github.lukaslt1993.codebaseindexerjava;

import com.github.lukaslt1993.codebaseindexerjava.v1.OnlyNames;
import com.github.lukaslt1993.codebaseindexerjava.v2.NamesPlusSignatures;
import com.github.lukaslt1993.codebaseindexerjava.v3.NamesPlusSignaturesPlusProperties;

import java.io.IOException;

public class Application {

    // Path to the root folder of the project for which you are generating a codebase index
    // After running the program, you will find the generated codebase_index.txt in the same path
    public static final String ROOT = "/Users/yourUser/IdeaProjects/yourProject";

    public enum IndexType {
        NAMES_ONLY,
        NAMES_PLUS_SIGNATURES,
        NAMES_PLUS_SIGNATURES_PLUS_PROPERTIES
    }

    public static IndexType type = IndexType.NAMES_ONLY;

    public static void main(String[] args) throws IOException {
        switch (type) {
            case NAMES_ONLY -> OnlyNames.main(args);
            case NAMES_PLUS_SIGNATURES -> NamesPlusSignatures.main(args);
            case NAMES_PLUS_SIGNATURES_PLUS_PROPERTIES -> NamesPlusSignaturesPlusProperties.main(args);
        }
    }
}
