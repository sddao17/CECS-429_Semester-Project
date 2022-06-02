package application.Test;

import application.Application;
import application.documents.DirectoryCorpus;
import application.documents.DocumentCorpus;
import application.indexes.Index;
import application.indexes.Posting;
import application.queries.BooleanQueryParser;
import application.queries.QueryComponent;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static application.Application.indexCorpus;
import static org.junit.Assert.assertEquals;

public class QueryProcessingTest {

    HashMap<String, List<Posting>> indexMap = new HashMap<>() {
        {
            // String term, ArrayList postings
            put("350", new ArrayList<>() {
                {
                    // int documentID, ArrayList positions
                    add(new Posting(4, new ArrayList<>() {
                        {
                            // int position
                            add(3);
                        }
                    }));
                }
            });
            put("boost", new ArrayList<>() {
                {
                    add(new Posting(4, new ArrayList<>() {
                        {
                            add(2);
                        }
                    }));
                }
            });


            put("in", new ArrayList<>() {
                {
                    add(new Posting(2, new ArrayList<>() {
                        {
                            add(4);
                        }
                    }));
                }
            });
            put("jump", new ArrayList<>() {
                {
                    add(new Posting(0, new ArrayList<>() {
                        {
                            add(3);
                        }
                    }));
                }
            });
            put("jumpman", new ArrayList<>() {
                {
                    add(new Posting(0, new ArrayList<>() {
                        {
                            add(5);
                        }
                    }));
                }
            });
            put("kany", new ArrayList<>() {
                {
                    add(new Posting(2, new ArrayList<>() {
                        {
                            add(7);
                        }
                    }));
                }
            });
            put("la", new ArrayList<>() {
                {
                    add(new Posting(1, new ArrayList<>() {
                        {
                            add(1);
                        }
                    }));
                }
            });
            put("la", new ArrayList<>() {
                {
                    add(new Posting(2, new ArrayList<>() {
                        {
                            add(5);
                        }
                    }));
                }
            });
            put("more", new ArrayList<>() {
                {
                    add(new Posting(2, new ArrayList<>() {
                        {
                            add(2);
                        }
                    }));
                }
            });
            put("no", new ArrayList<>() {
                {
                    add(new Posting(2, new ArrayList<>() {
                        {
                            add(1);
                        }
                    }));
                }
            });
            put("over", new ArrayList<>() {
                {
                    add(new Posting(0, new ArrayList<>() {
                        {
                            add(4);
                        }
                    }));
                }
            });
            put("parti", new ArrayList<>() {
                {
                    add(new Posting(2, new ArrayList<>() {
                        {
                            add(3);
                        }
                    }));
                }
            });
            put("ram", new ArrayList<>() {
                {
                    add(new Posting(1, new ArrayList<>() {
                        {
                            add(2);
                        }
                    }));
                }
            });

            put("runner", new ArrayList<>() {
                {
                    add(new Posting(3, new ArrayList<>() {
                        {
                            add(2);
                        }
                    }));
                }
            });
            put("west", new ArrayList<>() {
                {
                    add(new Posting(2, new ArrayList<>() {
                        {
                            add(8);
                        }
                    }));
                }
            });
            put("yeezi", new ArrayList<>() {
                {
                    add(new Posting(0, new ArrayList<>() {
                        {
                            add(1);
                            add(2);
                        }
                    }));
                }
            });
            put("yeezi", new ArrayList<>() {
                {
                    add(new Posting(3, new ArrayList<>() {
                        {
                            add(1);
                        }
                    }));
                }
            });
            put("yeezi", new ArrayList<>() {
                {
                    add(new Posting(4, new ArrayList<>() {
                        {
                            add(1);
                        }
                    }));
                }
            });
        }

    };
    String directoryPathString = "./corpus/kanye-test";
    String extensionType = ".txt";
    DocumentCorpus testCorpus = DirectoryCorpus.loadTextDirectory(
            Paths.get(directoryPathString).toAbsolutePath(), extensionType);
    Index index = Application.indexCorpus(testCorpus);

    BooleanQueryParser parser = new BooleanQueryParser();
    @Test
    public void singleQueryTest(){
        String query = "yeezi";
    }

    @Test
    public void andQueryTest(){
        String query = "yeezi 350";
        QueryComponent parsedQuery = parser.parseQuery(query);
        List<Posting> resultPostings = parsedQuery.getPostings(index);
        int queryDocumentId = resultPostings.get(0).getDocumentId();
        assertEquals("The Document ID should be the same.",4, queryDocumentId);
    }

    @Test
    public void orQueryTest(){

    }
}
