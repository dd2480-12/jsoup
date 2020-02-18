package org.jsoup.select;

import org.jsoup.internal.StringUtil;
import org.jsoup.helper.Validate;
import org.jsoup.parser.TokenQueue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jsoup.internal.Normalizer.normalize;

/**
 * Parses a CSS selector into an Evaluator tree.
 */
public class QueryParser {
    private final static String[] combinators = {",", ">", "+", "~", " "};
    private static final String[] AttributeEvals = new String[]{"=", "!=", "^=", "$=", "*=", "~="};

    private TokenQueue tq;
    private String query;
    private List<Evaluator> evals = new ArrayList<>();

    public static boolean[] coverage = new boolean[29];
    /**
     * Create a new QueryParser.
     * @param query CSS query
     */
    private QueryParser(String query) {
        Validate.notEmpty(query);
        query = query.trim();
        this.query = query;
        this.tq = new TokenQueue(query);
    }

    /**
     * Parse a CSS query into an Evaluator.
     * @param query CSS query
     * @return Evaluator
     */
    public static Evaluator parse(String query) {
        try {
            QueryParser p = new QueryParser(query);
            return p.parse();
        } catch (IllegalArgumentException e) {
            throw new Selector.SelectorParseException(e.getMessage());
        }
    }

    /**
     * Parse the query
     * @return Evaluator
     */
    Evaluator parse() {
        tq.consumeWhitespace();

        if (tq.matchesAny(combinators)) { // if starts with a combinator, use root as elements
            evals.add(new StructuralEvaluator.Root());
            combinator(tq.consume());
        } else {
            findElements();
        }

        while (!tq.isEmpty()) {
            // hierarchy and extras
            boolean seenWhite = tq.consumeWhitespace();

            if (tq.matchesAny(combinators)) {
                combinator(tq.consume());
            } else if (seenWhite) {
                combinator(' ');
            } else { // E.class, E#id, E[attr] etc. AND
                findElements(); // take next el, #. etc off queue
            }
        }

        if (evals.size() == 1)
            return evals.get(0);

        return new CombiningEvaluator.And(evals);
    }

    private void combinator(char combinator) {
        tq.consumeWhitespace();
        String subQuery = consumeSubQuery(); // support multi > childs

        Evaluator rootEval; // the new topmost evaluator
        Evaluator currentEval; // the evaluator the new eval will be combined to. could be root, or rightmost or.
        Evaluator newEval = parse(subQuery); // the evaluator to add into target evaluator
        boolean replaceRightMost = false;

        if (evals.size() == 1) {
            rootEval = currentEval = evals.get(0);
            // make sure OR (,) has precedence:
            if (rootEval instanceof CombiningEvaluator.Or && combinator != ',') {
                currentEval = ((CombiningEvaluator.Or) currentEval).rightMostEvaluator();
                replaceRightMost = true;
            }
        }
        else {
            rootEval = currentEval = new CombiningEvaluator.And(evals);
        }
        evals.clear();

        // for most combinators: change the current eval into an AND of the current eval and the new eval
        if (combinator == '>')
            currentEval = new CombiningEvaluator.And(newEval, new StructuralEvaluator.ImmediateParent(currentEval));
        else if (combinator == ' ')
            currentEval = new CombiningEvaluator.And(newEval, new StructuralEvaluator.Parent(currentEval));
        else if (combinator == '+')
            currentEval = new CombiningEvaluator.And(newEval, new StructuralEvaluator.ImmediatePreviousSibling(currentEval));
        else if (combinator == '~')
            currentEval = new CombiningEvaluator.And(newEval, new StructuralEvaluator.PreviousSibling(currentEval));
        else if (combinator == ',') { // group or.
            CombiningEvaluator.Or or;
            if (currentEval instanceof CombiningEvaluator.Or) {
                or = (CombiningEvaluator.Or) currentEval;
                or.add(newEval);
            } else {
                or = new CombiningEvaluator.Or();
                or.add(currentEval);
                or.add(newEval);
            }
            currentEval = or;
        }
        else
            throw new Selector.SelectorParseException("Unknown combinator: " + combinator);

        if (replaceRightMost)
            ((CombiningEvaluator.Or) rootEval).replaceRightMostEvaluator(currentEval);
        else rootEval = currentEval;
        evals.add(rootEval);
    }

