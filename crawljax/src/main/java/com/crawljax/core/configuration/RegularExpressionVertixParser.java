package com.crawljax.core.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class RegularExpressionVertixParser {
List<Pattern> patternList;
Map<Pattern,String> matchList;

public RegularExpressionVertixParser() {
	patternList = new ArrayList<Pattern>();
	matchList = new HashMap<Pattern,String>();
}

public void addMatchPattern(Pattern p){
	patternList.add(p);
}

public void addReplacePattern(Pattern p, String s){
	patternList.add(p);
	matchList.put(p,s);
}

public List<Pattern> getPatternList() {
	return patternList;
}

public Map<Pattern, String> getMatchList() {
	return matchList;
}



}
