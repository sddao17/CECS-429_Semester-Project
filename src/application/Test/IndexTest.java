package application.Test;

import application.documents.DirectoryCorpus;
import application.documents.DocumentCorpus;
import application.indexes.Index;
import application.indexes.PositionalInvertedIndex;
import application.indexes.Posting;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.ArrayList;

import static application.Application.indexCorpus;
import static junit.framework.Assert.assertEquals;


public class IndexTest {
    @Test
    public void testKanyeCorpus(){
        //inverted index by hand
        HashMap indexMap = new HashMap<>() {
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
                put("for", new ArrayList<>() {
                    {
                        add(new Posting(1, new ArrayList<>() {
                            {
                                add(3);
                            }
                        }));
                    }
                });
                put("front", new ArrayList<>() {
                    {
                        add(new Posting(1, new ArrayList<>() {
                            {
                                add(1);
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
                                add(2);
                            }
                        }));
                    }
                });
                put("jumpman", new ArrayList<>() {
                    {
                        add(new Posting(0, new ArrayList<>() {
                            {
                                add(4);
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
                                add(4);
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
                                add(3);
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
                                add(5);
                            }
                        }));
                    }
                });
                put("row", new ArrayList<>() {
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

        //run test corpus through index
        String directoryPathString = "./corpus/kanye-test";
        String extensionType = ".txt";
        DocumentCorpus testCorpus = DirectoryCorpus.loadTextDirectory(
                Paths.get(directoryPathString).toAbsolutePath(), extensionType);
        Index index = indexCorpus(testCorpus);

        for(String term: indexMap.keySet()) {
            assertEquals(indexMap.get(term), index.getPostings(term));
        }




        //assertequal index by hand by

    }
}
