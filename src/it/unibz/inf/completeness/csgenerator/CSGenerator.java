package it.unibz.inf.completeness.csgenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

/**
 * Generating realistic completeness statements from real data sources
 * 
 * @author Fariz Darari (fadirra@gmail.com)
 *
 */
public class CSGenerator {

	private static String sparqlEndpoint = "http://de.dbpedia.org/sparql";
	
	public static void main(String[] args) {
		
		String className = "http://dbpedia.org/ontology/Band"; // the class for instances we want to construct completeness statements		
		System.out.println(generateCompletenessStatements(className, 1000));

	}

	/**
	 * @param className
	 * @param limit
	 * @return a list of generated completeness statements from instances of className limited by limit
	 */
	public static List<CompletenessStatement> generateCompletenessStatements(String className, int limit) {
		
		List<CompletenessStatement> csList = new ArrayList<CompletenessStatement>();
		
		// generate instances of className
		List<String> resourcesList = getSomeResources(className, limit);
		
		// creating a BGP representing a completeness statement template we want to instantiate with resources
		Triple t1 = Triple.create(NodeFactory.createVariable("x"), NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), NodeFactory.createURI("http://dbpedia.org/ontology/Song"));
		Triple t2 = Triple.create(NodeFactory.createVariable("x"), NodeFactory.createURI("http://dbpedia.org/ontology/artist"), NodeFactory.createURI("http://dbpedia.org/resource/___TEMPLATE___"));
		List<Triple> pattern = new ArrayList<Triple>();
		pattern.add(t1);
		pattern.add(t2);
		CompletenessStatement cs = new CompletenessStatement(pattern, new ArrayList<Triple>());
		System.out.println("CS template = " + cs);
		
		// now instantiate the template
		for (String resource : resourcesList) {
			
			List<Triple> newTriplesList = new ArrayList<Triple>();
			List<Triple> triplesList = cs.getPattern().getList();
			for (Triple oldTriple : triplesList) {
				Node oldSubject = oldTriple.getSubject();
				Node oldObject = oldTriple.getObject();
				Node newSubject = replace(oldSubject, resource);
				Node newObject = replace(oldObject, resource);
				newTriplesList.add(new Triple(newSubject, oldTriple.getPredicate(), newObject));
			}
			csList.add(new CompletenessStatement(newTriplesList, new ArrayList<Triple>()));
			
		}
		
		return csList;
	}
	
	/**
	 * @param n
	 * @param resource
	 * @return if n is a template node, then replace it with resource
	 */
	public static Node replace(Node n, String resource) {
		
		if(n.isURI() && n.toString().contains("___TEMPLATE___") ) {
			return NodeFactory.createURI(resource);
		}
		else
			return n;
		
	}
	
	/**
	 * @param className
	 * @param limit
	 * @return a list of resources of class className limited by limit
	 */
	public static List<String> getSomeResources(String className, int limit) {
		
		List<String> resourcesList = new ArrayList<String>();
		QueryEngineHTTP qe = new QueryEngineHTTP(sparqlEndpoint, "SELECT ?x WHERE {?x a <" + className + ">} LIMIT "+ limit);
		ResultSet resultSet = qe.execSelect();
		while(resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			resourcesList.add(qs.get("x").toString());
		}
		qe.close();
		return resourcesList;
		
	}
	
	/**
	 * Credit: http://answers.semanticweb.com/questions/19935/jena-throws-queryparsingexception-on-correct-but-non-standard-sparql
	 * 
	 * @param className
	 * @return a random resource of the class className
	 */
	public static String getRandomResource(String className) {
		
		QueryEngineHTTP qe = new QueryEngineHTTP(sparqlEndpoint, "SELECT COUNT(?x) AS ?total WHERE {?x a <" + className + ">}"); // count the total number of instances of className
		ResultSet resultSet = qe.execSelect();
		int totalNumber = resultSet.next().getLiteral("total").getInt();
		qe.close();
		
		int randomNumber = new Random().nextInt(totalNumber); // get a random number
		qe = new QueryEngineHTTP(sparqlEndpoint, "SELECT ?x WHERE {?x a <" + className + ">} LIMIT 1 OFFSET " + randomNumber); // get an instance of className at a random position
		resultSet = qe.execSelect();
		String randomResource = ResultSetFormatter.asText(resultSet);
		qe.close();
		
		return randomResource;
		
	}
	
}