package assign.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

import assign.domain.NewProject;
import assign.domain.Project;

public class ProjectServiceImpl implements ProjectService {

	String dbURL = "";
	String dbUsername = "";
	String dbPassword = "";
	DataSource ds;

	////////////////////////////////////////////////////////////////////////////////////////////////////////
	// DB connection information
	public ProjectServiceImpl(String dbUrl, String username, String password) {
		this.dbURL = dbUrl;
		this.dbUsername = username;
		this.dbPassword = password;
		
		ds = setupDataSource();
	}
	
	//Data Source configuration
	public DataSource setupDataSource() {
        BasicDataSource ds = new BasicDataSource();
        ds.setUsername(this.dbUsername);
        ds.setPassword(this.dbPassword);
        ds.setUrl(this.dbURL);
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        return ds;
    }
	////////////////////////////////////////////////////////////////////////////////////////////////////////

	//POST (CREATE)
	public NewProject addProject(NewProject c) throws Exception {
		Connection conn = ds.getConnection();
		
		String insert = "INSERT INTO projects(project_name, project_description) VALUES(?, ?)";
		PreparedStatement stmt = conn.prepareStatement(insert,
                Statement.RETURN_GENERATED_KEYS);
		
		stmt.setString(1, c.getName());
		stmt.setString(2, c.getProjectDescription());
		
		int affectedRows = stmt.executeUpdate();

        if (affectedRows == 0) {
            throw new SQLException("Response status: 400");
        }
        
        ResultSet generatedKeys = stmt.getGeneratedKeys();
        if (generatedKeys.next()) {
        	c.setProjectId(generatedKeys.getInt(1));
        }
        else {
            throw new SQLException("Creating course failed, no ID obtained.");
        }
        
        // Close the connection
        conn.close();
        
		return c;
	}
	
	//PUT (UPDATE)
	public int updateProject(Project c, String projectId) throws Exception {
		Connection conn = ds.getConnection();
		
		//Validate name and description are not empty
		if(c.getName() == "" || c.getProjectDescription() == "") {
			return 0;
		}
		
		String update = "UPDATE projects SET project_name = ?, project_description = ? WHERE project_id = ?";
		PreparedStatement stmt = conn.prepareStatement(update);
		
		stmt.setString(1, c.getName());
		stmt.setString(2, c.getProjectDescription());
		stmt.setString(3, projectId);
		
		int affectedRows = stmt.executeUpdate();

        if (affectedRows == 0) {
            return -1;
        }
        
        // Close the connection
        conn.close();
        
        return 1;
	}
	
	//GET (READ)
	public Project getProject(String projectId) throws Exception {
		String query = "SELECT * FROM projects WHERE project_id=?";
		Connection conn = ds.getConnection();
		PreparedStatement s = conn.prepareStatement(query);
		
		s.setString(1, projectId);
		
		ResultSet r = s.executeQuery();
		
		if (!r.next()) {
			return null;
		}
		
		Project c = new Project();
		c.setProjectDescription(r.getString("project_description"));
		c.setName(r.getString("project_name"));
		c.setProjectId(r.getInt("project_id"));
		
		conn.close();
		
		return c;
	}
	
	//DELETE
	public int deleteProject(String projectId) throws Exception {
		String delete = "DELETE FROM projects WHERE project_id=?";
		Connection conn = ds.getConnection();
		PreparedStatement s = conn.prepareStatement(delete);
		
		s.setString(1, projectId);
		
		int affectedRows = s.executeUpdate();
		
		if (affectedRows == 0) {
            return -1;
        }
		
		return 1;
	}
}
