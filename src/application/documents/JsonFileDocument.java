
package application.documents;

import java.io.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.nio.file.Files;
import java.nio.file.Path;

// Incorporate directory-selection and JSON documents into your application.
public class JsonFileDocument implements FileDocument, Comparable<Document> {

    private final int mDocumentId;
    private final Path mFilePath;
    private final String documentTitle;

    public JsonFileDocument(int id, Path absoluteFilePath) {
        mDocumentId = id;
        mFilePath = absoluteFilePath;
        documentTitle = (String) getJsonObject().get("title");
    }

    private JSONObject getJsonObject() {
        // store a dedicated parser object that can read JSON files
        JSONParser parser = new JSONParser();
        JSONObject jsonObject;

        try (BufferedReader reader = Files.newBufferedReader(mFilePath)) {
            // use the parser to extract the JSON file at the given path and store it as an object
            Object obj = parser.parse(reader);
            jsonObject = (JSONObject) obj;

        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }

        return jsonObject;
    }

    @Override
    public Path getFilePath() {
        return mFilePath;
    }

    @Override
    public int getId() {
        return mDocumentId;
    }

    @Override
    public Reader getContent() {
        return new StringReader((String) getJsonObject().get("body"));
    }

    @Override
    public String getTitle() {
        return documentTitle;
    }

    /**
     * Analogous function to `loadTextFileDocument()`.
     * @param absolutePath  the absolute path of the file
     * @param documentId    the document ID of the file
     * @return              a new instance of the JSON file document at the absolute path
     */
    public static FileDocument loadJsonFileDocument(Path absolutePath, int documentId) {
        return new JsonFileDocument(documentId, absolutePath);
    }

    @Override
    public int compareTo(Document otherDocument) {
        return documentTitle.compareTo(otherDocument.getTitle());
    }
}
