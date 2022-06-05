
package application.Test;

import application.documents.DirectoryCorpus;
import application.documents.DocumentCorpus;
import application.indexes.Index;
import application.indexes.Posting;
import org.junit.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static application.Application.indexCorpus;
import static org.junit.Assert.assertTrue;

public class IndexTest {
    // run test corpus through our index
    Path directoryPath = Path.of("./corpus/kanye-test");
    DocumentCorpus testCorpus = DirectoryCorpus.loadDirectory(directoryPath);
    Index<String, Posting> index = indexCorpus(testCorpus, 3);
    //inverted index by hand
    HashMap<String, List<Posting>> indexMap;
    int oneDocId, twoDocId, threeDocId, fourDocId, fiveDocId;

    @Test
    public void testYeezyPositions() {
        setupKanyeCorpus();
        boolean positionsMatch = comparePostings(index.getPostings("yeezi"), (indexMap.get("yeezi")));

        assertTrue("Postings should be the same between the handmade index and the actual index.", positionsMatch);
    }

    @Test
    public void testLAPositions() {
        setupKanyeCorpus();
        boolean positionsMatch = comparePostings(index.getPostings("la"), (indexMap.get("la")));

        assertTrue("Postings should be the same between the handmade index and the actual index.", positionsMatch);
    }

    @Test
    public void testWavePositions() {
        setupKanyeCorpus();
        boolean positionsMatch = comparePostings(index.getPostings("wave"), (indexMap.get("wave")));

        assertTrue("Postings should be the same between the handmade index and the actual index.", positionsMatch);
    }

    @Test
    public void testKanyePositions() {
        setupKanyeCorpus();
        boolean positionsMatch = comparePostings(index.getPostings("kany"), (indexMap.get("kany")));

        assertTrue("Postings should be the same between the handmade index and the actual index.", positionsMatch);
    }

    @Test
    public void testJumpPositions() {
        setupKanyeCorpus();
        boolean positionsMatch = comparePostings(index.getPostings("jump"), (indexMap.get("jump")));

        assertTrue("Postings should be the same between the handmade index and the actual index.", positionsMatch);
    }

    public void setupKanyeCorpus() {
        for(int i = 0; i < 5; i++) {
            String title = testCorpus.getDocument(i).getTitle();
            switch (title) {
                case "one.txt" -> oneDocId = i;
                case "two.txt" -> twoDocId = i;
                case "three.txt" -> threeDocId = i;
                case "four.txt" -> fourDocId = i;
                case "five.txt" -> fiveDocId = i;
            }
        }

        //inverted index by hand
        indexMap = new HashMap<>() {
            {
                // String term, ArrayList postings
                put("350", new ArrayList<>() {
                    {
                        // int documentID, ArrayList positions
                        add(new Posting(twoDocId, new ArrayList<>() {
                            {
                                // int position
                                add(3);
                            }
                        }));
                    }
                });
                put("boost", new ArrayList<>() {
                    {
                        add(new Posting(twoDocId, new ArrayList<>() {
                            {
                                add(2);
                            }
                        }));
                    }
                });


                put("in", new ArrayList<>() {
                    {
                        add(new Posting(oneDocId, new ArrayList<>() {
                            {
                                add(4);
                            }
                        }));
                    }
                });
                put("jump", new ArrayList<>() {
                    {
                        add(new Posting(fiveDocId, new ArrayList<>() {
                            {
                                add(3);
                            }
                        }));
                    }
                });
                put("jumpman", new ArrayList<>() {
                    {
                        add(new Posting(fiveDocId, new ArrayList<>() {
                            {
                                add(5);
                            }
                        }));
                    }
                });
                put("kany", new ArrayList<>() {
                    {
                        add(new Posting(oneDocId, new ArrayList<>() {
                            {
                                add(7);
                            }
                        }));
                    }
                });
                put("la", new ArrayList<>() {
                    {
                        add(new Posting(fourDocId, new ArrayList<>() {
                            {
                                add(1);
                            }
                        }));
                        add(new Posting(oneDocId, new ArrayList<>() {
                            {
                                add(5);
                            }
                        }));
                    }
                });
                put("more", new ArrayList<>() {
                    {
                        add(new Posting(oneDocId, new ArrayList<>() {
                            {
                                add(2);
                            }
                        }));
                    }
                });
                put("no", new ArrayList<>() {
                    {
                        add(new Posting(oneDocId, new ArrayList<>() {
                            {
                                add(1);
                            }
                        }));
                    }
                });
                put("over", new ArrayList<>() {
                    {
                        add(new Posting(fiveDocId, new ArrayList<>() {
                            {
                                add(4);
                            }
                        }));
                    }
                });
                put("parti", new ArrayList<>() {
                    {
                        add(new Posting(oneDocId, new ArrayList<>() {
                            {
                                add(3);
                            }
                        }));
                    }
                });
                put("ram", new ArrayList<>() {
                    {
                        add(new Posting(fourDocId, new ArrayList<>() {
                            {
                                add(2);
                            }
                        }));
                    }
                });

                put("runner", new ArrayList<>() {
                    {
                        add(new Posting(threeDocId, new ArrayList<>() {
                            {
                                add(4);
                            }
                        }));
                    }
                });
                put("wave", new ArrayList<>() {
                    {
                        add(new Posting(threeDocId, new ArrayList<>() {
                            {
                                add(3);
                            }
                        }));
                    }
                });
                put("west", new ArrayList<>() {
                    {
                        add(new Posting(oneDocId, new ArrayList<>() {
                            {
                                add(7);
                            }
                        }));
                    }
                });
                put("yeezi", new ArrayList<>() {
                    {
                        add(new Posting(twoDocId, new ArrayList<>() {
                            {
                                add(1);
                            }
                        }));
                        add(new Posting(threeDocId, new ArrayList<>() {
                            {
                                add(2);
                            }
                        }));
                        add(new Posting(fiveDocId, new ArrayList<>() {
                            {
                                add(1);
                                add(2);
                            }
                        }));
                    }
                });
            }
        };
    }

    private boolean comparePostings(List<Posting> leftList, List<Posting> rightList) {
        // return false if they don't have the same number of postings
        if (leftList.size() != rightList.size()) {
            System.out.println(leftList.size() + "\n" + rightList.size());
            return false;
        }

        // compare postings by index number
        for (int i = 0; i < leftList.size(); ++i) {
            ArrayList<Integer> leftPositions = leftList.get(i).getPositions();
            ArrayList<Integer> rightPositions = rightList.get(i).getPositions();
            int leftPositionSize = leftPositions.size();
            int rightPositionSize = rightPositions.size();

            // immediately return false if the positions don't match
            if (leftPositionSize != rightPositionSize) {
                System.out.println(leftPositions + "\n" + rightPositions);
                return false;
            }

            // compare positions by index number
            for (int j = 0; j < leftPositionSize; ++j) {
                int currentLeftPosition = leftPositions.get(j);
                int currentRightPosition = rightPositions.get(j);

                if (currentLeftPosition != currentRightPosition) {
                    System.out.println(leftPositions + "\n" + rightPositions);
                    return false;
                }
            }
        }

        // if we've reached this point, the positions lists match
        return true;
    }

}
