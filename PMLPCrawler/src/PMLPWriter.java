
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;

public class PMLPWriter {

	//General Variables
	private String serverURL, hasURL, hasName;
	AlfrescoClient aClient;

	//PML-P Person Variables
	private String fname, lname, shortName;

	//PML-P Person + FOAF Variables
	private String email, sha1Sum, depiction, phone, title;
	private String homePage, workPage, projectPage, schoolPage;
	private String memberOfURI;

	//PML-P Publication Variables
	public String hasPublisher, hasPublicationDate, hasAbstract;
	public String[] authors;

	public String getPMLPublication(String hasName, String pmlp_url){

		String pmlp_pub = "";

		pmlp_pub += '\t' + "<rdf:RDF" + '\n';
		pmlp_pub += '\t' + "xmlns=\"http://inference-web.org/2.0/pml-provenance.owl#\"" + '\n';
		pmlp_pub += '\t' + "xmlns:ds=\"http://inference-web.org/2.0/ds.owl#\"" + '\n';
		pmlp_pub += '\t' + "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"" + '\n';
		pmlp_pub += '\t' + "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"" + '\n';
		pmlp_pub += '\t' + "xmlns:owl=\"http://www.w3.org/2002/07/owl#\"" + '\n';
		pmlp_pub += '\t' + "xmlns:daml=\"http://www.daml.org/2001/03/daml+oil#\">" + '\n';

		pmlp_pub += '\t' + "<Publication rdf:about=\"" + pmlp_url + "\">" + '\n';
		pmlp_pub += '\t' + "<hasDescription>" + '\n';
		pmlp_pub += '\t' + "<Information>" + '\n';
		
		if(hasURL != null && !hasURL.isEmpty()){
			pmlp_pub += '\t' + "<hasURL rdf:datatype=\"http://www.w3.org/2001/XMLSchema#anyURI\">" + hasURL + "</hasURL>" + '\n';
		}
		pmlp_pub += '\t' + "</Information>" + '\n';
		pmlp_pub += '\t' + "</hasDescription>" + '\n';
		pmlp_pub += '\t' + "<hasName rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">" + hasName + "</hasName>" + '\n';
		pmlp_pub += '\t' + "<hasAuthorList>" + '\n';
		pmlp_pub += '\t' + "<AgentList>" + '\n';

		//Loop for each author.
		for(int i = 0; i < authors.length; i++)
			pmlp_pub += '\t' + "<ds:first rdf:resource=\"" + authors[i] + "\"/>" + '\n';


		pmlp_pub += '\t' + "</AgentList>" + '\n';
		pmlp_pub += '\t' + "</hasAuthorList>" + '\n';

		pmlp_pub += '\t' + "<hasAbstract rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">" + hasAbstract + "</hasAbstract>" + '\n';
		pmlp_pub += '\t' + "<hasPublisher rdf:resource=\"" + hasPublisher + "\"/>" + '\n';
		pmlp_pub += '\t' + "<hasPublicationDateTime rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime\">" + hasPublicationDate + "</hasPublicationDateTime>" + '\n';
		pmlp_pub += '\t' + "</Publication>" + '\n';
		pmlp_pub += '\t' + "</rdf:RDF>" + '\n';

		return pmlp_pub;
	}

	public String getPersonURI(String person){
		String query = "PREFIX pmlp: <http://inference-web.org/2.0/pml-provenance.owl#>" +
				"select ?URI ?NAME where {" + 
				"?URI a pmlp:Person . " + 
				"?URI pmlp:hasName ?NAME ." + 
				"FILTER regex(?NAME, \"" + person + "\") }";

		String URI = aClient.executeQuery(query);
		ResultSet results = ResultSetFactory.fromXML(URI);

		if(results != null){
			QuerySolution QS = results.nextSolution();
			return QS.get("?URI").toString();
		}else{
			return null;
		}

	}

	public String getOrganizationURI(String org){
		String query = "PREFIX pmlp: <http://inference-web.org/2.0/pml-provenance.owl#>" +
				"select ?URI ?NAME where {" + 
				"?URI a pmlp:Organization . " + 
				"?URI pmlp:hasName ?NAME ." + 
				"FILTER regex(?NAME, \"" + org + "\") }";

		String URI = aClient.executeQuery(query);
		ResultSet results = ResultSetFactory.fromXML(URI);

		if(results != null){
			QuerySolution QS = results.nextSolution();
			return QS.get("?URI").toString();
		}else{
			return null;
		}

	}

