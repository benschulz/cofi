package de.benshu.cofi.cofic.frontend;

import de.benshu.cofi.types.tags.IndividualTag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Currage {
    public static final IndividualTag<Currage> TAG = IndividualTag.named("Currage");

    private static final List<Currage> instances = new ArrayList<>(Arrays.asList((Currage) null));
    private static volatile int size = 1;

    public static Currage getInstance(int currage) {
        if (currage < 1) {
            throw new IllegalArgumentException(Integer.toString(currage));
        }

        if (size <= currage) {
            synchronized (instances) {
                while (size <= currage) {
                    instances.add(new Currage(size));
                    ++size;
                }
            }
        }

        return instances.get(currage);
    }

    private int currage;

    private Currage(int currage) {
        this.currage = currage;
    }
}
