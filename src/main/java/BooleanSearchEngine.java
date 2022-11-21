import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BooleanSearchEngine implements SearchEngine {
    private Map<String, List<PageEntry>> wordCount = new HashMap<>();
    private File pdfsDir;
    private Set<String> stopRu = new HashSet<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        this.pdfsDir = pdfsDir;
        try (Scanner scanner = new Scanner(new File("stop-ru.txt"))) {
            while (scanner.hasNext()) {
                stopRu.add(scanner.nextLine());
            }
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }
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
        String[] requestedWords = word.split("\\P{IsAlphabetic}+");
        if (requestedWords.length == 1) {
            return wordCount.get(word.toLowerCase()).stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        } else {
            List<PageEntry> pageEntries = new ArrayList<>();
            File[] listOfFiles = pdfsDir.listFiles();
            for (File file : listOfFiles) {
                if (file.isFile() && file.getName().endsWith(".pdf")) {
                    try (var doc = new PdfDocument((new PdfReader(file)))) {
                        for (int i = 1; i <= doc.getNumberOfPages(); i++) {
                            PdfPage page = doc.getPage(i);
                            String text = PdfTextExtractor.getTextFromPage(page);
                            String[] words = text.split("\\P{IsAlphabetic}+");
                            Map<String, Integer> freqs = new HashMap<>();
                            for (String oneWord : words) {
                                if (oneWord.isEmpty()) {
                                    continue;
                                }
                                oneWord = oneWord.toLowerCase();
                                freqs.put(oneWord, freqs.getOrDefault(oneWord, 0) + 1);
                            }
                            int count = 0;
                            for (String requestedWord : requestedWords) {
                                if (freqs.containsKey(requestedWord) && !stopRu.contains(requestedWord)) {
                                    count += freqs.get(requestedWord);
                                }
                            }
                            if (count != 0) {
                                pageEntries.add(new PageEntry(file.getName(), i, count));
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return pageEntries;
        }
    }
}
