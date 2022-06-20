
    private static final int VOCABULARY_PRINT_SIZE = 1_000; // number of vocabulary terms to print
    private static final int MAX_DISPLAYED_RANKED_ENTRIES = 10;  // the maximum number of ranked entries to display

    private static CorpusSelection cSelect;
    private static DirectoryCorpus corpus;  // we need only one of each corpus and index active at a time,
    private static Index<String, Posting> corpusIndex;  // and multiple methods need access to them
    private static KGramIndex kGramIndex = new KGramIndex();
    private static BiwordIndex biwordIndex = new BiwordIndex();
    private static final List<Double> lds = new ArrayList<>();    // the values representing document weights

    public static boolean enabledLogs = false;
    public static final List<Closeable> closeables = new ArrayList<>(); // considers all cases of indexing

    public static void main(String[] args) {
        System.out.printf("""
                %nCopy/paste for testing:
                ./corpus/parks
                ./corpus/parks-test
                ./corpus/kanye-test
                ./corpus/moby-dick%n""");
        startApplication();

        //cSelect = new CorpusSelection();
        //cSelect.CorpusSelectionUI();
    }

    private static void startApplication() {
        Scanner in = new Scanner(System.in);
        closeables.add(in);

        String input = Menu.showBuildOrQueryIndexMenu(in);

        /* 1. At startup, ask the user for the name of a directory that they would like to index,
          and construct a DirectoryCorpus from that directory. */
        String directoryString = promptCorpusDirectory(in);
        Map<String, String> indexPaths = PostingUtility.createIndexPathsMap(directoryString);

        // depending on the user's input, either build the index from scratch or read from an on-disk index
        switch (input) {
            case "1" -> initializeComponents(indexPaths);
            case "2" -> readFromComponents(indexPaths);
        }

        input = Menu.showBooleanOrRankedMenu(in);

        String queryMode = switch (input) {
            case "1" -> "boolean";
            case "2" -> "ranked";
            default -> throw new RuntimeException("Unexpected input: " + input);
        };

        startQueryLoop(in, queryMode);
        closeOpenFiles();
    }

    private static String promptCorpusDirectory(Scanner in) {
        System.out.print("\nEnter the path of the directory corpus:\n >> ");
        String directoryString = in.nextLine();
        corpus = DirectoryCorpus.loadDirectory(Path.of(directoryString));

        return directoryString;
    }

    private static void initializeComponents(Map<String, String> indexPaths) {
        corpusIndex = indexCorpus(corpus);

        DiskIndexWriter.createIndexDirectory(indexPaths.get("indexDirectory"));
        System.out.println("\nWriting files to index directory...");

        // write the documents weights to disk
        DiskIndexWriter.writeLds(indexPaths.get("docWeightsBin"), lds);
        System.out.println("Document weights written to `" + indexPaths.get("docWeightsBin") + "` successfully.");

        // write the postings using the corpus index to disk
        List<Integer> bytePositions = DiskIndexWriter.writeIndex(indexPaths.get("postingsBin"), corpusIndex);
        System.out.println("Postings written to `" + indexPaths.get("postingsBin") + "` successfully.");

        // write the B+ tree mappings of term -> byte positions to disk
        DiskIndexWriter.writeBTree(indexPaths.get("bTreeBin"), corpusIndex.getVocabulary(), bytePositions);
        System.out.println("B+ Tree written to `" + indexPaths.get("bTreeBin") + "` successfully.");

        // write the k-grams to disk
        DiskIndexWriter.writeKGrams(indexPaths.get("kGramsBin"), kGramIndex);
        System.out.println("K-Grams written to `" + indexPaths.get("kGramsBin") + "` successfully.");

        //write the biword index to disk
        DiskIndexWriter.writeBiword(indexPaths.get("biwordBin"), biwordIndex);
        System.out.println("Biword index written to `" + indexPaths.get("BiwordBin") + "` successfully.");

        //write a biword b-tre
        // after writing the components to disk, we can terminate the program
        System.exit(0);
    }

    private static void readFromComponents(Map<String, String> indexPaths) {
        System.out.println("\nReading from the on-disk index...");

        // initialize the DiskPositionalIndex and k-grams using pre-constructed indexes on disk
        corpusIndex = new DiskPositionalIndex(indexPaths.get("bTreeBin"), indexPaths.get("postingsBin"));
        kGramIndex = DiskIndexReader.readKGrams(indexPaths.get("kGramsBin"));
        //biwordIndex = DiskIndexReader.readBiwords(indexPaths.get("biwordBin"));
        DocumentWeightScorer.setRandomAccessor(indexPaths.get("docWeightsBin"));

        // if we're reading from disk using DiskPositionalIndex, then we know it is Closeable
        closeables.add((Closeable) corpusIndex);

        System.out.printf("""
                Reading complete.
                
                Found %s documents.
                Distinct k-grams: %s
                """, corpus.getCorpusSize(), kGramIndex.getDistinctKGrams().size());
    }

    public static Index<String, Posting> indexCorpus(DocumentCorpus corpus) {
        /* 2. Index all documents in the corpus to build a positional inverted index.
          Print to the screen how long (in seconds) this process takes. */
        System.out.println("\nIndexing...");
        long startTime = System.nanoTime();

        PositionalInvertedIndex index = new PositionalInvertedIndex();
        VocabularyTokenProcessor vocabProcessor = new VocabularyTokenProcessor();
        WildcardTokenProcessor wildcardProcessor = new WildcardTokenProcessor();

        // scan all documents and process each token into terms of our vocabulary
        for (Document document : corpus.getDocuments()) {
            // at the beginning of each document reading, the position always starts at 1
            int currentPosition = 1;
            Map<String, Integer> tftds = new HashMap<>();

            try (Reader documentContent = document.getContent();
                 EnglishTokenStream stream = new EnglishTokenStream(documentContent)) {
                Iterable<String> tokens = stream.getTokens();

                for (String token : tokens) {
                    // before we normalize the token, add it to a minimally processed vocabulary for wildcards
                    List<String> wildcardTokens = wildcardProcessor.processToken(token);

                    // add each unprocessed token to our k-gram index as we traverse through the documents
                    kGramIndex.buildKGramIndex(wildcardTokens, 3);

                    // process the vocabulary token before evaluating whether it exists within our index
                    List<String> terms = vocabProcessor.processToken(token);

                    // since each token can produce multiple terms, add all terms using the same documentID and position
                    for (String term : terms) {
                        index.addTerm(term, document.getId(), currentPosition);
                        biwordIndex.addTerm(term, document.getId());

                        // build up L(d) for the current document
                        if (tftds.get(term) == null) {
                            tftds.put(term, 1);
                        } else {
                            int oldTftd = tftds.get(term);
                            tftds.replace(term, oldTftd + 1);
                        }
                    }
                    // after each token addition, update the position count
                    ++currentPosition;
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // after processing all tokens into terms, calculate L(d) for the document and add it to our list
            lds.add(DocumentWeightScorer.calculateLd(tftds));
        }

        long endTime = System.nanoTime();
        double timeElapsedInSeconds = (double) (endTime - startTime) / 1_000_000_000;
        System.out.printf("""
                Indexing complete.
                Time elapsed: %s seconds
                
                Found %s documents.
                Distinct k-grams: %s
                """, timeElapsedInSeconds, corpus.getCorpusSize(), kGramIndex.getDistinctKGrams().size());


        return index;
    }

    private static void startQueryLoop(Scanner in, String queryMode) {
        String query;

        do {
            // 3a. Ask for a search query.
            System.out.print("\nEnter the query (`:?` to list special commands):\n >> ");
            query = in.nextLine();
            String[] splitQuery = query.split(" ");

            // skip empty input
            if (splitQuery.length > 0) {
                String potentialCommand = splitQuery[0];
                String parameter = "";
                if (splitQuery.length > 1) {
                    parameter = splitQuery[1];
                }
                // check if the user enabled printing logs to console
                if (splitQuery[splitQuery.length - 1].equals("--log")) {
                    enabledLogs = true;
                    query = query.substring(0, query.lastIndexOf(" --log"));
                }

                // 3(a, i). If it is a special query, perform that action.
                switch (potentialCommand) {
                    case ":index" -> initializeComponents(PostingUtility.createIndexPathsMap(parameter));
                    case ":stem" -> {
                        TokenStemmer stemmer = new TokenStemmer();
                        System.out.println(parameter + " -> " + stemmer.processToken(parameter).get(0));
                    }
                    case ":vocab" -> {
                        List<String> vocabulary = corpusIndex.getVocabulary();
                        int vocabularyPrintSize = Math.min(vocabulary.size(), VOCABULARY_PRINT_SIZE);

                        for (int i = 0; i < vocabularyPrintSize; ++i) {
                            System.out.println(vocabulary.get(i));
                        }
                        if (vocabulary.size() > VOCABULARY_PRINT_SIZE) {
                            System.out.println("...");
                        }
                        System.out.println("Found " + vocabulary.size() + " terms.");
                    }
                    case ":kgrams" -> {
                        List<String> vocabulary = kGramIndex.getVocabulary();
                        int vocabularyPrintSize = Math.min(vocabulary.size(), VOCABULARY_PRINT_SIZE);

                        for (int i = 0; i < vocabularyPrintSize; ++i) {
                            String currentType = vocabulary.get(i);
                            System.out.println(currentType + " -> " + kGramIndex.getPostings(currentType));
                        }
                        if (vocabulary.size() > VOCABULARY_PRINT_SIZE) {
                            System.out.println("...");
                        }
                        System.out.println("Found " + vocabulary.size() + " types.");
                    }
                    case ":?" ->
                        System.out.printf("""
                            %nSpecial Commands:
                            :index `directory-name`  --  Index the folder at the specified path.
                                      :stem `token`  --  Stem, then print the token string.
                                             :vocab  --  Print the first %s terms in the vocabulary of the corpus,
                                                         then print the total number of vocabulary terms.
                                            :kgrams  --  Print the first %s k-gram mappings of vocabulary types to
                                                         k-gram tokens, then print the total number of vocabulary types.
                                      `query` --log  --  Enable printing a debugging log to the console before printing
                                                         the query results.
                                                 :q  --  Exit the program.
                            """, VOCABULARY_PRINT_SIZE, VOCABULARY_PRINT_SIZE);
                    case ":q", "" -> {}
                    default -> {
                        int numOfResults;
                        switch (queryMode) {
                            case "boolean" -> numOfResults = displayBooleanResults(query);
                            case "ranked" -> numOfResults = displayRankedResults(query);
                            default -> numOfResults = 0;
                        }

                        if (numOfResults > 0) {
                            PostingUtility.promptForDocumentContent(in, corpus);
                        }
                    }
                }
            }
            enabledLogs = false;
        } while (!query.equals(":q"));
    }

    private static int displayBooleanResults(String query) {
        // 3(a, ii). If it isn't a special query, then parse the query and retrieve its postings.
        BooleanQueryParser parser = new BooleanQueryParser();
        QueryComponent parsedQuery = parser.parseQuery(query);
        TokenProcessor processor = new VocabularyTokenProcessor();

        List<Posting> resultPostings = parsedQuery.getPostings(corpusIndex, processor);
        // in case the query contains wildcards, only display each unique posting once
        if (parsedQuery instanceof WildcardLiteral) {
            resultPostings = PostingUtility.getDistinctPostings(resultPostings);
        }
        PostingUtility.displayPostings(corpus, resultPostings);

        return resultPostings.size();
    }

    private static int displayRankedResults(String query) {
        DocumentWeightScorer documentScorer = new DocumentWeightScorer();
        closeables.add(documentScorer);

        documentScorer.storeTermAtATimeDocuments(corpusIndex, query);
        List<Map.Entry<Integer, Double>> rankedEntries = documentScorer.getRankedEntries(MAX_DISPLAYED_RANKED_ENTRIES);

        if (rankedEntries.size() > 0) {
            for (Map.Entry<Integer, Double> entry : rankedEntries) {
                int currentDocumentId = entry.getKey();
                double score = entry.getValue();

                DecimalFormat decimalFormat = new DecimalFormat("###.######");
                System.out.printf("- " + corpus.getDocument(currentDocumentId).getTitle() +
                        " (ID: " + currentDocumentId + ") -- " + decimalFormat.format(score) + "\n");
            }
        } else {
            System.out.println("Found 0 documents that matched the query.");
        }

        return rankedEntries.size();
    }

    private static void closeOpenFiles() {
        // close all open file resources case-by-case
        for (Closeable stream : closeables) {
            try {
                stream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static DirectoryCorpus getCorpus() {
        return corpus;
    }

    public static Index<String, String> getKGramIndex() {
        return kGramIndex;
    }
}

