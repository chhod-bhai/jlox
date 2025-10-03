package jlox.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <DirectoryName>");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
                "Binary: Expr left, Token operator, Expr right",
                "Grouping: Expr expression",
                "Literal: Object value",
                "Unary: Token operator, Expr right"
        ));
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fields) {
        writer.printf("  static class %s extends %s {\n", className, baseName);
        writer.printf("    %s (%s) {\n", className, fields);
        List<String> fieldsList = Arrays.stream(fields.split(",")).map(String::trim).toList();
        fieldsList.forEach(str -> {
            String fieldName = str.split(" ")[1];
            writer.printf("      this.%s = %s;\n", fieldName, fieldName);
        });
        writer.println("    }");
        writer.println();
        fieldsList.forEach(str -> writer.printf("    %s;\n", str));
        writer.println();
        writer.println("    @Override");
        writer.println("    <R> R accept(Visitor<R> visitor) {");
        writer.printf("      return visitor.visit%s%s(this);\n", className, baseName);
        writer.println("    }");
        writer.println("  }");


    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = String.format("%s/%s.java", outputDir, baseName);
        PrintWriter writer = new PrintWriter(path, Charset.defaultCharset());
        writer.println("package jlox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println("abstract class " + baseName + " {");
        defineVisitor(writer, baseName, types);
        writer.println();
        writer.println("  abstract <R> R accept(Visitor<R> visitor);");
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }
        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("  interface Visitor<R> {");
        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.printf("    R visit%s%s(%s %s);\n", typeName, baseName, typeName, baseName.toLowerCase());
        }
        writer.println("  }");
    }
}
