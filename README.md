# tempeh

**tempeh** is a XBRL parser written in Java to read XBRL documents.  It's mainly been tested on parsing SEC (US Securities and Exchange Commission) 10-Q/10-K XBRL files.  **tempeh** allows you to customize different reports to generate.  You have complete flexibility to choose the presentation links to parse or pick and choose the facts to look at.

To get started

```java
String xbrlInstance = "http://www.sec.gov/Archives/edgar/data/7623/000143774915001434/artw-20141130.xml";
final LocalFileCache fileCache = new LocalFileCache("schemas");
XbrlFinancialStatementTask task = new XbrlFinancialStatementTask(fileCache, xbrlInstance);
task.runTask();
```

Where xbrlInstance is the URL to a XBRL instance document.