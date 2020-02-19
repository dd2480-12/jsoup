package org.jsoup.parser;

import org.jsoup.nodes.Element;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

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
    
    // add one more test to improve the coverage
    @Test
    public void stateSetToInFrameset() {
    	HtmlTreeBuilder htb = new HtmlTreeBuilder();
    	ArrayList<Element> stack = new ArrayList<>(2);
    	Element node = new Element("p");
    	Element node2 = new Element("frameset");
    	stack.add(node);
    	stack.add(node2);
    	htb.stack = stack;
    	htb.resetInsertionMode();
    	assertEquals(htb.state(), HtmlTreeBuilderState.InFrameset);
    }
    
    /**
     * If "html" is a node in Stack, htb.state() should be "BeforeHead"
     */
    @Test
    public void stateSetToInBeforeHead() {
    	HtmlTreeBuilder htb = new HtmlTreeBuilder();
    	ArrayList<Element> stack = new ArrayList<>(2);
    	Element node = new Element("p");
    	Element node2 = new Element("html");
    	stack.add(node);
    	stack.add(node2);
    	htb.stack = stack;
    	htb.resetInsertionMode();
    	assertEquals(htb.state(), HtmlTreeBuilderState.BeforeHead);
    }
    
    /**
     * Checks that the test correctly transitions to the last element in the stack. 
     */
    @Test
    public void treeCorrectlyTransitionsToLastElement() {
        HtmlTreeBuilder tree = new HtmlTreeBuilder();
        tree.setContextElement(new Element("select"));

        ArrayList<Element> stack = new ArrayList<>();
        stack.add(new Element("dummy"));
        tree.stack = stack;

        tree.resetInsertionMode();
        assertEquals(tree.state(), HtmlTreeBuilderState.InSelect);
    }

    @Test
    public void treeCorrectlyTransitionsToElements() {
        HtmlTreeBuilder tree = new HtmlTreeBuilder();
        ArrayList<Element> stack = new ArrayList<>();
        stack.add(new Element("dummy"));
        tree.stack = stack;

        Element[] elements = { new Element("select"), new Element("caption"), new Element("tr") };
        HtmlTreeBuilderState[] states = { HtmlTreeBuilderState.InSelect, HtmlTreeBuilderState.InCaption,
                HtmlTreeBuilderState.InRow };

        for (int i = 0; i < elements.length; i++) {
            tree.setContextElement(elements[i]);
            tree.resetInsertionMode();
            assertEquals(tree.state(), states[i]);
        }
    }
}