	public String getPMLPersonPlusFOAF(String hasName, String pmlp_uri, String memberOfURI){

		shortName = hasName;
		shortName = shortName.replaceAll("[*<>\\[\\]\\+\",]", "-");
		shortName = shortName.replaceAll(" ", "_");

		hasName = fname + "_" + lname; 

		pmlp_uri = serverURL.trim() + "#" + shortName;
		pmlp_uri.replaceAll("(\\r|\\n)", "");

		String pml_foaf = "<rdf:RDF" + '\n';

		//Imports
		pml_foaf += '\t' + "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"" + '\n';
		pml_foaf += '\t' + "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"" + '\n';
		pml_foaf += '\t' + "xmlns:pmlp=\"http://inference-web.org/2.0/pml-provenance.owl#\"" + '\n';
		pml_foaf += '\t' + "xmlns:owl=\"http://www.w3.org/2002/07/owl#\"" + '\n';
		pml_foaf += '\t' + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"" + '\n';
		pml_foaf += '\t' + "xmlns:foaf=\"http://xmlns.com/foaf/0.1/\"" + '\n';
		pml_foaf += '\t' + "xmlns:admin=\"http://webns.net/mvcb/\">" + '\n';

		//Foaf/PML declarations
		pml_foaf += '\t' + "<foaf:Person rdf:about=\"" + pmlp_uri + "\">" + '\n';
		pml_foaf += "\t\t" + "<rdf:type rdf:resource=\"http://inference-web.org/2.0/pml-provenance.owl#Person\"/>" + '\n';

		//PML Info
		pml_foaf += "\t\t" + "<pmlp:hasName rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">" + hasName + "</pmlp:hasName>" + '\n';
		if(!memberOfURI.isEmpty() && memberOfURI != null)
			pml_foaf += "\t\t" + "<pmlp:isMemberOf rdf:resource=\"" + memberOfURI + "\"/>" + '\n';

		//FOAF Info

		pml_foaf += "\t\t" + "<foaf:name>" + hasName + "</foaf:name>" + '\n';

		//get sha1 for email
		if(!email.isEmpty() && email != null){
			try {
				sha1Sum = SHA1(email);
				pml_foaf += "\t\t" + "<foaf:mbox_sha1sum>" + sha1Sum + "</foaf:mbox_sha1sum>" + '\n';
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if(!title.isEmpty() && title != null)
			pml_foaf += "\t\t" + "<foaf:title>" + title + "</foaf:title>" + '\n';		
		if(!depiction.isEmpty() && depiction != null)
			pml_foaf += "\t\t" + "<foaf:depiction rdf:resource=\"" + depiction + "\"/>" + '\n';
		if(!phone.isEmpty() && phone != null)
			pml_foaf += "\t\t" + "<foaf:phone rdf:resource=\"tel:" + phone + "\"/>" + '\n';
		if(!homePage.isEmpty() && homePage != null)
			pml_foaf += "\t\t" + "<foaf:homepage rdf:resource=\"" + homePage + "\"/>" + '\n';
		if(!workPage.isEmpty() && workPage != null)
			pml_foaf += "\t\t" + "<foaf:workplaceHomepage rdf:resource=\"" + workPage  + "\"/>" + '\n';
		if(!projectPage.isEmpty() && projectPage != null)
			pml_foaf += "\t\t" + "<foaf:workInfoHomepage rdf:resource=\"" + projectPage + "\"/>" + '\n';
		if(!schoolPage.isEmpty() && schoolPage != null)
			pml_foaf += "\t\t" + "<foaf:schoolHomepage rdf:resource=\"" + schoolPage + "\"/>" + '\n';

		//Knows
		pml_foaf += "\t\t" + "<foaf:knows>" + '\n';
		pml_foaf += "\t\t" + "</foaf:knows>" + '\n';

		//End File
		pml_foaf += '\t' + "</foaf:Person>" + '\n';
		pml_foaf += "</rdf:RDF>" + '\n';

		return pml_foaf;
	}

	public String getPMLPerson(String hasName, String pmlp_uri, String memberOfURI){

		shortName = lname + "_" + fname;
		shortName = shortName.replaceAll("[*<>\\[\\]\\+\",]", "-");
		shortName = shortName.replaceAll(" ", "_");

		hasName = fname + "_" + lname; 

		pmlp_uri = serverURL.trim() + "#" + shortName;
		pmlp_uri.replaceAll("(\\r|\\n)", "");

		String pmlP = "<rdf:RDF" + '\n';
		pmlP += '\t' + "xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'" + '\n';
		pmlP += '\t' + "xmlns:pmlp='http://inference-web.org/2.0/pml-provenance.owl#'" + '\n';
		pmlP += '\t' + "xmlns:owl='http://www.w3.org/2002/07/owl#'" + '\n';
		pmlP += '\t' + "xmlns:xsd='http://www.w3.org/2001/XMLSchema#'" + '\n';
		pmlP += '\t' + "xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'>" + '\n';
		pmlP += '\t' + "<pmlp:Person rdf:about='" + pmlp_uri + "'>" + '\n';
		pmlP += '\t' + "<pmlp:hasName rdf:datatype='http://www.w3.org/2001/XMLSchema#string'>" + hasName + "</pmlp:hasName>" + '\n';
		if(!memberOfURI.isEmpty() && memberOfURI != null)
			pmlP += "\t\t" + "<pmlp:isMemberOf rdf:resource=\"" + memberOfURI + "\"/>" + '\n';
		pmlP += '\t' + "</pmlp:Person>" + '\n';
		pmlP += "</rdf:RDF>";

		return pmlP;

	}

	private static String convertToHex(byte[] data) { 
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) { 
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do { 
				if ((0 <= halfbyte) && (halfbyte <= 9)) 
					buf.append((char) ('0' + halfbyte));
				else 
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while(two_halfs++ < 1);
		} 
		return buf.toString();
	} 

	public static String SHA1(String text) 
			throws NoSuchAlgorithmException, UnsupportedEncodingException  { 
		MessageDigest md;
		md = MessageDigest.getInstance("SHA-1");
		byte[] sha1hash = new byte[40];
		md.update(text.getBytes("iso-8859-1"), 0, text.length());
		sha1hash = md.digest();
		return convertToHex(sha1hash);
	} 

	public String getPMLOrganization(String orgName, String pmlp_url, String memberOrURI){

		String shortName = orgName;
		shortName = shortName.replaceAll("[*<>\\[\\]\\+\",]", "-");
		shortName = shortName.replaceAll(" ", "_");

		String pmlP = "<rdf:RDF" + '\n';
		pmlP += '\t' + "xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'" + '\n';
		pmlP += '\t' + "xmlns:pmlp='http://inference-web.org/2.0/pml-provenance.owl#'" + '\n';
		pmlP += '\t' + "xmlns:owl='http://www.w3.org/2002/07/owl#'" + '\n';
		pmlP += '\t' + "xmlns:xsd='http://www.w3.org/2001/XMLSchema#'" + '\n';
		pmlP += '\t' + "xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'>" + '\n';

		pmlP += '\t' + "<pmlp:Organization rdf:about='" + pmlp_url + '#' + shortName + "'>" + '\n';
		pmlP += '\t' + "<pmlp:hasName rdf:datatype='http://www.w3.org/2001/XMLSchema#string'>" + shortName + "</pmlp:hasName>" + '\n';

		if(hasURL != null && !hasURL.isEmpty()){
			pmlP += '\t' + "<pmlp:hasDescription>" + '\n';
			pmlP += "\t\t" + "<pmlp:Information>" + '\n';
			pmlP += "\t\t\t" + "<pmlp:hasURL rdf:datatype='http://www.w3.org/2001/XMLSchema#anyURI'>" + hasURL + "</pmlp:hasURL>" + '\n';
			pmlP += "\t\t" + "</pmlp:Information>" + '\n';
			pmlP += '\t' + "</pmlp:hasDescription>" + '\n';
		}

		//check if member of another Organization
		if(memberOfURI != null && !memberOfURI.isEmpty()){
			pmlP += '\t' + "<pmlp:isMemberOf rdf:resource='" + memberOfURI  + "'/>" + '\n';
		}

		pmlP += '\t' + "</pmlp:Organization>" + '\n';
		pmlP += "</rdf:RDF>";

		return pmlP;
	}

}
