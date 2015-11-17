package it.unibz.inf.completeness.csgenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.BasicPattern;

//TODO: This class should be in its right package!

/**
 * Class for completeness statements
 * 
 * @author Fariz Darari (fadirra@gmail.com)
 *
 */
public class CompletenessStatement {

	private BasicPattern pattern;
	private BasicPattern condition;

	/**
	 * Constructs a completeness statement
	 * using basic patterns
	 * 
	 * @param pattern the pattern of the completeness statement
	 * @param condition the condition of the completeness statement
	 */
	public CompletenessStatement(BasicPattern pattern, BasicPattern condition) {
		setPattern(pattern);
		setCondition(condition);
	}
	
	/**
	 * Constructs completeness statements
	 * using lists of triples
	 * 
	 * @param pattern the pattern of the completeness statement
	 * @param condition the condition of the completeness statement
	 */
	public CompletenessStatement(List<Triple> pattern, List<Triple> condition) {
		setPattern(BasicPattern.wrap(pattern));
		setCondition(BasicPattern.wrap(condition));
	}
	
	/**
	 * Sets the pattern of this completeness statement
	 * 
	 * @param pattern a BGP to set the pattern of this completeness statement
	 */
	public void setPattern(BasicPattern pattern) {		
		this.pattern = pattern;		
	}
	
	/**
	 * Sets the condition of this completeness statement
	 * 
	 * @param condition a BGP to set the condition of this completeness statement
	 */
	public void setCondition(BasicPattern condition) {		
		this.condition = condition;		
	}
	
	/**
	 * 
	 * @return the pattern of this completeness statement
	 */
	public BasicPattern getPattern() {		
		return pattern;		
	}
	
	/**
	 * 
	 * @return the condition of this completeness statement
	 */
	public BasicPattern getCondition() {		
		return condition;		
	}
	
	/**
	 * 
	 * @return the list of triple patterns of this completeness statement
	 */
	public List<Triple> getBodyAsTriplePatterns() {
		
		List<Triple> lTP = new ArrayList<Triple>();
		lTP.addAll(pattern.getList());
		lTP.addAll(condition.getList());
		return lTP;
		
	}
	
	/**
	 * 
	 * @return the sorted list of all unique predicates of this completeness statement
	 */
	public ArrayList<String> getUniquePredicateList() {
		
		ArrayList<String> ls = new ArrayList<String>();
		List<Triple> lt = getBodyAsTriplePatterns();
		HashSet<String> hs = new HashSet<String>();
		
		Iterator<Triple> ltIterator = lt.iterator();
		while(ltIterator.hasNext()) {
			Triple tp = ltIterator.next();
			hs.add(tp.getPredicate().toString());
		}
		
		ls.addAll(hs);
		Collections.sort(ls);
		return ls;
		
	}

	/**
	 * 
	 * @return the length of the pattern of this completeness statement
	 */
	public int getPatternLength() {
		return pattern.size();
	}
	
	/**
	 * 
	 * @return the length of the condition of this completeness statement
	 */
	public int getConditionLength() {
		return condition.size();
	}
	
	/**
	 * 
	 * @return the length of this completeness statement
	 */
	public int getLength() {
		
		return pattern.size() + condition.size();
		
	}
	
	/**
	 *  
	 * @return the SPARQL CONSTRUCT query of this completeness statement
	 */
	public Query toQuery() {
		
		return QueryFactory.create(this.toQueryString());
	
	}
	
	/**
	 * 
	 * @return the string version of the SPARQL CONSTRUCT query of this completeness statement
	 */
	public String toQueryString() {
    	
    	String patternString 	= " ";
    	String conditionString 	= " ";    	    	
    	
    	List<Triple> patternList = pattern.getList();
    	List<Triple> conditionList = condition.getList();
    	
    	Iterator<Triple> patternIt = patternList.iterator();
    	while(patternIt.hasNext()) {
    		Triple thisTriple = patternIt.next();    		
    		patternString = patternString + formatTriplePatternForConcreteQuery(thisTriple) + " . ";
    	}
    	    	
    	Iterator<Triple> conditionIt = conditionList.iterator();    	  
    	while(conditionIt.hasNext()) {
    		Triple thisTriple = conditionIt.next();
    		conditionString = conditionString + formatTriplePatternForConcreteQuery(thisTriple) + " . ";
    	}

		return buildCONSTRUCTQuery(patternString, patternString + conditionString);

	}
	
	/**
	 * 
	 * Overrides the toString method of the Object class
	 * 
	 */
	public String toString() {
		return this.toQueryString();
	}
	
	/**
	 * 
	 * @param head the head to build the SPARQL CONSTRUCT query
	 * @param body the body to build the SPARQL CONSTRUCT query
	 * @return a SPARQL CONSTRUCT query built from the head and body
	 */
	public static String buildCONSTRUCTQuery(String head, String body) {
		
		return "CONSTRUCT { " + head + " } WHERE { " + body + " }";
		
	}

	/**
	 * 
	 * @param tp the triple pattern
	 * @return a concrete SPARQL representation of the triple pattern
	 */
	public static String formatTriplePatternForConcreteQuery(Triple tp) {
		
		return formatTriplePatternComponentForConcreteQuery(tp.getSubject())  + " " + 
		       formatTriplePatternComponentForConcreteQuery(tp.getPredicate()) + " " + 
			   formatTriplePatternComponentForConcreteQuery(tp.getObject());
	
	}

	/**
	 * 
	 * @param component the component (s, p or o) of a triple pattern
	 * @return a concrete SPARQL representation of the component (s, p or o) of a triple pattern
	 */
	public static String formatTriplePatternComponentForConcreteQuery(Node component) {
		
		if(component.isVariable())
			return component.toString(); //already led by the question mark '?'
		
		else if (component.isURI())
			return "<" + component.toString() + ">"; //has to be enclosed by angle brackets manually
		
		else //otherwise, a literal
			return component.toString(); //already enclosed by the quotes ""
		
	}

}