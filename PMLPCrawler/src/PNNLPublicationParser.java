/**
Copyright (c) 2012, University of Texas at El Paso
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation 
and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH 
DAMAGE.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class PNNLPublicationParser {

	private static String hasName, hasAbstract, hasPublisher, hasPublishDate, hasURL;
	private ArrayList<String> hasAuthorList;
	private boolean inPublication = false;

	private AlfrescoClient aClient;
	private PMLPWriter Writer;
	private String username, password, server, project;

	public void readPublicationFile(String queryURL){
		String inputLine;

		try{
			URL oracle = new URL(queryURL);
			BufferedReader in = new BufferedReader( new InputStreamReader(oracle.openStream()));

			while ((inputLine = in.readLine()) != null){
				if(inputLine.contains("<tr>")){

					String temp = in.readLine();
					hasName = temp.substring(temp.indexOf("<td>") + 4, temp.indexOf("&nbsp;</td>"));	//* title
					in.readLine();//					* type
					in.readLine();//					* product date

					temp = in.readLine();
					hasAbstract = temp.substring(temp.indexOf("<td>") + 4, temp.indexOf("&nbsp;</td>"));	//* product description
					in.readLine();//					* product comment
					in.readLine();//					* keywords
					in.readLine();//					* proceeding name
					in.readLine();//					* volume
					in.readLine();//					* issue
					in.readLine();//					* pages
					in.readLine();//					* digital oid
					in.readLine();//					* chapter
					in.readLine();//					* chapter name
					in.readLine();//					* book editor
					in.readLine();//					* clearance #

					temp = in.readLine();
					hasPublishDate = temp.substring(temp.indexOf("<td>") + 4, temp.indexOf("&nbsp;</td>"));	//* date published
					in.readLine();//					* date in press
					in.readLine();//					* date submitted
					in.readLine();//					* date clearead

					temp = in.readLine();
					hasPublisher = temp.substring(temp.indexOf("<td>") + 4, temp.indexOf("&nbsp;</td>"));	//* publisher
					in.readLine();//					* publisher location
					in.readLine();//					* year
					in.readLine();//					* invited author
					in.readLine();//					* peer reviewed
					in.readLine();//					* arm user
					in.readLine();//					* emsl user
					in.readLine();//					* project number
					in.readLine();//					* BR number

					String coAuthor = "", org = "";
					while(!(inputLine = in.readLine()).contains("</table>")){
						if(inputLine.contains("span")){
							coAuthor = inputLine.substring(inputLine.indexOf('>') + 1, inputLine.indexOf("</span>"));
							temp = inputLine.substring(inputLine.indexOf("</span>") + 8);
							org = temp.substring(temp.indexOf('>') + 1, temp.lastIndexOf("</span>"));

							//Query for Person existence
							String orgUri = Writer.getOrganizationURI(org);
							if(orgUri == null){
								//create Organization PMLP, get URI
								String filename = org;
								filename = filename.replaceAll("[*<>\\[\\]\\+\",]", "-");
								filename = filename.replaceAll(" ", "_");

								String nodeUri = aClient.createNode(project, filename + ".owl");
								//create pmlp
								String content = Writer.getPMLOrganization(org, server + nodeUri, null);
								try{
									FileWriter fstream = new FileWriter(filename + ".owl");
									BufferedWriter out = new BufferedWriter(fstream);
									out.write(content);
									fstream.close();
									out.close();
								}catch (Exception e){System.err.println("Error: " + e.getMessage());}
								
								File pmlpFile = new File(filename + ".owl");
								pmlpFile.deleteOnExit();
								aClient.addContentToNode(server + nodeUri, pmlpFile);

								orgUri = server + nodeUri;
							}

							//Query for Person existence
							String authorURI = Writer.getPersonURI(coAuthor);
							if(authorURI != null && !authorURI.isEmpty()){
								hasAuthorList.add(authorURI);
							}else{
								//create Person PMLP, get URI:

								String filename = coAuthor;
								filename = filename.replaceAll("[*<>\\[\\]\\+\",]", "-");
								filename = filename.replaceAll(" ", "_");

								String nodeUri = aClient.createNode(project, filename + ".owl");
								//create pmlp
								String content = Writer.getPMLPersonPlusFOAF(coAuthor, server + nodeUri, orgUri);
								try{
									FileWriter fstream = new FileWriter(filename + ".owl");
									BufferedWriter out = new BufferedWriter(fstream);
									out.write(content);
									fstream.close();
									out.close();
								}catch (Exception e){System.err.println("Error: " + e.getMessage());}
								
								File pmlpFile = new File(filename + ".owl");
								pmlpFile.deleteOnExit();
								aClient.addContentToNode(server + nodeUri, pmlpFile);

								hasAuthorList.add(server + nodeUri);

							}
						}
					}	
				}
				String filename = hasName;
				filename = filename.replaceAll("[*<>\\[\\]\\+\",]", "-");
				filename = filename.replaceAll(" ", "_");

				String nodeUri = aClient.createNode(project, filename + ".owl");

				Writer.authors = (String[])hasAuthorList.toArray();
				Writer.hasAbstract = hasAbstract;
				Writer.hasPublisher = hasPublisher;
				Writer.hasPublicationDate = hasPublishDate;

				String content = Writer.getPMLPublication(hasName, server + nodeUri);

				try{
					FileWriter fstream = new FileWriter(filename + ".owl");
					BufferedWriter out = new BufferedWriter(fstream);
					out.write(content);
					fstream.close();
					out.close();
				}catch (Exception e){System.err.println("Error: " + e.getMessage());}
				
				File pmlpFile = new File(filename + ".owl");
				pmlpFile.deleteOnExit();
				aClient.addContentToNode(server + nodeUri, pmlpFile);
				
				System.out.println("name: " + hasName);
				System.out.println("abstract: ");
				System.out.println(hasAbstract);
				System.out.println("publisher: " + hasPublisher);
				System.out.println("publish date: " + hasPublishDate);
				
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {


		PNNLPublicationParser parser = new PNNLPublicationParser();

		parser.username = "admin";
		parser.password = "admin";
		parser.server = "http://localhost:8080/alfresco";
		parser.project = "ProjectY";

		parser.aClient = new AlfrescoClient(parser.username, parser.password, parser.server);
		parser.Writer = new PMLPWriter();
		parser.readPublicationFile("file:///D:/ProgrammingProjects/CVSTrustLab/OrgWithMembers/erica.html");

	}

}
