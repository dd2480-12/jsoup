package org.jsoup.parser;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.jsoup.nodes.Element;

public class HtmlTreeBuilderTest {
    @Test
    public void ensureSearchArraysAreSorted() {
        String[][] arrays = {
            HtmlTreeBuilder.TagsSearchInScope,
            HtmlTreeBuilder.TagSearchList,
            HtmlTreeBuilder.TagSearchButton,
            HtmlTreeBuilder.TagSearchTableScope,
            HtmlTreeBuilder.TagSearchSelectScope,
            HtmlTreeBuilder.TagSearchEndTags,
            HtmlTreeBuilder.TagSearchSpecial
        };

        for (String[] array : arrays) {
            String[] copy = Arrays.copyOf(array, array.length);
            Arrays.sort(array);
            assertArrayEquals(array, copy);
        }
    }
    
    @Test
    public void stateSetToInColumnGroup() {
    	HtmlTreeBuilder htb = new HtmlTreeBuilder();
    	ArrayList<Element> stack = new ArrayList<>(2);
    	Element node = new Element("p");
    	Element node2 = new Element("colgroup");
    	stack.add(node);
    	stack.add(node2);
    	htb.stack = stack;
    	htb.resetInsertionMode();
    	assertEquals(htb.state(), HtmlTreeBuilderState.InColumnGroup);
    }
    
    @Test
    public void stateSetToInTable() {
    	HtmlTreeBuilder htb = new HtmlTreeBuilder();
    	ArrayList<Element> stack = new ArrayList<>(2);
    	Element node = new Element("p");
    	Element node2 = new Element("table");
    	stack.add(node);
    	stack.add(node2);
    	htb.stack = stack;
    	htb.resetInsertionMode();
    	assertEquals(htb.state(), HtmlTreeBuilderState.InTable);
    }
}
