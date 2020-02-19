# Report for assignment 3

This is a template for your report. You are free to modify it as needed.
It is not required to use markdown for your report either, but the report
has to be delivered in a standard, cross-platform format.

## Project

Name: JSoup

URL: https://jsoup.org/

JSoup is a library for parsing HTML. It implements the WHATWG HTML5 specification, and parses HTML to the same DOM as modern browsers do.

## Onboarding experience

_Did it build and run as documented?_ 

There is no documentation on how to build or run the project from the repo. It is a Maven project so we just cloned and imported it the standard way for Maven projects. For 3/4 people in the group the build succeeded without problems. One person couldn't compile it in IntelliJ though, which is proabably due to some config/versioning error with Maven on his end. 

Tests run and pass without problems. 

## Complexity

The methods we selected are the following:
| # | Package.Class::Method | LOC | CCN (Liz/Clov) 
| --- | --- | --- | --- |  
1 | Parser.CharactherReader::nextIndexOf | 17 | 10/10 
2 | Helper.HttpConnection::Response::Execute | 81 | 33/32 
3 | Helper.DataUtil::parseInputStream | 77 | 26/25 
4 | Select.QueryParser::findElements | 60 | 30/30 
5 | Parser.TokenQueue::chompBalanced | 35 | 20/20 
6 | Parser.HtmlTreeBuilder::resetInsertionMode | 48 | 19/19 
7 | Helper.DataUtil::detectCharsetFromBom | 19 | 17/17 
8 | Nodes.Entities::escape | 68 | 16/20  

### Manual CCN calc: 
Write the number from above and the calculation (full equation): M = E-N+2P or π - s + 2
https://en.wikipedia.org/wiki/Cyclomatic_complexity

Method # | Result
---|---
4        |  29-2+2=29
5        |  42-21+2 = 23
6        |  36-19+2 = 19
7        |  16-4+2 = 14 


1. What are your results for eight complex functions?

   * _Did all tools/methods get the same result?_

      We used Cloverage and Lizard. The results were very similar but not exactly equal. Typically +/-3, with Lizard reporting the higher CCN
   * _Are the results clear?_
      
      Yes, in Lizard you can get very detailed data with the proper flags. For Cloverage the GUI is clear and easy to use.

2. Are the functions just complex, or also long?
   
   Mostly they are also long, but a few of them have switch statements that contribute to most of the complexity. 

3. What is the purpose of the functions? 

__Grading criteria__: 
* 1: __You identify ten functions/methods with high complexity, and document the purpose of them, and why the complexity should be high (or not).__
* 4: __The purpose of each of the high-complexity methods is documented in detail w.r.t. the different out- comes resulting in branches in the code.__


   1. The CharactherReader class consumes tokens of a string. The class contains a char buffer holding the characters of the string. The function nextIndexOf calculates the number of characters in the buffer between the buffers current position and the position of an input sequence of chars. The function has two return statements, one for when the offset is found and one for the case when the sequence does not occur from the buffers current position till the end. What makes the function complex is the many if-statements, two for-loops and one while-loop (many of which are nested). To add further to the complexity many of the conditions contain the &&-operator. However the level of complexity is needed to achieve what the requirements for the function. The first for-loop assure that the function continues to run to the end of the buffer, even if we at some point stumbles across the first character of the input sequence in the buffer. The first if-statement checks if the first character in the sequence does not equal the character at the current place in the buffer, if the character is not equal a while loop is started which iterates till the first character of the sequence is found in the buffer. The second if statement checks if there is enough buffer space left to fit the sequence within the buffer. The second for-loop is used to check if the characters in the buffer and sequence match from the second character to the last. The final if-statement checks if the for-loop iterated to the index of the supposedly last buffer index that contained the character sequence, if true the sequence have been found and the function returns the offset from current buffer position to the position of the sequence occurrence in the buffer. If all of this fails and the first for-loop has reached the end of the buffer the function returns -1.
   2. Validates, sets up, executes and returns a HTTP response. While running, it checks for possible bugs, errors, and controls what kind of response it will return. This adds up to a high complexity, but many of the if statements can be refactored out and become more easily tested.
   3. Decode the input string as UTF-8. Determine if the input contains <meta http-equiv="Content-Type" content="text/html;charset=gb2312"> or HTML5 <meta charset="gb2312">. If the input does not contain any of them, check if it contains xml encoding. If it doesn’t, the function re-decodes the input.
   4. findElements parses a query to find the different tokens used. It checks for each defined token and consume them while creating an evaluator tree through specialized private method calls. The complexity is needed because of the parsing. If an invalid token is encountered, an exception is thrown. 
   5. Format the string to get a balanced substring. I.e if the queue is((one) two), the function will return “one” and leave “ two” on the queue.  
   6. Sets the current state of the HTML tree builder, i.e. if inside body, table, before html, etc. It has high complexity because it needs to set a different state for different HTML tags, resulting in many if statements.
   7. The DataUtil class contain static utilities for handling data. The function detectCharsetFromBom takes buffer data as an argument and checks if the place in the buffer that should contain the byte order mark (BOM, a special unicode character that appears in the start of a text stream) matches with the BOM for UTF-32, UTF-16 and UTF-8. This is done by inspecting the first characters of the buffer data and try to match these characters to the ones presented for the different UTF encodings. The function returns an instance of the class BomCharset which contain information about the encoding type. There are 4 return statements one for UTF-32, UTF-16, UTF-8 and a general return null if an encoding could not be decided. To check for matches between the buffer characters and the different BOM encodings is what makes the function complex.To add to the complexity of the function the BOM for UTF-32 and UTF-16 are dependent on endianness, so for each encoding two checks have to be performed, one for big-endian and one for little-endian. One way of avoiding this complexity would be to use libraries such as “juniversalchardet” which have built in support for detecting the charset from the BOM. The first if statement checks if the remaining length of the buffer is big enough to contain the four characters needed for the UTF-32 BOM. The second if-statement check if the supposedly BOM is of UTF-32 standard for both big-endian and little-endian. The first else if statement does the same but for the UTF-16 encoding BOM standard (both big-endian and little-endian) and the final else if checks for the UTF-8 encoding BOM standard. If none of these BOM checks evaluate to true a final return statement returns null. If any of the three BOM statements evaluate to true, they return a BomCharset object containing the respective UTF encoding information.
   8. Potentially normalises whitespace. Then encodes the string as html, ex. “<” will be returned as “&lt”. For each character it checks if it is one of four special HTML characters, and if so, replaces them. 

