
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
    HashMap<String, List<Posting>> indexMap = new HashMap<>() {
        {
            // String term, ArrayList postings
            put("350", new ArrayList<>() {
                {
                    // int documentID, ArrayList positions
                    add(new Posting(1, new ArrayList<>() {
                        {
                            // int position
                            add(3);
                        }
                    }));
                }
            });
            put("boost", new ArrayList<>() {
                {
                    add(new Posting(1, new ArrayList<>() {
                        {
                            add(2);
                        }
                    }));
                }
            });


            put("in", new ArrayList<>() {
                {
                    add(new Posting(0, new ArrayList<>() {
                        {
                            add(4);
                        }
                    }));
                }
            });
            put("jump", new ArrayList<>() {
                {
                    add(new Posting(4, new ArrayList<>() {
                        {
                            add(3);
                        }
                    }));
                }
            });
            put("jumpman", new ArrayList<>() {
                {
                    add(new Posting(4, new ArrayList<>() {
                        {
                            add(5);
                        }
                    }));
                }
            });
            put("kany", new ArrayList<>() {
                {
                    add(new Posting(0, new ArrayList<>() {
                        {
                            add(7);
                        }
                    }));
                }
            });
            put("la", new ArrayList<>() {
                {
                    add(new Posting(3, new ArrayList<>() {
                        {
                            add(1);
                        }
                    }));
                    add(new Posting(0, new ArrayList<>() {
                        {
                            add(5);
                        }
                    }));
                }
            });
            put("more", new ArrayList<>() {
                {
                    add(new Posting(0, new ArrayList<>() {
                        {
                            add(2);
                        }
                    }));
                }
            });
            put("no", new ArrayList<>() {
                {
                    add(new Posting(0, new ArrayList<>() {
                        {
                            add(1);
                        }
                    }));
                }
            });
            put("over", new ArrayList<>() {
                {
                    add(new Posting(4, new ArrayList<>() {
                        {
                            add(4);
                        }
                    }));
                }
            });
            put("parti", new ArrayList<>() {
                {
                    add(new Posting(0, new ArrayList<>() {
                        {
                            add(3);
                        }
                    }));
                }
            });
            put("ram", new ArrayList<>() {
                {
                    add(new Posting(3, new ArrayList<>() {
                        {
                            add(2);
                        }
                    }));
                }
            });

            put("runner", new ArrayList<>() {
                {
                    add(new Posting(2, new ArrayList<>() {
                        {
                            add(4);
                        }
                    }));
                }
            });
            put("wave", new ArrayList<>() {
                {
                    add(new Posting(2, new ArrayList<>() {
                        {
                            add(3);
                        }
                    }));
                }
            });
            put("west", new ArrayList<>() {
                {
                    add(new Posting(0, new ArrayList<>() {
                        {
                            add(7);
                        }
                    }));
                }
            });
            put("yeezi", new ArrayList<>() {
                {
                    add(new Posting(1, new ArrayList<>() {
                        {
                            add(1);
                        }
                    }));
                    add(new Posting(2, new ArrayList<>() {
                        {
                            add(2);
                        }
                    }));
                    add(new Posting(4, new ArrayList<>() {
                        {
                            add(1);
                            add(2);
                        }
                    }));
                }
            });
        }
    };

    private boolean comparePostings(List<Posting> leftList, List<Posting> rightList) {
        // return false if they don't have the same number of postings
        if (leftList.size() != rightList.size()) {
            System.err.println("Left Posting list size (Expected): " + leftList.size() +
                    "\nRight Posting list size (Actual): " + rightList.size());
            return false;
        }

        // compare left positions to right positions
        for (Posting leftPosting : leftList) {
            // store values for readability
            ArrayList<Integer> leftPositions = leftPosting.getPositions();
            boolean found = false;

            for (Posting rightPosting : rightList) {
                ArrayList<Integer> rightPositions = rightPosting.getPositions();
                found = leftPositions.equals(rightPositions);

                // stop checking the left list against the rest of right lists if we've found a match
                if (found) {
                    break;
                }
            }

            // if we've checked all right lists and the left list was not found, return false
            if (!found) {
                System.err.println("Positions list was not found: " + leftPositions);
                return false;
            }
        }

        // if we've reached this point, the positions lists match
        return true;
    }

    @Test
    public void testYeezyPositions() {
        boolean positionsMatch = comparePostings(indexMap.get("yeezi"), index.getPostings("yeezi"));

        assertTrue("Postings should be the same between the handmade index and the actual index.", positionsMatch);
    }

    @Test
    public void testLAPositions() {
        boolean positionsMatch = comparePostings(indexMap.get("la"), index.getPostings("la"));

        assertTrue("Postings should be the same between the handmade index and the actual index.", positionsMatch);
    }

    @Test
    public void testWavePositions() {
        boolean positionsMatch = comparePostings(indexMap.get("wave"), index.getPostings("wave"));

        assertTrue("Postings should be the same between the handmade index and the actual index.", positionsMatch);
    }

    @Test
    public void testKanyePositions() {
        boolean positionsMatch = comparePostings(indexMap.get("kany"), index.getPostings("kany"));

        assertTrue("Postings should be the same between the handmade index and the actual index.", positionsMatch);
    }

    @Test
    public void testJumpPositions() {
        boolean positionsMatch = comparePostings(indexMap.get("jump"), index.getPostings("jump"));

        assertTrue("Postings should be the same between the handmade index and the actual index.", positionsMatch);
    }
}
