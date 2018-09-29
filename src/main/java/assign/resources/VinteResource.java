package assign.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;


import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import assign.domain.NewProject;
import assign.domain.Project;
import assign.services.ProjectService;
import assign.services.ProjectServiceImpl;

@Path("/projects")
public class MyEavesdropResource {
	
	ProjectService projectService;
	String password;
	String username;
	String dburl;
	String dbhost;
	String dbname;
	
	//DB Info
	public MyEavesdropResource(@Context ServletContext servletContext) {		
		dbhost = servletContext.getInitParameter("DBHOST");
		dbname = servletContext.getInitParameter("DBNAME");
		dburl = "jdbc:mysql://" + dbhost + ":3306/" + dbname;
		username = servletContext.getInitParameter("DBUSERNAME");
		password = servletContext.getInitParameter("DBPASSWORD");
		this.projectService = new ProjectServiceImpl(dburl, username, password);
	}
	
	//SANITY CHECK
	@GET
	@Path("/helloworld")
	@Produces("text/html")
	public String helloWorld() {
		System.out.println("Inside helloworld");
		System.out.println("DB creds are:");
		System.out.println("DBURL:" + dburl);
		System.out.println("DBUsername:" + username);
		System.out.println("DBPassword:" + password);		
		return "Hello world " + dburl + " " + username + " " + password;		
	}
	
	@POST
	@Consumes("application/xml")
	public Response createProject(InputStream is) throws Exception {
		
		NewProject newProject = readNewProject(is);
		
		//Validate name and description are not empty
		if(newProject.getName() == "" || newProject.getProjectDescription() == "") {
			return Response.status(400).build();
		}
		
		newProject = this.projectService.addProject(newProject);
		return Response.created(URI.create("/myeavesdrop/projects" + newProject.getProjectId())).build();
	}
	
	@PUT
	@Path("/{projectId}")
	@Consumes("application/xml")
	public Response updateProject(InputStream is, @PathParam("projectId") String projectId) throws Exception {
		
		try{
		    Integer.parseInt(projectId);
		}catch (NumberFormatException ex) {
		    return Response.status(400).build();
		}
		
		Project project = readProject(is);
		int code = projectService.updateProject(project, projectId);
		
		if(code == 1)
			return Response.status(204).build();
		else if(code == 0)
			return Response.status(400).build();
		
		return Response.status(404).build();
	}
	
	@GET
	@Path("/{projectId}")
	@Produces("application/xml")
	public Response getProject(@PathParam("projectId") String projectId) throws Exception {
	
		final Project project = this.projectService.getProject(projectId);
		
		if(project == null)
			return Response.status(404).build();
		
		StreamingOutput so = new StreamingOutput() {
	         public void write(OutputStream outputStream) throws IOException, WebApplicationException {
	            outputProject(outputStream, project);
	         }
	    };
	    
	    return Response.ok(so).build();
	}
	
	@DELETE
	@Path("/{projectId}")
	@Produces("application/xml")
	public Response deleteProject(@PathParam("projectId") String projectId) throws Exception {
		
		int code = projectService.deleteProject(projectId);
		
		if(code == 1)
			return Response.status(200).build();
		
	    return Response.status(404).build();
	}
	
	//Marshalling for GET
	protected void outputProject(OutputStream os, Project project) throws IOException {
		try { 
			JAXBContext jaxbContext = JAXBContext.newInstance(Project.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
	 
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(project, os);
		} catch (JAXBException jaxb) {
			jaxb.printStackTrace();
			throw new WebApplicationException();
		}
	}
	
	//Unmarshalling for POST
	protected NewProject readNewProject(InputStream is) {
	      try {
	         DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	         Document doc = builder.parse(is);
	         Element root = doc.getDocumentElement();
	         NewProject nproject = new NewProject();
	         NodeList nodes = root.getChildNodes();
	         for (int i = 0; i < nodes.getLength(); i++) {
	            Element element = (Element) nodes.item(i);
	            if (element.getTagName().equals("name")) {
	            	nproject.setName(element.getTextContent());
	            }
	            else if (element.getTagName().equals("description")) {
	            	nproject.setProjectDescription(element.getTextContent());
	            }
	         }
	         return nproject;
	      }
	      catch (Exception e) {
	         throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
	      }
	   }
	
	//Unmarshalling for PUT
	protected Project readProject(InputStream is) {
	      try {
	         DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	         Document doc = builder.parse(is);
	         Element root = doc.getDocumentElement();
	         Project project = new Project();
	         NodeList nodes = root.getChildNodes();
	         for (int i = 0; i < nodes.getLength(); i++) {
	            Element element = (Element) nodes.item(i);
	            if (element.getTagName().equals("name")) {
	            	project.setName(element.getTextContent());
	            }
	            else if (element.getTagName().equals("description")) {
	            	project.setProjectDescription(element.getTextContent());
	            }
	         }
	         return project;
	      }
	      catch (Exception e) {
	         throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
	      }
	   }
}
