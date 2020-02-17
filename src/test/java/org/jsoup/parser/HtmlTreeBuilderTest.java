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
