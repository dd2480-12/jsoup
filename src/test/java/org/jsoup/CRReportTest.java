package org.jsoup;

import org.jsoup.helper.DataUtil;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Entities;
import org.jsoup.parser.CharacterReader;
import org.jsoup.parser.HtmlTreeBuilder;
import org.jsoup.parser.TokenQueue;
import org.jsoup.select.QueryParser;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class CRReportTest {

    private static String generateMethodReport(boolean[] coverage, String methodName) {
        String lb = System.lineSeparator();
        StringBuilder cvrd = new StringBuilder("Branches covered: ");
        StringBuilder notcvrd = new StringBuilder("Branches NOT covered: ");
        int covered = 0;
        for (int i = 0; i < coverage.length; i++) {
            if (coverage[i]) {
                cvrd.append(i);
                cvrd.append(' ');
                covered++;
            } else {
                notcvrd.append(i);
                notcvrd.append(' ');
            }
        }
        cvrd.append(lb);
        notcvrd.append(lb);
        String s = methodName + ": " + (((double) covered) / ((double) coverage.length)) + lb + cvrd.toString() + notcvrd.toString();
        return s + "------------------------------------------------------" + lb;

    }

    private static class MethodResult {
        boolean[] coverage;
        String name;

        MethodResult(boolean[] coverage, String name) {
            this.coverage = coverage;
            this.name = name;
        }
    }

    private static void report() {
        StringBuilder sb = new StringBuilder();

        ArrayList<MethodResult> mcvr = new ArrayList<>();

        mcvr.add(new MethodResult(CharacterReader.NextIndexOf_test.entered_branch, "CharacterReader::nextIndexOf"));
        mcvr.add(new MethodResult(HttpConnection.Response.ExecuteCoverage.branches, "HttpConnection.Response::Execute"));
        mcvr.add(new MethodResult(DataUtil.parseInputStream_test.branch_num, "DataUtil::parseInputStream"));
        mcvr.add(new MethodResult(QueryParser.coverage, "QueryParser::findElements"));
        mcvr.add(new MethodResult(TokenQueue.chompBalanced_test.branch_num, "TokenQueue::chompBalanced"));
        mcvr.add(new MethodResult(HtmlTreeBuilder.ResetInsertionModeCoverage.branches, "HtmlTreeBuilder::resetInsertionMode"));
        mcvr.add(new MethodResult(DataUtil.DetectCharsetFromBom_test.entered_branch, "DataUtil::detectCharSetFromBom"));
        mcvr.add(new MethodResult(Entities.coverage, "Entities::escape"));
        mcvr.add(new MethodResult(HtmlTreeBuilder.parseFragmentCoverage, "HtmlTreeBuilder::parseFragment"));

        for(MethodResult mr : mcvr) {
            sb.append(generateMethodReport(mr.coverage, mr.name));
        }

        File f = new File("CReport.out");
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write(sb.toString());
            bw.flush();
            bw.close();
        } catch (IOException e) {
            System.out.println("Error writing report to file");
        }
    }

    @AfterClass
    public static void setShutDownHook() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                report();
            }
        });
        Runtime.getRuntime().addShutdownHook(t);
    }

    @Test
    public void trivial() {
        assertTrue(true);
    }
}
