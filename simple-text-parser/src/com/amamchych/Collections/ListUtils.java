package com.amamchych.Collections;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

public class ListUtils {

    /**
     * Selects all elements, what equal to given argument listElement on given JList.
     * @param list JList instance.
     * @param listElement elements, which must be selected.
     * @param indexesOfDuplicates in this list indexes of selected elements will be added. This argument can be null. 
     */
    public static void selectAllElementsLike(JList list, Object listElement,
            List<Integer> indexesOfDuplicates) {
        DefaultListModel model = (DefaultListModel) list.getModel();
        ListSelectionModel sm = list.getSelectionModel();
        int lastIndex = 0;
        for (Object element : model.toArray()) {
            if (element.equals(listElement)){
                int idx = model.indexOf(element, lastIndex);
                if (idx+1 < model.size()) {
                    lastIndex = idx+1; //possible Ex here
                }
                sm.addSelectionInterval(idx, idx);
                if (indexesOfDuplicates != null) {
                    indexesOfDuplicates.add(idx);
                }
            }
        }
    }

    /**
     * Selects all duplicate elements on given JList.
     * @param list JList instance.
     * @return Set<Object> set with unique elements. 
     */
    public static Set<Object> highlightDuplicates(JList list) {
        Set<Object> listElements = new HashSet<Object>();

        DefaultListModel model = (DefaultListModel) list.getModel();
        ListSelectionModel sm = list.getSelectionModel();
        sm.clearSelection();
        model.trimToSize();
        for (Object element : model.toArray()) {
            if (listElements.contains(element)) {
                selectAllElementsLike(list, element.toString(), null);
            } else {
                listElements.add(element);
            }
        }
        return listElements;
    }
}
