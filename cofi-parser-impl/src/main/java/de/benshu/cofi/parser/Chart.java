package de.benshu.cofi.parser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class Chart implements Iterable<Chart.Entry> {
    public static Chart create(final int index) {
        return new Chart(index);
    }

    private final Map<Entry, Entry> entries = new HashMap<>();
    private final int index;

    private Chart(final int index) {
        this.index = index;
    }

    public boolean add(Entry entry) {
        final Entry old = entries.put(entry, entry);

        if (old != null) {
            if (old.relationship != EntryRelationship.PREDICTION) {
                switch (entry.relationship) {
                    case INITIALIZATION:
                    case PREDICTION:
                        throw new AssertionError();
                    case RECONSTRUCTION:
                    case SCANNING:
                        if (old.relationship != entry.relationship || !old.parent.equals(entry.parent)) {
                            throw new AmbiguityException(entry, old);
                        }
                        break;
                    default:
                        throw new AssertionError();
                }
            }
        }

        return old == null;
    }

    public Entry createFinderEntry(Entry entry, int dot) {
        return new Entry(entry.rule, dot, entry.prediction, null, null);
    }

    public Entry createInitEntry(Rule dummyStartRule) {
        return new Entry(dummyStartRule, 0, 0, null, EntryRelationship.INITIALIZATION);
    }

    public Entry createPredictionEntry(Entry parent, Rule rule, int index) {
        return new Entry(rule, 0, index, parent, EntryRelationship.PREDICTION);
    }

    public Entry createReconstructionEntry(Entry prediction, Entry parent) {
        if (parent.prediction != prediction.getChart().getIndex()) {
            throw new AssertionError();
        }

        return new Entry(prediction.rule, prediction.dot + 1, prediction.prediction, parent,
                EntryRelationship.RECONSTRUCTION);
    }

    public Entry createScanningEntry(Entry parent) {
        return new Entry(parent.rule, parent.dot + 1, parent.prediction, parent, EntryRelationship.SCANNING);
    }

    public Entry getEntry(Entry finder) {
        return entries.get(finder);
    }

    public int getIndex() {
        return index;
    }

    @Override
    public Iterator<Entry> iterator() {
        final Entry[] entries = this.entries.keySet().toArray(new Entry[this.entries.size()]);

        return new Iterator<Entry>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < entries.length;
            }

            @Override
            public Entry next() {
                return entries[i++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public String toString() {
        return entries.keySet().toString();
    }

    public class Entry {
        public final Rule rule;
        public final int dot;
        public final int prediction;
        private final int hash;

        private final Entry parent;
        private final EntryRelationship relationship;

        public Entry(Rule rule, int dot, int prediction, Entry parent, EntryRelationship relationship) {
            this.rule = rule;
            this.dot = dot;
            this.prediction = prediction;
            this.hash = calcHash(rule, dot, prediction);
            this.parent = parent;
            this.relationship = relationship;
        }

        private int calcHash(Rule rule, int dot, int prediction) {
            final int prime = 31;
            int hash = 1;
            hash = prime * hash + rule.hashCode();
            hash = prime * hash + dot;
            hash = prime * hash + prediction;
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Entry) {
                final Entry oe = (Entry) o;
                return oe.rule.equals(rule) && oe.dot == dot && oe.prediction == prediction;
            }

            return false;
        }

        public Chart getChart() {
            return Chart.this;
        }

        public Entry getParent() {
            return parent;
        }

        public Symbol getPredicted() {
            return rule.production.get(dot);
        }

        public EntryRelationship getRelationship() {
            return relationship;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        public boolean isCompleted() {
            return rule.production.size() == dot;
        }

        @Override
        public String toString() {
            return "(" + rule + ", " + dot + ", " + prediction + ", " + relationship + ")";
        }

        public Constructed construct(Object tree) {
            return new Constructed(tree);
        }

        public class Constructed {
            private final Object tree;

            public Constructed(Object tree) {
                this.tree = tree;
            }

            public Object getTree() {
                return tree;
            }

            public Entry getChartEntry() {
                return Entry.this;
            }
        }
    }

    public enum EntryRelationship {
        PREDICTION,
        INITIALIZATION,
        RECONSTRUCTION,
        SCANNING
    }
}
