
package application.documents;

import java.io.IOException;
import java.io.Reader;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

// TODO:
// Incorporate directory-selection and JSON documents into your application.
public class JsonFileDocument implements FileDocument {

    private final int mDocumentId;
    private final Path mFilePath;
    private String documentTitle;

    public JsonFileDocument(int id, Path absoluteFilePath) {
        mDocumentId = id;
        mFilePath = absoluteFilePath;
        documentTitle = absoluteFilePath.getFileName().toString();
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
        // store a dedicated parser object that can read JSON files
        JSONParser parser = new JSONParser();

        try {
            // use the parser to extract the JSON file at the given path and store it as an object
            Object obj = parser.parse(Files.newBufferedReader(mFilePath));
            JSONObject jsonObject = (JSONObject) obj;

            // using its accessor method, convert the JSON object's fields to a String/StringReader
            documentTitle = (String) jsonObject.get("title");
            return new StringReader((String) jsonObject.get("body"));

        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
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
}
