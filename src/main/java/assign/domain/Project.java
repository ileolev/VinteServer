package assign.domain;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "project")
@XmlAccessorType(XmlAccessType.FIELD)
public class Project {

	String name;
	String description;
	int project_id;

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getProjectDescription() {
		return description;
	}
	
	public void setProjectDescription(String description) {
		this.description = description;
	}
	
	public int getProjectId() {
		return project_id;
	}
	
	public void setProjectId(int project_id) {
		this.project_id = project_id;
	}
}
