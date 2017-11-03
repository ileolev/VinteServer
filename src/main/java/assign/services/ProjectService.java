package assign.services;

import assign.domain.NewProject;
import assign.domain.Project;

public interface ProjectService {

	public NewProject addProject(NewProject c) throws Exception;
	
	public int updateProject(Project c, String projectId) throws Exception;
	
	public Project getProject(String projectId) throws Exception;
	
	public int deleteProject(String projectId) throws Exception;
	
}
