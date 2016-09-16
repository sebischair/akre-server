package services.annotationType.spotlight;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mahabaleshwar on 6/23/2016.
 */
@XmlRootElement(name ="Annotation")
public class Annotation {

    private List<Resource> Resources = new ArrayList<Resource>();


    @XmlElementWrapper(name="Resources")
    @XmlElement(name="Resource")
    public List<Resource> getResources() {
        return Resources;
    }

    public void setResources(List<Resource> resources) {
        Resources = resources;
    }

}
