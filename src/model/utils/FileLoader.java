package model.utils;

import model.FileLoaded;
import model.utils.converters.HtmlToPdfConverter;

import java.io.*;

public abstract class FileLoader {
    public static FileLoaded loadFile(File file) throws IOException {
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        StringBuilder stringBuilder = new StringBuilder();

        String line = bufferedReader.readLine();
        stringBuilder.append(line);
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append("\n");
            stringBuilder.append(line);
        }

        bufferedReader.close();
        fileReader.close();

        return new FileLoaded(file.getName(), file.getPath(), stringBuilder.toString());
    }

    public static void saveFile(FileLoaded fileLoaded) throws IOException {

        String text = fileLoaded.getMarkDownText();
        writeFile(fileLoaded.getFilePath(), text);
    }

    public static void exportFile(FileLoaded fileLoaded, String filePath) throws IOException {
        if (filePath.toLowerCase().endsWith(".pdf")) {
            var file = new File(fileLoaded.getFilePath());
            HtmlToPdfConverter.convertAndSave(fileLoaded.getHtmlText(), filePath);
        } else {
            String text = fileLoaded.getHtmlText();
            writeFile(filePath, text);
        }
    }

    private static void writeFile(String filePath, String text) throws IOException {
        FileWriter fileWriter = new FileWriter(filePath);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        for (var line : text.split("\n")) {
            bufferedWriter.write(line);
            bufferedWriter.newLine();
        }

        bufferedWriter.close();
        fileWriter.close();
    }
}