    private String consumeSubQuery() {
        StringBuilder sq = StringUtil.borrowBuilder();
        while (!tq.isEmpty()) {
            if (tq.matches("("))
                sq.append("(").append(tq.chompBalanced('(', ')')).append(")");
            else if (tq.matches("["))
                sq.append("[").append(tq.chompBalanced('[', ']')).append("]");
            else if (tq.matchesAny(combinators))
                break;
            else
                sq.append(tq.consume());
        }
        return StringUtil.releaseBuilder(sq);
    }

    private void covered(int i) {
        coverage[i] = true;
    }

    private void findElements() {
        if (tq.matchChomp("#")) {
            covered(0);
            byId();
        }
        else if (tq.matchChomp(".")){
            covered(1);
            byClass();
        }
        else if (tq.matchesWord() || tq.matches("*|")){
            covered(2);
            byTag();
        }
        else if (tq.matches("[")){
            covered(3);
            byAttribute();
        }
        else if (tq.matchChomp("*")){
            covered(4);
            allElements();
        }
        else if (tq.matchChomp(":lt(")){
            covered(5);
            indexLessThan();
        }
        else if (tq.matchChomp(":gt(")){
            covered(6);
            indexGreaterThan();
        }
        else if (tq.matchChomp(":eq(")){
            covered(7);
            indexEquals();
        }
        else if (tq.matches(":has(")){
            covered(8);
            has();
        }
        else if (tq.matches(":contains(")){
            covered(9);
            contains(false);
        }
        else if (tq.matches(":containsOwn(")){
            covered(10);
            contains(true);
        }
        else if (tq.matches(":containsData(")){
            covered(11);
            containsData();
        }
        else if (tq.matches(":matches(")){
            covered(12);
            matches(false);
        }
        else if (tq.matches(":matchesOwn(")){
            covered(13);
            matches(true);
        }
        else if (tq.matches(":not(")){
            covered(14);
            not();
        }
		else if (tq.matchChomp(":nth-child(")){
		    covered(15);
        	cssNthChild(false, false);
		}
        else if (tq.matchChomp(":nth-last-child(")){
            covered(16);
        	cssNthChild(true, false);
        }
        else if (tq.matchChomp(":nth-of-type(")){
            covered(17);
        	cssNthChild(false, true);
        }
        else if (tq.matchChomp(":nth-last-of-type(")) {
            covered(18);
            cssNthChild(true, true);
        }
        else if (tq.matchChomp(":first-child")){
            covered(19);
        	evals.add(new Evaluator.IsFirstChild());
        }
        else if (tq.matchChomp(":last-child")) {
            covered(20);
            evals.add(new Evaluator.IsLastChild());
        }
        else if (tq.matchChomp(":first-of-type")){
            covered(21);
        	evals.add(new Evaluator.IsFirstOfType());
        }
        else if (tq.matchChomp(":last-of-type")){
            covered(22);
        	evals.add(new Evaluator.IsLastOfType());
        }
        else if (tq.matchChomp(":only-child")){
            covered(23);
        	evals.add(new Evaluator.IsOnlyChild());
        }
        else if (tq.matchChomp(":only-of-type")){
            covered(24);
        	evals.add(new Evaluator.IsOnlyOfType());
        }
        else if (tq.matchChomp(":empty")){
            covered(25);
        	evals.add(new Evaluator.IsEmpty());
        }
        else if (tq.matchChomp(":root")) {
            covered(26);
            evals.add(new Evaluator.IsRoot());
        }
        else if (tq.matchChomp(":matchText")){
            covered(27);
            evals.add(new Evaluator.MatchText());
        }
		else // unhandled
        {
            covered(28);
            //report();
            throw new Selector.SelectorParseException("Could not parse query '%s': unexpected token at '%s'", query, tq.remainder());
        }
        //report();
    }

