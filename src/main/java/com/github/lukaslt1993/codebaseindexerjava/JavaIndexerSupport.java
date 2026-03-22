package com.github.lukaslt1993.codebaseindexerjava;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.github.lukaslt1993.codebaseindexerjava.Application.ROOT;

public final class JavaIndexerSupport {

    private static final Pattern TYPE_DECLARATION_PATTERN = Pattern.compile(
            "^(?:(?:public|protected|private|abstract|final|static|sealed|non-sealed|strictfp)\\s+)*" +
                    "(class|interface|enum|record)\\b"
    );
    private static final Pattern METHOD_DECLARATION_PATTERN = Pattern.compile(
            "^(?:(?:public|protected|private|static|final|abstract|synchronized|native|default|strictfp)\\s+)+" +
                    "(?:<[^>]+>\\s+)?[\\w.$\\[\\]<>?,@]+\\s+\\w+\\s*\\("
    );
    private static final Pattern CONSTRUCTOR_DECLARATION_PATTERN = Pattern.compile(
            "^(?:(?:public|protected|private)\\s+)+[A-Z][A-Za-z0-9_]*\\s*\\("
    );
    private static final Pattern FIELD_DECLARATION_PATTERN = Pattern.compile(
            "^(?:(?:public|protected|private|static|final|transient|volatile)\\s+)+" +
                    "[\\w.$\\[\\]<>?,@]+\\s+\\w+\\s*(?:=.+)?;"
    );

    private JavaIndexerSupport() {
    }

    public static void indexDirectory(File dir, StringBuilder indexBuilder, FileHandler fileHandler) throws IOException {
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        try (Stream<Path> files = Files.walk(dir.toPath())) {
            files.map(Path::toFile)
                    .filter(File::isFile)
                    .filter(file -> !shouldSkip(file))
                    .sorted(Comparator.comparing(File::getAbsolutePath))
                    .forEach(file -> fileHandler.handle(file, indexBuilder));
        }
    }

    public static boolean shouldSkip(File file) {
        String path = file.getPath();
        return path.contains("/build/")
                || path.contains("/out/")
                || path.contains("/target/")
                || path.contains("/generated/")
                || path.contains("/.git/")
                || path.contains("/.idea/");
    }

    public static void writeIndex(StringBuilder indexBuilder) throws IOException {
        Files.writeString(new File(ROOT, "codebase_index.txt").toPath(), indexBuilder.toString());
    }

    public static String relativePath(File file) {
        return new File(ROOT).toPath().relativize(file.toPath()).toString();
    }

    public static void indexOtherFile(File file, StringBuilder indexBuilder) {
        String fileName = file.getName();
        String extension = extensionOf(fileName);
        if (List.of("xml", "md", "yml", "yaml", "properties").contains(extension)
                || "Dockerfile".equals(fileName)) {
            indexBuilder.append("CONFIG/OTHER FILE: ")
                    .append(relativePath(file))
                    .append(System.lineSeparator());
        }
    }

    public static boolean isJavaFile(File file) {
        return file.getName().endsWith(".java");
    }

    public static List<String> readLines(File file) throws IOException {
        return Files.readAllLines(file.toPath());
    }

    public static List<String> collectAnnotations(List<String> lines, int[] indexRef) {
        List<String> annotations = new ArrayList<>();
        while (indexRef[0] < lines.size()) {
            String trimmed = lines.get(indexRef[0]).trim();
            if (!trimmed.startsWith("@")) {
                break;
            }
            annotations.add(trimmed);
            indexRef[0]++;
        }
        return annotations;
    }

    public static String collectDeclaration(List<String> lines, int[] indexRef) {
        StringBuilder declaration = new StringBuilder(lines.get(indexRef[0]).trim());
        while (indexRef[0] + 1 < lines.size() && !endsDeclaration(declaration.toString())) {
            indexRef[0]++;
            declaration.append(System.lineSeparator())
                    .append("    ")
                    .append(lines.get(indexRef[0]).trim());
        }
        return declaration.toString();
    }

    public static boolean isTypeDeclaration(String line) {
        return TYPE_DECLARATION_PATTERN.matcher(line).find();
    }

    public static boolean isConstructorDeclaration(String line) {
        return CONSTRUCTOR_DECLARATION_PATTERN.matcher(line).find();
    }

    public static boolean isMethodDeclaration(String line) {
        return METHOD_DECLARATION_PATTERN.matcher(line).find();
    }

    public static boolean isFieldDeclaration(String line) {
        return FIELD_DECLARATION_PATTERN.matcher(line).find();
    }

    public static void appendDeclaration(StringBuilder indexBuilder, List<String> annotations, String declaration, String indent) {
        for (String annotation : annotations) {
            indexBuilder.append(indent)
                    .append(annotation)
                    .append(System.lineSeparator());
        }

        String[] declarationLines = declaration.split("\\R");
        for (String declarationLine : declarationLines) {
            indexBuilder.append(indent)
                    .append(declarationLine)
                    .append(System.lineSeparator());
        }
    }

    public static int braceDelta(String declaration) {
        int delta = 0;
        for (int i = 0; i < declaration.length(); i++) {
            char current = declaration.charAt(i);
            if (current == '{') {
                delta++;
            } else if (current == '}') {
                delta--;
            }
        }
        return delta;
    }

    private static boolean endsDeclaration(String declaration) {
        return declaration.contains("{") || declaration.trim().endsWith(";") || declaration.contains(")");
    }

    private static String extensionOf(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex >= 0 ? fileName.substring(dotIndex + 1) : "";
    }

    @FunctionalInterface
    public interface FileHandler {
        void handle(File file, StringBuilder indexBuilder);
    }
}
