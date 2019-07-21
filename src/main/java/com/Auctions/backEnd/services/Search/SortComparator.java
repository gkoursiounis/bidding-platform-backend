package com.Auctions.backEnd.services.Search;

import com.Auctions.backEnd.models.Item;

import java.util.Comparator;
import java.util.Map;

public class SortComparator implements Comparator<Item> {
    private final Map<Item, Integer> freqMap;

    // Assign the specified map
    public SortComparator(Map<Item, Integer> tFreqMap)
    {
        this.freqMap = tFreqMap;
    }

    // Compare the values
    @Override
    public int compare(Item k1, Item k2)
    {

        // Compare value by frequency
        int freqCompare = freqMap.get(k2).compareTo(freqMap.get(k1));

        // Compare value if frequency is equal
        int valueCompare = k1.compareTo(k2);

        // If frequency is equal, then just compare by value, otherwise -
        // compare by the frequency.
        if (freqCompare == 0)
            return valueCompare;
        else
            return freqCompare;
    }
}