    public static void report() {
        String lb = System.lineSeparator();
        StringBuilder cvrd = new StringBuilder("Branches covered:" + lb);
        StringBuilder notcvrd = new StringBuilder("Branches NOT covered:" + lb);
        boolean[] coverage = QueryParser.coverage;
        int covered = 0;
        for(int i = 0; i < coverage.length; i++) {
            if(coverage[i]) {
                cvrd.append(i);
                cvrd.append(lb);
                covered++;
            }
            else {
                notcvrd.append(i);
                notcvrd.append(lb);
            }
        }
        File f = new File("QueryParserTest.out");
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            String s = "Coverage: " + (((double) covered)/((double) coverage.length)) + lb + cvrd.toString() + notcvrd.toString();
            bw.write(s);
            bw.flush();
            bw.close();
        } catch (IOException e) {
            System.out.println("Error writing to file");
        }
    }

    private void byId() {
        String id = tq.consumeCssIdentifier();
        Validate.notEmpty(id);
        evals.add(new Evaluator.Id(id));
    }

    private void byClass() {
        String className = tq.consumeCssIdentifier();
        Validate.notEmpty(className);
        evals.add(new Evaluator.Class(className.trim()));
    }

    private void byTag() {
        // todo - these aren't dealing perfectly with case sensitivity. For case sensitive parsers, we should also make
        // the tag in the selector case-sensitive (and also attribute names). But for now, normalize (lower-case) for
        // consistency - both the selector and the element tag
        String tagName = normalize(tq.consumeElementSelector());
        Validate.notEmpty(tagName);

        // namespaces: wildcard match equals(tagName) or ending in ":"+tagName
        if (tagName.startsWith("*|")) {
            evals.add(new CombiningEvaluator.Or(new Evaluator.Tag(tagName), new Evaluator.TagEndsWith(tagName.replace("*|", ":"))));
        } else {
            // namespaces: if element name is "abc:def", selector must be "abc|def", so flip:
            if (tagName.contains("|"))
                tagName = tagName.replace("|", ":");

            evals.add(new Evaluator.Tag(tagName));
        }
    }

    private void byAttribute() {
        TokenQueue cq = new TokenQueue(tq.chompBalanced('[', ']')); // content queue
        String key = cq.consumeToAny(AttributeEvals); // eq, not, start, end, contain, match, (no val)
        Validate.notEmpty(key);
        cq.consumeWhitespace();

        if (cq.isEmpty()) {
            if (key.startsWith("^"))
                evals.add(new Evaluator.AttributeStarting(key.substring(1)));
            else
                evals.add(new Evaluator.Attribute(key));
        } else {
            if (cq.matchChomp("="))
                evals.add(new Evaluator.AttributeWithValue(key, cq.remainder()));

            else if (cq.matchChomp("!="))
                evals.add(new Evaluator.AttributeWithValueNot(key, cq.remainder()));

            else if (cq.matchChomp("^="))
                evals.add(new Evaluator.AttributeWithValueStarting(key, cq.remainder()));

            else if (cq.matchChomp("$="))
                evals.add(new Evaluator.AttributeWithValueEnding(key, cq.remainder()));

            else if (cq.matchChomp("*="))
                evals.add(new Evaluator.AttributeWithValueContaining(key, cq.remainder()));

            else if (cq.matchChomp("~="))
                evals.add(new Evaluator.AttributeWithValueMatching(key, Pattern.compile(cq.remainder())));
            else
                throw new Selector.SelectorParseException("Could not parse attribute query '%s': unexpected token at '%s'", query, cq.remainder());
        }
    }

    private void allElements() {
        evals.add(new Evaluator.AllElements());
    }

    // pseudo selectors :lt, :gt, :eq
    private void indexLessThan() {
        evals.add(new Evaluator.IndexLessThan(consumeIndex()));
    }

    private void indexGreaterThan() {
        evals.add(new Evaluator.IndexGreaterThan(consumeIndex()));
    }

    private void indexEquals() {
        evals.add(new Evaluator.IndexEquals(consumeIndex()));
    }
    
    //pseudo selectors :first-child, :last-child, :nth-child, ...
    private static final Pattern NTH_AB = Pattern.compile("(([+-])?(\\d+)?)n(\\s*([+-])?\\s*\\d+)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern NTH_B  = Pattern.compile("([+-])?(\\d+)");

	private void cssNthChild(boolean backwards, boolean ofType) {
		String argS = normalize(tq.chompTo(")"));
		Matcher mAB = NTH_AB.matcher(argS);
		Matcher mB = NTH_B.matcher(argS);
		final int a, b;
		if ("odd".equals(argS)) {
			a = 2;
			b = 1;
		} else if ("even".equals(argS)) {
			a = 2;
			b = 0;
		} else if (mAB.matches()) {
			a = mAB.group(3) != null ? Integer.parseInt(mAB.group(1).replaceFirst("^\\+", "")) : 1;
			b = mAB.group(4) != null ? Integer.parseInt(mAB.group(4).replaceFirst("^\\+", "")) : 0;
		} else if (mB.matches()) {
			a = 0;
			b = Integer.parseInt(mB.group().replaceFirst("^\\+", ""));
		} else {
			throw new Selector.SelectorParseException("Could not parse nth-index '%s': unexpected format", argS);
		}
		if (ofType)
			if (backwards)
				evals.add(new Evaluator.IsNthLastOfType(a, b));
			else
				evals.add(new Evaluator.IsNthOfType(a, b));
		else {
			if (backwards)
				evals.add(new Evaluator.IsNthLastChild(a, b));
			else
				evals.add(new Evaluator.IsNthChild(a, b));
		}
	}

    private int consumeIndex() {
        String indexS = tq.chompTo(")").trim();
        Validate.isTrue(StringUtil.isNumeric(indexS), "Index must be numeric");
        return Integer.parseInt(indexS);
    }

    // pseudo selector :has(el)
    private void has() {
        tq.consume(":has");
        String subQuery = tq.chompBalanced('(', ')');
        Validate.notEmpty(subQuery, ":has(el) subselect must not be empty");
        evals.add(new StructuralEvaluator.Has(parse(subQuery)));
    }

    // pseudo selector :contains(text), containsOwn(text)
    private void contains(boolean own) {
        tq.consume(own ? ":containsOwn" : ":contains");
        String searchText = TokenQueue.unescape(tq.chompBalanced('(', ')'));
        Validate.notEmpty(searchText, ":contains(text) query must not be empty");
        if (own)
            evals.add(new Evaluator.ContainsOwnText(searchText));
        else
            evals.add(new Evaluator.ContainsText(searchText));
    }

    // pseudo selector :containsData(data)
    private void containsData() {
        tq.consume(":containsData");
        String searchText = TokenQueue.unescape(tq.chompBalanced('(', ')'));
        Validate.notEmpty(searchText, ":containsData(text) query must not be empty");
        evals.add(new Evaluator.ContainsData(searchText));
    }

    // :matches(regex), matchesOwn(regex)
    private void matches(boolean own) {
        tq.consume(own ? ":matchesOwn" : ":matches");
        String regex = tq.chompBalanced('(', ')'); // don't unescape, as regex bits will be escaped
        Validate.notEmpty(regex, ":matches(regex) query must not be empty");

        if (own)
            evals.add(new Evaluator.MatchesOwn(Pattern.compile(regex)));
        else
            evals.add(new Evaluator.Matches(Pattern.compile(regex)));
    }

    // :not(selector)
    private void not() {
        tq.consume(":not");
        String subQuery = tq.chompBalanced('(', ')');
        Validate.notEmpty(subQuery, ":not(selector) subselect must not be empty");

        evals.add(new StructuralEvaluator.Not(parse(subQuery)));
    }
}
