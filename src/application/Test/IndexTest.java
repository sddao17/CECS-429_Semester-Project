package application.Test;

import application.documents.DirectoryCorpus;
import application.documents.DocumentCorpus;
import application.indexes.Index;
import application.indexes.Posting;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static application.Application.indexCorpus;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IndexTest {
    @Test
    public void testKanyeCorpus(){
        //inverted index by hand
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
                        add(new Posting(3, new ArrayList<>() {
                            {
                                add(1);
                            }
                        }));
                        add(new Posting(4, new ArrayList<>() {
                            {
                                add(1);
                            }
                        }));
                    }
                });

            }

        };

        //run test corpus through our index
        String directoryPathString = "./corpus/kanye-test";
        String extensionType = ".txt";
        DocumentCorpus testCorpus = DirectoryCorpus.loadTextDirectory(
                Paths.get(directoryPathString).toAbsolutePath(), extensionType);
        Index index = indexCorpus(testCorpus);

        //assertequal index by hand by
        Comparator<List<Posting>> comparator = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                List<Posting> posting1;
                List<Posting> posting2;
                if(o1 instanceof List){
                    posting1 = (List<Posting>) o1;
                } else {
                    return -1;
                }
                if(o2 instanceof List){
                    posting2 = (List<Posting>) o2;
                }
                else {
                    return -1;
                }
                if (posting1.size()< posting2.size()){
                    return -1;
                }
                if (posting1.size()> posting2.size()){
                    return 1;
                }
                for(int i=0; posting1.size()>i; i++){
                    // we're iterating through one posting at a time
                    // store each documentID for readability
                    int leftDocumentId = posting1.get(i).getDocumentId();
                    int rightDocumentId = posting2.get(i).getDocumentId();

                    if (leftDocumentId != rightDocumentId) {
                        return -1;
                    }

                    ArrayList<Integer> leftPositions = posting1.get(i).getPositions();
                    ArrayList<Integer> rightPositions = posting2.get(i).getPositions();
                    if(leftPositions.size()>rightPositions.size()){
                        return 1;
                    }
                    if(leftPositions.size()<rightPositions.size()){
                        return -1;
                    }
                    for(int j =0; leftPositions.size()>j; j++){
                        int leftPosition = leftPositions.get(j);
                        int rightPosition = rightPositions.get(j);

                        if (leftPosition != rightPosition)
                            return -1;
                    }
                }

                return 0;
            }
        };
        for(String term: indexMap.keySet()) {
            //assertEquals(indexMap.get(term), index.getPostings(term));
            int result = comparator.compare(indexMap.get(term), index.getPostings(term));
            assertEquals("Index generated from indexCorpus method should be equal to the index made by hand", 0, result);
        }

    }
}