4. Are exceptions taken into account in the given measurements?

   Yes, they are counted as exit points

5. Is the documentation clear w.r.t. all the possible outcomes?

   Generally no, the documentation is not that detailed and often even missing. 

## Coverage

### Tools
TODO

Document your experience in using a "new"/different coverage tool.

How well was the tool documented? Was it possible/easy/difficult to
integrate it with your build environment?

### DYI

Show a patch (or link to a branch) that shows the instrumented code to
gather coverage measurements.

https://github.com/dd2480-12/jsoup/tree/coverage/src/main

What kinds of constructs does your tool support, and how accurate is its output?

Only `if-else`, `while`, `switch-case`

### Evaluation 

1. How detailed is your coverage measurement?
It tells you which branches (ID) were taken, and how many % of branches taken. 

2. What are the limitations of your own tool?
We can't handle the ternary operator because we need to use side effects to modify the data structure (technically we can but it would require some ugly workarounds). A better tool would work on the AST for example. Lack of automation is error prone. 

3. Are the results of your tool consistent with existing coverage tools?
They're quite consistent but differ by a few %. The real tools usually finds more branches, proably because they add implicit `else` for example. 

### Coverage improvement

TODO 
Show the comments that describe the requirements for the coverage.

Report of old coverage: 

   See ~/CloverReports/initialCloverageReport locally

The methods we selected are the following:
| # | Package.Class::Method | Init Coverage % (Clov/Own) | Added tests | Impr Coverage 
| --- | --- | --- | --- | --- | 
1 | Parser.CharactherReader::nextIndexOf | 100/100 | 0 | 100
2 | Helper.HttpConnection::Response::Execute | 92.7/90.3 | 1 | 94.8 
3 | Helper.DataUtil::parseInputStream | 92.2/83.3 | 1 | 95.9 
4 | Select.QueryParser::findElements | 97.2/96.6 | 1 | 100
5 | Parser.TokenQueue::chompBalanced | 98.5/100 | 1 | 100
6 | Parser.HtmlTreeBuilder::resetInsertionMode | 67.4/58.8 | 6 | 98.9
7 | Helper.DataUtil::detectCharsetFromBom | 95.2/100 | 1 | 100
8 | Nodes.Entities::escape | 100/100 | 0 | 100
9 | Parser.HtmlTreeBuilder::parseFragment | 63.6/66.7 | 4 | ??
| | __Sum added tests__ | | 15 |

Report of new coverage: 
   See ~/CloverReports/improvedCloverReport locally

#### Test cases added:

(source 979d5dd)

