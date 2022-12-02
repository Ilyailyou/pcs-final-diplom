import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BooleanSearchEngine implements SearchEngine {
    private Map<String, List<PageEntry>> wordCount = new HashMap<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        File[] listOfFiles = pdfsDir.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(".pdf")) {
                var doc = new PdfDocument(new PdfReader(file));
                for (int i = 1; i <= doc.getNumberOfPages(); i++) {
                    PdfPage page = doc.getPage(i);
                    String text = PdfTextExtractor.getTextFromPage(page);
                    String[] words = text.split("\\P{IsAlphabetic}+");
                    Map<String, Integer> freqs = new HashMap<>();
                    for (String word : words) {
                        if (word.isEmpty()) {
                            continue;
                        }
                        word = word.toLowerCase();
                        freqs.put(word, freqs.getOrDefault(word, 0) + 1);
                    }
                    for (Map.Entry<String, Integer> entry : freqs.entrySet()) {
                        if (!wordCount.containsKey(entry.getKey())) {
                            wordCount.put(entry.getKey(), new ArrayList<>());
                        }
                        List<PageEntry> list = wordCount.get(entry.getKey());
                        list.add(new PageEntry(file.getName(), i, entry.getValue()));
                    }
                }
            }
        }
    }

    @Override
    public List<PageEntry> search(String word) {
        return wordCount.get(word.toLowerCase()).stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
    }
}