| Pkg.Class::Method | Tests added | git diff ... | Link 
|---|---|---|---|
Helper.HttpConnection::Response::Execute | 1 | git diff 979d5dd 38fadf1 src/test/java/org/jsoup/integration/ConnectTest.java | [Commit](https://github.com/dd2480-12/jsoup/commit/38fadf16ea6192056882f5cc33611b65c7e95dde)
Helper.DataUtil::parseInputStream | 1 | git diff 979d5dd fd40893  src/test/java/org/jsoup/helper/DataUtilTest.java | [Commit](https://github.com/dd2480-12/jsoup/commit/fd40893eb887d6cfe1d61d03ad9077eadd91cf78)
Select.QueryParser::findElements | 1 | git diff 979d5dd 3d35832 | [Commit](https://github.com/dd2480-12/jsoup/commit/3d3583222333a5c0878c66cc73c892cbc5c1262d)
Parser.HtmlTreeBuilder::resetInsertionMode | 2 | git diff 979d5dd 38fadf1 src/test/java/org/jsoup/parser/HtmlTreeBuilderTest.java | [Commit1](https://github.com/dd2480-12/jsoup/commit/38fadf16ea6192056882f5cc33611b65c7e95dde#diff-6862faf9576b1a665a83c0952d0049a2) 
|| 2 |git diff 979d5dd 093d849 | [Commit2](https://github.com/dd2480-12/jsoup/commit/093d849233e9e572e2e447b7c8dcb073b2523e40)
|| 2 | git diff 979d5dd 12715e6 src/test/java/org/jsoup/parser/HtmlTreeBuilderTest.java | [Commit3](https://github.com/dd2480-12/jsoup/commit/12715e612a26a634b47fe5b457f7a3e15aecd763)
Helper.DataUtil::detectCharsetFromBom | 1 | git diff 979d5dd 5663647 | [Commit](https://github.com/dd2480-12/jsoup/commit/56636476af066a3bec2b6bdda1b059f866f18fd6)
Parser.TokenQueue::chompBalanced | 1 | git diff 979d5dd fd40893 src/test/java/org/jsoup/parser/TokenQueueTest.java | [Commit](https://github.com/dd2480-12/jsoup/commit/fd40893eb887d6cfe1d61d03ad9077eadd91cf78)
Parser.HtmlTreeBuilder::parseFragment | 1 | git diff 979d5dd bf9ee64 | [Commit](https://github.com/dd2480-12/jsoup/commit/bf9ee64f1421d0101c6bce035154082583208e4a)
|| 2 | git diff 979d5dd 539b7ea | [Commit2](https://github.com/dd2480-12/jsoup/commit/539b7ea95a22c9f320cda8dd8486dc8c9b14a4ad)
|| 1 | git diff 979d5dd 7f11aa0 | [Commit3](https://github.com/dd2480-12/jsoup/commit/7f11aa0da279626a4d7fa302477450e90573c475)

## Refactoring

Plan for refactoring complex code:

1. __resetInsertionMode__: The high complexity is neccessary in a way that the code needs to check for particular tags. However, there are different ways of doing it than having a list of if-statements. You can map each element to its corresponding state. When the method is called, get the state by calling it in the map using the element node name. If it doesn't exist, then continue. The only exception is the `th` tag, where we need to check if it's the last element. This reduces the amount of if-statements by a large amount, but introduces the need of having to instantiate a map every time the function is called. This may be an expensive procedure, depending on the amount of times the function is called. An alternative is having the map be global, and thus only created once. However, this introduces global data, which could increase the risk of bugs if not tested properly. 

2. Refactoring can be applied to chompBalanced function in TokenQueue.java
The main purpose of chompBalanced function is to pull a string off the queue 
from another substring. There is a while loop in the funciton. It can be splited from 
the function and turn it to be a function which be processed only if the current string is not 
empty. This function will be looped for several times until it find the substring with 
the highest depth of given parameter in the original string. The impact of refactoring is that the complexity of the class
 will be lower.

3. __detectCharsetFromBom__: One easy way of reducing the complexity of detectCharsetFromBom would be to move the conditions of the if-statements to separate functions returning a boolean value. This split of the function would lower the complexity (of the function) and result in more easily readable code. The reason why this function is a good candidate to split is because of the “nested” conditions in the if-statements (several conditions linked by &&- and ||-operators). The drawback of this solution is that the overall complexity of the class doesn’t decrease.

Estimated impact of refactoring (lower CC, but other drawbacks?).

### Carried out refactoring 

| Pkg.Class::method | Old CCN | New CCN | New/Old
|---|---|---|---|
Parser.HtmlTreeBuilder::resetInsertionMode | 19 | 7 | 0.368
Helper.DataUtil::detectCharsetFromBom | 17 | 5 | 0.294

[resetInsertionMode](https://github.com/dd2480-12/jsoup/commit/bba8604a4f68fcda6349f7a42681bb69e44c225d): 

```git diff 979d5dd bba8604```

[detectCharsetFromBom](https://github.com/dd2480-12/jsoup/commit/92cd0e9309dd5a87ec638957f52303fe10d1878b):

```git diff 979d5dd 92cd0e9```



## Overall experience  

Learned to use and interpret Cloverage and IDEA built in coverage tool. Learned how to use them to improve unit tests. One big take-away is that lower complexity generally means it's easier to achieve better coverage, but it might make the code harder to understand or more messy. 

What are your main take-aways from this project? What did you learn?


